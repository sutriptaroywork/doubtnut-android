package com.doubtnutapp.ui.main.demoanimation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.Log
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.camera.interactor.GetCameraSettingConfig
import com.doubtnutapp.camera.interactor.GetDemoAnimationDetails
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.camerascreen.entity.CameraSettingEntity
import com.doubtnutapp.domain.camerascreen.entity.DemoAnimationEntity
import com.doubtnutapp.plus
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

/**
 * Created by devansh on 2020-07-07.
 */

class DemoAnimationActivityViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val getCameraSettingConfig: GetCameraSettingConfig,
    private val demoAnimationDetails: GetDemoAnimationDetails,
    private val analyticsPublisher: AnalyticsPublisher
) : BaseViewModel(compositeDisposable) {

    private val _cameraSettingConfig = MutableLiveData<Outcome<CameraSettingEntity>>()
    val cameraSettingConfig: LiveData<Outcome<CameraSettingEntity>>
        get() = _cameraSettingConfig

    private val _demoAnimationList = MutableLiveData<Outcome<List<DemoAnimationEntity>>>()
    val demoAnimationList: LiveData<Outcome<List<DemoAnimationEntity>>>
        get() = _demoAnimationList

    private val _message: MutableLiveData<Outcome<String>> = MutableLiveData()
    val message: LiveData<Outcome<String>>
        get() = _message


    fun getCameraSetting(hasCameraPermission: Boolean) {
        _cameraSettingConfig.value = Outcome.loading(true)
        compositeDisposable + getCameraSettingConfig.execute(
            GetCameraSettingConfig.Param(
                hasCameraPermission
            )
        )
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                _cameraSettingConfig.value = Outcome.loading(false)
                _cameraSettingConfig.value = Outcome.success(it)
            }, {
                demoAnimationDataFailure(it)
            })
    }

    fun getDemoAnimation() {
        _demoAnimationList.value = Outcome.loading(true)
        compositeDisposable + demoAnimationDetails.execute(Unit)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                _demoAnimationList.value = Outcome.loading(false)
                _demoAnimationList.value = Outcome.success(it)
            }, {
                demoAnimationDataFailure(it)
            })
    }

    fun sendEvent(
        eventName: String,
        params: HashMap<String, Any>,
        ignoreSnowplow: Boolean = false
    ) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                params,
                ignoreSnowplow = ignoreSnowplow
            )
        )
    }

    private fun demoAnimationDataFailure(error: Throwable) {
        _message.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
        Log.e(error)
    }
}