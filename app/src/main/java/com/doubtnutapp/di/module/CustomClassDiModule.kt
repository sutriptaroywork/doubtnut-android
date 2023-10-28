package com.doubtnutapp.di.module

import com.doubtnutapp.ui.browser.ProgressChromeClient
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Module used to bind custom classes.
 * Reference: https://github.com/google/dagger/issues/720#issuecomment-581868740
 */
@Module
internal abstract class CustomClassDiModule {

    @ContributesAndroidInjector
    abstract fun bindProgressChromeClient(): ProgressChromeClient

}