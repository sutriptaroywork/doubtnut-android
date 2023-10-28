package com.doubtnutapp.ui.splash

import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStatus
import com.doubtnutapp.ui.onboarding.model.ApiLoginTimer
import io.reactivex.Completable
import io.reactivex.Single

interface SplashRepository {

    fun registerDailyStreakEvent(): Completable

    fun saveCameraScreenConfig(): Single<HashMap<String, Any>>

    fun getOnBoardingStatus(): Single<ApiOnBoardingStatus>

    fun getLoginTimer(): Single<ApiLoginTimer>
}
