package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class GroupListData(

    @SerializedName("id") val groupId: String,
    @SerializedName("name") val groupName: String,
    @SerializedName("class") val groupClass: String,
    @SerializedName("is_active") val groupIsActive: String,

    @SerializedName("active_from") val groupActiveFrom: String,

    @SerializedName("active_till") val groupActiveTill: String,
    @SerializedName("is_delete") val groupIsDelete: String,

    @SerializedName("group_creator") val groupCreator: String,
    @SerializedName("created_at") val groupCreatedAt: String,
    @SerializedName("type") val groupType: String,
    @SerializedName("icon_url") val imageUrl: String

)
