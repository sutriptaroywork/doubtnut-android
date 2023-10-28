package com.doubtnut.core.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.lottieDataStore by preferencesDataStore(
    name = "lottie_preferences"
)

class LottieAnimDatastoreImpl
@Inject constructor(appContext: Context) : LottieAnimDataStore {

    private val dataStore = appContext.lottieDataStore

    override suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    override suspend fun <T> get(key: Preferences.Key<T>): Flow<T?> {
        return dataStore.data.map { preferences ->
            preferences[key]
        }
    }

    override val incomingCallAnimationUrl: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[INCOMING_CALL_ANIMATION] ?: ""
        }

    override val otpScreenAnimationUrl: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[OTP_SCREEN_ANIMATION] ?: ""
        }

    override val callAnimationUrl: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[CALL_ANIMATION] ?: ""
        }

    override val messageAnimationUrl: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[MESSAGE_ANIMATION] ?: ""
        }

    override val noInternetAnimationUrl: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[NO_INTERNET_ANIMATION] ?: ""
        }

    override val missedCallAnimationUrl: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[MISSED_CALL_ANIMATION] ?: ""
        }

    override val missedCallSuccessAnimationUrl: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[MISSED_CALL_SUCCESS_ANIMATION] ?: ""
        }

    override val invalidImageAnimationUrl: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[INVALID_IMAGE_ANIMATION] ?: ""
        }

    override val matchGestureDataAnimationUrl: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[MATCH_GESTURE_DATA_ANIMATION] ?: ""
        }

    override val matchPageLoader1DataAnimationUrl: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[MATCH_PAGE_LOADER_1_ANIMATION] ?: ""
        }

    override val matchPageLoader2DataAnimationUrl: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[MATCH_PAGE_LOADER_2_ANIMATION] ?: ""
        }

    override val videoPageReferAndEarnAnimationUrl: Flow<String?>
        get() = dataStore.data.map {
            it[PREF_VIDEO_PAGE_REFER_AND_EARN_ANIMATION]?:""
        }

    companion object {

        const val KEY_OTP_BACK_PRESS_ANIMATION = "otp_back_press"
        const val KEY_PHONE_BACK_PRESS_ANIMATION = "phone_back_press"

        private const val KEY_INCOMING_CALL_ANIMATION = "incoming_call"
        val INCOMING_CALL_ANIMATION = stringPreferencesKey(KEY_INCOMING_CALL_ANIMATION)

        private const val KEY_MESSAGE_ANIMATION = "message"
        val MESSAGE_ANIMATION = stringPreferencesKey(KEY_MESSAGE_ANIMATION)

        private const val KEY_OTP_SCREEN_ANIMATION = "otp_screen"
        val OTP_SCREEN_ANIMATION = stringPreferencesKey(KEY_OTP_SCREEN_ANIMATION)

        private const val KEY_CALL_ANIMATION = "call"
        val CALL_ANIMATION = stringPreferencesKey(KEY_CALL_ANIMATION)

        private const val KEY_NO_INTERNET_ANIMATION = "no_internet"
        val NO_INTERNET_ANIMATION = stringPreferencesKey(KEY_NO_INTERNET_ANIMATION)

        private const val KEY_MISSED_CALL_ANIMATION = "missed_call"
        val MISSED_CALL_ANIMATION = stringPreferencesKey(KEY_MISSED_CALL_ANIMATION)

        private const val KEY_MISSED_CALL_SUCCESS_ANIMATION = "missed_call_success"
        val MISSED_CALL_SUCCESS_ANIMATION = stringPreferencesKey(KEY_MISSED_CALL_SUCCESS_ANIMATION)

        private const val KEY_INVALID_IMAGE_ANIMATION = "invalid_image"
        val INVALID_IMAGE_ANIMATION = stringPreferencesKey(KEY_INVALID_IMAGE_ANIMATION)

        private const val KEY_MATCH_GESTURE_DATA_ANIMATION = "match_gesture_data"
        val MATCH_GESTURE_DATA_ANIMATION = stringPreferencesKey(KEY_MATCH_GESTURE_DATA_ANIMATION)

        private const val KEY_MATCH_PAGE_LOADER_1_ANIMATION = "match_page_loader_1"
        val MATCH_PAGE_LOADER_1_ANIMATION = stringPreferencesKey(KEY_MATCH_PAGE_LOADER_1_ANIMATION)

        private const val KEY_MATCH_PAGE_LOADER_2_ANIMATION = "match_page_loader_2"
        val MATCH_PAGE_LOADER_2_ANIMATION = stringPreferencesKey(KEY_MATCH_PAGE_LOADER_2_ANIMATION)

        private const val KEY_REFER_AND_EARN_VIDEO_PAGE="video_screen_refer_and_earn"
        val PREF_VIDEO_PAGE_REFER_AND_EARN_ANIMATION= stringPreferencesKey( KEY_REFER_AND_EARN_VIDEO_PAGE)
    }
}

@Singleton
interface LottieAnimDataStore {

    val incomingCallAnimationUrl: Flow<String?>
    val otpScreenAnimationUrl: Flow<String?>
    val callAnimationUrl: Flow<String?>
    val messageAnimationUrl: Flow<String?>
    val noInternetAnimationUrl: Flow<String?>
    val missedCallAnimationUrl: Flow<String?>
    val missedCallSuccessAnimationUrl: Flow<String?>
    val invalidImageAnimationUrl: Flow<String?>
    val matchGestureDataAnimationUrl: Flow<String?>
    val matchPageLoader1DataAnimationUrl: Flow<String?>
    val matchPageLoader2DataAnimationUrl: Flow<String?>
    val videoPageReferAndEarnAnimationUrl:Flow<String?>

    suspend fun <T> set(key: Preferences.Key<T>, value: T)

    suspend fun <T> get(key: Preferences.Key<T>): Flow<T?>
}