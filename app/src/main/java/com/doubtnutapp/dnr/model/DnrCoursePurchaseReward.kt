package com.doubtnutapp.dnr.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DnrCoursePurchaseReward(
    @SerializedName("assortment_id")
    val assortmentId: String,

    @SerializedName("assortment_type")
    val assortmentType: String,

    @SerializedName("type")
    override val type: String
) : BaseDnrReward
