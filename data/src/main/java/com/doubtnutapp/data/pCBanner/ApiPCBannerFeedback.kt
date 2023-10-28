package com.doubtnutapp.data.pCBanner

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiPCBanner(
    @SerializedName("index") val index: Int,
    @SerializedName("list_key") val listKey: String,
    @SerializedName("type") val type: String,
    @SerializedName("data") val dataList: List<ApiPCDataList>,
    @SerializedName("resource_type") val resourceType: String = "pcBanner"
) {
    @Keep
    class ApiPCDataList(
        @SerializedName("image_url") val imageUrl: String,
        @SerializedName("action_activity") val actionActivity: String,
        @SerializedName("position") val bannerPosition: Int,
        @SerializedName("banner_order") val bannerOrder: Int,
        @SerializedName("page_type") val pageType: String,
        @SerializedName("class") val studentClass: String,
        @SerializedName("action_data") val actionData: ApiBannerPCActionData?
    ) {
        @Keep
        class ApiBannerPCActionData(
            @SerializedName("playlist_id")
            val playlistId: String?,
            @SerializedName("playlist_title")
            val playlistTitle: String?,
            @SerializedName("is_last")
            val isLast: Int?,
            @SerializedName("event_key")
            val eventKey: String?,
            @SerializedName("faculty_id")
            val facultyId: Int?,
            @SerializedName("ecm_id")
            val ecmId: Int?,
            @SerializedName("subject")
            val subject: String?
        )
    }
}
