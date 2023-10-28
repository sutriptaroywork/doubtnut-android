package com.doubtnutapp.data.remote.models.whatsappadmin

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class StateDistrictModel(
    @SerializedName("state") val state: String,
    @SerializedName("districts") val districts: ArrayList<String>
) : Parcelable

@Keep
@Parcelize
data class StateDistrictApiResponse(
    @SerializedName("states") val states: ArrayList<StateDistrictModel>
) : Parcelable
