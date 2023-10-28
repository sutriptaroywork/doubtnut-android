package com.doubtnutapp.ui.topperStudyPlan

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class StudyPlanData(@SerializedName("chapters") val chapters: List<Chapter>, @SerializedName("playlist_data") val playlistData: List<WidgetEntityModel<WidgetData, WidgetAction>>?) {

    @Keep
    data class Chapter(@SerializedName("id") val id: Long, @SerializedName("image_url") val imageUrl: String,
                       @SerializedName("title") val title: String?, @SerializedName("subject") val subject: String,
                       @SerializedName("chapter") val chapterName: String?,
                       @SerializedName("max_microconcept_viewed") val maxMicroConceptViewed: Int,
                       @SerializedName("microconcept_viewed") val microConceptViewed: Int,
                       @SerializedName("total_microconcepts") val totalMicroConcepts: Int)
}