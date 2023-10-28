package com.doubtnutapp.common.promotional.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

/**
 * Created by Anand Gaurav on 2019-10-07.
 */
@Keep
@Parcelize
data class PromotionalActionDataViewItem(
    val playlistId: String,
    val playlistTitle: String,
    val isLast: Int,
    val facultyId: Int?,
    val ecmId: Int?,
    val subject: String?
) : Parcelable
