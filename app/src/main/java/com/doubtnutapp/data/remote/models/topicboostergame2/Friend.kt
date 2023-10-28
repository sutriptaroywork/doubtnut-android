package com.doubtnutapp.data.remote.models.topicboostergame2

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 22/06/21.
 */

@Keep
data class Friend(
    @SerializedName("name") val name: String,
    @SerializedName("student_id") val studentId: Long,
    @SerializedName("student_class") val studentClass: String,
    @SerializedName("image") val image: String,
    @SerializedName("deeplink") val deeplink: String? = null,
    var isSelected: Boolean = false
)

@Keep
data class FriendsList(
    @SerializedName("no_members_title") val noMembersTitle: String,
    @SerializedName("no_members_subtitle") val noMembersSubtitle: String,
    @SerializedName("user_data") val friendsList: List<Friend>?,
)
