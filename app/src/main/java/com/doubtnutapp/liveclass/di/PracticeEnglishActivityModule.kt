package com.doubtnutapp.liveclass.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.liveclass.ui.practice_english.PracticeEnglishService
import com.doubtnutapp.liveclass.ui.practice_english.PracticeEnglishViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

/**
 * Created by Akshat Jindal on 04/12/21.
 */
@Module
abstract class PracticeEnglishActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(PracticeEnglishViewModel::class)
    abstract fun bindViewModel(practiceEnglishViewModel: PracticeEnglishViewModel): ViewModel

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun providesPracticeEnglishService(@ApiRetrofit retrofit: Retrofit): PracticeEnglishService {
            return retrofit.create(PracticeEnglishService::class.java)
        }

    }
}