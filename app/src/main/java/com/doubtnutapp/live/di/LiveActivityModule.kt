package com.doubtnutapp.live.di

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnut.core.di.scope.PerFragment
import com.doubtnutapp.feed.viewmodel.CreatePostViewModel
import com.doubtnutapp.live.ui.*
import com.doubtnutapp.live.viewmodel.LiveOverlayViewModel
import com.doubtnutapp.live.viewmodel.LiveStreamViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class LiveActivityModule {

    @PerActivity
    abstract fun provides(activity: LiveActivity): AppCompatActivity

    @Binds
    @IntoMap
    @ViewModelKey(CreatePostViewModel::class)
    internal abstract fun bindCreatePostViewModel(createPostViewModel: CreatePostViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LiveOverlayViewModel::class)
    internal abstract fun bindLiveOverlayViewModel(liveOverlayViewModel: LiveOverlayViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LiveStreamViewModel::class)
    internal abstract fun bindLiveStreamViewModel(liveStreamViewModel: LiveStreamViewModel): ViewModel

    @PerFragment
    @ContributesAndroidInjector
    abstract fun contributeLiveOverlayFragment(): LiveOverlayFragment

    @PerFragment
    @ContributesAndroidInjector
    abstract fun contributeLiveStreamFragment(): LiveStreamFragment

    @PerFragment
    @ContributesAndroidInjector
    abstract fun contributeLiveStreamPublishFragment(): LiveStreamPublishFragment

    @PerFragment
    @ContributesAndroidInjector
    abstract fun contributeScheduleLiveFragment(): ScheduleLiveFragment
}