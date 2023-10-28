package com.doubtnutapp.gamification.popactivity

//Key that is going to be used as HashMap key
const val POP_TYPE_KEY = "popup_type"
const val POP_DIRECTION_KEY = "popup_direction"
const val POP_MESSAGE_KEY = "message"
const val POP_DESCRIPTION_KEY = "description"
const val POP_IMAGE_URL_KEY = "img_url"
const val POP_DURATION_KEY = "duration"
const val POP_UNLOCK_ACTION_DATA_KEY = "action_data"
const val POP_BUTTON_TEXT_KEY = "button_text"

/**
 * Different values that is going to be used as HashMap value for above [POP_TYPE_KEY] key
 */
const val POPUP_TYPE_LEVEL_UP = "popup_levelup"
const val POPUP_TYPE_ALERT_UP = "popup_alert"
const val POPUP_TYPE_BADGE = "popup_badge"
const val POPUP_TYPE_BADGE_ACHIEVED = "popup_badge_achieved"
const val POPUP_TYPE_POINTS_ACHIEVED = "popup_points_achieved"
const val POPUP_TYPE_POPUP_UNLOCKED = "popup_unlock"