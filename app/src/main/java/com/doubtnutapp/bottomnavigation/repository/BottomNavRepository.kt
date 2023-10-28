package com.doubtnutapp.bottomnavigation.repository

import androidx.datastore.preferences.core.Preferences
import com.doubtnut.core.data.BottomNavIconsNotificationDataStore
import com.doubtnut.core.data.BottomNavIconsNotificationsDataStoreImpl
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.data.DefaultDataStoreImpl
import com.doubtnutapp.bottomnavigation.model.BottomNavigationTabsData
import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.isNotNullAndNotEmpty
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class BottomNavRepository @Inject constructor(
    private val networkService: NetworkService,
    private val defaultDataStore: DefaultDataStore,
    private val bottomNavIconsNotificationsDataStore: BottomNavIconsNotificationDataStore
) {

    suspend fun fetchAndStoreNavIcons() {
        val response = networkService.getAppWideBottomNavIcons().data
        storeBottomNavNotificationBadgeData(response)
        val gsonBottomNavData = Gson().toJson(response)
        defaultDataStore.set(
            DefaultDataStoreImpl.PREF_KEY_BOTTOM_NAVIGATION_ICONS_DATA,
            gsonBottomNavData
        )
    }

    private suspend fun storeBottomNavNotificationBadgeData(responseRemote: BottomNavigationTabsData?) {
        if (responseRemote == null) {
            return
        }
        val bottomNavData = defaultDataStore.bottomNavigationIconsData.firstOrNull().orEmpty()
        if (bottomNavData.isNotNullAndNotEmpty()) {
            val bottomNavLocalDataResponse = Gson().fromJson<BottomNavigationTabsData>(
                bottomNavData,
                BottomNavigationTabsData::class.java
            )
            updateNotificationBadgePrefValue(
                responseRemote.tab1,
                bottomNavLocalDataResponse.tab1,
                BottomNavIconsNotificationsDataStoreImpl.PREF_KEY_TAB1_SHOW_NOTIFICATION_BADGE
            )
            updateNotificationBadgePrefValue(
                responseRemote.tab2,
                bottomNavLocalDataResponse.tab2,
                BottomNavIconsNotificationsDataStoreImpl.PREF_KEY_TAB2_SHOW_NOTIFICATION_BADGE
            )
            updateNotificationBadgePrefValue(
                responseRemote.tab3,
                bottomNavLocalDataResponse.tab3,
                BottomNavIconsNotificationsDataStoreImpl.PREF_KEY_TAB3_SHOW_NOTIFICATION_BADGE
            )
            updateNotificationBadgePrefValue(
                responseRemote.tab4,
                bottomNavLocalDataResponse.tab4,
                BottomNavIconsNotificationsDataStoreImpl.PREF_KEY_TAB4_SHOW_NOTIFICATION_BADGE
            )
        } else {
            updateNotificationBadgePrefValue(
                responseRemote.tab1?.showNotificationBadge,
                BottomNavIconsNotificationsDataStoreImpl.PREF_KEY_TAB1_SHOW_NOTIFICATION_BADGE
            )
            updateNotificationBadgePrefValue(
                responseRemote.tab2?.showNotificationBadge,
                BottomNavIconsNotificationsDataStoreImpl.PREF_KEY_TAB2_SHOW_NOTIFICATION_BADGE
            )
            updateNotificationBadgePrefValue(
                responseRemote.tab3?.showNotificationBadge,
                BottomNavIconsNotificationsDataStoreImpl.PREF_KEY_TAB3_SHOW_NOTIFICATION_BADGE
            )
            updateNotificationBadgePrefValue(
                responseRemote.tab4?.showNotificationBadge,
                BottomNavIconsNotificationsDataStoreImpl.PREF_KEY_TAB4_SHOW_NOTIFICATION_BADGE
            )
        }
    }

    /**
     *  if last updated time in prefs is null or last updated time in remote response
    is greater than time stored in prefs for a particular tab, then update value in local preferences
     */
    private suspend fun updateNotificationBadgePrefValue(
        responseRemoteTabData: BottomNavigationTabsData.TabData?,
        bottomNavLocalResponseTabData: BottomNavigationTabsData.TabData?,
        prefKey: Preferences.Key<Boolean>
    ) {
        responseRemoteTabData?.let { remoteTabData ->
            remoteTabData.lastUpdatedTime?.let { lastUpdatedTimeRemote ->

                if (bottomNavLocalResponseTabData?.lastUpdatedTime == null ||
                    lastUpdatedTimeRemote > bottomNavLocalResponseTabData.lastUpdatedTime
                ) {
                    remoteTabData.showNotificationBadge?.let { showNotificationBadge ->
                        bottomNavIconsNotificationsDataStore.set(
                            prefKey,
                            showNotificationBadge
                        )
                    }
                }
            }
        }

    }

    private suspend fun updateNotificationBadgePrefValue(
        value: Boolean?,
        prefKey: Preferences.Key<Boolean>
    ) {
        value?.let {
            bottomNavIconsNotificationsDataStore.set(
                prefKey,
                value
            )
        }

    }
}