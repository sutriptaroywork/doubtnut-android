package com.doubtnutapp.data.newlibrary.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class ApiLibraryTabData(
    @SerializedName("list") val list: List<WidgetEntityModel<WidgetData, WidgetAction>>
)
