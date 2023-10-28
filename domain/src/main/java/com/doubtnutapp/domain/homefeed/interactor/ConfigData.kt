package com.doubtnutapp.domain.homefeed.interactor

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ConfigData(
    @SerializedName("onboarding") val onboardingList: List<OnboardingData>?,
    @SerializedName("first_page_deeplink") val firstPageDeeplink: String?,
    @SerializedName("popup_deeplink") val popupDeeplink: String?,
    @SerializedName("live_class_data") val liveClassData: LiveClassData?,
    @SerializedName("pre_purchase_calling_card_data") val prePurchaseCallingData: PrePurchaseCallingData?,
    @SerializedName("app_data_collect") val appDataCollect: Int?,
    @SerializedName("hamburger_data") val hamburgerData: HamburgerData?,
    @SerializedName("default_onboarding_deeplink") val defaultOnboardingDeeplink: String?,
    @SerializedName("profile_data") val profileData: HamburgerData?,
    @SerializedName("default_online_class_tab_tag") val defaultOnlineClassTabTag: String?,
    @SerializedName("journey_count") val journeyCount: Map<String, Int>?
)

@Keep
data class HamburgerData(
    @SerializedName("whatsapp_text") val text: String?,
    @SerializedName("whatsapp_icon_url") val iconUrl: String?,
    @SerializedName("whatsapp_deeplink") val deeplink: String?,
    @SerializedName("practice_english_text") val practiceEnglishText: String?,
    @SerializedName("practice_english_icon_url") val practiceEnglishIcon: String?,
    @SerializedName("practice_english_deeplink") val practiceEnglishDeeplink: String?
)

@Keep
data class OnboardingData(
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("audio_url") val audioUrl: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("position") val position: Int?,
    @SerializedName("id") val id: Int?,
    @SerializedName("button_text") val buttonText: String?,
)

@Keep
data class LiveClassData(
    @SerializedName("show_my_courses_tab") val shouldShowMyCourse: Boolean?,
    @SerializedName("show_course_selection") val shouldShowCourseSelection: Boolean?,
    @SerializedName("vip_assortment_id") val purchasedAssortmentId: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("show_free_classes") val shouldShowFreeClasses: Boolean?,
    @SerializedName("show_timetable") val showTimetable: Boolean?,
)

@Keep
data class PrePurchaseCallingData(
    @SerializedName("title_problem_search") val titleProblemSearch: String?,
    @SerializedName("subtitle_problem_search") val subtitleProblemSearch: String?,
    @SerializedName("title_problem_purchase") val titleProblemPurchase: String?,
    @SerializedName("subtitle_problem_purchase") val subtitleProblemPurchase: String?,
    @SerializedName("title_payment_failure") val titlePaymentFailure: String?,
    @SerializedName("subtitle_payment_failure") val subtitlePaymentFailure: String?,
    @SerializedName("chat_deeplink") val chatDeeplink: String? = null,
    @SerializedName("callback_deeplink") val callbackDeepLink: String?,
    @SerializedName("flag_id") val flagId: String?,
    @SerializedName("variant_id") val variantId: String?,
    @SerializedName("callback_btn_show") val callbackBtnShow: Boolean?,
    @SerializedName("chat_btn_show") val chatBtnShow: Boolean?,
    @SerializedName("title_text_size") val titleTextSize: String?,
    @SerializedName("title_text_color") val titleTextColor: String?,
    @SerializedName("subtitle_text_size") val subtitleTextSize: String?,
    @SerializedName("subtitle_text_color") val subtitleTextColor: String?,
    @SerializedName("action") val action: String?,
    @SerializedName("action_text_size") val actionTextSize: String?,
    @SerializedName("action_text_color") val actionTextColor: String?,
    @SerializedName("action_image_url") val actionImageUrl: String?,
    @SerializedName("image_url") val imageUrl: String?
)
