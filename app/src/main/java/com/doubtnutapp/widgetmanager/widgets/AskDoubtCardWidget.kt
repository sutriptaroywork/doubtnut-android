package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.EventBus.WidgetShownEvent
import com.doubtnutapp.analytics.AnalyticsPublisher

import com.doubtnutapp.databinding.WidgetAskDoubtCardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by devansh on 9/1/21.
 */

class AskDoubtCardWidget(context: Context)
    : BaseBindingWidget<AskDoubtCardWidget.WidgetHolder, AskDoubtCardWidget.Model,
        WidgetAskDoubtCardBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetAskDoubtCardBinding {
        return WidgetAskDoubtCardBinding.inflate(LayoutInflater.from(context),this,true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(),this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        val data = model.data
        val binding = holder.binding
        binding.apply {
            tvHeading.text = data.heading
            tvTitle.text = data.title
            tvSubtitle.text = data.subtitle

            setOnClickListener {
                deeplinkAction.performAction(context, data.deeplink)
                DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams))
            }
        }
        DoubtnutApp.INSTANCE.bus()?.send(WidgetShownEvent(model.extraParams))

        return holder
    }

    class WidgetHolder(binding: WidgetAskDoubtCardBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetAskDoubtCardBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
            @SerializedName("heading") val heading: String,
            @SerializedName("title") val title: String,
            @SerializedName("subtitle") val subtitle: String,
            @SerializedName("deeplink") val deeplink: String
    ) : WidgetData()
}