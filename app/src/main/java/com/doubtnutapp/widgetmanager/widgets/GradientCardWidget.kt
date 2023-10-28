package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetGradientCardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.utils.Utils
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by devansh on 3/2/21.
 */

class GradientCardWidget(context: Context) :
    BaseBindingWidget<GradientCardWidget.WidgetHolder, GradientCardWidget.Model, WidgetGradientCardBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetGradientCardBinding {
        return WidgetGradientCardBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val data = model.data

        holder.binding.apply {
            textView.text = data.title
            cardView.background = Utils.getGradientView(
                startGradient = data.gradient.colorStart,
                midGradient = data.gradient.colorMid,
                endGradient = data.gradient.colorEnd,
                orientation = GradientDrawable.Orientation.TR_BL,
                cornerRadius = 4f.dpToPx()
            )

            cardView.setOnClickListener {
                DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams))
                deeplinkAction.performAction(context, data.deeplink)
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetGradientCardBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetGradientCardBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("id") val id: String?,
        @SerializedName("gradient") val gradient: Gradient
    ) : WidgetData()

    @Keep
    data class Gradient(
        @SerializedName("color_start") val colorStart: String,
        @SerializedName("color_mid") val colorMid: String,
        @SerializedName("color_end") val colorEnd: String
    )
}