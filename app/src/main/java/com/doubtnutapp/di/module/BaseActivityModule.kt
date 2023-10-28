package com.doubtnutapp.di.module

import androidx.appcompat.app.AppCompatActivity
import com.doubtnut.core.di.scope.PerActivity
import dagger.Binds
import dagger.Module

@Module
abstract class BaseActivityModule<T : AppCompatActivity> {

    @Binds
    @PerActivity
    abstract fun provides(activity: T): AppCompatActivity
}
