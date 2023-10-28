package com.doubtnutapp.dnr.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.databinding.WidgetDnrEarnedHistoryBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.hide
import com.doubtnutapp.loadImage
import com.doubtnutapp.show
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName

class DnrEarnedHistoryWidget(context: Context) : BaseBindingWidget<
    DnrEarnedHistoryWidget.WidgetHolder,
    DnrEarnedHistoryWidget.Model,
    WidgetDnrEarnedHistoryBinding>(context) {

    var source: String? = null

    override fun getViewBinding(): WidgetDnrEarnedHistoryBinding =
        WidgetDnrEarnedHistoryBinding.inflate(LayoutInflater.from(context), this, true)

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
        val adapter = WidgetLayoutAdapter(context, actionPerformer)

        binding.apply {
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(
                context,
                RecyclerView.VERTICAL, false
            )
            tvTitle.text = data.title
            if (data.totalDnrContainer != null) {
                val totalEarnedData = data.totalDnrContainer
                totalRupyaGroup.show()
                val size = totalEarnedData.titleSize ?: 16f
                tvTotalRupyaTitle.apply {
                    text = totalEarnedData.title
                    textSize = size
                }
                tvTotalRupyaCount.apply {
                    text = totalEarnedData.dnr.toString()
                    setTextColor(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.color_badge
                        )
                    )
                }
                ivTotalCoin.loadImage(totalEarnedData.dnrImage)
            } else {
                totalRupyaGroup.hide()
            }
        }

        adapter.setWidgets(data.items.orEmpty())
        return holder
    }

    class WidgetHolder(binding: WidgetDnrEarnedHistoryBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetDnrEarnedHistoryBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("total_container") val totalDnrContainer: TotalEarnedData?,
        @SerializedName("completed_milestones") val items: List<WidgetEntityModel<*, *>?>?,
    ) : WidgetData()

    @Keep
    data class TotalEarnedData(
        @SerializedName("title") val title: String,
        @SerializedName("title_size") val titleSize: Float?,
        @SerializedName("dnr") val dnr: Int?,
        @SerializedName("dnr_image") val dnrImage: String?,
    )
}
