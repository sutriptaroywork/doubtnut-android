package com.doubtnutapp.di.module

import com.doubtnut.core.view.audiotooltipview.AudioTooltipView
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Module used to bind custom Views.
 * Reference: https://github.com/google/dagger/issues/720#issuecomment-581868740
 */
@Module
internal abstract class CoreCustomViewDiModule {

    @ContributesAndroidInjector
    abstract fun bindAudioTooltipView(): AudioTooltipView

}