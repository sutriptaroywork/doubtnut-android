package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel

/**
 * Created by Akshat Jindal on 11/09/21.
 */
@Keep
data class MatchPageWidgetViewItem(
    val widget: WidgetEntityModel<*, *>,
    override val viewType: Int = widget.type.hashCode()
) : MatchQuestionViewItem {
    companion object {
        const val type: String = "widget"
    }
}