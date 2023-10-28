package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep

@Keep
data class MatchQuestion(
    val matchedQuestions: MutableList<MatchQuestionViewItem>,
    val questionId: String,
    val matchedCount: Int,
    val questionImage: String?,
    val ocrText: String,
    val message: String,
    val isBlur: Boolean?,
    val youtubeFlag: Boolean,
    val autoPlay: Boolean?,
    val autoPlayDuration: Long?,
    val autoPlayInitiation: Long?,
    val isImageBlur: Boolean,
    val isImageHandwritten: Boolean,
    val p2pThumbnailImages: List<String>?,
    val liveTabData: LiveTabData?,
    val bottomTextData: HashMap<String, BottomTextData>?,
    val tabUrls: HashMap<String, String>?,
    val partialMatchedQuestions: MutableList<MatchQuestionViewItem>
)

