package com.doubtnutapp.videoPage.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.videoPage.entities.VideoDislikeFeedbackOptionEntity
import com.doubtnutapp.domain.videoPage.interactor.GetDislikeVideoFeedbackOption
import com.doubtnutapp.domain.videoPage.interactor.LikedDislikedVideoInteractor
import com.doubtnutapp.domain.videoPage.interactor.PostVideoDislikeFeedback
import com.doubtnutapp.plus
import com.doubtnutapp.videoPage.event.VideoPageEventManager
import com.doubtnutapp.videoPage.mapper.VideoDislikeFeedbackOptionMapper
import com.doubtnutapp.videoPage.model.VideoDislikeFeedbackOption
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.util.*
import javax.inject.Inject

class VideoDislikeFeedbackViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable,
        private val getVideoDislikeFeedbackOption: GetDislikeVideoFeedbackOption,
        private val postVideoDislikeFeedback: PostVideoDislikeFeedback,
        private val videoDislikeFeedbackOptionMapper: VideoDislikeFeedbackOptionMapper,
        private val getLikedDislikedVideoInteractor: LikedDislikedVideoInteractor,
        private val videoPageEventManager: VideoPageEventManager
) : BaseViewModel(compositeDisposable) {

    private val _feedbackOptionListVideo: MutableLiveData<Outcome<List<VideoDislikeFeedbackOption>>> = MutableLiveData()

    val feedbackOptionListVideo: LiveData<Outcome<List<VideoDislikeFeedbackOption>>>
        get() = _feedbackOptionListVideo

    private var _otherText: String = ""

    fun setOtherText(otherText: String) {
        _otherText = otherText
    }

    val _feedbackOptionList = mutableListOf<String>()

    fun updateFeedbackOption(option: String, toRemove: Boolean) {
        if (toRemove) {
            _feedbackOptionList.remove(option)
        } else {
            _feedbackOptionList.add(option)
        }
    }

    private val _onVideoFeedbackSubmit: MutableLiveData<Outcome<Boolean>> = MutableLiveData()

    val onVideoFeedbackSubmit: LiveData<Outcome<Boolean>>
        get() = _onVideoFeedbackSubmit

    fun getFeedbackOptions(source: String) {
        compositeDisposable + getVideoDislikeFeedbackOption
                .execute(GetDislikeVideoFeedbackOption.Param(source))
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(this::onFeedbackOptionsSuccess, this::onFeedbackOptionsError)
    }

    private fun onFeedbackOptionsSuccess(videoDislikeFeedbackOptionEntities: List<VideoDislikeFeedbackOptionEntity>) {
        _feedbackOptionListVideo.value = Outcome.success(videoDislikeFeedbackOptionMapper.map(videoDislikeFeedbackOptionEntities))
    }

    private fun onFeedbackOptionsError(error: Throwable) {
        _feedbackOptionListVideo.value = Outcome.loading(false)
        _feedbackOptionListVideo.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }

    fun submitVideoDislikeFeedback(video_name: String, question_id: String, answer_id: String, view_time: Long, source: String, is_liked: Boolean, view_id: String) {
        if (_otherText.isNotEmpty()) {
            _feedbackOptionList.add(_otherText)
        }
        compositeDisposable + getLikedDislikedVideoInteractor
                .execute(LikedDislikedVideoInteractor.Param(video_name, question_id, answer_id, view_time.toString(), source, is_liked, _feedbackOptionList.joinToString(","), view_id))
                .applyIoToMainSchedulerOnCompletable()
                .subscribeToCompletable(this::onSuccessfulSubmit)
    }

    private fun onSuccessfulSubmit() {
        _onVideoFeedbackSubmit.value = Outcome.success(true)
    }

    private fun sendEvent(event: String, param: HashMap<String, Any>) {
        videoPageEventManager.eventWith(event, param)
    }


}