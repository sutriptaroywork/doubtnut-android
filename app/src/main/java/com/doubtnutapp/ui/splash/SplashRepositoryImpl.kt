package com.doubtnutapp.ui.splash

import com.doubtnutapp.data.base.di.qualifier.Udid
import com.doubtnutapp.data.camerascreen.datasource.ConfigDataSources
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStatus
import com.doubtnutapp.ui.onboarding.model.ApiLoginTimer
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class SplashRepositoryImpl @Inject constructor(
    private val splashService: SplashService,
    private val configDataSources: ConfigDataSources,
    private val userPreference: UserPreference,
    @Udid private val udid: String

) : SplashRepository {

    override fun registerDailyStreakEvent(): Completable {
        return if (userPreference.getUserLoggedIn()) {
            splashService
                .registerDailyStreakEvent().doOnComplete {}
        } else {
            Completable.complete()
        }
    }

    override fun saveCameraScreenConfig(): Single<HashMap<String, Any>> {
        val studentID = userPreference.getUserStudentId()
        val gaid = userPreference.getGaid()

        return if (studentID.isBlank()) {
            splashService
                .getCameraScreenConfig(gaid).flatMap {
                    Single.fromCallable<HashMap<String, Any>> {
                        configDataSources.updateCameraScreenConfig(it.data)
                        configDataSources.updateEnabledFeatures(it.data)
                        configDataSources.setBaseCdnUrl(it.data)
                        configDataSources.setForceUpdate(it.data)
                        configDataSources.setCampaignLandingDeeplink(it.data)
                        it.data
                    }
                }
        } else {
            splashService
                .getCameraScreenConfig(userPreference.getUserStudentId(), gaid).flatMap {
                    Single.fromCallable<HashMap<String, Any>> {
                        configDataSources.updateCameraScreenConfig(it.data)
                        configDataSources.updateEnabledFeatures(it.data)
                        configDataSources.setBaseCdnUrl(it.data)
                        configDataSources.setForceUpdate(it.data)
                        configDataSources.setCampaignLandingDeeplink(it.data)
                        it.data
                    }
                }
        }
    }

    override fun getOnBoardingStatus(): Single<ApiOnBoardingStatus> =
        splashService.getOnBoardingStatus().map { apiResponse ->
            apiResponse.data
        }

    override fun getLoginTimer(): Single<ApiLoginTimer> =
        splashService.getLoginTimer(udid).map { it.data }
}
