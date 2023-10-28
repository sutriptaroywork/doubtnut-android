package com.doubtnutapp.gamification.popactivity.popupbuilder

import com.doubtnutapp.gamification.popactivity.model.GamificationPopup

interface PopupBuilder {
    fun build(data: Map<String, String>): GamificationPopup?
}