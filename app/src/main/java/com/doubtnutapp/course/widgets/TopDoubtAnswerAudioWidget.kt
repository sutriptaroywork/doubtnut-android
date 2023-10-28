package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.PlayAudioEvent
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetTopDoubtAnswerAudioBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.liveclass.ui.AudioPlayerActivity
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class TopDoubtAnswerAudioWidget(context: Context) :
    BaseBindingWidget<TopDoubtAnswerAudioWidget.WidgetHolder,
        TopDoubtAnswerAudioWidgetModel, WidgetTopDoubtAnswerAudioBinding>(context) {

    companion object {
        const val TAG = "TopDoubtAnswerAudioWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetTopDoubtAnswerAudioBinding {
        return WidgetTopDoubtAnswerAudioBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: TopDoubtAnswerAudioWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data: TopDoubtAnswerAudioWidgetData = model.data
        binding.tvAnswer.text =
            context.getString(R.string.answer) + " " + (holder.adapterPosition + 1) + "."
        binding.tvTitle.text = data.title.orEmpty()
        binding.tvViewSolution.setOnClickListener {
            DoubtnutApp.INSTANCE.bus()?.send(PlayAudioEvent(true))
            AudioPlayerActivity.getStartIntent(
                widgetViewHolder.itemView.context,
                data.resourceUrl
            ).apply {
                widgetViewHolder.itemView.context.startActivity(this)
            }
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.WIDGET_VIEW_SOLUTION_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.WIDGET to TAG
                    ).apply {
                        putAll(model.extraParams ?: hashMapOf())
                    },
                    ignoreSnowplow = true
                )
            )
        }
        return holder
    }

    class WidgetHolder(binding: WidgetTopDoubtAnswerAudioBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTopDoubtAnswerAudioBinding>(binding, widget)
}

class TopDoubtAnswerAudioWidgetModel :
    WidgetEntityModel<TopDoubtAnswerAudioWidgetData, WidgetAction>()

@Keep
data class TopDoubtAnswerAudioWidgetData(
    @SerializedName("message") val title: String?,
    @SerializedName("resource_url") val resourceUrl: String
) : WidgetData()
