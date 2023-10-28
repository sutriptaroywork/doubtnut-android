package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetParentCourse2Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.setVisibleState
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 01/10/20.
 */
class CourseParentWidget(context: Context) : BaseBindingWidget<CourseParentWidget.WidgetHolder,
    CourseParentWidget.WidgetChildModel, WidgetParentCourse2Binding>(context) {

    companion object {
        const val TAG = "CourseParentWidget"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var source: String? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun getViewBinding(): WidgetParentCourse2Binding {
        return WidgetParentCourse2Binding
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
        val binding = holder.binding
        val data: WidgetChildData = model.data
        binding.textViewTitleMain.apply {
            text = data.title.orEmpty()
            binding.textViewTitleMain.setVisibleState(!data.title.isNullOrEmpty())
        }
        binding.btnViewAll.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.LC_SUBJECT_VIEW_ALL_CLICK,
                    hashMapOf<String, Any>().apply {
                        putAll(model.extraParams ?: HashMap())
                    }
                )
            )
            if (!data.deeplink.isNullOrBlank()) {
                deeplinkAction.performAction(context, data.deeplink)
            }
        }

        binding.textViewViewAll.text = data.linkText.orEmpty()
        when (model.data.scrollDirection) {
            "grid" -> {
                binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
            }
            "horizontal" -> {
                binding.recyclerView.layoutManager = LinearLayoutManager(
                    context,
                    RecyclerView.HORIZONTAL, false
                )
            }
            else -> {
                binding.recyclerView.layoutManager = LinearLayoutManager(
                    context,
                    RecyclerView.VERTICAL, false
                )
            }
        }
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
                widget.extraParams?.put(EventConstants.PARENT_TITLE, data.title.orEmpty())
                widget.extraParams?.put(EventConstants.WIDGET_TYPE, widget.type)
            }
        }
        adapter.setWidgets(data.items.orEmpty())
        return holder
    }

    class WidgetHolder(binding: WidgetParentCourse2Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetParentCourse2Binding>(binding, widget)

    class WidgetChildModel : WidgetEntityModel<WidgetChildData, WidgetAction>()

    @Keep
    data class WidgetChildData(
        @SerializedName("title") val title: String?,
        @SerializedName("link_text") val linkText: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("scroll_direction") val scrollDirection: String?,
        @SerializedName("items") val items: List<WidgetEntityModel<*, *>>?
    ) : WidgetData()
}
