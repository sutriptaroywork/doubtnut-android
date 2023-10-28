package com.doubtnutapp.liveclasshome.ui

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

/**
 * Created by Anand Gaurav on 16/05/20.
 */
@Keep
@Parcelize
data class FilterItem(
        val id: Int,
        val title: String,
        val isSelected: Boolean
) : Parcelable