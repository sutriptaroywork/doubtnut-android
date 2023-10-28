package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import com.doubtnut.core.widgets.ui.WidgetBindingVH

import com.doubtnutapp.data.remote.models.SimpleTextWidgetModel
import com.doubtnutapp.databinding.WidgetSimpleTextBinding
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.utils.Utils

class SimpleTextWidget(context: Context) : BaseBindingWidget<SimpleTextWidget.WidgetHolder,
        SimpleTextWidgetModel, WidgetSimpleTextBinding>(context) {

    override fun getViewBinding(): WidgetSimpleTextBinding {
        return WidgetSimpleTextBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: SimpleTextWidgetModel): WidgetHolder {
        val binding = holder.binding
        val marginTop = Utils.convertDpToPixel(model.data.marginTop?.toFloatOrNull() ?: 100F)
        setMargins(WidgetLayoutConfig(marginTop.toInt(), 0, 0, 0))
        binding.textView.text = model.data.title.orEmpty()
        return holder
    }

    class WidgetHolder(binding: WidgetSimpleTextBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetSimpleTextBinding>(binding, widget)

}