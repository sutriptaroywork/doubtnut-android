package com.doubtnutapp.data.remote.models

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 9/1/21.
 */

@Keep
data class AppExitDialogData(
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("core_actions_done") val coreActionsDoneList: List<String>?,
    @SerializedName("open_count") val openCount: Int,
    @SerializedName("show_on_backpress") val showOnBackpress: Boolean?,
    @SerializedName("experiment") val experiment: Int,
    @SerializedName("list_count") val maxListCount: Int,
    @SerializedName("title") val title: String?
)
