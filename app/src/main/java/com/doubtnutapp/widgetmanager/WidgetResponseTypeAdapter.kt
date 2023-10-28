package com.doubtnutapp.widgetmanager

import com.doubtnut.core.DnException
import com.doubtnut.core.constant.ErrorConstants
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.*
import java.lang.reflect.Type

class WidgetResponseTypeAdapter : JsonDeserializer<WidgetResponse?> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): WidgetResponse? {
        val array = json as? JsonObject
        val widget: WidgetResponse = Gson().fromJson(json, typeOfT)
        if (array == null) {
            return null
        } else if (array.getAsJsonArray("widgets") != null) {
            widget.objects = ArrayList()
            var currentPosition = 0
            for (`object` in array.getAsJsonArray("widgets")) {
                val model = getModelFromJson(`object`, context) ?: continue
                if ((model._data == null && model._widgetData == null) || (model._type.isNullOrEmpty() && model._widgetType.isNullOrEmpty())) {
                    with(FirebaseCrashlytics.getInstance()) {
                        log(model.toString())
                        FirebaseCrashlytics.getInstance().setCustomKey(ErrorConstants.DN_FATAL, true)
                        recordException(DnException("Inconsistent Data"))
                    }
                    continue
                }
                currentPosition++
                widget.objects!!.add(model)
            }
        }
        return widget
    }

    private fun getModelFromJson(
        `object`: JsonElement,
        context: JsonDeserializationContext
    ): WidgetEntityModel<out WidgetData?, out WidgetAction?>? {
        return WidgetTypeAdapter().deserialize(`object`, WidgetEntityModel::class.java, context)
    }
}