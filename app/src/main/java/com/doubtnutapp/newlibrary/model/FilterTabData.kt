package com.doubtnutapp.newlibrary.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Mehul Bisht on 03/12/21
 */

@Parcelize
data class FilterTabData(
    val filter: List<LibraryPreviousYearPapersFilter>
) : Parcelable