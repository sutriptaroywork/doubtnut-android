package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class StudyGroupList(
    @SerializedName("groups") val groups: List<StudyGroup>,
    @SerializedName("user_left_message") val userLeftMessage: String?,
    @SerializedName("user_blocked_message") val userBlockedMessage: String?,
    @SerializedName("cta_text") val ctaText: String?,
    @SerializedName("can_create_group") val canCreateGroup: Boolean,
    @SerializedName("no_group_container") val noGroupContainer: SgChatRequestDialogConfig?,
)

@Keep
data class StudyGroup(
    @SerializedName("group_id") val groupId: String,
    @SerializedName("group_name") val groupName: String,
    @SerializedName("group_image") val groupImage: String?,
    @SerializedName("image_updated_by") val imageUpdatedBy: String?,
    @SerializedName("image_updated_at") val imageUpdatedAt: String?,
    @SerializedName("group_created_at") val groupCreatedAt: String?,
    @SerializedName("is_admin") val isAdmin: Int,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("is_left") val isLeft: Int?,
    @SerializedName("left_at") val leftAt: String?,
    @SerializedName("is_blocked") val isBlocked: Int?,
    @SerializedName("blocked_by") val blockedBy: String?,
    @SerializedName("blocked_at") val blockedAt: String?,
    @SerializedName("is_active") val isActive: Int?,
    @SerializedName("invite_status") var invitationStatus: Int?,
    @SerializedName("is_faq") val isFaq: Boolean,
    @SerializedName("is_mute") val isMute: Boolean?,
    @SerializedName("is_selected") var isSelected: Boolean? = false,
    @SerializedName("is_verified") var isVerified: Boolean? = false,
)