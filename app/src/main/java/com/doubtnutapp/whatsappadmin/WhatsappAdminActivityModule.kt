package com.doubtnutapp.whatsappadmin

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerActivity
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class WhatsappAdminActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(WhatsappAdminViewModel::class)
    @PerActivity
    abstract fun bindWhatsappAdminViewModel(viewModel: WhatsappAdminViewModel): ViewModel
}