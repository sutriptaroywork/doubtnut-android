package com.doubtnutapp.dnr.widgets

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetDnrHomeBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.isValidColorCode
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Akshat Jindal on 18/10/21.
 */
class DnrHomeWidget(context: Context) :
    BaseBindingWidget<DnrHomeWidget.WidgetHolder, DnrHomeWidget.DnrHomeWidgetModel, WidgetDnrHomeBinding>(
        context
    ) {

    companion object {
        private const val TAG = "DnrRedeemVoucherWidget"
        const val EVENT_TAG = "dnr_home_widget"
    }

    var source: String? = null

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding() = WidgetDnrHomeBinding
        .inflate(LayoutInflater.from(context), this, true)

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: DnrHomeWidgetModel
    ): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(20, 0, 15, 15)
            }
        )
        val data = model.data
        val binding = holder.binding

        binding.apply {
            if (data.backgroundColor.isValidColorCode()) {
                rootContainer.setBackgroundColor(Color.parseColor(data.backgroundColor))
            }

            if (data.bgStartColor.isValidColorCode() && data.bgEndColor.isValidColorCode()) {
                val gradient = Utils.getGradientView(
                    data.bgStartColor!!,
                    data.bgStartColor,
                    data.bgEndColor!!
                )
                rootContainer.background = gradient
            } else {
                if (data.backgroundColor.isValidColorCode()) {
                    rootContainer.setBackgroundColor(Color.parseColor(data.backgroundColor))
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                title.text =
                    Html.fromHtml(data.title.orEmpty(), Html.FROM_HTML_MODE_LEGACY).trim('\n')
            } else {
                title.text = Html.fromHtml(data.title.orEmpty()).trim('\n')
            }
            title.setTextColor(Color.parseColor(data.titleColor))
            cta.text = data.cta
            if (data.ctaColor.isValidColorCode()) {
                cta.setTextColor(Color.parseColor(data.ctaColor))
            }
            ivCoin.loadImageEtx(data.coinImageUrl.orEmpty())
            if (data.coinImageScaleType.isNotNullAndNotEmpty()) {
                ivCoin.scaleType = ImageView.ScaleType.CENTER_CROP
            } else {
                ivCoin.scaleType = ImageView.ScaleType.FIT_CENTER
            }

            setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        name = "${EVENT_TAG}_${EventConstants.CLICKED}",
                        params = hashMapOf<String, Any>().apply {
                            put(EventConstants.SOURCE, source.orEmpty())
                        }.apply {
                            putAll(model.extraParams ?: hashMapOf())
                        }
                    )
                )
                deeplinkAction.performAction(context, data.deeplink)
            }
        }

        return holder
    }

    class WidgetHolder(binding: WidgetDnrHomeBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetDnrHomeBinding>(binding, widget)

    class DnrHomeWidgetModel : WidgetEntityModel<DnrHomeWidgetData, WidgetAction>()

    @Keep
    data class DnrHomeWidgetData(
        @SerializedName("title_line_1") val title: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("coin_image_url") val coinImageUrl: String?,
        @SerializedName("coin_image_scale_type") val coinImageScaleType: String?,
        @SerializedName("cta") val cta: String?,
        @SerializedName("cta_color") val ctaColor: String?,
        @SerializedName("background_color") val backgroundColor: String?,
        @SerializedName("bg_start_color") val bgStartColor: String?,
        @SerializedName("bg_end_color") val bgEndColor: String?,
        @SerializedName("deeplink") val deeplink: String?
    ) : WidgetData()
}
