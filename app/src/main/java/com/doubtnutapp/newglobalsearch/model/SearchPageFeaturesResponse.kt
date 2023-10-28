package com.doubtnutapp.newglobalsearch.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class SearchPageFeaturesResponse(
    @SerializedName("data") var data: List<WidgetEntityModel<WidgetData, WidgetAction>>
)