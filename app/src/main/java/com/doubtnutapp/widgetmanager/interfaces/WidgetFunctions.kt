package com.doubtnutapp.widgetmanager.interfaces

import com.doubtnut.core.widgets.ui.CoreWidgetVH
import com.doubtnut.core.widgets.entities.WidgetEntityModel

interface WidgetFunctions<VH : CoreWidgetVH?, WM : WidgetEntityModel<*, *>?> {

    fun bindWidget(holder: VH, model: WM): VH
}