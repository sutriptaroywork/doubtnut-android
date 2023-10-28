package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class StudyGroupMessage(
    @SerializedName("rows") val messageList: List<WidgetEntityModel<*, *>?>?,
    @SerializedName("offsetCursor") val offsetCursor: String?,
    @SerializedName("page") val page: Int,
)

@Keep
data class StudyGroupMessageWithId(
    @SerializedName("rows") val messageList: List<MessageWithId>?,
    @SerializedName("offsetCursor") val offsetCursor: String?,
    @SerializedName("page") val page: Int,
)

@Keep
data class MessageWithId(
    @SerializedName("id") val id: String?,
    @SerializedName("message") val message: WidgetEntityModel<*, *>?
)

@Keep
data class StudyGroupWrappedMessage(
    @SerializedName("rows") val messageList: List<StudyGroupChatWrapper>?,
    @SerializedName("offsetCursor") val offsetCursor: String?,
    @SerializedName("page") val page: Int,
)

@Keep
data class StudyGroupChatWrapper(
    @SerializedName("message") val message: WidgetEntityModel<*, *>?,
    @SerializedName("message_id") val messageId: String? = null,
    @SerializedName("room_id") val roomId: String?,
    @SerializedName("room_type") val roomType: String?,
    @SerializedName("student_id") val studentId: String,
    @SerializedName("_id") val id: String? = null,
    @SerializedName("millis") val millis: Long? = null,
    @SerializedName("view_id") val viewId: String? = null,
    @SerializedName("members") val members: List<String>? = null,
    @SerializedName("is_message") val isMessage: Boolean
)

@Keep
data class StudyGroupDashboardMessage(
    @SerializedName("rows") val message: List<WidgetEntityModel<*, *>?>?,
    @SerializedName("page") val page: Int,
    @SerializedName("primary_cta") val primaryCta: Cta?,
    @SerializedName("secondary_cta") val secondaryCta: Cta?,
)

@Keep
data class Cta(
    @SerializedName("title") val title: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("sender_id") val senderId: String?,
    @SerializedName("sender_name") val senderName: String?,
    @SerializedName("message_id") val messageId: String?,
    @SerializedName("action") val action: String?,
    @SerializedName("event") val event: String?,
    @SerializedName("confirmation_popup") val confirmationPopup: ConfirmationPopup?,
)

@Keep
data class ConfirmationPopup(
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("primary_cta") val primaryCta: String?,
    @SerializedName("secondary_cta") val secondaryCta: String?,
)