package com.doubtnutapp.notification.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


@Keep
data class MaarkAsReadData(@SerializedName("status") val status: String?)