package com.doubtnutapp.libraryhome.coursev3.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.di.module.BaseActivityModule
import com.doubtnutapp.libraryhome.coursev3.ui.TimetableActivity
import com.doubtnutapp.libraryhome.coursev3.viewmodel.TimetableViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class TimetableActivityModule : BaseActivityModule<TimetableActivity>() {

    @Binds
    @IntoMap
    @ViewModelKey(TimetableViewModel::class)
    internal abstract fun bindTimetableViewModel(timetableViewModel: TimetableViewModel): ViewModel
}