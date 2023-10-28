package com.doubtnutapp.domain.similarVideo.entities

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class ApiNcertSimilar(
    @SerializedName("ncert_similar") val ncertSimilar: List<WidgetEntityModel<WidgetData, WidgetAction>>?,
    @SerializedName("ncert_books") val ncertBooks: List<WidgetEntityModel<WidgetData, WidgetAction>>?
)
