package com.doubtnutapp.data.mainscreen.repository

import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.mainscreen.apiservice.MainScreenService
import com.doubtnutapp.domain.homefeed.entites.model.WebViewData
import com.doubtnutapp.domain.mainscrren.repository.MainScreenRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class MainScreenRepositoryImpl @Inject constructor(
    private val mainScreenService: MainScreenService,
    private val userPreference: UserPreference
) : MainScreenRepository {

    override fun isBottomNavigationButtonClicked(key: String): Single<Boolean> {
        return Single.fromCallable {
            userPreference.isBottomNavigationButtonClicked(key)
        }
    }

    override fun setBottomNavigationButtonStateToTrue(key: String) = Completable.fromCallable {
        userPreference.setBottomNavigationButtonClicked(key)
    }

    override fun getWebViewData(studentClass: String): Single<WebViewData> =
        mainScreenService.getWebViewData(studentClass).map {
            return@map WebViewData(
                url = it.data.url,
                count = it.data.count ?: 0
            )
        }
}
