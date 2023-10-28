package com.doubtnutapp.domain.homefeed.entites.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel

@Keep
data class HomeFeedWidgetEntity(
    val widget: WidgetEntityModel<WidgetData, WidgetAction>
) : HomeFeedItem {
    companion object {
        const val type: String = "widget"
    }
}
