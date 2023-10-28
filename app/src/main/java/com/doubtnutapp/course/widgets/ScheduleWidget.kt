package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetScheduleBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 30/03/20.
 */
class ScheduleWidget(context: Context) : BaseBindingWidget<ScheduleWidget.WidgetHolder,
    ScheduleWidget.WidgetChildModel, WidgetScheduleBinding>(context) {

    companion object {
        const val TAG = "ScheduleWidget"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher
    var source: String? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun getViewBinding(): WidgetScheduleBinding {
        return WidgetScheduleBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: WidgetChildModel): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val data: WidgetChildData = model.data
        val binding = holder.binding

        binding.textViewDate.text = data.date.orEmpty()
        binding.textViewDay.text = data.day.orEmpty()

        binding.textViewDate.isVisible = data.date.isNullOrEmpty().not()
        binding.textViewDay.isVisible = data.day.isNullOrEmpty().not()

        binding.recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL, false
        )

        val adapter = WidgetLayoutAdapter(context, actionPerformer, source.orEmpty())
        binding.recyclerView.adapter = adapter
        data.items?.mapIndexed { index, widget ->
            if (widget != null) {
                if (widget.extraParams == null) {
                    widget.extraParams = hashMapOf()
                }
                widget.extraParams?.putAll(model.extraParams ?: HashMap())
                widget.extraParams?.put(EventConstants.ITEM_POSITION, index)
                source?.let { widget.extraParams?.put(EventConstants.SOURCE, it) }
                widget.extraParams?.put(EventConstants.WIDGET_TYPE, widget.type)
                widget.extraParams?.put(EventConstants.PARENT_ID, data.id.orEmpty())
            }
        }
        adapter.setWidgets(data.items.orEmpty())
        return holder
    }

    class WidgetHolder(binding: WidgetScheduleBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetScheduleBinding>(binding, widget)

    class WidgetChildModel : WidgetEntityModel<WidgetChildData, WidgetAction>()

    @Keep
    data class WidgetChildData(
        @SerializedName("id") val id: String?,
        @SerializedName("tag") val tag: String?,
        @SerializedName("date") val date: String?,
        @SerializedName("day") val day: String?,
        @SerializedName("resources") val items: List<WidgetEntityModel<*, *>>?
    ) : WidgetData()
}
