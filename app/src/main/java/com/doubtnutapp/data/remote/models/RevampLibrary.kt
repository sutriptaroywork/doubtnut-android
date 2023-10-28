package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by pradip on
 * 05, May, 2019
 **/
@Parcelize
data class RevampLibrary(
    @SerializedName("name") val title: String,
    @SerializedName("id") val playlistId: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("is_last") val isLast: Int

) : Parcelable
