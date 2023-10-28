package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetKheloJeetoBannerBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.gson.annotations.SerializedName
import javax.inject.Inject


/**
 * Created by Pankaj on 09/07/21.
 */

class KheloJeetoBannerWidget(context: Context) :
    BaseBindingWidget<KheloJeetoBannerWidget.WidgetHolder, KheloJeetoBannerWidget.Model,WidgetKheloJeetoBannerBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {

        val data = model.data
        val binding = holder.binding

        with(binding) {

            cardContainer.setBackgroundColor(Utils.parseColor(data.imageBackgroundColor))

            ivBanner.apply {
                loadImage(data.image)
                setBackgroundColor(Utils.parseColor(data.imageBackgroundColor))
            }

            tvKheloJeetoTitle.apply {
                text = data.title
                setTextColor(Utils.parseColor(data.titleDescriptionColor))
            }

            tvKheloJeetoSubtitle.apply {
                isVisible = data.subtitle.isNullOrEmpty().not()
                text = data.subtitle
                setTextColor(Utils.parseColor(data.subtitleDescriptionColor))
            }
            buttonKheloJeeto.text = data.ctaText

            val clickAction = {
                publishEvent(
                    EventConstants.TOPIC_BOOSTER_GAME_HOME_PAGE_VISITED, hashMapOf(
                        EventConstants.SOURCE to EventConstants.HOME_PAGE_BANNER
                    )
                )

                DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams?.apply {
                    put(Constants.ID, data.id)
                    put(Constants.STATE, data.type)
                }))
                deeplinkAction.performAction(context, data.deeplink)
            }

            buttonKheloJeeto.setOnClickListener {
                clickAction()
            }
            setOnClickListener {
                clickAction()
            }
        }
        trackingViewId = data.id
        return holder
    }

    class WidgetHolder(binding: WidgetKheloJeetoBannerBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetKheloJeetoBannerBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String,
        @SerializedName("title") val title: String,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("cta_text") val ctaText: String,
        @SerializedName("image") val image: String,
        @SerializedName("card_width") val cardWidth: String,
        @SerializedName("title_color") val titleDescriptionColor: String,
        @SerializedName("subtitle_color") val subtitleDescriptionColor: String,
        @SerializedName("image_background_color") val imageBackgroundColor: String,
        @SerializedName("background_color") val backgroundColor: String,
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("deeplink") val deeplink: String,
        @SerializedName("type") val type: String,
    ) : WidgetData()

    override fun getViewBinding(): WidgetKheloJeetoBannerBinding {
        return WidgetKheloJeetoBannerBinding.inflate(LayoutInflater.from(context), this, true)
    }
}
