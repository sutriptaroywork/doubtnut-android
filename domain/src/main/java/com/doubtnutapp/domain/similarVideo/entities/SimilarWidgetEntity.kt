package com.doubtnutapp.domain.similarVideo.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel

@Keep
data class SimilarWidgetEntity(
    val widget: WidgetEntityModel<WidgetData, WidgetAction>
) : DoubtnutViewItem {
    companion object {
        const val type: String = "widget"
    }
}
