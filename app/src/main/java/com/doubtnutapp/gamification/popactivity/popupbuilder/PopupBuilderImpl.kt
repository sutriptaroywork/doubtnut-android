package com.doubtnutapp.gamification.popactivity.popupbuilder

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import com.doubtnutapp.R
import com.doubtnutapp.gamification.popactivity.*
import com.doubtnutapp.gamification.popactivity.model.*
import com.doubtnut.core.utils.ViewUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PopupBuilderImpl(
    private val context: Context,
    private val screenWidth: Int,
    private val gson: Gson
) : PopupBuilder {

    override fun build(data: Map<String, String>): GamificationPopup? {

        val popupType = getPopUpType(data)

        return if (popupType != null && popupType.isNotEmpty()) {
            getPreparedData(popupType, data)
        } else {
            null
        }
    }

    private fun getPreparedData(
        popupType: String,
        popupData: Map<String, String>
    ): GamificationPopup? {
        return when (popupType) {

            POPUP_TYPE_BADGE -> PopupBadge(
                R.layout.popupview_badge,
                getGravity(popupData),
                getWidth(popupType),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                getPopUpDuration(popupData[POP_DURATION_KEY]),
                popupData[POP_DESCRIPTION_KEY] ?: "",
                popupData[POP_MESSAGE_KEY] ?: "",
                popupData[POP_IMAGE_URL_KEY] ?: ""
            )

            POPUP_TYPE_BADGE_ACHIEVED -> PopupBadgeAchieved(
                R.layout.popupview_badgeachieved,
                getGravity(popupData),
                getWidth(popupType),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                getPopUpDuration(popupData[POP_DURATION_KEY]),
                popupData[POP_DESCRIPTION_KEY] ?: "",
                popupData[POP_MESSAGE_KEY] ?: "",
                popupData[POP_IMAGE_URL_KEY] ?: ""
            )

            POPUP_TYPE_LEVEL_UP -> PopUpLevelUp(
                R.layout.popupview_levelup,
                getGravity(popupData),
                getWidth(popupType),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                getPopUpDuration(popupData[POP_DURATION_KEY]),
                popupData[POP_DESCRIPTION_KEY] ?: "",
                popupData[POP_MESSAGE_KEY] ?: "",
                popupData[POP_IMAGE_URL_KEY] ?: ""
            )

            POPUP_TYPE_POINTS_ACHIEVED -> PopupPointAchieved(
                R.layout.popupview_gamification_point,
                getGravity(popupData),
                getWidth(popupType),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                getPopUpDuration(popupData[POP_DURATION_KEY]),
                popupData[POP_DESCRIPTION_KEY] ?: "",
                popupData[POP_MESSAGE_KEY] ?: ""
            )

            POPUP_TYPE_POPUP_UNLOCKED -> PopupUnlock(
                R.layout.popupview_pcmunlock,
                getGravity(popupData),
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                getPopUpDuration(popupData[POP_DURATION_KEY]),
                popupData[POP_DESCRIPTION_KEY] ?: "",
                popupData[POP_MESSAGE_KEY] ?: "",
                popupData[POP_IMAGE_URL_KEY] ?: "",
                gson.fromJson(
                    popupData[POP_UNLOCK_ACTION_DATA_KEY].toString(),
                    PopUnlockActionData::class.java
                )
            )

            POPUP_TYPE_ALERT_UP -> PopupAlert(
                R.layout.popupview_alert,
                getGravity(popupData),
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                getPopUpDuration(popupData[POP_DURATION_KEY]),
                gson.fromJson(
                    (popupData[POP_DESCRIPTION_KEY]
                        ?: ""), object : TypeToken<List<String>>() {}.type
                ),
                popupData[POP_MESSAGE_KEY] ?: "",
                popupData[POP_IMAGE_URL_KEY] ?: "",
                popupData[POP_BUTTON_TEXT_KEY] ?: ""

            )

            else -> null
        }
    }

    private fun getPopUpDuration(duration: String?): Long {
        return if (duration == null || duration.isEmpty()) DEFAULT_DURATION else duration.toLong()
    }

    private fun getWidth(popupType: String): Int {

        return when (popupType) {
            POPUP_TYPE_BADGE, POPUP_TYPE_BADGE_ACHIEVED, POPUP_TYPE_LEVEL_UP -> {
                screenWidth
            }

            POPUP_TYPE_POINTS_ACHIEVED -> ViewUtils.dpToPx(250f, context).toInt()

            else -> ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    private fun getGravity(popupData: Map<String, String>): Int {
        return if (popupData.containsKey(POP_DIRECTION_KEY)) {
            when (popupData[POP_DIRECTION_KEY]) {
                "TOP_LEFT" -> Gravity.TOP or Gravity.START
                "TOP_RIGHT" -> Gravity.TOP or Gravity.END
                "BOTTOM_RIGHT" -> Gravity.BOTTOM or Gravity.END
                "BOTTOM_LEFT" -> Gravity.BOTTOM or Gravity.START
                "BOTTOM_CENTER" -> Gravity.BOTTOM or Gravity.CENTER
                "BOTTOM" -> Gravity.BOTTOM
                "TOP" -> Gravity.TOP
                "TOP_CENTER" -> Gravity.TOP or Gravity.CENTER
                "CENTER" -> Gravity.CENTER
                else -> Gravity.BOTTOM
            }
        } else {
            Gravity.BOTTOM
        }
    }

    private fun getPopUpType(popupData: Map<String, String>): String? {
        return if (popupData.containsKey(POP_TYPE_KEY)) {
            popupData[POP_TYPE_KEY]
        } else {
            null
        }
    }

    companion object {
        private const val DEFAULT_DURATION = 0L
    }
}