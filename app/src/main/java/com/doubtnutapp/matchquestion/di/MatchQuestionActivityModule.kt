package com.doubtnutapp.matchquestion.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.matchquestion.service.MatchQuestionService
import com.doubtnutapp.matchquestion.viewmodel.MatchQuestionViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class MatchQuestionActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(MatchQuestionViewModel::class)
    abstract fun bindMatchQuestionActivityViewModel(matchQuestionViewModel: MatchQuestionViewModel): ViewModel

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideMatchQuestionService2(@ApiRetrofit retrofit: Retrofit): MatchQuestionService =
            retrofit.create(MatchQuestionService::class.java)
    }
}