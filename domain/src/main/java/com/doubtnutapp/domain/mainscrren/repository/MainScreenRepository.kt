package com.doubtnutapp.domain.mainscrren.repository

import com.doubtnutapp.domain.homefeed.entites.model.WebViewData
import io.reactivex.Completable
import io.reactivex.Single

interface MainScreenRepository {

    fun isBottomNavigationButtonClicked(key: String): Single<Boolean>

    fun setBottomNavigationButtonStateToTrue(key: String): Completable

    fun getWebViewData(studentClass: String): Single<WebViewData>
}
