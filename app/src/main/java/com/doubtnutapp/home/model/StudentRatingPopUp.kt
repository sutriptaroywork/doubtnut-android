package com.doubtnutapp.home.model

import androidx.annotation.Keep
import com.doubtnutapp.common.model.PopUpDialog
import com.doubtnutapp.common.model.PopUpSubDataModel

@Keep
data class StudentRatingPopUp (
        val shouldShow: Boolean,
        val subData: PopUpSubDataModel?
): PopUpDialog {
    companion object {
        const val TYPE = "rating"
    }
}