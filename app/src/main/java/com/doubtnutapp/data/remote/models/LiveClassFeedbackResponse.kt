package com.doubtnutapp.data.remote.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LiveClassFeedbackResponse(@SerializedName("msg") val message: String?)
