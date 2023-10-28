package com.doubtnutapp.domain.profile

import io.reactivex.Completable
import io.reactivex.Single

interface UserProfileRepository {
    fun getProfile(): Single<UserProfileEntity>

    fun getClass(): Completable
}
