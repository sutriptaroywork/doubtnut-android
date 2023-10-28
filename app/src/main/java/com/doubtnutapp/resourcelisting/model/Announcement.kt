package com.doubtnutapp.resourcelisting.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
@Keep
@Parcelize
data class Announcement(val type: String,
                        val state: Boolean) : Parcelable