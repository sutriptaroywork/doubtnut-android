package com.doubtnut.scholarship.di.module

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.scholarship.ui.ScholarshipActivityVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ScholarshipActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(ScholarshipActivityVM::class)
    abstract fun bindViewModel(viewModel: ScholarshipActivityVM): ViewModel
}