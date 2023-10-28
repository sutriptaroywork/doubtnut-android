package com.doubtnutapp.matchquestion.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class MatchFailureOption(
    val title: String,
    val content: String,
    val hint: String,
    val placeholder: String
) : Parcelable
