package com.doubtnutapp.quiztfs.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetDailyPracticeBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 06-09-2021
 */
class DailyPracticeWidget
constructor(
    context: Context
) : BaseBindingWidget<DailyPracticeWidget.WidgetHolder, DailyPracticeWidgetModel, WidgetDailyPracticeBinding>(
    context
) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetDailyPracticeBinding {
        return WidgetDailyPracticeBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.forceUnWrap()?.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: DailyPracticeWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(8,8,16,16)
        })
        val data = model.data

        holder.binding.title.text = data.title
        holder.binding.subTitle.text = data.subtitle
        holder.binding.bottomLabel.text = data.bottomText
        holder.binding.bottomLabel.setBackgroundColor(Utils.parseColor(data.colorBottomBg))
        holder.binding.bottomLabel.setTextColor(resources.getColor(R.color.tomato))
        holder.binding.iconBg.loadImage(data.imageBgUrl)
        holder.binding.icon.loadImage(data.imageUrl)

        holder.itemView.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    model.type
                            + "_" + EventConstants.WIDGET_ITEM_CLICK,
                    hashMapOf<String, Any>().apply {
                        putAll(model.extraParams ?: hashMapOf())
                    }
                )
            )
            deeplinkAction.performAction(context, data.deepLink)
        }

        return holder
    }

    class WidgetHolder(
        binding: WidgetDailyPracticeBinding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<WidgetDailyPracticeBinding>(binding, widget)
}

@Keep
class DailyPracticeWidgetModel :
    WidgetEntityModel<DailyPracticeWidgetData, WidgetAction>()

@Keep
data class DailyPracticeWidgetData(
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("bottom_text") val bottomText: String,
    @SerializedName("color_bottom_bg") val colorBottomBg: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("image_bg_url") val imageBgUrl: String,
    @SerializedName("deeplink") val deepLink: String
) : WidgetData()