package com.doubtnutapp.dnr.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DnrRewardSgMessage(
    @SerializedName("show_reward_popup") val showRewardPopup: Boolean
)
