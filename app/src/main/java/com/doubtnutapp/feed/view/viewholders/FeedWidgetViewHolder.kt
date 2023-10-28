package com.doubtnutapp.feed.view.viewholders

import com.doubtnut.core.widgets.ui.CoreWidgetVH
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.feed.view.FeedAdapter
import com.doubtnutapp.show
import com.doubtnutapp.widgetmanager.WidgetFactory
import com.doubtnutapp.widgetmanager.widgets.BannerImageWidget

class FeedWidgetViewHolder(val widgetViewHolder: CoreWidgetVH) :
    FeedAdapter.FeedViewHolder(widgetViewHolder.itemView) {

    fun bind(data: WidgetEntityModel<*, *>) {
        val holder = WidgetFactory.bindViewHolder(widgetViewHolder, data)
        trackingViewId = holder?.trackingViewId
        if (holder is BannerImageWidget.BannerImageWidgetHolder) {
            holder.binding.divider.show()
        }
    }
}