package com.doubtnutapp.data.remote.models.feed

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

class IncompleteChapterWidgetModel : WidgetEntityModel<IncompleteChapterWidgetData, WidgetAction>()

@Keep
data class IncompleteChapterWidgetData(

    @SerializedName("title")
    val title: String,

    @SerializedName("deeplink")
    val deeplink: String?
) : WidgetData()
