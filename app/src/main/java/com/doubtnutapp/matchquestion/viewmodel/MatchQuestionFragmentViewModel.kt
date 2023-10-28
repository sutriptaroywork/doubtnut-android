package com.doubtnutapp.matchquestion.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.PlayMatchedSolutionVideo
import com.doubtnutapp.base.TrackAutoPlayVideo
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO
import com.doubtnutapp.matchquestion.event.MatchQuestionEventManager
import com.doubtnutapp.matchquestion.mapper.YoutubeResultMapper
import com.doubtnutapp.matchquestion.model.MatchQuestionViewItem
import com.doubtnutapp.matchquestion.service.MatchQuestionRepository
import com.doubtnutapp.plus
import com.doubtnutapp.screennavigator.NavigationModel
import com.doubtnutapp.screennavigator.TextSolutionScreen
import com.doubtnutapp.screennavigator.VideoScreen
import com.doubtnutapp.utils.Event
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.audio.AudioAttributes
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class MatchQuestionFragmentViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val matchQuestionEventManager: MatchQuestionEventManager,
    private val youtubeResultMapper: YoutubeResultMapper,
    private val matchQuestionRepository: MatchQuestionRepository,
) : BaseViewModel(compositeDisposable) {

    companion object {
        const val TAG_MATCH_QUESTION = "MatchQuestion"
        const val TAG_ADVANCED_SEARCH = "AdvancedSearch"
        const val TAG_PREVENT_MATCH_EXIT = "PreventMatchExit"
        const val TAG_MATCH_PAGE_NOTIFICATION = "match_page_notif"
        const val TAG_IN_APP_DIALOG = "match_page_dialog"
    }

    var youtubeQid: Long? = null

    private val _youtubeResults: MutableLiveData<List<MatchQuestionViewItem>> = MutableLiveData()
    val youtubeResults: LiveData<List<MatchQuestionViewItem>>
        get() = _youtubeResults

    private val _isAutoPlayMute: MutableLiveData<Boolean> = MutableLiveData()
    val isAutoPlayMute: LiveData<Boolean>
        get() = _isAutoPlayMute

    var askedQuestionId: String = ""

    var matchResults: List<MatchQuestionViewItem> = listOf()

    var isAdvancedSearchEnabled: Boolean = false
    var preventMatchPageExit: Boolean = false
    var matchesFromNotification: Boolean = false
    var matchesFromNInApp: Boolean = false

    fun handleAction(action: Any) {
        when (action) {
            is PlayMatchedSolutionVideo -> openVideoScreen(action)
            is TrackAutoPlayVideo -> trackAutoPlayMatchVideo(
                action.questionId, action.answerId,
                action.answerVideo, action.engagementTime, askedQuestionId
            )
        }
    }

    fun muteAutoPlay(status: Boolean) {
        _isAutoPlayMute.value = status
    }

    private fun openVideoScreen(action: PlayMatchedSolutionVideo) {
        val source: String = when {
            preventMatchPageExit -> TAG_PREVENT_MATCH_EXIT
            matchesFromNInApp -> TAG_IN_APP_DIALOG
            matchesFromNotification -> TAG_MATCH_PAGE_NOTIFICATION
            isAdvancedSearchEnabled -> TAG_ADVANCED_SEARCH
            else -> TAG_MATCH_QUESTION
        }

        val eventParams = hashMapOf<String, Any>()
        if (action.viewType == R.layout.item_autoplay_match_result) {
            eventParams.apply {
                put(EventConstants.QUESTION_ASKED_ID, askedQuestionId)
                put(
                    EventConstants.AUTO_PLAY_TIME,
                    if (action.currentPosition != null) action.currentPosition / 1000 else 0
                )
                put(
                    EventConstants.CONTINUE_WATCHING_BUTTON_VISIBLE,
                    action.showContinueWatching ?: false
                )
            }
        }
        eventParams.apply {
            put(EventConstants.QUESTION_ID, action.id)
            put(EventConstants.ANSWER_ID, action.answerId ?: 0)
            put(
                EventConstants.ANSWER_VIDEO,
                action.videoResource?.resource.orEmpty()
            )
            put(Constants.POSITION, action.position)
            put(Constants.QUESTION_ID, action.id)
            put(EventConstants.QUESTION_ASKED_ID, askedQuestionId)
            put(EventConstants.ANSWER_ID, (action.answerId ?: 0))
            put(EventConstants.SOURCE, source)
        }

        sendEvent(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, eventParams)

        val argument = hashMapOf<String, Any>(
            Constants.PAGE to action.page,
            Constants.QUESTION_ID to action.id,
            Constants.PARENT_ID to askedQuestionId,
            Constants.OCR_TEXT to action.ocrText.orEmpty(),
            Constants.PARENT_PAGE to if (isAdvancedSearchEnabled) Constants.PAGE_ADV_SEARCH else ""
        )

        if (action.videoResource != null &&
            action.videoResource.resource.isNotBlank()
        ) {
            argument[Constants.VIDEO_DATA] = action.videoResource
        }
        val screen = if (action.resourceType == SOLUTION_RESOURCE_TYPE_VIDEO) {
            VideoScreen
        } else {
            if (action.resourceType == Constants.DYNAMIC_TEXT) {
                argument[Constants.RESOURCE_TYPE] = action.resourceType
                argument[Constants.RESOURCE_DATA] = action.html ?: ""
                argument[Constants.OCR_TEXT] = action.ocrText.orEmpty()
            }
            TextSolutionScreen
        }

        _navigateLiveData.postValue(Event((NavigationModel(screen, argument))))
    }

    fun publishEventWith(eventName: String, ignoreSnowplow: Boolean = false) {
        matchQuestionEventManager.eventWith(eventName, ignoreSnowplow)
    }

    fun sendEvent(event: String, params: HashMap<String, Any>, ignoreSnowplow: Boolean = false) {
        matchQuestionEventManager.eventWith(event, params, ignoreSnowplow)
    }

    fun getYoutubeResults(questionId: String, ocr: String) {

        sendEvent(EventConstants.EVENT_SHOW_MORE_CLICKED, hashMapOf<String, Any>().apply {
            put(EventConstants.QUESTION_ASKED_ID, askedQuestionId)
        })

        compositeDisposable + matchQuestionRepository.getYoutubeResults(questionId, ocr)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                youtubeQid = it.youtubeQid
                _youtubeResults.postValue(youtubeResultMapper.map(it.youtubeMatches))
            }, {
                // Don't show error, remove ShowMoreViewHolder
                _youtubeResults.postValue(emptyList())
            })
    }

    fun getAudioAttributes(): AudioAttributes =
        AudioAttributes.Builder().apply {
            setUsage(C.USAGE_MEDIA)
            setContentType(C.CONTENT_TYPE_SPEECH)
        }.build()

    private fun trackAutoPlayMatchVideo(
        questionId: String, answerId: Long,
        answerVideo: String, videoTime: Long, parentId: String
    ) {
        sendEvent(EventConstants.MATCH_AUTO_PLAYED, hashMapOf<String, Any>().apply {
            put(EventConstants.QUESTION_ASKED_ID, parentId)
            put(EventConstants.QUESTION_ID, questionId)
            put(EventConstants.AUTO_PLAY_TIME, videoTime)
            put(EventConstants.ANSWER_ID, answerId)
            put(EventConstants.ANSWER_VIDEO, answerVideo)
        })
    }
}