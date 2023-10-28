package com.doubtnutapp.data.gamification.settings.repository

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.gamification.settings.model.AboutUs
import com.doubtnutapp.data.gamification.settings.model.ContactUs
import com.doubtnutapp.data.gamification.settings.model.PrivacyData
import com.doubtnutapp.data.gamification.settings.model.TermsAndCondition
import io.reactivex.Single
import retrofit2.http.GET

/**
 * Created by shrreya on 3/7/19.
 */
interface SettingDetailService {

    @GET("v2/settings/get-about-us")
    fun getAboutUsData(): Single<ApiResponse<AboutUs>>

    @GET("v2/settings/get-contact-us")
    fun getContactUsData(): Single<ApiResponse<ContactUs>>

    @GET("v2/settings/get-tnc")
    fun getTermsAndConditions(): Single<ApiResponse<TermsAndCondition>>

    @GET("v2/settings/get-privacy")
    fun getPrivacyPolicy(): Single<ApiResponse<PrivacyData>>
}
