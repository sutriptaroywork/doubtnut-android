package com.doubtnutapp.bottomnavigation.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 21/1/21.
 */

@Keep
data class BottomNavigationItemData(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("icon") val icon: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("is_selected") val isSelected: Boolean = false
)
