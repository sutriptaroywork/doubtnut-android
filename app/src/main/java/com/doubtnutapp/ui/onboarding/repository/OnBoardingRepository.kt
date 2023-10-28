package com.doubtnutapp.ui.onboarding.repository

import androidx.annotation.Keep
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStatus
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingSteps
import io.reactivex.Single

/**
 * Created by Sachin Saxena on 2020-02-11.
 */
@Keep
interface OnBoardingRepository {

    fun getOnBoardingSteps(
        type: String?,
        code: String?,
        variant: Int,
        languageCode: String
    ): Single<ApiOnBoardingSteps>

    fun submitOnBoardingStep(
        title: List<String>,
        type: String,
        code: List<String>,
        variant: Int
    ): Single<ApiOnBoardingSteps>

    fun getOnBoardingStatus(): Single<ApiOnBoardingStatus>

    fun getClassAndLanguage(): Single<ApiOnBoardingStatus>
}
