package com.doubtnutapp.domain.videoPage.entities

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Keep
@Parcelize
data class EventDetails(
        val eventMap: Map<String, @RawValue Any>?
) : Parcelable