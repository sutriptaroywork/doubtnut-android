package com.doubtnutapp.feed.view.widgets

import com.doubtnutapp.data.remote.models.feed.FeedPostItem
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetEntityModel

class FeedPostWidgetModel(type: String, data: FeedPostItem) :
    WidgetEntityModel<FeedPostItem, WidgetAction>(
        _widgetType = type,
        _widgetData = data
    )