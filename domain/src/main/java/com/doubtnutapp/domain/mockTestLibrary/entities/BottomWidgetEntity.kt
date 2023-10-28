package com.doubtnutapp.domain.mockTestLibrary.entities

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BottomWidgetEntity(
    @SerializedName("button_text") val bottomText: String?,
    @SerializedName("button_deeplink") val bottomDeeplink: String?,
    @SerializedName("text_solution") val textSolution: String?
) : Parcelable
