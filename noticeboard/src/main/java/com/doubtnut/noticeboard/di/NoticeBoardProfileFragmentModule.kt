package com.doubtnut.noticeboard.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.noticeboard.ui.NoticeBoardProfileFragmentVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class NoticeBoardProfileFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(NoticeBoardProfileFragmentVM::class)
    abstract fun bindStudyGroupListViewModel(viewModel: NoticeBoardProfileFragmentVM): ViewModel

}