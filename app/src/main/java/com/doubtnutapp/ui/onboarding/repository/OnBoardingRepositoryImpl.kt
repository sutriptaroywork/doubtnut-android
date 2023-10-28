package com.doubtnutapp.ui.onboarding.repository

import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStatus
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingSteps
import com.doubtnutapp.data.toRequestBody
import io.reactivex.Single
import javax.inject.Inject

class OnBoardingRepositoryImpl @Inject constructor(
    private val onBoardingService: OnBoardingService
) : OnBoardingRepository {

    override fun getOnBoardingSteps(
        type: String?,
        code: String?,
        variant: Int,
        languageCode: String
    ): Single<ApiOnBoardingSteps> =
        onBoardingService.getOnBoardingSteps(type, code, variant, languageCode)
            .map {
                it.data
            }

    override fun submitOnBoardingStep(
        title: List<String>,
        type: String,
        code: List<String>,
        variant: Int
    ): Single<ApiOnBoardingSteps> {
        val params: HashMap<String, Any> = HashMap()
        params["title"] = title
        params["type"] = type
        params["code"] = code
        params["variant"] = variant
        return onBoardingService.postStudentOnBoardingSteps(params.toRequestBody()).map {
            it.data
        }
    }

    override fun getOnBoardingStatus(): Single<ApiOnBoardingStatus> =
        onBoardingService.getOnBoardingStatus().map { apiResponse ->
            apiResponse.data
        }

    override fun getClassAndLanguage(): Single<ApiOnBoardingStatus> =
        onBoardingService.getClassAndLanguage().map { apiResponse ->
            apiResponse.data
        }
}
