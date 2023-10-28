package com.doubtnutapp.widgetmanager.ui

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.CoreWidgetVH

@Deprecated("Use com.doubtnut.core.widgets.ui.WidgetisedRecyclerView instead")
class WidgetisedRecyclerView : RecyclerView {

    private var widgets: List<WidgetEntityModel<*, *>?>? = null

    private var widgetToHolder: Map<WidgetEntityModel<*, *>, CoreWidgetVH>? = null

    private var adapter: WidgetLayoutAdapter? = null

    constructor(context: Context) : super(context) {
        initialise()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialise()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initialise()
    }

    private fun initialise() {
        adapter = WidgetLayoutAdapter(context)
        if (!isInEditMode) {
            layoutManager = LinearLayoutManager(context)
        }
        setAdapter(adapter)
        setHasFixedSize(true)
    }

    fun setWidgets(widgets: List<WidgetEntityModel<*, *>>?) {
        this.widgets = widgets
        addWidgets()
    }

    private fun addWidgets() {
        if (widgets == null) return
        if (widgetToHolder == null) widgetToHolder = HashMap(1)
        val widgetEntityModels = ArrayList<WidgetEntityModel<*, *>>()
        for (widgetEntityModel in widgets!!) {
            if (widgetEntityModel == null) continue
            widgetEntityModels.add(widgetEntityModel)
        }
        adapter!!.setWidgets(widgetEntityModels)
    }
}