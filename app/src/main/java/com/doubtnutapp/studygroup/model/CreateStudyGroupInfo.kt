package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CreateStudyGroupInfo(
    @SerializedName("message") val message: String,
    @SerializedName("heading") val heading: String,
    @SerializedName("group_types") val groupTypes: List<GroupType>
)

@Keep
data class GroupType(
    @SerializedName("title") val title: String,
    @SerializedName("can_create_group") val canCreateGroup: Boolean,
    @SerializedName("active_tab") val activeTab: Boolean,
    @SerializedName("heading") val heading: String,
    @SerializedName("group_type") val groupType: Int,
    @SerializedName("guidelines") val guidelines: List<Guideline>,
    @SerializedName("no_group_create_message") val noGroupCreateMessage: String,
    @SerializedName("group_name_title") val groupNameTitle: String,
    @SerializedName("group_image_title") val groupImageTitle: String,
    @SerializedName("only_sub_admin_can_post_toggle") val onlySubAdminCanPostToggle: Boolean,
    @SerializedName("sub_admin_post_container") val subAdminPostContainer: MemberPostContainer?,
)

@Keep
data class Guideline(
    @SerializedName("index") val index: String,
    @SerializedName("content") val content: String,
)

@Keep
data class MemberPostContainer(
    @SerializedName("title") val title: String,
    @SerializedName("only_sub_admin_can_post") val onlySubAdminCanPost: Boolean,
)