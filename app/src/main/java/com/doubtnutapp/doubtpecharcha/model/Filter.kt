package com.doubtnutapp.doubtpecharcha.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class Filter(
    val title: String? = "",
    val id: String? = ""
) : Parcelable