package com.doubtnutapp.data.newglobalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiTrendingSearchPlaylistItem(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("display") val display: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("is_last") val isLast: Int?,
    @SerializedName("question_id") val questionId: Int?,
    @SerializedName("type") val type: String?,
    @SerializedName("resource_type") val resourceType: String?,
    @SerializedName("resource_path") val resourcePath: String?,
    @SerializedName("deeplink_url") val deeplinkUrl: String?,
    @SerializedName("class") val `class`: Int?,
    @SerializedName("subject") val subject: String?,
    @SerializedName("chapter") val chapter: String?,
    @SerializedName("doubt") val doubt: String?,
    @SerializedName("ocr_text") val ocrText: String?,
    @SerializedName("question") val question: String?,
    @SerializedName("is_active") val isActive: Int?,
    @SerializedName("bg_color") val videoBgColor: String?,
    @SerializedName("tab_type") val tabType: String?,
    @SerializedName("live_tag") val liveTag: Boolean?,
    @SerializedName("live_order") val liverOrder: Boolean?,
    @SerializedName("search_key") val searchKey: String?
)
