package com.doubtnutapp.videoPage.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.videoPage.viewmodel.VideoDislikeFeedbackViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Sachin Saxena on 14/6/19.
 */
@Module
abstract class VideoDislikeFeedbackFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(VideoDislikeFeedbackViewModel::class)
    abstract fun bindVideoDislikeFeedbackViewModel(videoDislikeFeedbackViewModel: VideoDislikeFeedbackViewModel): ViewModel

}