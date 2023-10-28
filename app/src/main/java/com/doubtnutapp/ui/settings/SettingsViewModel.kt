package com.doubtnutapp.ui.settings

import android.content.Context
import com.doubtnutapp.*
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.PublicUser
import com.doubtnutapp.data.remote.models.Settings
import com.doubtnutapp.utils.UserUtil.getStudentClass
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils
import io.reactivex.disposables.CompositeDisposable

import okhttp3.ResponseBody

class SettingsViewModel(compositeDisposable: CompositeDisposable) : BaseViewModel(compositeDisposable) {

    fun aboutus(): RetrofitLiveData<ApiResponse<Settings>> {
        return DataHandler.INSTANCE.settingsRepository.aboutus()
    }

    fun contactus(): RetrofitLiveData<ApiResponse<Settings>> {
        return DataHandler.INSTANCE.settingsRepository.contactus()
    }

    fun privacy(): RetrofitLiveData<ApiResponse<Settings>> {
        return DataHandler.INSTANCE.settingsRepository.privacy()
    }

    fun termsnconditions(): RetrofitLiveData<ApiResponse<Settings>> {
        return DataHandler.INSTANCE.settingsRepository.termsnconditions()
    }

    fun logout(): RetrofitLiveData<ApiResponse<ResponseBody>> {
        return DataHandler.INSTANCE.studentsRepositoryv2.logout(authToken(DoubtnutApp.INSTANCE.applicationContext))
    }

    fun refreshtoken(): RetrofitLiveData<ApiResponse<String>> {
        return DataHandler.INSTANCE.studentsRepositoryv2.refreshToken(getStudentId())
    }

    fun addPublicUser(context: Context): RetrofitLiveData<ApiResponse<PublicUser>> {
        val applicationContext = DoubtnutApp.INSTANCE.applicationContext
        val params: HashMap<String, Any> = HashMap()
        params["gcm_reg_id"] =
            defaultPrefs(applicationContext).getString(Constants.GCM_REG_ID,"") ?: ""
        params["udid"] = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        params["name"] = ""
        params["app_version"] = Utils.getVersionName()
        params["is_verified"] = ""
        params["email"] = Utils.getEmail(DoubtnutApp.INSTANCE.applicationContext)
        params["class"] = getStudentClass()
        params["language"] =
            defaultPrefs(context!!).getString(Constants.STUDENT_LANGUAGE_CODE, "").orDefaultValue()


        return DataHandler.INSTANCE.studentsRepositoryv2.addPublicUser(params.toRequestBody())

    }

}
