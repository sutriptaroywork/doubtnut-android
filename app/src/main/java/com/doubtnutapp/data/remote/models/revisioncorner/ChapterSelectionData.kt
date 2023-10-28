package com.doubtnutapp.data.remote.models.revisioncorner

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by devansh on 12/08/21.
 */

@Keep
data class ChapterSelectionData(
    @SerializedName("topics") val topics: List<String>,
    @SerializedName("random_topic") val randomTopic: String?,
    @SerializedName("content") val content: SelectScreenContent,
    @SerializedName("is_recent_available") val isRecentAvailable: Boolean,
    @SerializedName("recent_container") val recentContainerData: RecentContainerData,
    @SerializedName("topic_object_list") var topicObjectList: List<Topic> = emptyList(),
    @SerializedName("recent_topic_object_list") var recentTopicObjectList: List<Topic>? = emptyList(),
    @SerializedName("subjects") val subjects: List<Subject>,
)

@Keep
data class SelectScreenContent(
    @SerializedName("heading") val heading: String,
    @SerializedName("description") val description: String,
    @SerializedName("random_opponent") val randomText: String,
    @SerializedName("previous_cta") val previousCta: String,
    @SerializedName("next_cta") val nextCta: String,
    @SerializedName("widget_id") val widgetId: Int,
    @SerializedName("select_chapters") val selectChapter: String,
    @SerializedName("choose_subject") val chooseSubject: String?,
    @SerializedName("select_chapter_for_game") val selectChapterForGame: String?,
    @SerializedName("search_placeholder") val searchPlaceholder: String?,
)

@Keep
data class RecentContainerData(
    @SerializedName("recent_title") val title: String,
    @SerializedName("recent_topics") val topics: List<Topic>?,
)

@Keep
@Parcelize
data class Subject(
    @SerializedName("title") val title: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("subject_alias") val subjectAlias: String,
) : Parcelable

@Keep
@Parcelize
data class Topic(
    @SerializedName("title") val title: String,
    @SerializedName("chapter_alias") val chapterAlias: String?,
    var isSelected: Boolean = false,
) : Parcelable
