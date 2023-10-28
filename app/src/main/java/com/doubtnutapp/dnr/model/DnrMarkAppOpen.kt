package com.doubtnutapp.dnr.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class DnrMarkAppOpen(
    @SerializedName("show_reward_pop_up")
    val showRewardPopUp: Boolean,

    @SerializedName("is_marked_app_open")
    val isMarkedAppOpen: Boolean,
) : Parcelable
