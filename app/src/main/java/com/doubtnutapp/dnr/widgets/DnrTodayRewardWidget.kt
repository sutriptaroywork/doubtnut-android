package com.doubtnutapp.dnr.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.databinding.WidgetDnrTodayRewardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.isValidColorCode
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Akshat Jindal on 18/10/21.
 */
class DnrTodayRewardWidget(context: Context) :
    BaseBindingWidget<
        DnrTodayRewardWidget.WidgetHolder,
        DnrTodayRewardWidget.DnrTodayRewardWidgetModel,
        WidgetDnrTodayRewardBinding>(context) {

    companion object {
        private const val TAG = "DnrTodayRewardWidget"
    }

    var source: String? = null

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.forceUnWrap()?.inject(this)
    }

    override fun getViewBinding() = WidgetDnrTodayRewardBinding
        .inflate(LayoutInflater.from(context), this, true)

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: DnrTodayRewardWidgetModel
    ): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 4, 4)
            }
        )
        val data = model.data
        val binding = holder.binding
        val width = Utils.getWidthFromScrollSize(holder.itemView.context, data.cardWidth ?: "2.7x")

        binding.apply {
            layoutParams.width = width
            tvSerialNumber.apply {
                text = data.serialNumber.toString()
                if (data.serialNumberColor.isValidColorCode()) {
                    setTextColor(Color.parseColor(data.serialNumberColor))
                }
                if (data.serialNumberBackgroundColor.isValidColorCode()) {
                    ivSerialNumber.setBackgroundColor(Color.parseColor(data.serialNumberBackgroundColor))
                }
            }
            title.apply {
                text = data.title
                if (data.titleColor.isValidColorCode()) {
                    setTextColor(Color.parseColor(data.titleColor))
                }
            }
            subtitle.apply {
                text = data.subTitle
                if (data.subTitleColor.isValidColorCode()) {
                    setTextColor(Color.parseColor(data.subTitleColor))
                }
            }

            if (data.subtitleBackgroundColor.isValidColorCode()) {
                view.setBackgroundColor(Color.parseColor(data.subtitleBackgroundColor))
            }
            ivCoin.loadImageEtx(data.dnrImage.orEmpty())
            setOnClickListener {
                deeplinkAction.performAction(context, data.deeplink)
            }
        }

        return holder
    }

    class WidgetHolder(binding: WidgetDnrTodayRewardBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetDnrTodayRewardBinding>(binding, widget)

    @Keep
    class DnrTodayRewardWidgetModel : WidgetEntityModel<DnrTodayRewardWidgetData, WidgetAction>()

    @Keep
    data class DnrTodayRewardWidgetData(
        @SerializedName("title") val title: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("subtitle_background_color") val subtitleBackgroundColor: String?,
        @SerializedName("subtitle") val subTitle: String?,
        @SerializedName("subtitle_color") val subTitleColor: String?,
        @SerializedName("dnr_image") val dnrImage: String?,
        @SerializedName("serial_number") val serialNumber: Int?,
        @SerializedName("serial_number_color") val serialNumberColor: String?,
        @SerializedName("card_width") val cardWidth: String?,
        @SerializedName("serial_number_background_color") val serialNumberBackgroundColor: String?,
        @SerializedName("background_image") val backgroundImage: String?,
        @SerializedName("border_color") val borderColor: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("dnr") val dnr: Int?
    ) : WidgetData()
}
