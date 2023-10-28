package com.doubtnutapp.ui.cameraGuide

import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.repository.AnswerRepository
import com.doubtnutapp.toRequestBody
import com.doubtnutapp.ui.cameraGuide.event.CameraGuideEventManager
import io.reactivex.disposables.CompositeDisposable
import retrofit2.Call
import javax.inject.Inject

class CameraGuideViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val cameraGuideEventManager: CameraGuideEventManager,
    private val answerRepository: AnswerRepository
) : BaseViewModel(compositeDisposable) {

    fun cameraGuide(): RetrofitLiveData<ApiResponse<Any>> {
        return DataHandler.INSTANCE.settingsRepository.cameraGuide()
    }

    fun updateAnswerView(
        viewId: String,
        videoTime: String,
        page: String,
        engageTime: String,
        source: String
    ): Call<ApiResponse<Any>> {
        val params: HashMap<String, Any> = HashMap()
        params["question_id"] = viewId
        params["video_time"] = videoTime
        params["page"] = page
        params["engage_time"] = engageTime
        params["source"] = source
        return answerRepository.updateViewOnBoarding(params.toRequestBody())
    }

    fun publishCameraButtonClickEvent(source: String) {
        cameraGuideEventManager.cameraButtonClicked(source)
    }

}