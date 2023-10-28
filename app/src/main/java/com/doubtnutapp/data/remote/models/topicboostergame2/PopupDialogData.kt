package com.doubtnutapp.data.remote.models.topicboostergame2

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Parcelize
@Keep
data class PopupDialogData(
    val description: String,
    val button1: String?,
    val button2: String?,
) : Parcelable
