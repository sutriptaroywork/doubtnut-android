package com.doubtnutapp.gamification.dailyattendance.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.gamification.dailyattendance.repository.DailyAttendanceRepositoryImpl
import com.doubtnutapp.data.gamification.dailyattendance.service.DailyAttendanceService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.di.module.BaseActivityModule
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.gamification.dailyattendance.repository.DailyAttendanceRepository
import com.doubtnutapp.gamification.dailyattendance.ui.DailyAttendanceActivity
import com.doubtnutapp.gamification.dailyattendance.ui.viewmodel.DailyAttendanceViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.reactivex.subjects.PublishSubject
import retrofit2.Retrofit

@Module
abstract class DailyAttendanceActivityModule : BaseActivityModule<DailyAttendanceActivity>() {

    @Binds
    @IntoMap
    @ViewModelKey(DailyAttendanceViewModel::class)
    @PerActivity
    abstract fun bindDailyAttendanceActivityViewModel(dailyAttendanceViewModel: DailyAttendanceViewModel): ViewModel

    @Binds
    @PerActivity
    abstract fun bindDailyAttendanceRepository(dailyAttendanceRepositoryImpl: DailyAttendanceRepositoryImpl): DailyAttendanceRepository


    @Module
    companion object {

        @Provides
        @JvmStatic
        @PerActivity
        fun provideDailyAttendanceService(@ApiRetrofit retrofit: Retrofit): DailyAttendanceService =
                retrofit.create(DailyAttendanceService::class.java)

        @Provides
        @JvmStatic
        @PerActivity
        fun providePublishSubjectString(): PublishSubject<String> = PublishSubject.create()
    }
}