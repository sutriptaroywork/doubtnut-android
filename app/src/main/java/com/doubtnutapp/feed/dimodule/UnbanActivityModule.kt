package com.doubtnutapp.feed.dimodule

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.feed.view.UnbannedRequestActivity
import com.doubtnutapp.feed.viewmodel.UnbanActivityViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class UnbanActivityModule {

    @Binds
    @PerActivity
    abstract fun provides(activity: UnbannedRequestActivity): AppCompatActivity

    @Binds
    @IntoMap
    @ViewModelKey(UnbanActivityViewModel::class)
    internal abstract fun bindCreatePostViewModel(unbanActivityViewModel: UnbanActivityViewModel): ViewModel
}