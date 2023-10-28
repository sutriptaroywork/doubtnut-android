package com.doubtnutapp.ui.onboarding.viewmodel

import com.doubtnutapp.*
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.base.di.qualifier.Udid
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.StudentClass
import com.doubtnut.noticeboard.data.remote.NoticeBoardRepository
import com.doubtnutapp.ui.onboarding.event.OnboardingEventManager
import com.doubtnutapp.utils.Utils
import io.reactivex.disposables.CompositeDisposable
import okhttp3.ResponseBody
import javax.inject.Inject

class SelectClassViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val onboardingEventManager: OnboardingEventManager,
    @Udid private val udid: String,
    private val userPreference: UserPreference
) : BaseViewModel(compositeDisposable) {

    fun getClassesList(lngCode: String): RetrofitLiveData<ApiResponse<ArrayList<StudentClass>>> {
        return DataHandler.INSTANCE.classRepository.getClassesWithSSC(lngCode)
    }

    fun updateProfileClass(isFromOnBoarding: Boolean): RetrofitLiveData<ApiResponse<ResponseBody>> {
        NoticeBoardRepository.clear()
        val applicationContext = DoubtnutApp.INSTANCE.applicationContext
        if (!isFromOnBoarding) {
            val params: HashMap<String, Any> = HashMap()
            params["student_class"] = userPreference.getUserClass()
            params["student_course"] = userPreference.getUserCourse()
            params[Constants.KEY_GCM_REG_ID] = defaultPrefs(applicationContext).getString(Constants.GCM_REG_ID,"").orDefaultValue()
            return DataHandler.INSTANCE.studentsRepositoryv2.updateUserProfile(params = params.toRequestBody())
        } else {
            val params: HashMap<String, Any> = HashMap()
            params[Constants.KEY_UDID] = udid
            params[Constants.KEY_APP_VERSION] = Utils.getVersionName()
            params[Constants.KEY_EMAIL] = Utils.getEmail(DoubtnutApp.INSTANCE)
            params["student_class"] = userPreference.getUserClass()
            params["locale"] = userPreference.getSelectedLanguage()
            params[Constants.KEY_IS_VERFIED] = ""
            params[Constants.KEY_GCM_REG_ID] =
                defaultPrefs(applicationContext).getString(Constants.GCM_REG_ID, "") ?: ""
            val fName = Utils.getAccountUsername(DoubtnutApp.INSTANCE.applicationContext)
            if (fName != "") {
                params[Constants.KEY_NAME] = fName
            }
            return DataHandler.INSTANCE.studentsRepositoryv2.updateUserProfile(params = params.toRequestBody())
        }
    }

    fun updatePreferences() {
        userPreference.setCameraScreenNavigationDataFetchedInCurrentSession(false)
    }

    fun publishSelectClassEvent(fromLibrary: Boolean, classSelected: String) {
        if (fromLibrary) {
            onboardingEventManager.onClassSelectFromLibrary(classSelected)
        } else {
            onboardingEventManager.onClassSelectFromHome(classSelected)
        }
    }

    fun publishSplashSelectClassEvent(classSelected: String) {
        onboardingEventManager.onSplashClassSelect(classSelected)
    }

    fun eventWith(eventName: String, ignoreSnowplow: Boolean = false) {
        onboardingEventManager.eventWith(eventName, ignoreSnowplow = ignoreSnowplow)
    }

}