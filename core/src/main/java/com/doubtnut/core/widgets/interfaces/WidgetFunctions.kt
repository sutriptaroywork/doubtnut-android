package com.doubtnut.core.widgets.interfaces

import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.CoreWidgetVH

interface WidgetFunctions<VH : CoreWidgetVH?, WM : WidgetEntityModel<*, *>?> {

    fun bindWidget(holder: VH, model: WM): VH
}