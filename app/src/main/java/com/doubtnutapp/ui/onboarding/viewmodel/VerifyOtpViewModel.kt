package com.doubtnutapp.ui.onboarding.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import com.doubtnutapp.*
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.Intro
import com.doubtnutapp.data.remote.models.PublicUser
import com.doubtnutapp.data.remote.models.VerifyOTP
import com.doubtnutapp.ui.onboarding.event.OnboardingEventManager
import com.doubtnutapp.utils.UserUtil.getStudentClass
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class VerifyOtpViewModel @Inject constructor(
    app: Application,
    private val onboardingEventManager: OnboardingEventManager
) : AndroidViewModel(app) {

    fun verifyOTP(session_id: String, otp: String): RetrofitLiveData<ApiResponse<VerifyOTP>> {
        val params: HashMap<String, Any> = HashMap()
        params["session_id"] = session_id
        params["otp"] = otp
        return DataHandler.INSTANCE.phoneVerificationRepository.verifyOTP(params = params.toRequestBody())
    }

    @SuppressLint("HardwareIds")
    fun addPublicUser(context: Context): RetrofitLiveData<ApiResponse<PublicUser>> {
        val params: HashMap<String, Any> = HashMap()
        params[Constants.KEY_UDID] = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        params[Constants.KEY_APP_VERSION] = Utils.getVersionName()
        params[Constants.KEY_EMAIL] = Utils.getEmail(getApplication())
        params[Constants.KEY_IS_VERFIED] = ""
        params["class"] = getStudentClass()
        params["language"] =
            defaultPrefs().getString(Constants.STUDENT_LANGUAGE_CODE, "").orDefaultValue()

        val fName = Utils.getAccountUsername(getApplication())
        if (fName != "") {
            params[Constants.KEY_NAME] = fName
        }

        return DataHandler.INSTANCE.studentsRepositoryv2.addPublicUser(params.toRequestBody())
    }

    fun setUserLoggedIn() {

        val applicationContext = getApplication<DoubtnutApp>().applicationContext

        defaultPrefs(applicationContext).edit {
            putString(Constants.STUDENT_LOGIN, "true")
        }
    }

    fun onOtpVerificationSuccess(verifyOtp: VerifyOTP) {
        updatePref(
            verifyOtp.token,
            verifyOtp.student_id,
            verifyOtp.onboardingVideo,
            verifyOtp.intro,
            verifyOtp.studentUsername
        )
    }

    fun onAddPublicUserSuccess(publicUserData: PublicUser) {
        updatePref(
            publicUserData.token,
            publicUserData.student_id,
            publicUserData.onboardingVideo,
            publicUserData.intro,
            publicUserData.studentUsername
        )
    }

    fun unSetFirebaseRegIdPref() {
        val applicationContext = getApplication<DoubtnutApp>().applicationContext
        defaultPrefs(applicationContext).edit {
            putBoolean(Constants.KEY_FIREBASE_REG_ID_UPDATED_ON_SERVER, false)
        }
    }

    fun updateFCMRegId(forceUpdate: Boolean) {

        if (isFcmRegIdUpdated() && !forceUpdate) return

        val applicationContext = getApplication<DoubtnutApp>().applicationContext

        val params: HashMap<String, Any> = HashMap()
        params[Constants.KEY_GCM_REG_ID] =
            defaultPrefs(applicationContext).getString(Constants.GCM_REG_ID, "") ?: ""
        params[Constants.KEY_STUDENT_ID] = getStudentId()
        if (!defaultPrefs(applicationContext).getString(Constants.XAUTH_HEADER_TOKEN, "")
                .isNullOrBlank()
        ) {

            DataHandler.INSTANCE.studentsRepositoryv2.updateUserProfileObservable(params.toRequestBody())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (DoubtnutApp.INSTANCE.fcmRegId.isEmpty()) return@subscribe
                    defaultPrefs(applicationContext).edit {
                        putBoolean(Constants.KEY_FIREBASE_REG_ID_UPDATED_ON_SERVER, true)
                    }
                }, {})
        }

    }


    private fun updatePref(
        accessToken: String,
        studentId: String,
        onBoardingVideoId: String,
        introList: List<Intro>,
        studentUserName: String
    ) {
        val applicationContext = getApplication<DoubtnutApp>().applicationContext

        defaultPrefs(applicationContext).edit {
            putString(Constants.STUDENT_ID, studentId)
            putString(Constants.ONBOARDING_VIDEO, onBoardingVideoId)
            putString(Constants.STUDENT_USER_NAME, studentUserName)
        }

        for (i in 0 until introList.size) {
            if (introList[i].type == Constants.TYPE_INTRO) {
                defaultPrefs(applicationContext).edit {
                    putString(Constants.TYPE_INTRO_QID, introList[i].questionId)
                    putString(Constants.TYPE_INTRO_URL, introList[i].video)

                }
            } else if (introList[i].type == Constants.TYPE_COMMUNITY) {
                defaultPrefs(applicationContext).edit {
                    putString(Constants.TYPE_COMMUNITY_QID, introList[i].questionId)
                    putString(Constants.TYPE_COMMUNITY_URL, introList[i].video)

                }
            }
        }
    }

    private fun isFcmRegIdUpdated() = defaultPrefs(getApplication()).getBoolean(
        Constants.KEY_FIREBASE_REG_ID_UPDATED_ON_SERVER,
        false
    )

    fun publishOnVerifyClickEvent(screenName: String) {
        onboardingEventManager.onVerifyClick(screenName)
    }

    fun publishOnOtpSubmitSuccessEvent(screenName: String) {
        onboardingEventManager.onOtpSubmitSuccess(screenName)
    }

}