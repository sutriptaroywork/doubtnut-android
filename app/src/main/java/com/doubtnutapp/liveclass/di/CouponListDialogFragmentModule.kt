package com.doubtnutapp.liveclass.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.libraryhome.course.viewmodel.CoursesViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class CouponListDialogFragmentModule {
    @Binds
    @IntoMap
    @ViewModelKey(CoursesViewModel::class)
    abstract fun bindViewModel(viewModel: CoursesViewModel): ViewModel
}