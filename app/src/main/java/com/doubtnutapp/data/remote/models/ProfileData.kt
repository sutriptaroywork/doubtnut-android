package com.doubtnutapp.data.remote.models

import com.doubtnutapp.data.gamification.userProfile.model.ApiDailyAttendanceItem
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

data class ProfileData(
    val student_id: String,
    val student_fname: String,
    val student_lname: String?,
    val student_email: String,
    val locale: String,
    val img_url: String,
    val school_name: String?,
    val student_class: String,
    val total_video_view_duration: String,
    val student_username: String,
    val pincode: String?,
    val coaching: String?,
    val dob: String?,
    val student_course: String?,
    val student_class_display: String?,
    val display_exam: String?,
    val display_board: String?,
    val display_class: String?,
    val lvl: Int,
    val points: Int,
    val ex_board: String?,
    val coins: Int,
    val follower: Int,
    val follows: Int,
    val is_follower: Int,
    val is_following: Int?,
    @SerializedName("language") val language: String?,
    @SerializedName("language_display") val languageDisplay: String?,
    @SerializedName("daily_streak_progress")
    val dailyStreakProgress: List<ApiDailyAttendanceItem>?,
    @SerializedName("is_verified") val isVerified: Boolean?,
    @SerializedName("verified_label") val verifiedLabel: String?,
    @SerializedName("last_srp_qid") val lastSrpQid: String?,
    @SerializedName("can_be_invited_to_group") val canBeInvitedToGroup: Boolean?,
    @SerializedName("can_start_personal_chat") val canStartPersonalChat: Boolean?,
    @SerializedName("study_group_invite_cta_text") val studyGroupInviteCtaText: String?,
    @SerializedName("personal_chat_invite_cta_text") val personalChatInviteCtaText: String?,
    @SerializedName("popular_courses") val popularCourses: List<WidgetEntityModel<WidgetData, WidgetAction>>?,
    @SerializedName("popup_deeplink") val popupDeeplink: String?,
    @SerializedName("trial_discount_card") val trialDiscountCard: WidgetEntityModel<WidgetData, WidgetAction>?,
    @SerializedName("doubt_pe_charcha_reward_widget") val doubtPeCharchaWidget: WidgetEntityModel<WidgetData, WidgetAction>?,
    @SerializedName("badge_outer_image_url") val outerCircleImageUrl: String?,
    @SerializedName("badge_start_image_url") val badgeStarImgUrl: String?,

    )