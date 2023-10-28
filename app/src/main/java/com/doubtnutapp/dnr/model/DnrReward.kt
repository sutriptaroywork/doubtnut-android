package com.doubtnutapp.dnr.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class DnrReward(
    @SerializedName("image")
    val image: String?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("subtitle")
    val subtitle: String?,

    @SerializedName("dialog_title")
    val dialogTitle: String?,

    @SerializedName("cta")
    val cta: DnrRewardCta?,

    @SerializedName("pop_up_count")
    val maxNoOfRewardBottomSheetShownCount: Int,

    @SerializedName("max_popup_count")
    val maxNoOfAnyDnrRewardPopUpShownCount: Int?,

    @SerializedName("pop_up_image")
    val popUpImage: String?,

    @SerializedName("duration")
    val duration: Long?,

    @SerializedName("no_max_limit_for_bottom_sheet")
    val noMaxLimitForBottomSheetType:Boolean?,

    @SerializedName("type")
    val type: String,
) : Parcelable

@Keep
@Parcelize
data class DnrRewardCta(
    @SerializedName("title")
    val title: String,

    @SerializedName("deeplink")
    val deeplink: String
) : Parcelable
