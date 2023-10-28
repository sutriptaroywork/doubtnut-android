package com.doubtnutapp.data.settings.repository

import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.domain.settings.repository.SettingRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val userPreference: UserPreference
) : SettingRepository {
    override fun getUserLoggedInState(): Single<Boolean> {
        return Single.fromCallable {
            userPreference.getUserLoggedIn()
        }
    }

    override fun logOutUser(): Completable {
        return Completable.fromCallable {
            userPreference.logOutUser()
        }
    }
}
