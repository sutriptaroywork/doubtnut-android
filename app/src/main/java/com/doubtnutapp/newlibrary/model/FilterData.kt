package com.doubtnutapp.newlibrary.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by Mehul Bisht on 03/12/21
 */

@Parcelize
data class FilterData(
    val filter: List<List<LibraryPreviousYearPapersFilter>>
) : Parcelable
