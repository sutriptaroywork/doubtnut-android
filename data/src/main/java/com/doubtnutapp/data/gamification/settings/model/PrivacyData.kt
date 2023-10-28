package com.doubtnutapp.data.gamification.settings.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by shrreya on 3/7/19.
 */
@Keep
data class PrivacyData(@SerializedName("privacy") val privacy: String)
