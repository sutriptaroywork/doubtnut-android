package com.doubtnutapp.libraryhome.library.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

/**
 * Created by Anand Gaurav on 2019-07-31.
 */
@Keep
@Parcelize
data class AnnouncementData(val type: String,
                            val state: Boolean) : Parcelable