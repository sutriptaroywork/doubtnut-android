package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.databinding.WidgetHeaderMessageBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.gson.annotations.SerializedName

class HeaderMessageWidget(
    context: Context
) : BaseBindingWidget<HeaderMessageWidget.HeaderMessageWidgetHolder,
        HeaderMessageWidget.HeaderMessageWidgetModel, WidgetHeaderMessageBinding>(context) {

    override fun getViewBinding(): WidgetHeaderMessageBinding {
        return WidgetHeaderMessageBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = HeaderMessageWidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: HeaderMessageWidgetHolder,
        model: HeaderMessageWidgetModel
    ): HeaderMessageWidgetHolder {
        holder.binding.tvTitle.text = model.data!!.text
        holder.binding.tvButton.text = model.data.buttonText

        return holder
    }

    class HeaderMessageWidgetHolder(binding: WidgetHeaderMessageBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetHeaderMessageBinding>(binding, widget)

    class HeaderMessageWidgetModel : WidgetEntityModel<HeaderMessageWidgetData, WidgetAction>()

    @Keep
    data class HeaderMessageWidgetData(
        @SerializedName("text") val text: String,
        @SerializedName("button_text") val buttonText: String
    ) : WidgetData()
}