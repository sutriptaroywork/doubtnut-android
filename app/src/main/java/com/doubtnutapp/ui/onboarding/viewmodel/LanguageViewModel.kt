package com.doubtnutapp.ui.onboarding.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import com.doubtnutapp.Constants
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiLanguage
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.StudentClass
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.toRequestBody
import com.doubtnutapp.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody

class LanguageViewModel(app: Application) : AndroidViewModel(app) {

    fun getLanguages(): RetrofitLiveData<ApiResponse<ApiLanguage>> {
        return DataHandler.INSTANCE.languageRepository.getLanguages(Utils.getUDID())
    }

    @SuppressLint("HardwareIds")
    fun setLanguage(
        ln_code: String,
        context: Context
    ): RetrofitLiveData<ApiResponse<ResponseBody>> {
        val params: HashMap<String, Any> = HashMap()
        params["locale"] = ln_code
        params["udid"] =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        params[Constants.KEY_GCM_REG_ID] =
            defaultPrefs(getApplication()).getString(Constants.GCM_REG_ID, "") ?: ""
        return DataHandler.INSTANCE.studentsRepositoryv2.updateUserProfile(params = params.toRequestBody())
    }

    fun getClassesList(lngCode: String): RetrofitLiveData<ApiResponse<ArrayList<StudentClass>>> {
        return DataHandler.INSTANCE.classRepository.getClassesWithSSC(lngCode)
    }

    fun storeOnBoardLanguage(locale: String) {
        val gcmId = defaultPrefs(getApplication()).getString(Constants.GCM_REG_ID, "") ?: return
        if (gcmId.isNotBlank()) {
            val params: HashMap<String, Any> = HashMap()
            params[Constants.LOCALE] = locale
            params[Constants.KEY_GCM_ID] = gcmId
            params[Constants.KEY_UDID] = Utils.getUDID()
            DataHandler.INSTANCE.studentsRepositoryv2.storeOnBoardLanguage(params = params.toRequestBody())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        }
    }
}
