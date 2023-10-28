package com.doubtnutapp.quiztfs.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.quiztfs.viewmodel.LiveQuestionsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Mehul Bisht on 25-08-2021
 */

@Module
abstract class LiveQuestionsModule {

    @Binds
    @IntoMap
    @ViewModelKey(LiveQuestionsViewModel::class)
    abstract fun bindLiveQuestionsViewModel(liveQuestionsViewModel: LiveQuestionsViewModel): ViewModel
}