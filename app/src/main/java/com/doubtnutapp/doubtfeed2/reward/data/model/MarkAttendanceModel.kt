package com.doubtnutapp.doubtfeed2.reward.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
data class MarkAttendanceModel(
    @SerializedName("popup_data") val popupData: RewardPopupModel? = null,
    @SerializedName("is_reward") val isRewardPresent: Boolean = false,
    @SerializedName("scratch_card") val rewardData: Reward?,
    @SerializedName("notification_data") val notificationData: RewardNotificationData?,
    @SerializedName("is_attendance_marked") val isAttendanceMarked: Boolean? = false,
    @SerializedName("is_streak_break") val isStreakBreak: Boolean,
    @SerializedName("day") val day: Int,
)

@Keep
@Parcelize
data class RewardPopupModel(
    @SerializedName("popup_heading") val popupHeading: String? = null,
    @SerializedName("popup_description") val popupDescription: String? = null,
    @SerializedName("is_attendance_marked") var isAttendanceMarked: Boolean = false,
    var isRewardPresent: Boolean = false,
    var isStreakBreak: Boolean = false,
    var isDataOnly: Boolean = false,
) : Parcelable

@Keep
data class RewardNotificationData(
    @SerializedName("topic") val notificationHeading: String? = null,
    @SerializedName("description") val notificationDescription: String? = null,
)
