package com.doubtnutapp.similarVideo.ui

import com.doubtnut.core.widgets.ui.CoreWidgetVH
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.similarVideo.model.SimilarWidgetViewItem
import com.doubtnutapp.widgetmanager.WidgetFactory

class SimilarWidgetViewHolder(val widgetViewHolder: CoreWidgetVH) :
    BaseViewHolder<SimilarWidgetViewItem>(widgetViewHolder.itemView) {

    override fun bind(data: SimilarWidgetViewItem) {
        WidgetFactory.bindViewHolder(widgetViewHolder, data.widget)
    }
}