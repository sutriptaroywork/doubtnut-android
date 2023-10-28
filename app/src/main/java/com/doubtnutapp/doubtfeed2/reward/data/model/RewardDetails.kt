package com.doubtnutapp.doubtfeed2.reward.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
data class RewardDetails(
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("info_title") val infoTitle: String,
    @SerializedName("info_description") val infoDescription: String,
    @SerializedName("reward_title") val rewardTitle: String,
    @SerializedName("last_marked_streak") val lastMarkedAttendance: Int,
    @SerializedName("last_streak_timestamp") val lastMarkedAttendanceTimestamp: String?,
    @SerializedName("streak_items") var streaks: List<Streak>?,
    @SerializedName("header_cta_text") val headerCtaText: String? = null,
    @SerializedName("header_cta_image_link") val headerCtaImage: String? = null,
    @SerializedName("rewards") val rewards: List<Reward>,
    @SerializedName("know_more") val knowMoreData: KnowMoreData,
    @SerializedName("toggle_content") val toggleContent: String?,
    @SerializedName("is_notification_opted") val isNotificationOpted: Boolean,
    @SerializedName("share_text") val shareText: String?,
    @SerializedName("video_url") val videoUrl: String?,
    @SerializedName("question_id") val questionId: String?,
    @SerializedName("thumbnail_url") val thumbnailUrl: String?,
    @SerializedName("backpress_popup") val backPressPopupData: BackPressPopupData?,
    @SerializedName("incomplete_day_text") val incompleteDayText: String?,
) {
    val currentLevel: Int
        get() = rewards.lastOrNull { it.day <= lastMarkedAttendance && it.isUnlocked }?.level ?: 0
}

@Keep
data class Streak(
    val dayNumber: Int,
    var state: Int = FUTURE,
    var isScratched: Boolean = false,
    var isUnlocked: Boolean = false,
    var showGift: Boolean = false,
    var isCurrentDay: Boolean = false,
) {
    companion object State {
        const val UNMARKED = 0
        const val MARKED = 1
        const val UNSCRATCHED = 2
        const val SCRATCHED = 3
        const val FUTURE = 4
    }
}

@Keep
@Parcelize
data class Reward(
    @SerializedName("level") val level: Int,
    @SerializedName("day") val day: Int,
    @SerializedName("reward_type") val rewardType: String?,
    @SerializedName("short_desc") val shortDescription: String?,
    @SerializedName("scratch_image_link") val scratchedImageLink: String?,
    @SerializedName("scratch_desc") val scratchDescription: String?,
    @SerializedName("locked_short_desc") val lockedShortDescription: String?,
    @SerializedName("locked_desc") val lockedLongDescription: String?,
    @SerializedName("locked_subtitle") val lockedSubtitle: String?,
    @SerializedName("cta_text") val ctaText: String? = null,
    @SerializedName("secondary_cta_text") val secondaryCtaText: String?,
    @SerializedName("deeplink") var deeplink: String? = null,
    @SerializedName("is_share_enabled") val isShareEnabled: Boolean,
    @SerializedName("is_unlocked") val isUnlocked: Boolean,
    @SerializedName("is_scratched") val isScratched: Boolean,
    @SerializedName("wallet_amount") val walletAmount: Int?,
    @SerializedName("coupon_code") val couponCode: String?,
) : Parcelable {
    companion object Type {
        const val BETTER_LUCK_NEXT_TIME = "better"
        const val WALLET = "wallet"
        const val NCERT = "ncert"
        const val COUPON = "coupon"
    }
}

@Keep
data class BackPressPopupData(
    @SerializedName("backpress_popup_text") val backpressPopupText: String?,
    @SerializedName("backpress_popup_cta_text") val backpressPopupCtaText: String?,
)
