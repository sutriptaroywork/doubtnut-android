@file:Suppress("PackageName")

package com.doubtnutapp.EventBus

import com.doubtnutapp.data.remote.models.userstatus.StatusAttachment

class StatusCreated(var status: StatusAttachment)
class StatusBottomSheetClosed
class StatusDeleted(val attachmentPostion: Int)
class StatusViewed(
    val source: String,
    val statusPosition: Int,
    val attachmentPostion: Int,
    val id: String
)

class StatusLiked(val source: String, val statusPosition: Int, val id: String, val value: Boolean)
class StatusFollowed(val source: String, val statusPosition: Int)
