package com.doubtnutapp.login.repository

import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStatus
import com.doubtnutapp.domain.login.entity.IntroEntity
import com.doubtnutapp.login.model.ApiGetOTP
import com.doubtnutapp.login.model.ApiVerifyUser
import com.doubtnutapp.login.model.LoginPinEntity
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created by Anand Gaurav on 2019-08-13.
 */
interface LoginRepository {
    fun verifyTrueCallerLogin(
        payload: String,
        signature: String,
        isOptin: Boolean,
        status: Boolean? = false,
        accessToken: String?,
        phone: String?,
        name: String?,
        countryCode: String,
        userConsent: Int,
    ): Single<ApiVerifyUser>

    fun verifyWhatsAppLogin(
        verificationToken: String,
        isOptin: Boolean,
        userConsent: Int,
    ): Single<ApiVerifyUser>

    fun updateUserData(
        studentId: String,
        onBoardingVideoId: String,
        introList: List<IntroEntity>,
        studentUserName: String,
    ): Single<Boolean>

    fun getOnBoardingStatus(): Single<ApiOnBoardingStatus>
    fun putOnBoardingCompleted(): Completable
    fun requestOtp(phoneNumber: String): Single<ApiGetOTP>
    fun verifyOtp(
        sessionId: String,
        otp: String,
        isOptin: Boolean,
        pinInserted: Boolean,
        pin: String? = null,
        userConsent: Int,
        callWithoutOtp: Boolean?
    ): Single<ApiVerifyUser>

    fun requestFirebaseLogin(
        countryCode: String,
        isOptin: Boolean,
        token: String,
        userConsent: Int,
    ): Single<ApiVerifyUser>

    fun updateFcmToken(): Completable
    fun putFcmTokenUpdatedOnServerStatus(status: Boolean): Completable
    fun preLoginOnBoardingUseCase(): Completable
    fun getUserProfileDataAndOldStudentId(): Single<Pair<HashMap<String, String>, String>>
    fun storePin(pin: String): Single<LoginPinEntity>

    fun addAnonymousUser(locale: String, source: String? = null): Single<ApiVerifyUser>
}
