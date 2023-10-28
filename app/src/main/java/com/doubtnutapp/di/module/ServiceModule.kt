package com.doubtnutapp.di.module

import com.doubtnutapp.fcm.FCMMessagingService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule {

    @ContributesAndroidInjector
    internal abstract fun contributeFCMMessagingService(): FCMMessagingService
}
