package com.doubtnutapp.dnr.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnutapp.databinding.WidgetDnrNoEarnedHistoryBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName

class DnrNoEarnedHistoryWidget(context: Context) : BaseBindingWidget<
    DnrNoEarnedHistoryWidget.WidgetHolder,
    DnrNoEarnedHistoryWidget.Model,
    WidgetDnrNoEarnedHistoryBinding>(context) {

    var source: String? = null

    override fun getViewBinding(): WidgetDnrNoEarnedHistoryBinding =
        WidgetDnrNoEarnedHistoryBinding.inflate(LayoutInflater.from(context), this, true)

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
            ivCoin.loadImage(data.dnrImage)

            tvSubtitle1.apply {
                text = data.subtitle
                setTextColor(Utils.parseColor(data.subtitleColor, Color.BLACK))
                isVisible = data.subtitle.isNotNullAndNotEmpty()
            }

            tvSubtitle2.apply {
                text = data.description
                setTextColor(Utils.parseColor(data.descriptionColor, Color.BLACK))
                isVisible = data.description.isNotNullAndNotEmpty()
            }

            tvRupyaTitle.text = data.earnedTitle
            tvRupyaCount.text = data.dnr
        }
        return holder
    }

    class WidgetHolder(binding: WidgetDnrNoEarnedHistoryBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetDnrNoEarnedHistoryBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String,
        @SerializedName("title_color") val titleColor: String,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("subtitle_color") val subtitleColor: String?,
        @SerializedName("description") val description: String?,
        @SerializedName("description_color") val descriptionColor: String?,
        @SerializedName("earned_title") val earnedTitle: String?,
        @SerializedName("dnr_image") val dnrImage: String?,
        @SerializedName("dnr") val dnr: String?,
    ) : WidgetData()
}
