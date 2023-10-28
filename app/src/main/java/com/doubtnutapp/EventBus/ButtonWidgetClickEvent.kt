package com.doubtnutapp.EventBus

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction

@Keep
data class ButtonWidgetClickEvent(val widgetAction: WidgetAction?)
