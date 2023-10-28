package com.doubtnut.noticeboard.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.noticeboard.ui.NoticeBoardDetailActivityVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class NoticeBoardDetailActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(NoticeBoardDetailActivityVM::class)
    abstract fun bindViewModel(viewModel: NoticeBoardDetailActivityVM): ViewModel
}