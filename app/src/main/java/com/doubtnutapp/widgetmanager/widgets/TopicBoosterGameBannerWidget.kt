package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.updateLayoutParams
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetTopicBoosterGameBannerBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.Utils
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class TopicBoosterGameBannerWidget(context: Context) :
    BaseBindingWidget<TopicBoosterGameBannerWidget.WidgetHolder, TopicBoosterGameBannerWidget.Model, WidgetTopicBoosterGameBannerBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetTopicBoosterGameBannerBinding {
        return WidgetTopicBoosterGameBannerBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val data = model.data
        val binding = holder.binding
        binding.apply {

            rootLayout.updateLayoutParams {
                this.width = Utils.getWidthFromScrollSize(holder.itemView.context, data.cardWidth) -
                        (marginStart + marginEnd)
            }

            cardContainer.background =
                Utils.getMaterialShapeDrawable(data.backgroundColor, 8f.dpToPx())

            tvGameDescription.apply {
                text = data.description
                setTextColor(Utils.parseColor(data.textDescriptionColor))
            }
            ivTopicBoosterGame.loadImage(data.image)
            buttonPlayNow.text = data.ctaText

            val clickAction = {
                DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams?.apply {
                    put(Constants.ID, data.id)
                }))
                deeplinkAction.performAction(context, data.deeplink)
            }

            buttonPlayNow.setOnClickListener {
                clickAction()
            }
            setOnClickListener {
                clickAction()
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetTopicBoosterGameBannerBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTopicBoosterGameBannerBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("text_description_color") val textDescriptionColor: String,
        @SerializedName("background_color") val backgroundColor: String,
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("description") val description: String,
        @SerializedName("image_url") val image: String,
        @SerializedName("card_width") val cardWidth: String,
        @SerializedName("deeplink") val deeplink: String,
        @SerializedName("id") val id: String,
        @SerializedName("cta_text") val ctaText: String,
    ) : WidgetData()
}