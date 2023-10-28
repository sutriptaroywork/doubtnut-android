package com.doubtnutapp.dnr.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnutapp.databinding.WidgetDnrTextBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName

class DnrTextWidget(context: Context) : BaseBindingWidget<
    DnrTextWidget.WidgetHolder,
    DnrTextWidget.Model,
    WidgetDnrTextBinding>(context) {

    var source: String? = null

    override fun getViewBinding(): WidgetDnrTextBinding =
        WidgetDnrTextBinding.inflate(LayoutInflater.from(context), this, true)

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        model.layoutConfig = if (model.layoutConfig == null)
            WidgetLayoutConfig(
                marginTop = 9,
                marginLeft = 16,
                marginRight = 16,
                marginBottom = 9,
            ) else model.layoutConfig
        super.bindWidget(holder, model)

        val binding = holder.binding
        val data = model.data

        with(binding) {
            tvTitle.apply {
                text = data.title
                setTextColor(Utils.parseColor(data.titleColor, Color.BLACK))
                isVisible = data.title.isNotNullAndNotEmpty()
            }

            tvRupyaTitle.apply {
                text = data.subtitle
                setTextColor(Utils.parseColor(data.subtitle, Color.BLACK))
                isVisible = data.subtitle.isNotNullAndNotEmpty()
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetDnrTextBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetDnrTextBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String,
        @SerializedName("title_color") val titleColor: String,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("subtitle_color") val subtitleColor: String?,
    ) : WidgetData()
}
