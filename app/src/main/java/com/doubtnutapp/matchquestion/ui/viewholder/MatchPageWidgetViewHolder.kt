package com.doubtnutapp.matchquestion.ui.viewholder


import com.doubtnut.core.widgets.ui.CoreWidgetVH
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.matchquestion.model.MatchPageWidgetViewItem
import com.doubtnutapp.widgetmanager.WidgetFactory

/**
 * Created by Akshat Jindal on 11/09/21.
 */
class MatchPageWidgetViewHolder(val widgetViewHolder: CoreWidgetVH) :
    BaseViewHolder<MatchPageWidgetViewItem>(widgetViewHolder.itemView) {
    override fun bind(data: MatchPageWidgetViewItem) {
        WidgetFactory.bindViewHolder(widgetViewHolder, data.widget)
    }
}