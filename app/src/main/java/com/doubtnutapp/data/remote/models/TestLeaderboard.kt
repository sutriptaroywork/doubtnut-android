package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by akshaynandwana on
 * 19, January, 2019
 **/
@Parcelize
data class TestLeaderboard(
    @SerializedName("test_id") val testId: Int,
    @SerializedName("student_id") val studentId: Int,
    @SerializedName("totalscore") val totalScore: Int,
    @SerializedName("totalmarks") val totalMarks: Int,
    @SerializedName("eligiblescore") val eligibleScore: Int,
    @SerializedName("student_username") val studentUsername: String?,
    @SerializedName("img_url") val imgUrl: String?
) : Parcelable
