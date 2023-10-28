package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.databinding.WidgetIconCtaBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.setWidthFromScrollSize
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by devansh on 10/08/21.
 */

class IconCtaWidget(context: Context) :
    BaseBindingWidget<IconCtaWidget.WidgetHolder, IconCtaWidget.Model, WidgetIconCtaBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetIconCtaBinding {
        return WidgetIconCtaBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        val data = model.data

        holder.itemView.setWidthFromScrollSize(data.cardWidth)

        holder.binding.apply {
            ivIcon.loadImage(data.icon)
            tvTitle.text = data.title

            root.setOnClickListener {
                deeplinkAction.performAction(it.context, data.deepLink)
                val extraParams = if (model.extraParams == null) hashMapOf() else model.extraParams
                extraParams?.put(
                    Constants.WIDGET_CLICK_SOURCE,
                    WidgetTypes.TYPE_ICON_CTA
                )
                extraParams?.put(Constants.SUBJECT, data.title)
                DoubtnutApp.INSTANCE.bus()?.send(
                    WidgetClickedEvent(
                        extraParams = extraParams,
                        widgetType = WidgetTypes.TYPE_ICON_CTA
                    )
                )
            }
        }
        trackingViewId = data.id.toString()
        return holder
    }

    class WidgetHolder(binding: WidgetIconCtaBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetIconCtaBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: Int,
        @SerializedName("title") val title: String,
        @SerializedName("icon") val icon: String,
        @SerializedName("deepLink") val deepLink: String?,
        @SerializedName("card_width") val cardWidth: String,
    ) : WidgetData()
}