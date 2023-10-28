package com.doubtnut.core.data

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.quizNotificationDataStore by preferencesDataStore(
    name = "quiz_notification_pref",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = "Quiz_Notification_Pref"
            )
        )
    }
)

class QuizNotificationDatastoreImpl
@Inject constructor(appContext: Context) : QuizNotificationDataStore {

    private val dataStore = appContext.quizNotificationDataStore

    override val checkFetch: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            val uiMode = preferences[CHECK_FETCH_KEY] ?: false
            uiMode
        }

    override val quizNotificationAndroidJobLastKnownTimestamp: Flow<Long?>
        get() = dataStore.data.map { preferences ->
            preferences[QUIZ_NOTIFICATION_ANDROID_JOB_LAST_KNOWN_TIMESTAMP]
        }

    override suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    companion object {
        val CHECK_FETCH_KEY = booleanPreferencesKey("CheckFetch")
        val QUIZ_NOTIFICATION_ANDROID_JOB_LAST_KNOWN_TIMESTAMP =
            longPreferencesKey("quiz_notification_android_job_last_known_timestamp")
    }
}

@Singleton
interface QuizNotificationDataStore {
    val checkFetch: Flow<Boolean>
    val quizNotificationAndroidJobLastKnownTimestamp: Flow<Long?>

    suspend fun <T> set(key: Preferences.Key<T>, value: T)
}
