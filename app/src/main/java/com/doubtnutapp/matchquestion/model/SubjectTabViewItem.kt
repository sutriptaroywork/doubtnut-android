package com.doubtnutapp.matchquestion.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

/**
 * Created by Anand Gaurav on 2019-07-25.
 */
@Keep
@Parcelize
data class SubjectTabViewItem(
    val subject: String,
    val display: String,
    var isSelected: Boolean = false
) : Parcelable