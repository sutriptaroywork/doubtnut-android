package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class GroupChatMessages(
    @SerializedName("messages")val MessagesList: ArrayList<Comment>,
    @SerializedName("cursor")val cursorData: String
)
