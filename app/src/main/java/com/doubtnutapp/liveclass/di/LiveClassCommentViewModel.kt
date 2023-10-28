package com.doubtnutapp.liveclass.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.liveclass.viewmodel.EmiReminderViewModel
import com.doubtnutapp.youtubeVideoPage.comment.CommentsInVideoPageViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class LiveClassCommentModule {

    @Binds
    @IntoMap
    @ViewModelKey(CommentsInVideoPageViewModel::class)
    abstract fun bindViewModel(viewModel: CommentsInVideoPageViewModel): ViewModel
}