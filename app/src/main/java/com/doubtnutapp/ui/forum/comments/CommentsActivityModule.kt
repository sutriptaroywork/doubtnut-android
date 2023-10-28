package com.doubtnutapp.ui.forum.comments

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Anand Gaurav on 2019-07-19.
 */
@Module
abstract class CommentsActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(CommentsViewModel::class)
    abstract fun bindCommentsViewModel(commentsViewModel: CommentsViewModel): ViewModel
}