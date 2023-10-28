package com.doubtnutapp.dnr.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class DnrCoursePurchaseInfo(
    @SerializedName("assortment_id")
    val assortmentIds: String,

    @SerializedName("assortment_type")
    val assortmentType: String
) : Parcelable
