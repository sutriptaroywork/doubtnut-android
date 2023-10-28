package com.doubtnutapp.common.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class PopUpSubDataModel(
    val header: String,
    val subHeader: String,
    val options: List<String>,
    val showGoogleReview: Boolean
) : Parcelable
