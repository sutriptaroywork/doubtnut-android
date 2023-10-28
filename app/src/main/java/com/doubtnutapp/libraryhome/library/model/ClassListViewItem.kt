package com.doubtnutapp.domain.library.entities

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Parcelize
@Keep
data class ClassListViewItem(
        val classNo: Int,
        val className: String
) : Parcelable {
    override fun toString(): String {
        return className
    }
}