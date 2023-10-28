package com.doubtnutapp.data.similarVideo.model

import androidx.annotation.Keep
import com.doubtnutapp.data.pCBanner.ApiPCBanner
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class ApiSimilarVideoList(
    @SerializedName("question_id") val questionIdSimilar: String,
    @SerializedName("ocr_text") val ocrTextSimilar: String,
    @SerializedName("thumbnail_image") val thumbnailImageSimilar: String,
    @SerializedName("locale_thumbnail_image") val localeThumbnailImageSimilar: String?,
    @SerializedName("bg_color") val bgColorSimilar: String,
    @SerializedName("duration") val durationSimilar: Int,
    @SerializedName("share") val shareCountSimilar: Int,
    @SerializedName("like") val likeCountSimilar: Int,
    @SerializedName("isLiked") val isLikedSimilar: Boolean,
    @SerializedName("share_message") val sharingMessage: String,
    @SerializedName("id") val id: String?,
    @SerializedName("youtube_id") val youTubeIdSimilar: String?,
    @SerializedName("key_name") val keyName: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("button_text") val buttonText: String?,
    @SerializedName("button_bg_color") val buttonBgColor: String?,
    @SerializedName("action_activity") val actionActivity: String?,
    @SerializedName("action_data") val actionData: WhatsappActionData?,
    @SerializedName("resource_type") val resourceType: String,
    @SerializedName("html") val html: String?,
    @SerializedName("search_text") val searchText: String?,
    @SerializedName("question") val questionTextConceptVideo: String?,
    @SerializedName("index") val index: Int,
    @SerializedName("list_key") val listKey: String,
    @SerializedName("subject") val subjectName: String?,
    @SerializedName("views") val views: String?,
    @SerializedName("target_course") val targetCourse: String?,
    @SerializedName("book_meta") val meta: String?,
    @SerializedName("is_locked") val isLocked: Int = 1,
    @SerializedName("data") val dataList: List<ApiPCBanner.ApiPCDataList>,
    @SerializedName("tags_list") val tagsList: List<String>?,
    @SerializedName("ref") val ref: String?,
    @SerializedName("views_text") val viewsText: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("is_submitted") val isSubmitted: Int?,
    @SerializedName("submitted_option") val submittedOption: String?,
    @SerializedName("options") val options: List<TopicBoosterOption>?,
    @SerializedName("type") val type: String?,
    @SerializedName("list") val list: List<ApiNcertEntity>?,
    @SerializedName("widget_type") val widgetType: String?,
    @SerializedName("submit_url_endpoint") val submitUrlEndpoint: String?,
    @SerializedName("background_color") val backgroundColor: String?,
    @SerializedName("solution_text_color") val solutionTextColor: String?,
    @SerializedName("heading") val heading: String?,
    @SerializedName("is_vip") val isVip: String?,
    @SerializedName("assortment_id") val assortmentId: String?,
    @SerializedName("variant_id") val variantId: String?,
    @SerializedName("payment_details") val paymentDetails: String?,
    @SerializedName("image_url_second") val imageUrlSecond: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("bottom_text") val bottomText: String?,
    @SerializedName("coupon_code") val couponCode: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("end_time") val endTime: Long?,
    @SerializedName("scratch_text") val scratchText: String?,
    @SerializedName("price_text") val priceText: String?,
    @SerializedName("buy_now_text") val buyNowText: String?,
    @SerializedName("nudge_id") val nudgeId: String?,
    @SerializedName("isPlayableInPIP") val isPlayableInPip: Boolean?,
    @SerializedName("widget_data") val widgetData: WidgetEntityModel<WidgetData, WidgetAction>,
    @SerializedName("question_tag") val questionTag: String?,
)

@Keep
data class WhatsappActionData(
    @SerializedName("url") val externalUrl: String
)

@Keep
data class TopicBoosterOption(
    @SerializedName("option_code") val optionCode: String,
    @SerializedName("option_title") val optionTitle: String,
    @SerializedName("is_answer") val isAnswer: Int,
    @SerializedName("type") val type: String
)

@Keep
data class ApiNcertEntity(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("is_last") val isLast: String?,
    @SerializedName("parent") val parent: String?,
    @SerializedName("resource_type") val resourceType: String?,
    @SerializedName("student_class") val studentClass: String?,
    @SerializedName("subject") val subject: String?,
    @SerializedName("main_description") val mainDescription: String?
)
