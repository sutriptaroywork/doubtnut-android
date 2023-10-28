package com.doubtnut.core.widgets.entities

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
class WidgetLayoutConfig(
    @SerializedName("margin_top") val marginTop: Int = 16,
    @SerializedName("margin_bottom") val marginBottom: Int = 0,
    @SerializedName("margin_left") val marginLeft: Int = 0,
    @SerializedName("margin_right") val marginRight: Int = 8
) {
    companion object {
        val NONE = WidgetLayoutConfig(
            marginTop = 0,
            marginBottom = 0,
            marginLeft = 0,
            marginRight = 0
        )
        val DEFAULT = WidgetLayoutConfig()
    }
}