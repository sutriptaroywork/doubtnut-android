package com.doubtnutapp.quiztfs.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetLiveQuestionsDailyPracticeRankBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.loadImageEtx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 26-08-2021
 */
class LiveQuestionsDailyPracticeRankWidget
constructor(
    context: Context
) : BaseBindingWidget<LiveQuestionsDailyPracticeRankWidget.WidgetHolder, LiveQuestionDailyPracticeRankWidgetModel, WidgetLiveQuestionsDailyPracticeRankBinding>(
    context
) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetLiveQuestionsDailyPracticeRankBinding {
        return WidgetLiveQuestionsDailyPracticeRankBinding
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
        model: LiveQuestionDailyPracticeRankWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig.DEFAULT
        })
        val data = model.data

        holder.binding.title.text = data.title
        holder.binding.subTitle.text = data.subTitle
        holder.binding.rank.text = data.rank
        holder.binding.points.text = data.points
        holder.binding.rankText.text = data.titleRank
        holder.binding.pointsText.text = data.titlePoint
        holder.binding.rankIcon.loadImageEtx(data.imageUrlRank)
        holder.binding.pointsIcon.loadImageEtx(data.imageUrlPoints)

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
        binding: WidgetLiveQuestionsDailyPracticeRankBinding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<WidgetLiveQuestionsDailyPracticeRankBinding>(binding, widget)
}

@Keep
class LiveQuestionDailyPracticeRankWidgetModel :
    WidgetEntityModel<LiveQuestionDailyPracticeRankWidgetData, WidgetAction>()

@Keep
data class LiveQuestionDailyPracticeRankWidgetData(
    @SerializedName("title") val title: String,
    @SerializedName("sub_title") val subTitle: String,
    @SerializedName("deeplink") val deepLink: String,
    @SerializedName("image_url_rank") val imageUrlRank: String,
    @SerializedName("image_url_points") val imageUrlPoints: String,
    @SerializedName("title_rank") val titleRank: String,
    @SerializedName("title_point") val titlePoint: String,
    @SerializedName("student_rank") val rank: String,
    @SerializedName("student_score") val points: String,
    @SerializedName("points_icon_color") val pointsIconColor: String
) : WidgetData()