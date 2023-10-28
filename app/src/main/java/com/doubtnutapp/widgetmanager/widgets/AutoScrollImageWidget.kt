package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetAutoScrollImageBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.Utils
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Pankaj on 10/7/21.
 */

class AutoScrollImageWidget(context: Context) :
    BaseBindingWidget<AutoScrollImageWidget.WidgetHolder, AutoScrollImageWidget.Model,
            WidgetAutoScrollImageBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    override fun getViewBinding(): WidgetAutoScrollImageBinding {
        return WidgetAutoScrollImageBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(),this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        if (model.layoutConfig == null) {
            model.layoutConfig = WidgetLayoutConfig(
                    marginTop = 16,
                    marginBottom = 8,
            )
        }
        val binding = holder.binding

        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val data = model.data
        binding.apply {
            val width =
                Utils.getWidthFromScrollSize(holder.itemView.context, data.cardWidth ?: "1.2") -
                        (cardView.marginStart + cardView.marginEnd)

            val ratio = data.cardRatio ?: Utils.getRatioFromScrollSize(data.cardWidth ?: "1.2")
            val radius = 4f.dpToPx()

            cardView.radius = radius
            cardView.layoutParams.width = width
            (imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = ratio

            requestLayout()

            imageView.loadImage(data.imageUrl)
            cardView.setOnClickListener {
                DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams?.apply {
                    put(Constants.ID, data.id)
                }))
                deeplinkAction.performAction(context, data.deeplink)
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetAutoScrollImageBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetAutoScrollImageBinding>(binding, widget)


    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("image") val imageUrl: String,
        @SerializedName("card_width") val cardWidth: String?,
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("max_image_height") val maxImageHeight: Int?,
        @SerializedName("deeplink") val deeplink: String,
        @SerializedName("id") val id: String
    ) : WidgetData()
}