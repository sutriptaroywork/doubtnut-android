package com.doubtnutapp.data.remote.models

import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

data class Widgets(@SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>)
