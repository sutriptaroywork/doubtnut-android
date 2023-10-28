package com.doubtnutapp.studygroup.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
data class CreateStudyGroup(
    @SerializedName("message") val message: String,
    @SerializedName("group_id") val groupId: String,
    @SerializedName("is_group_created") val isGroupCreated: Boolean,
    @SerializedName("initial_messages_data") val initialMessagesData: InitialMessageData?,
    @SerializedName("group_chat_deeplink") val groupChatDeeplink: String?
)

@Parcelize
@Keep
data class InitialMessageData(
    @SerializedName("group_guideline") val groupGuideline: String,
    @SerializedName("invite_message") val inviteMessage: String?,
    @SerializedName("invite_deeplink") val inviteDeeplink: String?,
    @SerializedName("invite_cta_text") val inviteCtaText: String?,
    @SerializedName("copy_invite_cta_text") val copyInviteCtaText: String?,
) : Parcelable