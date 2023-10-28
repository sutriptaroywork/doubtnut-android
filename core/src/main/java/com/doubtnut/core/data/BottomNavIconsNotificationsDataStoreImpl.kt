package com.doubtnut.core.data

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.bottomNavIconsNotificationDataStore by preferencesDataStore(
    name = "bottom_nav_icons_notifications_pref",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
)

@Singleton
class BottomNavIconsNotificationsDataStoreImpl @Inject constructor(val appContext: Context) :
    BottomNavIconsNotificationDataStore {

    val dataStore = appContext.bottomNavIconsNotificationDataStore

    companion object {

        private const val KEY_TAB1_SHOW_NOTIFICATION_BADGE = "tab1_show_notification_badge"
        val PREF_KEY_TAB1_SHOW_NOTIFICATION_BADGE = booleanPreferencesKey(
            KEY_TAB1_SHOW_NOTIFICATION_BADGE
        )

        private const val KEY_TAB2_SHOW_NOTIFICATION_BADGE = "tab2_show_notification_badge"
        val PREF_KEY_TAB2_SHOW_NOTIFICATION_BADGE = booleanPreferencesKey(
            KEY_TAB2_SHOW_NOTIFICATION_BADGE
        )

        private const val KEY_TAB3_SHOW_RED_NOTIFICATION = "tab3_show_notification_badge"
        val PREF_KEY_TAB3_SHOW_NOTIFICATION_BADGE = booleanPreferencesKey(
            KEY_TAB3_SHOW_RED_NOTIFICATION
        )

        const val KEY_TAB4_SHOW_RED_NOTIFICATION = "tab4_show_notification_badge"
        val PREF_KEY_TAB4_SHOW_NOTIFICATION_BADGE = booleanPreferencesKey(
            KEY_TAB4_SHOW_RED_NOTIFICATION
        )


    }

    override suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        dataStore.edit {
            it[key] = value
        }
    }

    override val tab1ShowNotificationBadge: Flow<Boolean>
        get() = dataStore.data.map {
            it[PREF_KEY_TAB1_SHOW_NOTIFICATION_BADGE] ?: false
        }

    override val tab2ShowNotificationBadge: Flow<Boolean>
        get() = dataStore.data.map {
            it[PREF_KEY_TAB2_SHOW_NOTIFICATION_BADGE] ?: false
        }

    override val tab3ShowNotificationBadge: Flow<Boolean>
        get() = dataStore.data.map {
            it[PREF_KEY_TAB3_SHOW_NOTIFICATION_BADGE] ?: false
        }

    override val tab4ShowNotificationBadge: Flow<Boolean>
        get() = dataStore.data.map {
            it[PREF_KEY_TAB4_SHOW_NOTIFICATION_BADGE] ?: false
        }


}

@Singleton
interface BottomNavIconsNotificationDataStore {

    val tab1ShowNotificationBadge: Flow<Boolean>
    val tab2ShowNotificationBadge: Flow<Boolean>
    val tab3ShowNotificationBadge: Flow<Boolean>
    val tab4ShowNotificationBadge: Flow<Boolean>

    suspend fun <T> set(key: Preferences.Key<T>, value: T)

}