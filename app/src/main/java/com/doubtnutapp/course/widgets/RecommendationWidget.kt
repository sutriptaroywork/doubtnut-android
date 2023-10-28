package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.OnNudgeClosed
import com.doubtnutapp.databinding.WidgetRecommendationBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class RecommendationWidget(context: Context) : BaseBindingWidget<RecommendationWidget.WidgetHolder,
    RecommendationWidget.RecommendationWidgetModel, WidgetRecommendationBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    override fun getViewBinding(): WidgetRecommendationBinding {
        return WidgetRecommendationBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: RecommendationWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data: RecommendationWidgetData = model.data
        with(binding) {
            tvTitle.text = data.title.orEmpty()
            tvTitle.textSize = data.titleSize?.toFloat() ?: 21f
            tvTitle.setTextColor(Utils.parseColor(data.titleColor))
            clMain.setBackgroundColor(Color.parseColor(data.bgColor ?: "#ffd8b4"))
            tvYes.text = data.yesText.orEmpty()
            tvYes.setTextColor(Color.parseColor(data.yesColor ?: "#ff4001"))
            tvNo.text = data.noText.orEmpty()
            tvNo.setTextColor(Color.parseColor(data.noColor ?: "#ff4001"))
            tvYes.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.RECOMMENDATION_WIDGET_CTA_CLICKED,
                        hashMapOf<String, Any>(EventConstants.CTA_TITLE to data.yesText.orEmpty()).apply {
                            putAll(model.extraParams ?: hashMapOf())
                        },
                        ignoreSnowplow = true
                    )
                )
                deeplinkAction.performAction(holder.itemView.context, data.yesDeeplink.orEmpty())
            }
            tvNo.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.RECOMMENDATION_WIDGET_CTA_CLICKED,
                        hashMapOf<String, Any>(EventConstants.CTA_TITLE to data.noText.orEmpty()).apply {
                            putAll(model.extraParams ?: hashMapOf())
                        },
                        ignoreSnowplow = true
                    )
                )
                actionPerformer?.performAction(OnNudgeClosed(""))
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetRecommendationBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetRecommendationBinding>(binding, widget)

    class RecommendationWidgetModel : WidgetEntityModel<RecommendationWidgetData, WidgetAction>()

    @Keep
    data class RecommendationWidgetData(
        @SerializedName("title") val title: String?,
        @SerializedName("title_size") val titleSize: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("bg_color") val bgColor: String?,
        @SerializedName("yes_text") val yesText: String?,
        @SerializedName("yes_text_color") val yesColor: String?,
        @SerializedName("no_text") val noText: String?,
        @SerializedName("no_text_color") val noColor: String?,
        @SerializedName("yes_deeplink") val yesDeeplink: String?
    ) : WidgetData()
}
