package com.doubtnutapp.fcm.notification

object NotificationConstants {

    const val DEFAULT_TITLE: String = "Doubtnut"

    /*all key use for notification goes here*/
    const val NOTIFICATION_PARAM_EVENT: String = "event"
    const val NOTIFICATION_PARAM_MESSAGE: String = "message"
    const val NOTIFICATION_PARAM_TITLE: String = "title"
    const val NOTIFICATION_PARAM_IMAGE_URL: String = "image"
    const val NOTIFICATION_EVENT_TAG: String = "firebase_eventtag"

    /*all constant use for video notification*/
    const val VIDEO_NOTIFICATION_REQUEST_CODE = 1
    const val VIDEO_NOTIFICATION = "video"
    const val CHANNEL_ID_VIDEO = VIDEO_NOTIFICATION

    /*all constant use for Course notification*/
    const val COURSE_NOTIFICATION_REQUEST_CODE = 2
    const val COURSE_NOTIFICATION = "course_notification"
    const val CHANNEL_ID_COURSE = COURSE_NOTIFICATION
    const val BANNER_REQUEST_CODE = 198
    const val BUTTON_REQUEST_CODE = 199

    //use where there is no need for small icon required in notification
    const val INVALID_SMALL_ICON_RESOURCE_ID = -1
    const val NOTIFICATION_GENERIC_ID_FOR_QUICK_SEARCH = 1001
    const val NOTIFICATION_USER_DISMISS_QUICK_SEARCH = "user_dismiss_quick_search"
    const val STICKY_NOTIFICATION_FLAGGR_UPDATE = "sticky_notification_flaggr_update"
    const val ACTION_QUICK_SEARCH: String = "com.doubtnut.action.QuickSearch"
    const val ACTION_COURSE_NOTIFICATION: String = "com.doubtnut.action.CourseNotification"

    const val ACTION_MATCH_PAGE: String = "com.doubtnut.action.MatchPage"
    const val MATCH_PAGE_CHANNEL_NAME: String = "Match Page Notification"
    const val MATCH_PAGE_CHANNEL_ID: String = "match_notification"
    const val OCR_FROM_IMAGE_CHANNEL_ID: String = "ocr_from_image_notification"
    const val OCR_FROM_IMAGE_CHANNEL_NAME: String = "Ocr From Image Notification"

    const val NOTIFICATION_STICKY_GENERIC_ID = 4444

    // Smart notification v2
    const val NOTIFICATION_ID_VIDEO_STICKY = 4444
    const val NOTIFICATION_CHANNEL_ID_VIDEO_STICKY = "Video playback"
    const val NOTIFICATION_USER_DISMISS_VIDEO_STICKY = "user_dismiss_video_sticky"
    const val NOTIFICATION_ACTION_VIDEO_STICKY = "com.doubtnut.action.VideoSticky"


    const val NOTIFICATION_ACTION_8PM = "Scheduled 8 pm notification"
    const val NOTIFICATION_ID_10PM = 5555
    const val NOTIFICATION_CHANNEL_ID_FALLBACK = "fallback_push_channel"
    const val NOTIFICATION_CHANNEL_NAME_FALLBACK = "fallback_push_channel"
    const val PENDING_INTENT_ALARM_10PM = 2298
    const val PENDING_INTENT_REPEAT_ALARM_10PM = 2204
    const val PENDING_INTENT_DEEPLINK_10PM = 2201
    const val PENDING_INTENT_FULL_SCREEN_INTENT = 2202

    //Reward notification
    const val NOTIFICATION_CHANNEL_ID_REWARD = "reward_push_channel"
    const val NOTIFICATION_CHANNEL_NAME_REWARD = "Reward Notification Channel"
    const val NOTIFICATION_ID_REWARD = 6601
    const val PENDING_INTENT_REWARD = 6602
    const val NOTIFICATION_ID_ATTENDANCE = 6603
    const val PENDING_INTENT_ATTENDANCE_CODE = 6604

    const val DEFAULT_NOTIFICATION_CHANNEL_ID = "default"

    // Study Group
    const val STUDY_GROUP_NOTIFICATION = "study_group_chat"
    const val STUDY_GROUP = "study_group"
    const val CHANNEL_ID_STUDY_GROUP = STUDY_GROUP_NOTIFICATION
    const val GROUP_KEY_STUDY_GROUP = "com.doubtnutapp.STUDY_GROUP"
    const val STUDY_GROUP_SUMMARY_NOTIFICATION_ID = 5555

    /*all constant use for sticky notification with timer*/
    const val GENERIC_STICKY_TIMER = "generic_sticky_timer"
    const val GENERIC_STICKY_TIMER_NOTIFICATION = GENERIC_STICKY_TIMER
    const val CHANNEL_ID_GENERIC_STICKY_TIMER = GENERIC_STICKY_TIMER
    const val GENERIC_STICKY_TIMER_BANNER_REQUEST_CODE = 19201
    const val CHANNEL_NAME_STICKY_TIMER = "Sticky Timer"
}