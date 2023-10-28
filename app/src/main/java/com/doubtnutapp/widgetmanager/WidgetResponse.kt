package com.doubtnutapp.widgetmanager

import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

class WidgetResponse {

    // Dummy serialized name so that Gson doesn't parse it automatically.
    @SerializedName("objects_no_parse")
    var objects: ArrayList<WidgetEntityModel<*, *>?>? = null

    fun getWidgetsOfType(type: String): List<WidgetEntityModel<*, *>> {
        val widgetEntityModels: MutableList<WidgetEntityModel<*, *>> = ArrayList()
        for (model in objects!!) {
            if (model == null) continue
            if (model.type == type) widgetEntityModels.add(model)
        }
        return widgetEntityModels
    }
}