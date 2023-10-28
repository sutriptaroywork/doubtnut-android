package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NcertExcercise(
    @SerializedName("exercise") val exercise: String,
    val exercise_name: String?,
    var isSelected: Boolean = false
) : Parcelable
