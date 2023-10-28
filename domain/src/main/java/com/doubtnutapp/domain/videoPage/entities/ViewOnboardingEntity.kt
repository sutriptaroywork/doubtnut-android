package com.doubtnutapp.domain.videoPage.entities

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ViewOnboardingEntity(@SerializedName("view_id") val viewId: String)
