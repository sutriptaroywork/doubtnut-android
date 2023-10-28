package com.doubtnutapp.studygroup.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
data class StudyGroupInfo(
    @SerializedName("group_data") val groupData: GroupData?,
    @SerializedName("is_group_enabled") val isGroupEnabled: Boolean?,
    @SerializedName("is_member") val isMember: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("group_guideline") val groupGuideline: String?,
    @SerializedName("know_more_text") val knowMoreText: String?,
    @SerializedName("know_more_deeplink") val knowMoreDeeplink: String?,
    @SerializedName("remaining_sub_admin_count") val remainingSubAdminCount: Int,
    @SerializedName("group_minimum_member_warning_message") val groupMinimumMemberWarningMessage: String?,
    @SerializedName("faq_deeplink") val faqDeeplink: String?,
    @SerializedName("invite_text") val inviteText: String?,
    @SerializedName("is_faq") val isFaq: Boolean,
    @SerializedName("is_mute") val isMute: Boolean?,
    @SerializedName("can_edit_group_info") val canEditGroupInfo: Boolean,
    @SerializedName("notification_id") val notificationId: Int?,
    @SerializedName("report_reasons") val reportReasons: ReportReasons,
    @SerializedName("report_bottom_sheet") val reportBottomSheet: ReportBottomSheet?,
    @SerializedName("member_title") val memberTitle: String?,
    @SerializedName("only_sub_admin_can_post_container") val onlySubAdminCanPostContainer: MessagePostAccessContainer?,
    @SerializedName("gif_container") val gifContainer: GifContainer?,
    @SerializedName("is_blocked") val isBlocked: Boolean?,
    @SerializedName("admin_id") val adminId: String?,
    @SerializedName("member_status") val memberStatus: Int?,
    @SerializedName("member_ids") val memberIds: List<String>?,
    @SerializedName("is_support") val isSupport: Boolean?,
)

@Keep
data class GroupData(
    @SerializedName("group_info") val groupInfo: GroupInfo?,
)

@Keep
data class GroupInfo(
    @SerializedName("group_id") val groupId: String?,
    @SerializedName("group_type") val groupType: Int?,
    @SerializedName("group_name") val groupName: String?,
    @SerializedName("group_image") val groupImage: String?,
    @SerializedName("group_created_at") val groupCreatedAt: String?,
    @SerializedName("last_message_sent_at") val lastMessageSentAt: String?,
    @SerializedName("only_sub_admin_can_post") val onlySubAdminCanPost: Boolean,
    @SerializedName("is_paid_group") val isPaidGroup: Boolean?
)

@Parcelize
@Keep
data class ReportReasons(
    @SerializedName("title") val title: String?,
    @SerializedName("reasons") val reasons: List<String>?,
    @SerializedName("primary_cta") val primaryCta: String?,
    @SerializedName("secondary_cta") val secondaryCta: String?,
    @SerializedName("other_reason") val otherReason: String?,
    @SerializedName("others_container") val otherContainer: OtherContainer,
) : Parcelable


@Parcelize
@Keep
data class ReportBottomSheet(
    @SerializedName("image") val image: String?,
    @SerializedName("heading") val heading: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("cta_text") val ctaText: String?,
    @SerializedName("can_access_chat") val canAccessChat: Boolean?,
    @SerializedName("can_skip") val canSkip: Boolean?,
    @SerializedName("can_request_for_review") val canRequestForReview: Boolean?,
    @SerializedName("type") val type: Int,
) : Parcelable

@Parcelize
@Keep
data class OtherContainer(
    @SerializedName("heading") val heading: String?,
    @SerializedName("placeholder") val placeholder: String?,
    @SerializedName("primary_cta") val primaryCta: String?,
    @SerializedName("secondary_cta") val secondaryCta: String?,
) : Parcelable

@Keep
data class MessagePostAccessContainer(
    @SerializedName("toggle ") val toggle: Boolean?,
    @SerializedName("title") val title: String?,
    @SerializedName("more_info") val tooltipMessage: String?,
    @SerializedName("message") val ToggleMessage: ToggleMessage?
)

@Keep
data class ToggleMessage(
    @SerializedName("true") val trueMessage: String,
    @SerializedName("false") val falseMessage: String,
)

@Keep
data class GifContainer(
    @SerializedName("is_gif_enabled") val isGifEnabled: Boolean?,
    @SerializedName("message") val message: String?,
)