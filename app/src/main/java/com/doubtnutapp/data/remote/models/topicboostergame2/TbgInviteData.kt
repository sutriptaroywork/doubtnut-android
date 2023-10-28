package com.doubtnutapp.data.remote.models.topicboostergame2

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class TbgInviteData(
    @SerializedName("tabs") val tabs: List<FriendTab>,
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("cta") val cta: String,
    @SerializedName("active_tab") val activeTab: Int,
    @SerializedName("game_id") val gameId: String,
    @SerializedName("multiple_invite") val multipleInvite: MultipleInvite,
    @SerializedName("whatsapp_cta") val whatsappCta: String?,
    @SerializedName("whatsapp_share_text") val whatsappShareText: String,
    @SerializedName("number_invite") val numberInvite: String,
    @SerializedName("send_invite_text") val sendInviteText: String,
    @SerializedName("search_placeholder") val searchPlaceholder: String,
)

@Keep
data class MultipleInvite(
    @SerializedName("message", alternate = ["title"]) val description: String,
    @SerializedName("primary_cta") val primaryCta: String,
    @SerializedName("secondary_cta") val secondaryCta: String,
)

@Keep
data class NumberInvite(
    @SerializedName("user_exist") val userExist: Boolean,
    @SerializedName("invitee_id") val inviteeId: String?,
    @SerializedName("chat_deeplink") val chatDeeplink: String?,
    @SerializedName("widget_data") val popupData: MultipleInvite?,
)
