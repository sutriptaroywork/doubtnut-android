package com.doubtnutapp.studygroup.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.dummy.DummyViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Mehul Bisht on 21-09-2021
 */

@Module
abstract class AudioPlayerDialogModule {

    @Binds
    @IntoMap
    @ViewModelKey(DummyViewModel::class)
    abstract fun bindViewModel(dummyViewModel: DummyViewModel): ViewModel
}