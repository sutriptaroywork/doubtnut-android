package com.doubtnutapp.data.remote.models.quiztfs

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class QuizTfsWaitData(
    @SerializedName("title") val title: String?,
    @SerializedName("subTitle") val subTitle: String?,
    @SerializedName("startingIn") val startingIn: String?
) : Parcelable
