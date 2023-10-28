package com.doubtnutapp.data.remote.models.doubtfeed2

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 16/07/21.
 */

@Keep
class DailyGoalSubmitResponse(
    @SerializedName("show_popup") val showPopup: Boolean,
    @SerializedName("popup_data") val popupData: DfPopupData?,
)
