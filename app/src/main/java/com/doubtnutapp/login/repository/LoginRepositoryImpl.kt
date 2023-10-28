package com.doubtnutapp.login.repository

import com.doubtnutapp.data.base.di.qualifier.AppVersion
import com.doubtnutapp.data.base.di.qualifier.Udid
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStatus
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.domain.login.entity.IntroEntity
import com.doubtnutapp.login.model.ApiGetOTP
import com.doubtnutapp.login.model.ApiVerifyUser
import com.doubtnutapp.login.model.LoginPinEntity
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-08-13.
 */
class LoginRepositoryImpl @Inject constructor(
    private val loginService: LoginService,
    private val userPreference: UserPreference,
    @Udid private val udid: String,
    @AppVersion private val appVersion: String,
) : LoginRepository {

    override fun verifyTrueCallerLogin(
        payload: String,
        signature: String,
        isOptin: Boolean,
        status: Boolean?,
        accessToken: String?,
        phone: String?,
        name: String?,
        countryCode: String,
        userConsent: Int,
    ): Single<ApiVerifyUser> {
        val params: HashMap<String, Any> = HashMap()
        if (payload.isNotEmpty()) params["payload"] = payload
        if (signature.isNotEmpty()) params["signature"] = signature
        params["gcm_reg_id"] = userPreference.getGcmRegistrationId()
        params["udid"] = udid
        params["class"] = userPreference.getUserClass()
        params["course"] = userPreference.getUserCourse()
        params["language"] = userPreference.getSelectedLanguage()
        params["app_version"] = appVersion
        params["is_optin"] = isOptin
        params["user_consent"] = userConsent
        params["status"] = status ?: false
        accessToken?.let { params["access_token"] = it }
        phone?.let { params["phone"] = it }
        name?.let { params["name"] = it }
        params["aaid"] = userPreference.getGaid()
        if (countryCode.isNotEmpty()) params["country_code"] = countryCode

        return loginService.verifyTrueCallerLogin(params.toRequestBody()).map { it.data }
    }

    override fun verifyWhatsAppLogin(
        verificationToken: String,
        isOptin: Boolean,
        userConsent: Int,
    ): Single<ApiVerifyUser> {
        val params: HashMap<String, Any> = HashMap()
        params["access_token"] = verificationToken
        params["gcm_reg_id"] = userPreference.getGcmRegistrationId()
        params["udid"] = udid
        params["class"] = userPreference.getUserClass()
        params["course"] = userPreference.getUserCourse()
        params["language"] = userPreference.getSelectedLanguage()
        params["app_version"] = appVersion
        params["is_optin"] = isOptin
        params["user_consent"] = userConsent
        params["aaid"] = userPreference.getGaid()

        return loginService.verifyWhatsAppLogin(params.toRequestBody()).map { it.data }
    }

    override fun updateUserData(
        studentId: String,
        onBoardingVideoId: String,
        introList: List<IntroEntity>,
        studentUserName: String,
    ): Single<Boolean> {
        return Single.fromCallable {
            userPreference.putUserData(
                studentId,
                onBoardingVideoId,
                introList,
                studentUserName
            )
            true
        }
    }

    override fun getOnBoardingStatus(): Single<ApiOnBoardingStatus> =
        loginService.getOnBoardingStatus().map { it -> it.data }

    override fun putOnBoardingCompleted(): Completable {
        return Completable.fromCallable { userPreference.putOnBoardingCompleted() }
    }

    override fun requestOtp(phoneNumber: String): Single<ApiGetOTP> {
        val params: HashMap<String, Any> = HashMap()
        params["phone_number"] = phoneNumber
        params["class"] = userPreference.getUserClass()
        params["course"] = userPreference.getUserCourse()
        params["language"] = userPreference.getSelectedLanguage()
        params["udid"] = udid
        params["aaid"] = userPreference.getGaid()
        params["app_version"] = appVersion
        params["gcm_reg_id"] = userPreference.getGcmRegistrationId()
        return loginService.requestOTP(params.toRequestBody()).map {
            it.data
        }
    }

    override fun verifyOtp(
        sessionId: String,
        otp: String,
        isOptin: Boolean,
        pinInserted: Boolean,
        pin: String?,
        userConsent: Int,
        callWithoutOtp: Boolean?
    ): Single<ApiVerifyUser> {
        val params: HashMap<String, Any> = HashMap()
        params["session_id"] = sessionId
        params["otp"] = otp
        params["is_optin"] = isOptin
        params["user_consent"] = userConsent
        params["udid"] = udid
        callWithoutOtp?.let {
            params["call_without_otp"] = it
        }
        when (pinInserted) {
            true -> {
                params["pin_inserted"] = true
                params["pin"] = pin!!
            }
            false -> {
                params["pin_inserted"] = false
            }
        }
        params["aaid"] = userPreference.getGaid()
        params["language"] = userPreference.getSelectedLanguage()
        return loginService.verifyOTP(params.toRequestBody()).map {
            it.data
        }
    }

    override fun requestFirebaseLogin(
        countryCode: String,
        isOptin: Boolean,
        token: String,
        userConsent: Int,
    ): Single<ApiVerifyUser> {
        val params: HashMap<String, Any> = HashMap()
        params["country_code"] = countryCode
        params["firebase_token"] = token
        params["is_optin"] = isOptin
        params["user_consent"] = userConsent
        params["gcm_reg_id"] = userPreference.getGcmRegistrationId()
        params["udid"] = udid
        params["class"] = userPreference.getUserClass()
        params["course"] = userPreference.getUserCourse()
        params["language"] = userPreference.getSelectedLanguage()
        params["app_version"] = appVersion
        params["aaid"] = userPreference.getGaid()
        return loginService.loginWithFirebase(params.toRequestBody()).map {
            it.data
        }
    }

    override fun updateFcmToken(): Completable {
        val params: HashMap<String, Any> = HashMap()
        params["gcm_reg_id"] = userPreference.getGcmRegistrationId()
        params["student_id"] = userPreference.getUserStudentId()
        params["app_version"] = appVersion
        params["language"] = userPreference.getSelectedLanguage()
        return loginService.updateFcmToken(params.toRequestBody())
    }

    override fun putFcmTokenUpdatedOnServerStatus(status: Boolean): Completable {
        return Completable.fromCallable { userPreference.putFcmTokenUpdatedOnServerStatus(status) }
    }

    override fun preLoginOnBoardingUseCase(): Completable {
        val params: HashMap<String, Any> = HashMap()
        params["gcm_reg_id"] = userPreference.getGcmRegistrationId()
        params["udid"] = udid
        params["app_version"] = appVersion
        params["language"] = userPreference.getSelectedLanguage()
        params["aaid"] = userPreference.getGaid()
        return loginService.sendPreLoginOnboardData(params.toRequestBody())
    }

    override fun getUserProfileDataAndOldStudentId(): Single<Pair<HashMap<String, String>, String>> {
        return Single.fromCallable { userPreference.getUserProfileDataAndOldStudentId() }
    }

    override fun storePin(pin: String): Single<LoginPinEntity> {
        val params: HashMap<String, Any> = HashMap()
        params["pin"] = pin
        params["udid"] = udid
        params["language"] = userPreference.getSelectedLanguage()
        return loginService.storePin(params.toRequestBody()).map {
            it.data
        }
    }

    override fun addAnonymousUser(
        locale: String,
        source: String?
    ): Single<ApiVerifyUser> {
        val params: HashMap<String, Any> = HashMap()
        params["gcm_reg_id"] = userPreference.getGcmRegistrationId()
        params["udid"] = udid
        params["language"] = locale
        params["locale"] = locale
        params["app_version"] = appVersion
        source?.let {
            params["source"] = source
        }
        return loginService.addPublicUserApp(params.toRequestBody()).map {
            it.data
        }
    }
}
