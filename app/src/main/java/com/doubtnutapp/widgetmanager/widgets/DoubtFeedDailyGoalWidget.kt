package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.EventBus.WidgetShownEvent
import com.doubtnutapp.analytics.AnalyticsPublisher

import com.doubtnutapp.databinding.WidgetDoubtFeedDailyGoalBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by devansh on 6/5/21.
 */

class DoubtFeedDailyGoalWidget(context: Context) :
    BaseBindingWidget<DoubtFeedDailyGoalWidget.WidgetHolder,
            DoubtFeedDailyGoalWidget.Model, WidgetDoubtFeedDailyGoalBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun getViewBinding(): WidgetDoubtFeedDailyGoalBinding {
        return WidgetDoubtFeedDailyGoalBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        holder.itemView.apply {
            super.bindWidget(holder, model.apply {
                layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
            })
            val data = model.data
            val binding = holder.binding

            if (data.isDone) {
                binding.tvNumber.hide()
                binding.ivTick.show()
            } else {
                binding.ivTick.hide()
                binding.tvNumber.show()
                binding.tvNumber.text = data.goalNumber.toString()
            }

            binding.tvTitle.text = data.title
            binding.tvSubtitle.text = data.subtitle
            binding.tvPracticing.text = data.practicingText
            binding.ivLineHorizontal.isVisible = data.isLast

            val dailyGoalExtraParams = mapOf(
                Constants.GOAL_ID to data.goalId,
                Constants.IS_DONE to data.isDone,
                Constants.GOAL_NUMBER to data.goalNumber,
            )
            if (model.extraParams == null) {
                model.extraParams = hashMapOf()
            }
            model.extraParams?.putAll(dailyGoalExtraParams)

            data.items?.forEach {
                if (it.extraParams == null) {
                    it.extraParams = hashMapOf()
                }
                it.extraParams?.putAll(dailyGoalExtraParams)
                model.extraParams?.put(Constants.WIDGET_TYPE, it.type)
            }

            val adapter = WidgetLayoutAdapter(context, actionPerformer)
            binding.recyclerView.adapter = adapter
            adapter.setWidgets(data.items.orEmpty())

            if (data.deeplink.isNullOrBlank().not()) {
                binding.viewClickInterceptor.setOnClickListener {
                    DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams))
                    deeplinkAction.performAction(context, data.deeplink)
                }
            }

            DoubtnutApp.INSTANCE.bus()?.send(WidgetShownEvent(model.extraParams))
        }
        return holder
    }

    class WidgetHolder(binding: WidgetDoubtFeedDailyGoalBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetDoubtFeedDailyGoalBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String,
        @SerializedName("subtitle") var subtitle: String,
        @SerializedName("goal_number") val goalNumber: Int,
        @SerializedName("is_last") val isLast: Boolean,
        @SerializedName("is_done") var isDone: Boolean,
        @SerializedName("goal_id") val goalId: Int,
        @SerializedName("items") val items: List<WidgetEntityModel<*, *>>?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("practicing_text") val practicingText: String,
    ) : WidgetData()
}