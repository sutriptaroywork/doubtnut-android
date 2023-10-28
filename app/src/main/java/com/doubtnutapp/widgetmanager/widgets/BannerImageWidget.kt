package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetBannerImageBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.BannerActionUtils
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class BannerImageWidget(
    context: Context
) : BaseBindingWidget<BannerImageWidget.BannerImageWidgetHolder,
        BannerImageWidget.BannerImageWidgetModel,
        WidgetBannerImageBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var source: String? = ""

    override fun getViewBinding(): WidgetBannerImageBinding {
        return WidgetBannerImageBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = BannerImageWidgetHolder(getViewBinding(), this)
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun bindWidget(
        holder: BannerImageWidgetHolder,
        model: BannerImageWidgetModel
    ): BannerImageWidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        if (!model.data.cardRatio.isNullOrEmpty()) {
            (binding.ivBanner.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio =
                model.data.cardRatio
        }

        binding.ivBanner.loadImage(model.data.imageUrl, null)
        binding.root.setOnClickListener {
            val actionData = model.action?.actionData
            if (actionData != null && model.action!!.actionActivity != null) {
                BannerActionUtils.performAction(
                    holder.itemView.context,
                    model.action!!.actionActivity!!,
                    actionData
                )
            } else {
                deeplinkAction.performAction(
                    holder.itemView.context,
                    model.data.deeplink,
                    source.orEmpty()
                )

                if (!model.data.deeplink.isNullOrEmpty() && model.data.deeplink!!.contains(
                        "dictionary",
                        true
                    )
                ) {
                    val param: HashMap<String, Any> = hashMapOf()
                    param[EventConstants.SOURCE] = source.orEmpty()
                    param[EventConstants.ID] = model.data.id
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.DC_BANNER_CLICK,
                            param,
                            ignoreSnowplow = true
                        )
                    )
                }
            }
            val param: HashMap<String, Any> = hashMapOf()
            param[EventConstants.SOURCE] = source.orEmpty()
            param[EventConstants.ID] = model.data.id
            param[EventConstants.EVENT_NAME_DEEPLINK]  = model.data.deeplink.orEmpty()
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.BANNER_WIDGET_CLICK,
                    param
                )
            )
        }
        trackingViewId = model.data.id
        return holder
    }

    class BannerImageWidgetHolder(binding: WidgetBannerImageBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetBannerImageBinding>(binding, widget)

    class BannerImageWidgetModel : WidgetEntityModel<BannerImageWidgetData, WidgetAction>()


    @Keep
    data class BannerImageWidgetData(
        @SerializedName("_id") val id: String,
        @SerializedName(value = "image_url", alternate = ["img_url"]) val imageUrl: String,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("card_width") val cardWidth: String?
    ) : WidgetData()

}