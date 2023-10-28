package com.doubtnutapp.data.remote.models

import androidx.annotation.Keep
import com.doubtnutapp.liveclass.adapter.LiveClassWidgetViewItem
import com.google.gson.annotations.SerializedName

@Keep
data class LiveClassChatData(
    @SerializedName("message") var message: String?,
    @SerializedName("student_id") val studentId: String? = "",
    @SerializedName("_id") val postId: String? = "",
    @SerializedName("student_img_url") val imageUrl: String? = "",
    @SerializedName("student_displayname") val name: String? = "",
    @SerializedName("room_type") val roomType: String? = "",
    @SerializedName("cdn_url") val cdnUrl: String? = "",
    @SerializedName("room_id") val roomId: String? = "",
    @SerializedName("is_author") val isAuthor: Boolean? = null,
    @SerializedName("attachment") var attachment: String? = "",
    @SerializedName("attachment_mime_type") var attachmentMimeType: String? = "",
    @SerializedName("user_tag") var userTag: String? = "",
    @SerializedName("is_admin") val isAdmin: Boolean? = false,
    @SerializedName("type") var type: Int = 0,
    @SerializedName("question_id") val questionId: String? = null,
    @SerializedName("thumbnail_image") val thumbnailImage: String? = null,
    @SerializedName("created_at") val timestamp: String? = "",
    @SerializedName("widget_data") val widgetData: LiveClassWidgetViewItem? = null
)

@Keep
data class LiveClassChatResponse(
    @SerializedName("rows") val messageList: List<LiveClassChatData>?,
    @SerializedName("offsetCursor") val offsetCursor: String?,
    @SerializedName("is_admin") val isAdminLoggedIn: Boolean?,
    @SerializedName("is_banned") val isUserBanned: Boolean?,
    @SerializedName("user_tag") val userTag: String? = ""
)

@Keep
data class ReportUserResponse(
    @SerializedName("_id") val studentId: String?
)

@Keep
data class BanUserData(
    @SerializedName("student_id") val studentId: String?,
    @SerializedName("room_id") val roomId: String?,
    @SerializedName("post_id") val postId: String?
)

@Keep
data class ReportUserData(
    @SerializedName("student_id") val studentId: String?,
    @SerializedName("room_id") val roomId: String?,
    @SerializedName("post_id") val postId: String?
)
