package com.doubtnutapp.home.dimodule

import com.doubtnut.core.di.scope.PerFragment
import com.doubtnutapp.home.HomeFeedFragmentV2
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class HomeFragmentProvider {

    @ContributesAndroidInjector(modules = [HomeFeedFragmentModule::class])
    @PerFragment
    internal abstract fun contributeHHomeFeedFragmentV2Injector(): HomeFeedFragmentV2
}