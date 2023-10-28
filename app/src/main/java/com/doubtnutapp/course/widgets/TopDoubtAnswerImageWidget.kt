package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.ImageViewerActivity
import com.doubtnutapp.databinding.WidgetTopDoubtAnswerImageBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.setVisibleState
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class TopDoubtAnswerImageWidget(context: Context) :
    BaseBindingWidget<TopDoubtAnswerImageWidget.WidgetHolder,
        TopDoubtAnswerImageWidgetModel, WidgetTopDoubtAnswerImageBinding>(context) {

    companion object {
        const val TAG = "TopDoubtAnswerImageWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetTopDoubtAnswerImageBinding {
        return WidgetTopDoubtAnswerImageBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: TopDoubtAnswerImageWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data: TopDoubtAnswerImageWidgetData = model.data
        binding.tvAnswer.text = "Answer " + (holder.adapterPosition + 1) + "."
        binding.tvTitle.text = data.title.orEmpty()
        binding.ivViewSolution.setVisibleState(!data.resourceUrl.isNullOrBlank())
        binding.ivViewSolution.loadImageEtx(data.resourceUrl.orEmpty())
        binding.ivViewSolution.setOnClickListener {
            if (!data.resourceUrl.isNullOrBlank()) {
                ImageViewerActivity.getStartIntent(
                    widgetViewHolder.itemView.context,
                    data.resourceUrl
                )
                    .apply {
                        widgetViewHolder.itemView.context.startActivity(this)
                    }
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

    class WidgetHolder(binding: WidgetTopDoubtAnswerImageBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTopDoubtAnswerImageBinding>(binding, widget)
}

class TopDoubtAnswerImageWidgetModel :
    WidgetEntityModel<TopDoubtAnswerImageWidgetData, WidgetAction>()

@Keep
data class TopDoubtAnswerImageWidgetData(
    @SerializedName("message") val title: String?,
    @SerializedName("resource_url") val resourceUrl: String?
) : WidgetData()
