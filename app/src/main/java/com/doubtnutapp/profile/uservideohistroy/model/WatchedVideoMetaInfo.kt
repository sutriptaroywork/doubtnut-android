package com.doubtnutapp.profile.uservideohistroy.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class WatchedVideoMetaInfo(
    val icon: String,
    val title: String,
    val description: String,
    val suggestionButtonText: String,
    val suggestionId: String?,
    val suggestionName: String
) : Parcelable