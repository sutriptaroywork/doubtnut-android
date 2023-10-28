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

private val Context.defaultDataStore by preferencesDataStore(
    name = "default_preferences",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = context.packageName + "_preferences",
                keysToMigrate = setOf(
                    DefaultDataStoreImpl.KEY_IS_NEW_USER,
                    DefaultDataStoreImpl.KEY_ETOOS_CONTENT_REGISTRATION_EVENT_SENT,
                    DefaultDataStoreImpl.KEY_SHOW_DNR_REWARD_POPUP,
                    DefaultDataStoreImpl.KEY_DNR_NEW_USER_REWARD,
                    DefaultDataStoreImpl.KEY_ETOOS_CONTENT_REGISTRATION_EVENT_SENT,
                    DefaultDataStoreImpl.KEY_ADMIN_DEEP_LINK,
                )
            )
        )
    }
)

class DefaultDataStoreImpl
@Inject constructor(appContext: Context) : DefaultDataStore {

    private val dataStore = appContext.defaultDataStore

    override val isNewUser: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[IS_NEW_USER] ?: false
        }

    override val etoosContentRegistrationEventSent: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[ETOOS_CONTENT_REGISTRATION_EVENT_SENT] ?: false
        }

    override val adminDeepLink: Flow<String>
        get() = dataStore.data.map { preferences ->
            preferences[ADMIN_DEEP_LINK] ?: ""
        }
    override val imageData: Flow<Float>
        get() = dataStore.data.map { preferences ->
            preferences[IMAGE_NETWORK_DATA_CONSUMED] ?: 0.0F
        }
    override val networkStatsResetDate: Flow<String>
        get() = dataStore.data.map { preferences ->
            preferences[NETWORK_STATS_RESET_DATE] ?: ""
        }

    override suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    // DNR Reward Start
    /**
     * This method returns whether DNR reward bottom sheet should be
     * shown base on the streak completion on Home screen.
     */
    override val shouldShowDnrStreakRewardPopUp: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[SHOW_DNR_REWARD_POPUP] ?: false
        }

    /**
     * This method returnss whether user received DNR reward after Signup.
     * After getting reward set this key to True
     */
    override val isNewUserClaimedSignupDnrReward: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[DNR_NEW_USER_REWARD] ?: false
        }

    /**
     * This method returns engagement time for SRP videos after
     * which user can claim DNR reward.
     */
    override val srpSfEngagementTimeToClaimDnrReward: Flow<Long?>
        get() = dataStore.data.map { preferences ->
            preferences[SRP_SF_ENGAGEMENT_TIME_TO_CLAIM_DNR_REWARD]
        }

    /**
     * This method returns engagement time for non SRP videos after
     * which user can claim DNR reward.
     */
    override val nonSrpSfEngagementTimeToClaimDnrReward: Flow<Long?>
        get() = dataStore.data.map { preferences ->
            preferences[NON_SRP_SF_ENGAGEMENT_TIME_TO_CLAIM_DNR_REWARD]
        }

    /**
     * This method returns engagement time for long form videos after
     * which user can claim DNR reward.
     */
    override val lfEngagementTimeToClaimDnrReward: Flow<Long?>
        get() = dataStore.data.map { preferences ->
            preferences[LF_ENGAGEMENT_TIME_TO_CLAIM_DNR_REWARD]
        }

    /**
     * This method returns the count of DNR reward bottom sheet
     * shown to the user for a specific reward type
     */
    override suspend fun getNoOfTimesDnrRewardBottomSheetShown(type: String): Flow<Int> {
        val key = intPreferencesKey("dnr_" + type + "_bottom_sheet")
        return dataStore.data.map { preferences ->
            preferences[key] ?: 0
        }
    }

    /**
     * This method returns the count of DNR reward dialog fragment
     * shown to the user for a specific reward type
     */
    override suspend fun getNoOfTimesDnrRewardDialogShown(type: String): Flow<Int> {
        val key = intPreferencesKey("dnr_" + type + "_dialog")
        return dataStore.data.map { preferences ->
            preferences[key] ?: 0
        }
    }
    // DNR Reward End

    override val courseCardBalloonIdsShown: Flow<String>
        get() = dataStore.data.map { preferences ->
            preferences[COURSE_CARD_BALLOON_IDS_SHOWN] ?: ""
        }

    override val enableGuestLogin: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[ENABLE_GUEST_LOGIN] ?: false
        }

    override val bottomNavigationIconsData: Flow<String?>
        get() = dataStore.data.map {
            it[PREF_KEY_BOTTOM_NAVIGATION_ICONS_DATA] ?: ""
        }

    override val referralId: Flow<String?>
        get() = dataStore.data.map {
            it[PREF_KEY_REFERRAL_ID] ?: ""
        }

    override val gmailVerificationScreenText: Flow<String>
        get() = dataStore.data.map {
            it[GMAIL_VERIFICATION_SCREEN_TEXT] ?: ""
        }

    companion object {
        const val KEY_IS_NEW_USER = "is_new_user"
        val IS_NEW_USER = booleanPreferencesKey(KEY_IS_NEW_USER)

        const val KEY_ETOOS_CONTENT_REGISTRATION_EVENT_SENT =
            "etoos_content_registration_event_sent"
        val ETOOS_CONTENT_REGISTRATION_EVENT_SENT =
            booleanPreferencesKey(KEY_ETOOS_CONTENT_REGISTRATION_EVENT_SENT)

        // DNR datastore keys start
        const val KEY_SHOW_DNR_REWARD_POPUP = "show_dnr_reward_popup"
        val SHOW_DNR_REWARD_POPUP = booleanPreferencesKey(KEY_SHOW_DNR_REWARD_POPUP)

        const val KEY_DNR_NEW_USER_REWARD = "dnr_new_user_reward"
        val DNR_NEW_USER_REWARD = booleanPreferencesKey(KEY_DNR_NEW_USER_REWARD)

        private const val KEY_SRP_SF_ENGAGEMENT_TIME_TO_CLAIM_DNR_REWARD =
            "srp_sf_dnr_engagement_time"
        val SRP_SF_ENGAGEMENT_TIME_TO_CLAIM_DNR_REWARD =
            longPreferencesKey(KEY_SRP_SF_ENGAGEMENT_TIME_TO_CLAIM_DNR_REWARD)

        private const val KEY_LF_ENGAGEMENT_TIME_TO_CLAIM_DNR_REWARD = "lf_dnr_engagement_time"
        val LF_ENGAGEMENT_TIME_TO_CLAIM_DNR_REWARD =
            longPreferencesKey(KEY_LF_ENGAGEMENT_TIME_TO_CLAIM_DNR_REWARD)

        private const val KEY_NON_SRP_SF_ENGAGEMENT_TIME_TO_CLAIM_DNR_REWARD =
            "non_srp_sf_dnr_engagement_time"
        val NON_SRP_SF_ENGAGEMENT_TIME_TO_CLAIM_DNR_REWARD =
            longPreferencesKey(KEY_NON_SRP_SF_ENGAGEMENT_TIME_TO_CLAIM_DNR_REWARD)
        // DNR datastore keys end

        const val KEY_ADMIN_DEEP_LINK = "admin_deep_link"
        val ADMIN_DEEP_LINK = stringPreferencesKey(KEY_ADMIN_DEEP_LINK)

        private const val KEY_COURSE_CARD_BALLOON_IDS_SHOWN =
            "course_card_balloon_ids_shown"
        val COURSE_CARD_BALLOON_IDS_SHOWN =
            stringPreferencesKey(KEY_COURSE_CARD_BALLOON_IDS_SHOWN)

        private const val KEY_IMAGE_NETWORK_DATA_CONSUMED = "image_network_data_consumed"
        val IMAGE_NETWORK_DATA_CONSUMED = floatPreferencesKey(KEY_IMAGE_NETWORK_DATA_CONSUMED)

        private const val KEY_NETWORK_STATS_RESET_DATE = "network_stats_reset_date"
        val NETWORK_STATS_RESET_DATE = stringPreferencesKey(KEY_NETWORK_STATS_RESET_DATE)

        // Guest Login keys start
        const val KEY_ENABLE_GUEST_LOGIN = "enable_guest_login"
        val ENABLE_GUEST_LOGIN = booleanPreferencesKey(KEY_ENABLE_GUEST_LOGIN)

        private const val KEY_BOTTOM_NAVIGATION_ICONS_DATA = "bottom_navigation_icons_data"
        val PREF_KEY_BOTTOM_NAVIGATION_ICONS_DATA = stringPreferencesKey(
            KEY_BOTTOM_NAVIGATION_ICONS_DATA
        )

        private const val KEY_REFERRAL_ID = "key_referral_id"
        val PREF_KEY_REFERRAL_ID = stringPreferencesKey(KEY_REFERRAL_ID)

        private const val KEY_GMAIL_VERIFICATION_SCREEN_TEXT = "key_gmail_verification_screen_text"
        val GMAIL_VERIFICATION_SCREEN_TEXT = stringPreferencesKey(KEY_GMAIL_VERIFICATION_SCREEN_TEXT)

        // Guest Login keys end
    }
}

@Singleton
interface DefaultDataStore {

    val isNewUser: Flow<Boolean>
    val etoosContentRegistrationEventSent: Flow<Boolean>
    val adminDeepLink: Flow<String>
    val imageData: Flow<Float>
    val networkStatsResetDate: Flow<String>

    /**
     * User should get DNR reward once a streak of 1 week is completed
     * after claiming reward by the user, set it to false
     */
    val shouldShowDnrStreakRewardPopUp: Flow<Boolean>

    /**
     * After sign up on this app user get DNR reward
     * once claimed, set it to true
     */
    val isNewUserClaimedSignupDnrReward: Flow<Boolean>

    /**
     * After this time of engagement time in SRP video
     * user can claim DNR reward.
     */
    val srpSfEngagementTimeToClaimDnrReward: Flow<Long?>

    /**
     * After this time of engagement time in non SRP video
     * user can claim DNR reward.
     */
    val nonSrpSfEngagementTimeToClaimDnrReward: Flow<Long?>

    /**
     * After this time of engagement time in LF video
     * user can claim DNR reward.
     */
    val lfEngagementTimeToClaimDnrReward: Flow<Long?>

    val bottomNavigationIconsData: Flow<String?>

    val referralId: Flow<String?>

    /**
     * Return no of times DNR bottom sheet shown
     * to the user for every type
     */
    suspend fun getNoOfTimesDnrRewardBottomSheetShown(type: String): Flow<Int>

    /**
     * Return no of times DNR dialog popup shown
     * to the user for every type
     */
    suspend fun getNoOfTimesDnrRewardDialogShown(type: String): Flow<Int>

    val courseCardBalloonIdsShown: Flow<String>

    suspend fun <T> set(key: Preferences.Key<T>, value: T)

    val enableGuestLogin: Flow<Boolean>

    val gmailVerificationScreenText: Flow<String>
}
