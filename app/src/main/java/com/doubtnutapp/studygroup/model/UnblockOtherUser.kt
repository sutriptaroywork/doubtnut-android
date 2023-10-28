package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UnblockOtherUser (
        @SerializedName("message") val message: String?,
        @SerializedName("is_unblocked") val isUnblock: Boolean?,
        var itemPosition: Int = -1
)