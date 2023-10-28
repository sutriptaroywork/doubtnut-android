package com.doubtnutapp.gamification.settings.settingdetail.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Log
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.gamification.settings.entity.SettingDetailEntity
import com.doubtnutapp.domain.gamification.settings.interactor.GetAboutUsDetails
import com.doubtnutapp.domain.gamification.settings.interactor.GetContactUsDetails
import com.doubtnutapp.domain.gamification.settings.interactor.GetPrivacyPolicy
import com.doubtnutapp.domain.gamification.settings.interactor.GetTncDetails
import com.doubtnutapp.gamification.settings.settingdetail.mapper.SettingDetailItemMapper
import com.doubtnutapp.gamification.settings.settingdetail.model.SettingDetails
import com.doubtnutapp.screennavigator.*
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

/**
 * Created by shrreya on 2/7/19.
 */
class SettingDetailViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val aboutUsDetails: GetAboutUsDetails,
    private val contactUsDetails: GetContactUsDetails,
    private val getPrivacyPolicy: GetPrivacyPolicy,
    private val getTncDetails: GetTncDetails,
    private val settingDetailItemMapper: SettingDetailItemMapper
) : BaseViewModel(compositeDisposable) {

    private val _settingsLiveData = MutableLiveData<Outcome<SettingDetails>>()
    val settingsLiveData: LiveData<Outcome<SettingDetails>>
        get() = _settingsLiveData

    fun checkScreen(screenType: String): Any =
        when (screenType) {
            TermsAndConditionsScreen.toString() -> getTnCData()
            AboutUsScreen.toString() -> getAboutUsData()
            ContactUsScreen.toString() -> getContactusData()
            PrivacyPolicyScreen.toString() -> getPrivacyPolicyData()
            else -> NoScreen
        }

    private fun getAboutUsData() {

        _settingsLiveData.value = Outcome.loading(true)

        compositeDisposable.add(
            aboutUsDetails.execute(Unit)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(this::onSettingDetailSuccess, this::onSettingDetailError)
        )
    }

    private fun getPrivacyPolicyData() {
        _settingsLiveData.value = Outcome.loading(true)

        compositeDisposable.add(
            getPrivacyPolicy.execute(Unit)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(this::onSettingDetailSuccess, this::onSettingDetailError)
        )
    }

    private fun getTnCData() {
        _settingsLiveData.value = Outcome.loading(true)

        compositeDisposable.add(
            getTncDetails.execute(Unit)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(this::onSettingDetailSuccess, this::onSettingDetailError)
        )

    }

    private fun getContactusData() {
        _settingsLiveData.value = Outcome.loading(true)

        compositeDisposable.add(
            contactUsDetails.execute(Unit)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(this::onSettingDetailSuccess, this::onSettingDetailError)
        )
    }

    private fun onSettingDetailSuccess(settingDetailEntity: SettingDetailEntity) {
        _settingsLiveData.value = Outcome.loading(false)
        _settingsLiveData.value = Outcome.success(
            settingDetailItemMapper.map(settingDetailEntity)
        )
    }

    private fun onSettingDetailError(error: Throwable) {
        _settingsLiveData.value = Outcome.loading(false)
        _settingsLiveData.value = if (error is HttpException) {
            val errorCode = error.response()?.code()
            when (errorCode) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                //TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
        logException(error)
    }

    private fun logException(error: Throwable) {
        try {
            if (error is JsonSyntaxException
                || error is NullPointerException
                || error is ClassCastException
                || error is FormatException
                || error is IllegalArgumentException
            ) {
                Log.e(error)
            }
        } catch (e: Exception) {

        }
    }

}