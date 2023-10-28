package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ChatInfo(
    @SerializedName("chat_data") val chatData: ChatData?,
    @SerializedName("is_chat_enabled") val isChatEnable: Boolean?,
    @SerializedName("is_member") val isMember: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("has_blocked_other") val hasBlockedOther: Boolean?,
    @SerializedName("has_blocked_by_other") val hasBlockedByOther: Boolean?,
    @SerializedName("block_status") val blockStatus: Int?,
    @SerializedName("other_blocked_status") val otherBlockStatus: Int?,
    @SerializedName("is_mute") val isMute: Boolean?,
    @SerializedName("faq_deeplink") val faqDeeplink: String?,
    @SerializedName("invite_text") val copyProfileLink: String?,
    @SerializedName("other_student_profile_deeplink") val otherStudentProfileDeeplink: String?,
    @SerializedName("notification_id") val notificationId: Int?,
    @SerializedName("block_pop_up") val blockPopUp: BlockPopUp?,
    @SerializedName("unblock_pop_up") val unblockPopUp: BlockPopUp?,
    @SerializedName("invite_bottom_sheet") val inviteBottomSheet: SgChatRequestDialogConfig?,
    @SerializedName("gif_container") val gifContainer: GifContainer?,
)

@Keep
data class ChatData(
    @SerializedName("room_name") val roomName: String?,
    @SerializedName("room_image") val roomImage: String?,
)

@Keep
data class BlockPopUp(
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("primary_cta") val primaryCta: String?,
    @SerializedName("secondary_cta") val secondaryCta: String?,
)