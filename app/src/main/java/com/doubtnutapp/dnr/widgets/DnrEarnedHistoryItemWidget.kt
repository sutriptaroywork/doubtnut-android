package com.doubtnutapp.dnr.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.databinding.WidgetDnrEarnedHistoryItemBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName

class DnrEarnedHistoryItemWidget(context: Context) : BaseBindingWidget<
    DnrEarnedHistoryItemWidget.WidgetHolder,
    DnrEarnedHistoryItemWidget.Model,
    WidgetDnrEarnedHistoryItemBinding>(context) {

    var source: String? = null

    override fun getViewBinding(): WidgetDnrEarnedHistoryItemBinding =
        WidgetDnrEarnedHistoryItemBinding.inflate(LayoutInflater.from(context), this, true)

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        val binding = holder.binding
        val data = model.data

        with(binding) {
            tvRupyaTitle.apply {
                text = data.title
                textSize = data.titleSize ?: 14f
                setTextColor(Utils.parseColor(data.titleColor, Color.GRAY))
            }
            tvRupyaCount.apply {
                text = data.dnr
                setTextColor(Utils.parseColor(data.dnrColor, Color.GRAY))
            }
            ivCoin.loadImage(data.dnrImage)
        }
        return holder
    }

    class WidgetHolder(binding: WidgetDnrEarnedHistoryItemBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetDnrEarnedHistoryItemBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("title_size") val titleSize: Float?,
        @SerializedName("dnr") val dnr: String?,
        @SerializedName("dnr_color") val dnrColor: String?,
        @SerializedName("dnr_image") val dnrImage: String?,
    ) : WidgetData()
}
