package com.doubtnutapp.gamification.badgesscreen.model

import androidx.annotation.Keep

@Keep
data class Badge(
        val featureType: String,
        val id: Int,
        val name: String,
        val description: String,
        val nudgeDescription: String,
        val imageUrl: String,
        val isAchieved: Boolean,
        val sharingMessage: String,
        val actionPage : String,
        var isOwn: Boolean,
        var blurImage: String,
        override val viewType: Int
): BaseBadgeViewType
