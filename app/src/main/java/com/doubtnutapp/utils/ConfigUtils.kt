package com.doubtnutapp.utils

import androidx.core.content.edit
import com.doubtnutapp.Constants
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.domain.homefeed.interactor.ConfigData

object ConfigUtils {
    fun saveToPref(data: ConfigData) {
        defaultPrefs().edit()
            .putString(Constants.FIRST_PAGE_DEEPLINK, data.firstPageDeeplink.orEmpty()).apply()
        defaultPrefs().edit().putString(Constants.POPUP_DEEPLINK, data.popupDeeplink.orEmpty())
            .apply()
        defaultPrefs().edit()
            .putString(Constants.DEFAULT_ONBOARDING_DEEPLINK, data.defaultOnboardingDeeplink)
            .apply()
        val liveClassData = data.liveClassData
        if (liveClassData != null) {
            defaultPrefs().edit().putBoolean(
                Constants.SHOULD_SHOW_MY_COURSE, liveClassData.shouldShowMyCourse
                    ?: false
            ).apply()
            defaultPrefs().edit().putBoolean(
                Constants.SHOULD_SHOW_COURSE_SELECTION, liveClassData.shouldShowCourseSelection
                    ?: false
            ).apply()
            defaultPrefs().edit().putString(
                Constants.PURCHASED_ASSORTMENT_ID,
                liveClassData.purchasedAssortmentId.orEmpty()
            ).apply()
            defaultPrefs().edit()
                .putString(Constants.CATEGORY_ID, liveClassData.categoryId.orEmpty()).apply()
        }

        val prePurchaseCallingData = data.prePurchaseCallingData
        defaultPrefs().edit().putString(
            Constants.TITLE_PROBLEM_SEARCH,
            prePurchaseCallingData?.titleProblemSearch.orEmpty()
        ).apply()
        defaultPrefs().edit().putString(
            Constants.SUBTITLE_PROBLEM_SEARCH,
            prePurchaseCallingData?.subtitleProblemSearch.orEmpty()
        ).apply()
        defaultPrefs().edit().putString(
            Constants.TITLE_PROBLEM_PURCHASE,
            prePurchaseCallingData?.titleProblemPurchase.orEmpty()
        ).apply()

        defaultPrefs().edit().putString(
            Constants.SUBTITLE_PROBLEM_PURCHASE,
            prePurchaseCallingData?.subtitleProblemPurchase.orEmpty()
        ).apply()
        defaultPrefs().edit().putString(
            Constants.TITLE_PAYMENT_FAILURE,
            prePurchaseCallingData?.titlePaymentFailure.orEmpty()
        ).apply()
        defaultPrefs().edit().putString(
            Constants.SUBTITLE_PAYMENT_FAILURE,
            prePurchaseCallingData?.subtitlePaymentFailure.orEmpty()
        ).apply()

        defaultPrefs().edit().putString(
            Constants.CHAT_DEEPLINK,
            prePurchaseCallingData?.chatDeeplink.orEmpty()
        ).apply()
        defaultPrefs().edit().putString(
            Constants.CALLBACK_DEEPLINK,
            prePurchaseCallingData?.callbackDeepLink.orEmpty()
        ).apply()

        defaultPrefs().edit().putBoolean(
            Constants.CALLBACK_BTN_SHOW,
            prePurchaseCallingData?.callbackBtnShow ?: true
        ).apply()
        defaultPrefs().edit().putBoolean(
            Constants.CHAT_BTN_SHOW,
            prePurchaseCallingData?.chatBtnShow ?: true
        ).apply()

        defaultPrefs().edit().putString(
            Constants.FLAG_ID,
            prePurchaseCallingData?.flagId.orEmpty()
        ).apply()
        defaultPrefs().edit().putString(
            Constants.VARIANT_ID,
            prePurchaseCallingData?.variantId.orEmpty()
        ).apply()

        defaultPrefs().edit().putString(
            Constants.CALLING_CARD_TITLE_TEXT_SIZE,
            prePurchaseCallingData?.titleTextSize.orEmpty()
        ).apply()
        defaultPrefs().edit().putString(
            Constants.CALLING_CARD_TITLE_TEXT_COLOR,
            prePurchaseCallingData?.titleTextColor.orEmpty()
        ).apply()
        defaultPrefs().edit().putString(
            Constants.CALLING_CARD_SUBTITLE_TEXT_SIZE,
            prePurchaseCallingData?.subtitleTextSize.orEmpty()
        ).apply()
        defaultPrefs().edit().putString(
            Constants.CALLING_CARD_SUBTITLE_TEXT_COLOR,
            prePurchaseCallingData?.subtitleTextColor.orEmpty()
        ).apply()
        defaultPrefs().edit().putString(
            Constants.CALLING_CARD_ACTION,
            prePurchaseCallingData?.action.orEmpty()
        ).apply()
        defaultPrefs().edit().putString(
            Constants.CALLING_CARD_ACTION_TEXT_SIZE,
            prePurchaseCallingData?.actionTextSize.orEmpty()
        ).apply()
        defaultPrefs().edit().putString(
            Constants.CALLING_CARD_ACTION_TEXT_COLOR,
            prePurchaseCallingData?.actionTextColor.orEmpty()
        ).apply()
        defaultPrefs().edit().putString(
            Constants.CALLING_CARD_ACTION_IMAGE_URL,
            prePurchaseCallingData?.actionImageUrl.orEmpty()
        ).apply()
        defaultPrefs().edit().putString(
            Constants.CALLING_CARD_IMAGE_URL,
            prePurchaseCallingData?.imageUrl.orEmpty()
        ).apply()


        defaultPrefs().edit().putString(
            Constants.HAMBURGER_WHATSAPP_TEXT,
            data.hamburgerData?.text.orEmpty()
        ).apply()

        defaultPrefs().edit().putString(
            Constants.HAMBURGER_WHATSAPP_ICON_URL,
            data.hamburgerData?.iconUrl.orEmpty()
        ).apply()

        defaultPrefs().edit().putString(
            Constants.HAMBURGER_WHATSAPP_DEEPLINK,
            data.hamburgerData?.deeplink.orEmpty()
        ).apply()

        defaultPrefs().edit().putString(
            Constants.PROFILE_WHATSAPP_TEXT,
            data.profileData?.text.orEmpty()
        ).apply()

        defaultPrefs().edit().putString(
            Constants.PROFILE_WHATSAPP_ICON_URL,
            data.profileData?.iconUrl.orEmpty()
        ).apply()

        defaultPrefs().edit().putString(
            Constants.PROFILE_WHATSAPP_DEEPLINK,
            data.profileData?.deeplink.orEmpty()
        ).apply()

        defaultPrefs().edit().putBoolean(
            Constants.SHOULD_SHOW_FREE_CLASS, liveClassData?.shouldShowFreeClasses
                ?: false
        ).apply()

        defaultPrefs().edit().putBoolean(
            Constants.SHOW_TIMETABLE, liveClassData?.showTimetable
                ?: false
        ).apply()

        defaultPrefs().edit().putString(
            Constants.PROFILE_PRACTICE_ENGLISH_TEXT,
            data.profileData?.practiceEnglishText.orEmpty()
        ).apply()

        defaultPrefs().edit().putString(
            Constants.PROFILE_PRACTICE_ENGLISH_ICON_URL,
            data.profileData?.practiceEnglishIcon.orEmpty()
        ).apply()

        defaultPrefs().edit().putString(
            Constants.PROFILE_PRACTICE_ENGLISH_DEEPLINK,
            data.profileData?.practiceEnglishDeeplink.orEmpty()
        ).apply()

        defaultPrefs().edit().putString(
            Constants.DEFAULT_ONLINE_CLASS_TAB_TAG,
            data.defaultOnlineClassTabTag.orEmpty()
        ).apply()

        defaultPrefs().edit().putString(
            Constants.USER_JOURNEY,
            data.journeyCount?.toString()
        ).apply()

        data.journeyCount?.forEach {
            val key = it.key
            val value = it.value
            defaultPrefs().edit {
                putInt(key, value)
            }
        }
    }
}