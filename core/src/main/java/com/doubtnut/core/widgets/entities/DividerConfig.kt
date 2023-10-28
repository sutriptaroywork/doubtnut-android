package com.doubtnut.core.widgets.entities

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DividerConfig(
    @SerializedName("background_color")
    val backgroundColor: String?,
    /**
     * Mistakenly added width key for the height
     */
    @SerializedName("width", alternate = ["height"])
    val height: Int?,

    @SerializedName("skip_margin")
    val skipMargin: Boolean?,

    @SerializedName("margin_top")
    val marginTop: Int?,
    @SerializedName("margin_bottom")
    val marginBottom: Int?,
    @SerializedName("margin_left")
    val marginLeft: Int?,
    @SerializedName("margin_right")
    val marginRight: Int?
)
