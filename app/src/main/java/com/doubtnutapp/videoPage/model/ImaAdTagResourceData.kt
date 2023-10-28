package com.doubtnutapp.videoPage.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class ImaAdTagResourceData(
    val adTag: String?,
    val adTimeout: Int?,
):Parcelable
