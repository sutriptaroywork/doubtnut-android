package com.doubtnutapp.dnr.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.databinding.WidgetDnrRewardHistoryBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.isValidColorCode
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Akshat Jindal on 19/10/21.
 */
class DnrRewardHistoryWidget(context: Context) :
    BaseBindingWidget<
        DnrRewardHistoryWidget.WidgetHolder,
        DnrRewardHistoryWidget.DnrRewardHistoryWidgetModel,
        WidgetDnrRewardHistoryBinding>(context) {

    companion object {
        private const val TAG = "DnrRewardHistoryWidget"
    }

    var source: String? = null

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding() = WidgetDnrRewardHistoryBinding
        .inflate(LayoutInflater.from(context), this, true)

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: DnrRewardHistoryWidgetModel
    ): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val data = model.data
        val binding = holder.binding
        val width = Utils.getWidthFromScrollSize(holder.itemView.context, data.cardWidth ?: "2x")

        binding.apply {
            layoutParams.width = width
            parentLayout.setBackgroundColor(Color.parseColor(data.background_color))
            title.text = data.title
            title.setTextColor(Color.parseColor(data.title_color))
            subtitle.text = data.sub_title

            if (data.sub_title_color.isValidColorCode()) {
                subtitle.setTextColor(Color.parseColor(data.sub_title_color))
            }

            ivCoin.setVisibleState(data.amount.isNullOrBlank().not())
            ivCoin.loadImageEtx(data.coin_image_url.orEmpty())
            tvCount.setVisibleState(data.amount.isNullOrBlank().not())
            tvCount.text = data.amount
            tvCount.setTextColor(Color.parseColor(data.amount_color))

            setOnClickListener {
                deeplinkAction.performAction(context, data.deeplink)
            }
        }

        return holder
    }

    class WidgetHolder(binding: WidgetDnrRewardHistoryBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetDnrRewardHistoryBinding>(binding, widget)

    class DnrRewardHistoryWidgetModel :
        WidgetEntityModel<DnrRewardHistoryWidgetData, WidgetAction>()

    @Keep
    data class DnrRewardHistoryWidgetData(
        @SerializedName("title") val title: String?,
        @SerializedName("title_color") val title_color: String?,
        @SerializedName("subtitle") val sub_title: String?,
        @SerializedName("subtitle_color") val sub_title_color: String?,
        @SerializedName("dnr_image") val coin_image_url: String?,
        @SerializedName("dnr") val amount: String?,
        @SerializedName("card_width") val cardWidth: String?,
        @SerializedName("dnr_color") val amount_color: String?,
        @SerializedName("background_color") val background_color: String?,
        @SerializedName("deeplink") val deeplink: String?
    ) : WidgetData()
}
