package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.updateLayoutParams
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.DfPreviousDoubtsViewAllClick
import com.doubtnutapp.data.remote.models.doubtfeed2.Topic
import com.doubtnutapp.databinding.WidgetDoubtFeedBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.doubtfeed2.ui.adapter.TopicAdapter
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by devansh on 12/07/21.
 */

class DoubtFeedWidget(context: Context) :
    BaseBindingWidget<DoubtFeedWidget.WidgetHolder, DoubtFeedWidget.Model, WidgetDoubtFeedBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetDoubtFeedBinding {
        return WidgetDoubtFeedBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        updateLayoutParams {
            height = 250.dpToPx()
        }
        val data = model.data
        val binding = holder.binding
        binding.tvYourDoubts.text = data.heading

        val topicAdapter = TopicAdapter(null)
        binding.rvTopics.adapter = topicAdapter
        topicAdapter.updateList(data.topics.orEmpty())

        val widgetAdapter = WidgetLayoutAdapter(binding.root.context)
        binding.rvCarousels.adapter = widgetAdapter
        widgetAdapter.setWidgets(data.carousels.orEmpty())

        binding.viewClickInterceptor.setOnClickListener {
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.DG_PREVIOUS_DOUBT_VIEW_ALL_CLICK))
            performAction(DfPreviousDoubtsViewAllClick(data))
        }

        return holder
    }

    class WidgetHolder(binding: WidgetDoubtFeedBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetDoubtFeedBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("heading") val heading: String?,
        @SerializedName("topics") val topics: List<Topic>?,
        @SerializedName("carousels") val carousels: List<WidgetEntityModel<*, *>>?,
        @SerializedName("is_available") val isAvailable: Boolean = false,
    ) : WidgetData()
}