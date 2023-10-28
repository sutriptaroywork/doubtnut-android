package com.doubtnut.core.data.remote

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
open class CoreResponse<T>(val meta: ResponseMeta, val data: T, val error: T? = null)

@Keep
data class ResponseMeta(
    val code: Int,
    val message: String,
    val success: String?,
    @SerializedName("inapp_pop_up")
    val inAppPopUp: InAppPopup? = null,
    val analytics: ApiMetaAnalytics? = null,
    val extras: UnauthorizedResponse? = null,
    val incrementKeys: Map<String, Int>? = null,
)

@Keep
data class InAppPopup(
    @SerializedName("deeplink") val deeplink: String?
)

@Keep
data class ApiMetaAnalytics(
    @SerializedName("variant_info") val variantInfo: List<VariantInfo>?,
    @SerializedName("events") val events: List<Event>?,
    @SerializedName("attributes") val attributes: List<Attribute>?
)

@Keep
data class VariantInfo(
    @SerializedName("flag_name") val flagName: String,
    @SerializedName("variant_id") val variationId: Int
)

@Keep
data class Event(
    @SerializedName("platforms") val platforms: List<String>?,
    @SerializedName("name") val name: String?,
    @SerializedName("params") var params: HashMap<String, Any>?
)

@Keep
data class Attribute(
    @SerializedName("platforms") val platforms: List<String>?,
    @SerializedName("key") val key: String?,
    @SerializedName("value") var value: Any?
)

@Keep
data class UnauthorizedResponse(
    @SerializedName("guestLoginAppUseLimitExceed") val guestLoginAppUseLimitExceed: Boolean?,
    @SerializedName("popupDetails") val popupDetails: PopupDetails?,
)

@Keep
@Parcelize
data class PopupDetails(
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("subTitle") val subtitle: String?,
    @SerializedName("ctaText") val ctaText: String?,
) : Parcelable
