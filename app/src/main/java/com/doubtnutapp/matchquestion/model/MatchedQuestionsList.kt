package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep
import com.doubtnutapp.videoPage.model.VideoResource

@Keep
data class MatchedQuestionsList(
    val id: String,
    val clazz: String?,
    val chapter: String?,
    val questionThumbnail: String,
    val questionThumbnailLocalized: String?,
    val source: ApiMatchedQuestionSource?,
    val canvas: ApiCanvas?,
    val html: String?,
    val resourceType: String,
    val videoResource: VideoResource?,
    var isMute: Boolean?,
    var showContinueWatching: Boolean?,
    var currentPosition: Long?,
    val answerId: Long?,
    val imageLoadingOrder: List<String>?,
    override var viewType: Int
) : MatchQuestionViewItem {
    fun isHtmlPresent() = html != null && html.isNotBlank()
}
