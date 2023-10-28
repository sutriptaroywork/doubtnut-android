package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by root on 9/7/18.
 */
@Parcelize class Playlist(
    val id: String,
    val name: String
) : Parcelable
