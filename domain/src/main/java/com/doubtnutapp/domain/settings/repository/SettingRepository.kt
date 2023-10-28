package com.doubtnutapp.domain.settings.repository

import io.reactivex.Completable
import io.reactivex.Single

interface SettingRepository {
    fun getUserLoggedInState(): Single<Boolean>
    fun logOutUser(): Completable
}
