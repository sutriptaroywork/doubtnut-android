package com.doubtnutapp.data.newlibrary.model

import androidx.annotation.Keep
import com.doubtnutapp.data.common.model.ApiPromotionalData
import com.google.gson.annotations.SerializedName

@Keep
data class ApiLibraryListData(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val title: String?,
    @SerializedName("view_type") val viewType: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("wa_url") val waUrl: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("student_class") val studentClass: String?,
    @SerializedName("student_id") val studentId: Int?,
    @SerializedName("scroll_size") val scrollSize: String?,
    @SerializedName("size") val size: String?,
    @SerializedName("list_key") val listKey: String?,
    @SerializedName("is_last") val isLast: String?,
    @SerializedName("start_gradient") val startGradient: String?,
    @SerializedName("sharing_message") val sharingMessage: String?,
    @SerializedName("resource_type") val resourceType: String?,
    @SerializedName("resource_path") val resourcePath: String?,
    @SerializedName("data") val promotionalData: List<ApiPromotionalData>?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("announcement") val announcement: ApiLibraryAnnouncement?
)
