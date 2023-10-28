package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by akshaynandwana on
 * 17, December, 2018
 **/
@Parcelize
data class TestResponse(
    @SerializedName("responsId") val responsId: Int?
) : Parcelable
