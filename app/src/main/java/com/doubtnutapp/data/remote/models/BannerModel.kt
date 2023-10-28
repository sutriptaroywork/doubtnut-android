package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class BannerModel(
    @SerializedName("id") val id: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("action_activity") val actionActivity: String,
    @SerializedName("action_data") val actionData: BannerActionData?,
    @SerializedName("start_date") val startDate: String?,
    @SerializedName("end_date") val endDate: String?,
    @SerializedName("is_active") val isActive: String?,
    @SerializedName("image_url_new") val imageUrlNew: String?,
    @SerializedName("button_text") val buttonText: String?
)
