package com.doubtnutapp.ui.feedback

import com.doubtnutapp.Constants
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.orDefaultValue
import com.doubtnutapp.toRequestBody
import com.doubtnutapp.utils.Utils
import io.reactivex.disposables.CompositeDisposable
import okhttp3.ResponseBody
import javax.inject.Inject

class FeedbackViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    fun updateNPSFeedbackResponse(
        type: String,
        id: String,
        selectedRating: String
    ): RetrofitLiveData<ApiResponse<ResponseBody>> {
        return DataHandler.INSTANCE.feedbackRepository.updateFeedbackResponse(
            Utils.getNPSFeedbackBody(type, id, selectedRating).toRequestBody()
        )
    }

    fun updateFeedbackResponse(
        type: String?,
        id: String?,
        options: String?
    ): RetrofitLiveData<ApiResponse<ResponseBody>> {
        return DataHandler.INSTANCE.feedbackRepository.updateFeedbackResponse(
            Utils.getFeedbackBody(
                type, id, options,
                defaultPrefs().getString(Constants.PARENT_QUESTION_ID, "")
                    .orDefaultValue()
            ).toRequestBody()
        )
    }

}
