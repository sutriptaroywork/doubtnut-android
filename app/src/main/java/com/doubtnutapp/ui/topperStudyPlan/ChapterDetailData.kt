package com.doubtnutapp.ui.topperStudyPlan

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class ChapterDetailData(@SerializedName("lecture_videos") val lectureVideosData: VideoDetailsData?,
                             @SerializedName("microconcept_videos") val microConceptVideos: VideoDetailsData?,
                             @SerializedName("playlist_data") val playlistData: List<WidgetEntityModel<WidgetData, WidgetAction>>?) {

    @Keep
    data class VideoDetailsData(val videos: List<VideoDetails>,
                                @SerializedName("total") val totalVideos: Int,
                                @SerializedName("watched") val videosWatched: Int) {

        @Keep
        data class VideoDetails(@SerializedName("question_id") val questionId: Long,
                                @SerializedName("ocr_text") val ocrText: String?,
                                @SerializedName("answer_id") val answerId: Long,
                                @SerializedName("duration") val duration: Int)
    }
}