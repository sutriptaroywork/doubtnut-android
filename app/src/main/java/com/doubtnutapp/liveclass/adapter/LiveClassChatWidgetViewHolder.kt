package com.doubtnutapp.liveclass.adapter

import com.doubtnut.core.widgets.ui.CoreWidgetVH
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.widgetmanager.WidgetFactory

class LiveClassChatWidgetViewHolder(val widgetViewHolder: CoreWidgetVH) :
    BaseViewHolder<LiveClassWidgetViewItem>(widgetViewHolder.itemView) {

    override fun bind(data: LiveClassWidgetViewItem) {
        WidgetFactory.bindViewHolder(widgetViewHolder, data.widget)
    }
}