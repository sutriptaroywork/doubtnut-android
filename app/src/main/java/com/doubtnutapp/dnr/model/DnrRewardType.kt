package com.doubtnutapp.dnr.model

enum class DnrRewardType(val type: String) {
    SIGN_UP("signup"),
    VIDEO_VIEW("video_view"),
    LIVE_CLASS("live_class"),
    DAILY_STREAK("weekly_streak"),
    STUDY_GROUP("study_group"),
    COURSE_PURCHASE("course_purchase"),
    PDF_PURCHASE("pdf_purchase"),
    REFER_AND_EARN_REWARD("refer_and_earn_reward")
}
