package com.doubtnutapp.dnr.widgets

import android.content.Context
import android.graphics.Color
import android.os.Parcelable
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.DnrTncClicked
import com.doubtnutapp.databinding.WidgetDnrTotalRewardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

/**
 * Created by Akshat Jindal on 20/10/21.
 */
class DnrTotalRewardWidget(context: Context) :
    BaseBindingWidget<
        DnrTotalRewardWidget.WidgetHolder,
        DnrTotalRewardWidget.Model,
        WidgetDnrTotalRewardBinding>(context) {

    companion object {
        private const val TAG = "DnrTotalRewardWidget"
    }

    var source: String? = null

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding() = WidgetDnrTotalRewardBinding
        .inflate(LayoutInflater.from(context), this, true)

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: Model
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val data = model.data
        val binding = holder.binding

        binding.apply {
            if (data.backgroundColor.isValidColorCode()) {
                card.setCardBackgroundColor(Color.parseColor(data.backgroundColor))
            }

            title.apply {
                text = data.title
                if (data.titleColor.isValidColorCode()) {
                    setTextColor(Color.parseColor(data.titleColor))
                }
            }

            subtitle.apply {
                text = data.subtitle
                if (data.subTitleColor.isValidColorCode()) {
                    setTextColor(Color.parseColor(data.subTitleColor))
                }
            }

            if (data.dividerColor.isValidColorCode()) {
                view.setBackgroundColor(Color.parseColor(data.dividerColor))
            }

            btnRedeem.apply {
                setVisibleState(data.cta.orEmpty().isNotNullAndNotEmpty())
                text = data.cta
                if (data.ctaColor.isValidColorCode()) {
                    setTextColor(Color.parseColor(data.ctaColor))
                }
                if (data.ctaBackgroundColor.isValidColorCode()) {
                    setBackgroundColor(Color.parseColor(data.ctaBackgroundColor))
                }
                setOnClickListener {
                    if (data.ctaDeeplink.isNullOrEmpty()) return@setOnClickListener
                    deeplinkAction.performAction(context, data.ctaDeeplink)
                }
            }
            upperHalfView.setOnClickListener {
                if (data.ctaDeeplink.isNullOrEmpty()) return@setOnClickListener
                deeplinkAction.performAction(context, data.ctaDeeplink)
            }

            tvInfo.apply {
                text = data.infoText
                setTextColor(Color.parseColor(data.infoTextColor))
                if (data.infoBackgroundColor.isValidColorCode()) {
                    setBackgroundColor(Color.parseColor(data.infoBackgroundColor))
                }
                setOnClickListener {
                    analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.DNR_FAQ_CLICKED, ignoreSnowplow = true))
                    if (data.infoDeeplink.isNullOrEmpty()) return@setOnClickListener
                    deeplinkAction.performAction(context, data.infoDeeplink)
                }
            }

            ivCoin.loadImageEtx(data.dnrImage.orEmpty())

            lowerHalfView.setOnClickListener {
                analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.DNR_FAQ_CLICKED, ignoreSnowplow = true))
                if (data.infoDeeplink.isNullOrEmpty()) return@setOnClickListener
                deeplinkAction.performAction(context, data.infoDeeplink)
            }

            tncLayout.setOnClickListener {
                analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.DNR_TC_CLICKED, ignoreSnowplow = true))
                actionPerformer?.performAction(
                    DnrTncClicked(
                        deeplink = "doubtnutapp://dnr/tnc",
                        data = data.tncDialogData
                    )
                )
            }

            tncTitle.text = data.tncText
        }

        return holder
    }

    class WidgetHolder(binding: WidgetDnrTotalRewardBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetDnrTotalRewardBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("subtitle_color") val subTitleColor: String?,
        @SerializedName("divider_color") val dividerColor: String?,
        @SerializedName("cta") val cta: String?,
        @SerializedName("cta_color") val ctaColor: String?,
        @SerializedName("cta_background_color") val ctaBackgroundColor: String?,
        @SerializedName("info_text") val infoText: String?,
        @SerializedName("info_text_color") val infoTextColor: String?,
        @SerializedName("info_background_color") val infoBackgroundColor: String?,
        @SerializedName("info_deeplink") val infoDeeplink: String?,
        @SerializedName("cta_deeplink") val ctaDeeplink: String?,
        @SerializedName("dnr_image") val dnrImage: String?,
        @SerializedName("background_color") val backgroundColor: String?,
        @SerializedName("tnc_text") val tncText: String?,
        @SerializedName("tnc_dialog_data") val tncDialogData: TncDialogData?
    ) : WidgetData()

    @Keep
    @Parcelize
    data class TncDialogData(
        @SerializedName("title") val title: String,
        @SerializedName("content") val content: String
    ) : Parcelable
}
