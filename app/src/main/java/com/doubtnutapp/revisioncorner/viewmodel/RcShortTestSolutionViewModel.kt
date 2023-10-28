package com.doubtnutapp.revisioncorner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.addEventNames
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.HomeWorkSolutionData
import com.doubtnutapp.data.remote.models.ShortTestSubmitData
import com.doubtnutapp.data.remote.repository.RevisionCornerRepository
import com.doubtnutapp.domain.addtoplaylist.interactor.RemovePlaylistUseCase
import com.doubtnutapp.domain.addtoplaylist.interactor.SubmitPlayListsUseCase
import com.doubtnutapp.domain.base.SolutionResourceType
import com.doubtnutapp.domain.resourcelisting.entities.ResourceListingEntity
import com.doubtnutapp.domain.resourcelisting.interactor.GetResourceListingUseCase
import com.doubtnutapp.plus
import com.doubtnutapp.resourcelisting.mapper.ResourceListingMapper
import com.doubtnutapp.resourcelisting.model.ResourceListingData
import com.doubtnutapp.revisioncorner.ui.RcShortTestSolutionFragmentArgs
import com.doubtnutapp.screennavigator.Screen
import com.doubtnutapp.screennavigator.TextSolutionScreen
import com.doubtnutapp.screennavigator.VideoScreen
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Created by devansh on 20/08/21.
 * A modified version of HomeworkViewModel.
 * Data classes are used as is from liveclass package
 */

class RcShortTestSolutionViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val revisionCornerRepository: RevisionCornerRepository,
    private val getResourceListingUseCase: GetResourceListingUseCase,
    private val resourceListingMapper: ResourceListingMapper,
    private val whatsAppSharing: WhatsAppSharing,
    private val submitPlayListsUseCase: SubmitPlayListsUseCase,
    private val removePlaylistUseCase: RemovePlaylistUseCase,
    private val analyticsPublisher: AnalyticsPublisher,
) : BaseViewModel(compositeDisposable) {

    //region LiveData
    private val _resultSolutionLiveData = MutableLiveData<Outcome<HomeWorkSolutionData>>()
    val resultLiveData: LiveData<Outcome<HomeWorkSolutionData>>
        get() = _resultSolutionLiveData

    private val _pdfUriLiveData = MutableLiveData<Pair<File, String>>()
    val pdfUriLiveData: LiveData<Pair<File, String>>
        get() = _pdfUriLiveData

    private val _solutionsLiveData = MutableLiveData<Outcome<ResourceListingData>>()
    val solutionsLiveData: LiveData<Outcome<ResourceListingData>>
        get() = _solutionsLiveData

    private val _navigateScreenLiveData = MutableLiveData<Event<Pair<Screen, Map<String, Any?>?>>>()
    val navigateScreenLiveData: LiveData<Event<Pair<Screen, Map<String, Any?>?>>>
        get() = _navigateScreenLiveData

    private val _onAddToWatchLater: MutableLiveData<Event<String>> = MutableLiveData()
    val onAddToWatchLater: LiveData<Event<String>>
        get() = _onAddToWatchLater

    val whatsAppShareableData: LiveData<Event<Triple<String?, String?, String?>>>
        get() = whatsAppSharing.whatsAppShareableData
    //endregion

    private var testType: String = ""

    fun getResult(args: RcShortTestSolutionFragmentArgs) {
        _resultSolutionLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            val resultFlow =
                if (args.resultId != null) {
                    revisionCornerRepository.getPreviousShortTestResult(
                        args.resultId.orEmpty(),
                        args.widgetId
                    )
                } else {
                    val submitData = ShortTestSubmitData(
                        allQuestions = args.allQuestions.orEmpty().toList(),
                        correctQuestions = args.correctQuestions.orEmpty().toList(),
                        incorrectQuestions = args.incorrectQuestions.orEmpty().toList(),
                        widgetId = args.widgetId,
                        subject = args.subject.orEmpty(),
                        chapterAlias = args.chapterAlias.orEmpty(),
                        submittedOptions = args.submittedOptions.orEmpty().toList(),
                    )
                    revisionCornerRepository.submitShortTestResult(submitData)
                }

            resultFlow
                .catch { e ->
                    _resultSolutionLiveData.value = Outcome.loading(false)
                    _resultSolutionLiveData.value = Outcome.failure(e)
                }
                .collect {
                    testType = it.data.header?.lectureName.orEmpty()
                    _resultSolutionLiveData.value = Outcome.loading(false)
                    _resultSolutionLiveData.value = Outcome.success(it.data)
                }
        }
    }

    fun getQuizSolutions(page: Int, playlistId: String, allQuestionIds: List<String>) {
        _solutionsLiveData.value = Outcome.loading(true)
        compositeDisposable + getResourceListingUseCase
            .execute(
                GetResourceListingUseCase.Param(
                    page = page,
                    playlistId = playlistId,
                    packageDetailsId = "",
                    autoPlayData = false,
                    questionIds = allQuestionIds
                )
            )
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({ onSuccess(it) }, {})
    }

    private fun onSuccess(videoPlaylistEntity: ResourceListingEntity) {
        val playlistData = resourceListingMapper.map(videoPlaylistEntity)
        _solutionsLiveData.value = Outcome.loading(false)
        _solutionsLiveData.value = Outcome.success(playlistData)
    }

    // region Solutions interaction related code
    fun handleAction(action: Any) {
        when (action) {
            is ShareOnWhatApp -> shareOnWhatsApp(action)
            is PlayVideo -> openVideoScreen(action, Constants.PAGE_PRACTICE_CORNER, testType)
            is WatchLaterRequest -> addToWatchLater(action.id)
        }
    }

    fun openVideoScreen(action: PlayVideo, page: String, testType: String) {
        sendEvent(
            EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, hashMapOf(
                EventConstants.QUESTION_ID to action.videoId,
                EventConstants.SOURCE to page
            )
        )
        sendEvent(
            EventConstants.RC_SOLUTION_VIDEO_CLICK, hashMapOf(EventConstants.TYPE to testType),
            ignoreSnowplow = true
        )

        val screen =
            if (action.resourceType == SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO) VideoScreen else TextSolutionScreen
        _navigateScreenLiveData.value = Event(
            Pair(
                screen, mapOf(
                    Constants.PAGE to page,
                    Constants.QUESTION_ID to action.videoId,
                    Constants.PARENT_ID to 0,
                    Constants.PLAYLIST_ID to action.playlistId
                )
            )
        )
    }

    private fun shareOnWhatsApp(action: ShareOnWhatApp) {
        whatsAppSharing.shareOnWhatsApp(action)
    }

    private fun addToWatchLater(id: String) {
        submitPlayLists(id, mutableListOf("1"))
    }

    private fun submitPlayLists(id: String, playListIds: List<String>) {
        compositeDisposable + submitPlayListsUseCase
            .execute(Pair(id, playListIds))
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                _onAddToWatchLater.value = Event(id)
            }, {})
    }

    fun removeFromPlaylist(id: String, playListId: String) {
        compositeDisposable + removePlaylistUseCase
            .execute(Pair(id, playListId))
            .applyIoToMainSchedulerOnCompletable()
            .subscribeToCompletable({})
    }
    //endregion

    private fun getFileDestinationPath(url: String): String {
        val context = DoubtnutApp.INSTANCE
        val externalDirectoryPath = context.getExternalFilesDir(null)?.path
            ?: ""
        val isChildDirCreated =
            FileUtils.createDirectory(externalDirectoryPath, AppUtils.PDF_DIR_NAME)

        if (isChildDirCreated) {
            val fileName = FileUtils.fileNameFromUrl(url)
            return AppUtils.getPdfDirectoryPath(context) + File.separator + fileName
        }
        return FileUtils.EMPTY_PATH
    }

    fun getPdfFilePath(url: String, type: String) {
        val filepath = getFileDestinationPath(url)
        if (FileUtils.isFilePresent(filepath)) {
            _pdfUriLiveData.value = Pair(File(filepath), type)
        } else {
            compositeDisposable.add(
                DataHandler.INSTANCE.pdfRepository.downloadPdf(url, filepath)
                    .subscribeOn(
                        Schedulers.io()
                    )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        _pdfUriLiveData.value = Pair(File(filepath), type)
                    }, {
                        _pdfUriLiveData.value = null
                    })
            )
        }
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf(), ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }

    fun sendEventByEventTracker(eventName: String) {
        application.getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(application).toString())
            .addStudentId(UserUtil.getStudentId())
            .addScreenName(Constants.PAGE_TOPIC_BOOSTER_GAME)
            .track()
    }

    fun sendEventByQid(eventName: String, qid: String) {
        application.getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(application).toString())
            .addStudentId(UserUtil.getStudentId())
            .addScreenName(Constants.PAGE_TOPIC_BOOSTER_GAME)
            .addEventParameter(Constants.QUESTION_ID, qid)
            .track()
    }

    fun sendCleverTapEvent(eventName: String) {
        application.getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(application).toString())
            .addStudentId(UserUtil.getStudentId())
            .addScreenName(Constants.PAGE_TOPIC_BOOSTER_GAME)
            .cleverTapTrack()
    }

    private val application: DoubtnutApp
        get() = DoubtnutApp.INSTANCE
}