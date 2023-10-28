package com.doubtnutapp.libraryhome.dailyquiz.di

import androidx.lifecycle.ViewModel
import com.doubtnutapp.libraryhome.dailyquiz.viewmodel.DailyQuizViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.quizLibrary.repository.DailyQuizRepositoryImpl
import com.doubtnutapp.data.quizLibrary.service.DailyQuizService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.domain.quizLibrary.repository.DailyQuizRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

/**
 * Created by shrreya on 14/6/19.
 */
@Module
abstract class DailyQuizFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(DailyQuizViewModel::class)
    abstract fun bindDailyQuizViewModel(dailyQuizViewModel: DailyQuizViewModel): ViewModel

    @Binds
    abstract fun bindDailyQuizRepository(dailyQuizRepositoryImpl: DailyQuizRepositoryImpl): DailyQuizRepository

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideDailyQuizService(@ApiRetrofit retrofit: Retrofit): DailyQuizService =
                retrofit.create(DailyQuizService::class.java)

    }

}