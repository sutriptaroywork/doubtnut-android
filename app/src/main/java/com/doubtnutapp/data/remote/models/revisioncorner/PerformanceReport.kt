package com.doubtnutapp.data.remote.models.revisioncorner

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 16/08/21.
 */

@Keep
data class PerformanceReport(
    @SerializedName("title") val title: String,
    @SerializedName("stats") val stats: List<Stats>,
    @SerializedName("icon") val icon: String,
    @SerializedName("no_stats") val unavailableStatsData: UnavailableStats?,
)

@Keep
data class Stats(
    @SerializedName("title") val title: String,
    @SerializedName("scores") val scores: List<Score>,
    @SerializedName("subject_progress_items") val subjectProgressItems: List<SubjectProgress>,
    @SerializedName("cta_text") val ctaText: String,
    @SerializedName("cta_deeplink") val ctaDeeplink: String,
)

@Keep
data class Score(
    @SerializedName("title") val title: String,
    @SerializedName("score_text") val scoreText: String,
    @SerializedName("background_color") val backgroundColor: String,
)

@Keep
data class SubjectProgress(
    @SerializedName("subject") val subject: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("track_color") val trackColor: String,
    @SerializedName("indicator_color") val indicatorColor: String,
    @SerializedName("progress_text") val progressText: String,
    @SerializedName("max_progress") val maxProgress: Int,
    @SerializedName("progress") val progress: Int,
)
