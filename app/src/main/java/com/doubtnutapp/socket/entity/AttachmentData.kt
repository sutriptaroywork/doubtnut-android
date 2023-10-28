package com.doubtnutapp.socket.entity

import androidx.annotation.Keep

@Keep
data class AttachmentData(
    val title: String?,
    val attachmentUrl: String,
    val attachmentType: AttachmentType,
    val audioDuration: Long? = null,
    val videoThumbnailUrl: String? = null,
    val isVideoCompressed: Boolean? = false,
    val roomId: String
)