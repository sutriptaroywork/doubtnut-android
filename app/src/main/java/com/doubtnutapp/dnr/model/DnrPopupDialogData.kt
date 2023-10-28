package com.doubtnutapp.dnr.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

/**
 * Created by Mehul Bisht on 27/10/21
 */

@Keep
@Parcelize
data class DnrPopupDialogData(
    val imageUrl: String,
    val title: String,
    val description: String,
    val ctaText: String,
    val deeplink: String
) : Parcelable
