package com.doubtnutapp.store.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.store.viewmodel.StoreItemBuyViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class StoreItemDisabledDialogFragmentBindModule {

    @Binds
    @IntoMap
    @ViewModelKey(StoreItemBuyViewModel::class)
    abstract fun bindBadgesViewModel(storeItemBuyViewModel: StoreItemBuyViewModel): ViewModel
}