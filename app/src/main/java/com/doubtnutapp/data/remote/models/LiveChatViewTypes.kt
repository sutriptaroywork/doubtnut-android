package com.doubtnutapp.data.remote.models

import androidx.annotation.IntDef

class LiveChatViewTypes {
    @IntDef(
        LiveChatViewTypes.LIVE_CHAT_RECEIVE,
        LiveChatViewTypes.LIVE_CHAT_SEND
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class ViewType
    companion object {
        const val LIVE_CHAT_RECEIVE = 1
        const val LIVE_CHAT_SEND = 2
    }
}
