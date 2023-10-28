package com.doubtnutapp.data.remote.models.topicboostergame2

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class ChapterSelectionData(
    @SerializedName("topics") val topics: List<String>,
    @SerializedName("random_topic") val randomTopic: String,
    @SerializedName("content") val content: SelectScreenContent,
    @SerializedName("is_recent_available") val isRecentAvailable: Boolean,
    @SerializedName("recent_container") val recentContainerData: RecentContainerData,
    @SerializedName("topic_object_list") var topicObjectList: List<Topic> = emptyList(),
    @SerializedName("recent_topic_object_list") var recentTopicObjectList: List<Topic>? = emptyList(),
    @SerializedName("subjects") val subjects: List<Subject>,
) : Parcelable

@Keep
@Parcelize
data class SelectScreenContent(
    @SerializedName("heading") val heading: String,
    @SerializedName("description") val description: String,
    @SerializedName("random_opponent") val randomText: String,
    @SerializedName("primary_cta") val primaryCta: String,
    @SerializedName("secondary_cta") val secondaryCta: String,
    @SerializedName("select_chapters") val selectChapter: String,
    @SerializedName("choose_subject") val chooseSubject: String?,
    @SerializedName("select_chapter_for_game") val selectChapterForGame: String?,
    @SerializedName("search_placeholder") val searchPlaceholder: String?,
) : Parcelable

@Keep
@Parcelize
data class RecentContainerData(
    @SerializedName("recent_title") val title: String,
    @SerializedName("recent_topics") val topics: List<Topic>?,
) : Parcelable
