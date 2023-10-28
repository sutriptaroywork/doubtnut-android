package com.doubtnutapp.data.store.model

import com.google.gson.annotations.SerializedName

data class ApiStoreData(
    @SerializedName("id") val id: Int,
    @SerializedName("resource_type") val resourceType: String?,
    @SerializedName("resource_id") val resourceId: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("img_url") val imgUrl: String?,
    @SerializedName("is_active") val isActive: Int?,
    @SerializedName("price") val price: Int?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("display_category") val displayCategory: String?,
    @SerializedName("is_last") val isLast: Int?,
    @SerializedName("redeem_status") val redeemStatus: Int

)
