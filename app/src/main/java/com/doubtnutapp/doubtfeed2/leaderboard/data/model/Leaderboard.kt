package com.doubtnutapp.doubtfeed2.leaderboard.data.model

import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 12/7/21.
 */

@Keep
data class Leaderboard(
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("tabs") val tabs: List<Tab>,
    @SerializedName("active_tab") val activeTab: Int,
    @SerializedName("leaderboard_data", alternate = ["winners"])
    val leaderboardData: List<LeaderboardStudent>?,
    @SerializedName("student_data") val studentData: StudentData,
    @SerializedName("rank") val rank: String?,
    @SerializedName("rank_text") val rankText: String?,
    @SerializedName("is_rank_available") val isRankAvailable: Boolean,
    @SerializedName("leaderboard_deeplink") val leaderboardDeeplink: String?,
)

@Keep
data class LeaderboardStudent(
    @SerializedName("name") val name: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("student_id") val studentId: String,
    @SerializedName("rank") val rank: Int,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("label") val label: String,
    @SerializedName("is_own") val isOwn: Int?,
) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LeaderboardStudent>() {

            override fun areItemsTheSame(
                oldItem: LeaderboardStudent,
                newItem: LeaderboardStudent
            ): Boolean {
                return oldItem.studentId == newItem.studentId
            }

            override fun areContentsTheSame(
                oldItem: LeaderboardStudent,
                newItem: LeaderboardStudent
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}

@Keep
data class StudentData(
    @SerializedName("rank") val rank: String,
    @SerializedName("score") val score: String,
    @SerializedName("name") val name: String?,
    @SerializedName("image") val image: String?,
)

@Keep
data class Tab(
    @SerializedName("title") val title: String,
    @SerializedName("id") val id: Int,
)
