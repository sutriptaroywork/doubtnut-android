package com.doubtnutapp.appexitdialog.di

import androidx.lifecycle.ViewModel
import com.doubtnutapp.appexitdialog.viewmodel.AppExitDialogViewModel
import com.doubtnut.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by devansh on 9/1/21.
 */

@Module
abstract class AppExitDialogModule {

    @Binds
    @IntoMap
    @ViewModelKey(AppExitDialogViewModel::class)
    abstract fun bindAppExitDialogViewModel(appExitDialogViewModel: AppExitDialogViewModel): ViewModel
}
