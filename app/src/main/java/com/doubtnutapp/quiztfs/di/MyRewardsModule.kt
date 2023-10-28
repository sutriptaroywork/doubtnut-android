package com.doubtnutapp.quiztfs.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.quiztfs.viewmodel.MyRewardsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Mehul Bisht on 07-09-2021
 */
@Module
abstract class MyRewardsModule {

    @Binds
    @IntoMap
    @ViewModelKey(MyRewardsViewModel::class)
    abstract fun bindRewardsViewModel(myRewardsViewModel: MyRewardsViewModel): ViewModel
}