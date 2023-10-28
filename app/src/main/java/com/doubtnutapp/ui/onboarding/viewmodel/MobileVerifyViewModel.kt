package com.doubtnutapp.ui.onboarding.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.doubtnut.analytics.EventConstants.EVENT_NAME_BTN_SEND_OTP_CLICK
import com.doubtnutapp.*
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.GetOTP
import com.doubtnutapp.ui.onboarding.event.OnboardingEventManager
import com.doubtnutapp.utils.UserUtil.getStudentClass
import com.doubtnutapp.utils.Utils
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class MobileVerifyViewModel @Inject constructor(
    private val onboardingEventManager: OnboardingEventManager,
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    @SuppressLint("HardwareIds")
    fun getotp(phoneNumber: String, context: Context): RetrofitLiveData<ApiResponse<GetOTP>> {
        onboardingEventManager.eventWith(EVENT_NAME_BTN_SEND_OTP_CLICK, true)
        val params: HashMap<String, Any> = HashMap()
        params["phone_number"] = phoneNumber
        params["class"] = getStudentClass()
        params["course"] = defaultPrefs(DoubtnutApp.INSTANCE.applicationContext).getString(
            Constants.STUDENT_COURSE,
            ""
        ).orDefaultValue()
        params["language"] = defaultPrefs(DoubtnutApp.INSTANCE.applicationContext).getString(
            Constants.STUDENT_LANGUAGE_CODE,
            ""
        ).orDefaultValue()
        params["udid"] =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val email = Utils.getEmail(DoubtnutApp.INSTANCE.applicationContext)
        if (email != "") {
            params["email"] = email
        }
        val fname = Utils.getAccountUsername(DoubtnutApp.INSTANCE.applicationContext)
        if (fname != "") {
            params["fname"] = fname
        }
        params["app_version"] = Utils.getVersionName()
        params["gcm_reg_id"] =
            defaultPrefs(DoubtnutApp.INSTANCE.applicationContext).getString(
                Constants.GCM_REG_ID,
                ""
            ) ?: ""
        return DataHandler.INSTANCE.phoneVerificationRepository.getOTP(params.toRequestBody())
    }

//    fun addPublicUser(context: Context): RetrofitLiveData<ApiResponse<PublicUser>> {
//        val params: HashMap<String, Any> = HashMap()
//        params[Constants.KEY_UDID] = android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
//        params[Constants.KEY_APP_VERSION] = Utils.getVersionName(getApplication())
//        params[Constants.KEY_EMAIL] = Utils.getEmail(getApplication())
//        params[Constants.KEY_IS_VERFIED] = ""
//        params["class"] = getStudentClass()
//        params["language"] = defaultPrefs(context!!).getString(Constants.STUDENT_LANGUAGE_CODE, "").orDefaultValue()
//        val fname = Utils.getAccountUsername(getApplication())
//        if (fname != "") {
//            params["fname"] = fname
//        }
//        return DataHandler.INSTANCE.studentsRepository.addPublicUser(params.toRequestBody())
//    }

//    fun onAddPublicUserSuccess(publicUserData: PublicUser) {
//        val applicationContext = getApplication<DoubtnutApp>().applicationContext
//        defaultPrefs(applicationContext).edit {
//            putString(Constants.STUDENT_ID, publicUserData.student_id)
//            putString(Constants.ONBOARDING_VIDEO, publicUserData.onboardingVideo)
//        }
//
//        val introList = publicUserData.intro
//        for (i in 0 until introList.size) {
//            if (introList[i].type == Constants.TYPE_INTRO) {
//                defaultPrefs(applicationContext).edit {
//                    putString(Constants.TYPE_INTRO_QID, introList[i].questionId)
//                    putString(Constants.TYPE_INTRO_URL, introList[i].video)
//
//                }
//            } else if (introList[i].type == Constants.TYPE_COMMUNITY) {
//                defaultPrefs(applicationContext).edit {
//                    putString(Constants.TYPE_COMMUNITY_QID, introList[i].questionId)
//                    putString(Constants.TYPE_COMMUNITY_URL, introList[i].video)
//
//                }
//            }
//        }
//    }

//    fun updateFCMRegId() {
//        if (isFcmRegIdUpdated()) return
//
//        val applicationContext = getApplication<DoubtnutApp>().applicationContext
//
//        val params: HashMap<String, Any> = HashMap()
//        params[Constants.KEY_GCM_REG_ID] = defaultPrefs(getApplication()).getString(Constants.GCM_REG_ID,"") ?: ""
//        params[Constants.KEY_STUDENT_ID] = getStudentId()
//
//        if (!defaultPrefs(applicationContext).getString(Constants.XAUTH_HEADER_TOKEN, "").isNullOrBlank()) {
//
//            DataHandler.INSTANCE.studentsRepositoryv2.updateUserProfileObservable(params.toRequestBody())
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe({
//                        defaultPrefs(applicationContext).edit {
//                            putBoolean(Constants.KEY_FIREBASE_REG_ID_UPDATED_ON_SERVER, true)
//                        }
//                    }, {})
//        }
//    }

//    private fun isFcmRegIdUpdated() = defaultPrefs(getApplication()).getBoolean(Constants.KEY_FIREBASE_REG_ID_UPDATED_ON_SERVER, false)

}