package com.doubtnutapp.data.remote.models.topicboostergame2

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.widgetmanager.widgets.FaqWidget
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by devansh on 14/06/21.
 */

data class TbgHomeData(
    @SerializedName("total_game_won") val totalGameWon: Int,
    @SerializedName("total_game") val totalGame: Int,
    @SerializedName("level_games") var levelGames: List<LevelGameNumber>,
    @SerializedName("level_title") val levelTitle: String,
    @SerializedName("level_description") val levelDescription: String?,
    @SerializedName("current_level") val currentLevel: Int,
    @SerializedName("is_recent_available") val isRecentAvailable: Boolean,
    @SerializedName("recent_container") val recentTopics: RecentTopics?,
    @SerializedName("is_subject_available") val isSubjectAvailable: Boolean,
    @SerializedName("subject_container") val subjects: SubjectData?,
    @SerializedName("is_quiz_history_available") val isQuizHistoryAvailable: Boolean,
    @SerializedName("quiz_history_container") val quizHistory: QuizHistory?,
    @SerializedName("is_leaderboard_available") val isLeaderboardAvailable: Boolean,
    @SerializedName("leaderboard_container") val leaderboardContainer: Leaderboard?,
    @SerializedName("faq") val faq: Faq,
    @SerializedName("bottom_banner") val bottomBanner: String?,
    @SerializedName("primary_cta") val primaryCta: String?,
    @SerializedName("primary_cta_deeplink") val primaryCtaDeeplink: String?,
)

data class RecentTopics(
    @SerializedName("recent_title") val recentTitle: String,
    @SerializedName("recent_topics") val recentTopics: List<Topic>,
)

@Keep
data class LevelGameNumber(
    val gameNumber: Int,
    var isDone: Boolean = false,
    var isLast: Boolean = false,
)

@Keep
@Parcelize
data class Topic(
    @SerializedName("title") val title: String,
    @SerializedName("chapter_alias") val chapterAlias: String?,
    var isSelected: Boolean = false,
) : Parcelable

@Keep
@Parcelize
data class SubjectData(
    @SerializedName("title") val title: String,
    @SerializedName("subjects") val subjects: List<Subject>,
) : Parcelable

@Keep
@Parcelize
data class Subject(
    @SerializedName("title") val title: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("subject_alias") val subjectAlias: String,
) : Parcelable

data class QuizHistory(
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("quiz_played_history") val quizPlayedHistory: List<QuizHistoryItem>,
    @SerializedName("show_view_more") val showViewMore: Boolean,
)

data class QuizHistoryViewMore(
    @SerializedName("page") val page: Int,
    @SerializedName("quiz_played_history") val quizPlayedHistory: List<QuizHistoryItem>,
)

@Keep
data class QuizHistoryItem(
    @SerializedName("title") val title: String,
    @SerializedName("result") val result: Int,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("result_text") val state: String?,
)

@Keep
@Parcelize
data class Faq(
    @SerializedName("title") val title: String? = "",
    @SerializedName("faq_list") val items: List<FaqWidget.FaqItem>? = listOf(),
) : Parcelable
