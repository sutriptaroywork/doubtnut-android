package com.doubtnutapp.domain.gamification.mybio.entity

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-11-22.
 */
@Keep
data class StudentClassEntity(
    @SerializedName("class") val name: Int,
    @SerializedName("class_display") val classDisplay: String?
)
