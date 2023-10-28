package com.doubtnutapp.freeTrialCourse.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class FreeTrialCourseActivationResponse(@SerializedName("message") val message: String?,)