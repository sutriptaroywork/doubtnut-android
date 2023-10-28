package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by akshaynandwana on
 * 17, October, 2018
 **/
@Parcelize
data class NewLibrary(
    @SerializedName("title") val title: String,
    @SerializedName("playlist_id") val playlistId: String,
    @SerializedName("image_url") val imageUrl: String
) : Parcelable
