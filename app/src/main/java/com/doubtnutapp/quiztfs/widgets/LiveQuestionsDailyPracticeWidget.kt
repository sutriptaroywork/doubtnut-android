package com.doubtnutapp.quiztfs.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetLiveQuestionsDailyPracticeBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.loadImage
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 26-08-2021
 */
class LiveQuestionsDailyPracticeWidget
constructor(
    context: Context
) : BaseBindingWidget<LiveQuestionsDailyPracticeWidget.WidgetHolder, LiveQuestionDailyPracticeWidgetModel, WidgetLiveQuestionsDailyPracticeBinding>(
    context
) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetLiveQuestionsDailyPracticeBinding {
        return WidgetLiveQuestionsDailyPracticeBinding
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
        model: LiveQuestionDailyPracticeWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig.DEFAULT
        })
        val data = model.data

        holder.binding.title.text = data.title
        holder.binding.subTitle.text = data.subTitle
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
        binding: WidgetLiveQuestionsDailyPracticeBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetLiveQuestionsDailyPracticeBinding>(binding, widget)
}

@Keep
class LiveQuestionDailyPracticeWidgetModel :
    WidgetEntityModel<LiveQuestionDailyPracticeWidgetData, WidgetAction>()

@Keep
data class LiveQuestionDailyPracticeWidgetData(
    @SerializedName("title") val title: String,
    @SerializedName("sub_title") val subTitle: String,
    @SerializedName("deeplink") val deepLink: String,
    @SerializedName("image_url") val imageUrl: String
) : WidgetData()