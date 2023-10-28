package com.doubtnutapp.survey.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.survey.repository.SurveyRepositoryImpl
import com.doubtnutapp.data.survey.service.UserSurveyService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.domain.survey.repository.SurveyRepository
import com.doubtnutapp.survey.viewmodel.UserSurveyViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class UserSurveyModule {

    @Binds
    @IntoMap
    @ViewModelKey(UserSurveyViewModel::class)
    abstract fun bindSurveyViewModel(userSurveyViewModel: UserSurveyViewModel): ViewModel

    @Binds
    abstract fun bindSurveyRepository(surveyRepositoryImpl: SurveyRepositoryImpl): SurveyRepository

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideSurveyService(@ApiRetrofit retrofit: Retrofit): UserSurveyService =
                retrofit.create(UserSurveyService::class.java)
    }

}