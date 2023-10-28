package com.doubtnutapp.matchquestion.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.matchquestion.viewmodel.BlurQuestionImageDialogFragmentViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by devansh on 08/07/20.
 */

@Module
abstract class BlurQuestionImageDialogFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(BlurQuestionImageDialogFragmentViewModel::class)
    abstract fun bindBlurQuestionImageDialogFragmentViewModel(
        blurQuestionImageDialogFragmentViewModel: BlurQuestionImageDialogFragmentViewModel
    ): ViewModel

}