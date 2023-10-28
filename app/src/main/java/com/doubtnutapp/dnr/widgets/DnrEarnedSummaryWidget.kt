package com.doubtnutapp.dnr.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.databinding.WidgetDnrEarnedSummaryBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.hide
import com.doubtnutapp.loadImage
import com.doubtnutapp.show
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName

class DnrEarnedSummaryWidget(context: Context) : BaseBindingWidget<
    DnrEarnedSummaryWidget.WidgetHolder,
    DnrEarnedSummaryWidget.Model,
    WidgetDnrEarnedSummaryBinding>(context) {

    var source: String? = null

    override fun getViewBinding(): WidgetDnrEarnedSummaryBinding =
        WidgetDnrEarnedSummaryBinding.inflate(LayoutInflater.from(context), this, true)

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val binding = holder.binding
        val data = model.data

        binding.apply {
            root.background = Utils.getShape(
                data.backgroundColor ?: "#ffffff",
                data.backgroundColor ?: "#ffffff",
                cornerRadius = 4f
            )
            val todayEarningContainerBinding = todayEarningContainer
            val todayEarningData = data.todayDnrData
            if (todayEarningData != null) {
                todayEarningContainerBinding.apply {
                    ivCoin.loadImage(data.dnrImage)
                    tvCoinCount.apply {
                        text = todayEarningData.dnr.toString()
                        setTextColor(Utils.parseColor(todayEarningData.titleColor, Color.WHITE))
                    }
                    tvTitle.apply {
                        text = todayEarningData.title
                        setTextColor(Utils.parseColor(todayEarningData.titleColor, Color.WHITE))
                    }
                }
                todayEarningContainerBinding.root.show()
            } else {
                todayEarningContainerBinding.root.hide()
            }

            val totalEarningContainerBinding = totalEarningContainer
            val totalEarningData = data.totalDnrData
            if (totalEarningData != null) {
                totalEarningContainerBinding.apply {
                    ivCoin.loadImage(data.dnrImage)
                    tvCoinCount.apply {
                        text = totalEarningData.dnr.toString()
                        setTextColor(Utils.parseColor(totalEarningData.titleColor, Color.WHITE))
                    }
                    tvTitle.apply {
                        text = totalEarningData.title
                        setTextColor(Utils.parseColor(totalEarningData.titleColor, Color.WHITE))
                    }
                }
                totalEarningContainerBinding.root.show()
            } else {
                totalEarningContainerBinding.root.hide()
            }
        }

        return holder
    }

    class WidgetHolder(binding: WidgetDnrEarnedSummaryBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetDnrEarnedSummaryBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("today_dnr_data") val todayDnrData: DnrEarnedContainer?,
        @SerializedName("total_dnr_data") val totalDnrData: DnrEarnedContainer?,
        @SerializedName("background_color") val backgroundColor: String?,
        @SerializedName("dnr_image") val dnrImage: String?,
    ) : WidgetData()

    @Keep
    data class DnrEarnedContainer(
        @SerializedName("title") val title: String,
        @SerializedName("color", alternate = ["title_color"]) val titleColor: String?,
        @SerializedName("dnr") val dnr: Int?
    )
}
