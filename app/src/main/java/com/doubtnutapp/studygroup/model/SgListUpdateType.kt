package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep

@Keep
enum class SgListUpdateType(val type: String) {
    GROUP_LIST("groups"),
    CHAT_LIST("chats")
}

const val LIST_TYPE = "list_type"