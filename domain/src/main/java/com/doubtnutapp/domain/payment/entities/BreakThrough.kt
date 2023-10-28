package com.doubtnutapp.domain.payment.entities

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2020-01-22.
 */
@Keep
data class BreakThrough(
    @SerializedName("days") val days: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("title") val title: String?
)
