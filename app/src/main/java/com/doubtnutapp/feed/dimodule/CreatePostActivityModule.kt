package com.doubtnutapp.feed.dimodule

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.feed.view.CreatePostActivity
import com.doubtnutapp.feed.viewmodel.CreatePostViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class CreatePostActivityModule {

    @Binds
    @PerActivity
    abstract fun provides(activity: CreatePostActivity): AppCompatActivity

    @Binds
    @IntoMap
    @ViewModelKey(CreatePostViewModel::class)
    internal abstract fun bindCreatePostViewModel(createPostViewModel: CreatePostViewModel): ViewModel
}