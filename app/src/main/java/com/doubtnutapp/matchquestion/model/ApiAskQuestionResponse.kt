package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class ApiAskQuestionResponse(
    @SerializedName("matched_questions") val matchedQuestions: List<ApiMatchedQuestionItem>,
    @SerializedName("question_id") val questionId: String,
    @SerializedName("matched_count") val matchedCount: Int,
    @SerializedName("question_image") val questionImage: String?,
    @SerializedName("ocr_text") val ocrText: String,
    @SerializedName("delay_notification") val delayNotification: ApiDelayNotification?,
    @SerializedName("message") val message: String?,
    @SerializedName("popup_deeplink") val popupDeeplink: String?,
    @SerializedName("is_blur") val isBlur: Boolean?,
    @SerializedName("youtube_flag") val youtubeFlag: Int?,
    @SerializedName("auto_play") val autoPlay: Boolean?,
    @SerializedName("auto_play_duration") val autoPlayDuration: Long?,
    @SerializedName("auto_play_initiation") val autoPlayInitiation: Long?,
    @SerializedName("facets") val facets: List<ApiAdvanceSearchData>?,
    @SerializedName("is_image_blur") val isImageBlur: Boolean?,
    @SerializedName("is_image_handwritten") val isImageHandwritten: Boolean?,
    @SerializedName("p2p_thumbnail_images") val p2pThumbnailImages: List<String>?,
    @SerializedName("ocr_loading_order") val ocrLoadingOrder: List<String>?,
    @SerializedName("backPressMatchArray") val backPressMatchArray: List<ApiMatchedQuestionItem>?,
    @SerializedName("live_tab_data") val liveTabData: LiveTabData?,
    @SerializedName("bottom_text_data") val bottomTextData: HashMap<String, BottomTextData>?,
    @SerializedName("tab_urls") val tabUrls: HashMap<String, String>?,
    @SerializedName("d0_user_data") val d0UserData: D0UserData?,
    @SerializedName("matches_display_config") val matchesDisplayComfig: MatchesDisplayConfig?,
    @SerializedName("back_press_variant") val backpressVariant: Int?,
    @SerializedName("scroll_animation") val scrollAnimation: Boolean?
)

@Keep
data class D0UserData(
    @SerializedName("hide_bottom_nav") val hideBottomNav: Boolean?,
    @SerializedName("back_press_dialog_variant") val backPressDialogVariant: Int?,
    @SerializedName("back_press_dialog_cta") val backPressDialogCta: String?,
    @SerializedName("back_press_dialog_cta_deeplink") val backPressDialogCtaDeeplink: String?,
)

@Keep
data class MatchesDisplayConfig(
    @SerializedName("display_limit") val displayLimit: Int,
    @SerializedName("display_more_action_widget") val displayMoreActionWidget: WidgetEntityModel<*, *>
)