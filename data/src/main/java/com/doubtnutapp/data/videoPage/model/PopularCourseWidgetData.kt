package com.doubtnutapp.data.videoPage.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PopularCourseWidget(
    @SerializedName("delay_in_sec")
    val delayInSec: Long?,
    @SerializedName("data")
    val data: PopularCourseWidgetData?,
    @SerializedName("extra_params")
    val extraParams: HashMap<String, Any>? = null
)

@Keep
data class PopularCourseWidgetData(
    @SerializedName("items") val items: List<PopularCourseWidgetItem>?,
    @SerializedName("title") val title: String?,
    @SerializedName("background_color") val backgroundColor: String?,
    @SerializedName("auto_scroll_time_in_sec") val autoScrollTimeInSec: Long?,
    @SerializedName("flagr_id") val flagrId: String?,
    @SerializedName("variant_id") val variantId: String?,
    @SerializedName("more_text") val moreText: String?,
    @SerializedName("more_deeplink") val moreDeepLink: String?,
    var selectedPagePosition: Int = 0
)

@Keep
data class PopularCourseWidgetItem(
    @SerializedName("image_bg") val imageUrl: String?,
    @SerializedName("starting_at") val startText: String?,
    @SerializedName("starting_at_color") val startTextColor: String?,
    @SerializedName("amount_to_pay") val price: String?,
    @SerializedName("amount_to_pay_color") val priceColor: String?,
    @SerializedName("amount_strike_through") val crossedPrice: String?,
    @SerializedName("strikethrough_text_color") val crossedPriceColor: String?,
    @SerializedName("strikethrough_color") val crossColor: String?,
    @SerializedName("text") val text: String?,
    @SerializedName("text_color") val textColor: String?,
    @SerializedName("trial_image") val trialImageUrl: String?,
    @SerializedName("banner_text") val bannerText: String?,
    @SerializedName("banner_text_color") val bannerTextcolor: String?,
    @SerializedName("button_cta") val buttonText: String?,
    @SerializedName("button_text_color") val buttonTextColor: String?,
    @SerializedName("button_background_color") val buttonBackgroundColor: String?,
    @SerializedName("deeplink_banner") val deeplinkBanner: String?,
    @SerializedName("deeplink_button") val deeplinkButton: String?,
    @SerializedName("assortment_id") val assortmentId: String?,
    @SerializedName("banner_type") val bannerType: Int?
)
