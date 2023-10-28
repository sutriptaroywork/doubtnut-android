package com.doubtnutapp.data.remote.models.doubtfeed2

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by devansh on 1/5/21.
 */

@Keep
@Parcelize
data class Topic(
    @SerializedName("title") val title: String,
    @SerializedName("key") val key: String,
    var isSelected: Boolean = false,
) : Parcelable
