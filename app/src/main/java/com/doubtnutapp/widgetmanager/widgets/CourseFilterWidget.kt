package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher

import com.doubtnutapp.base.ShowFilterOption
import com.doubtnutapp.data.remote.models.CourseFilterWidgetModel
import com.doubtnutapp.databinding.WidgetCourseFilterBinding
import com.doubtnutapp.liveclasshome.ui.FilterData
import com.doubtnutapp.liveclasshome.ui.FilterItem
import javax.inject.Inject

class CourseFilterWidget(context: Context) : BaseBindingWidget<CourseFilterWidget.WidgetHolder,
        CourseFilterWidgetModel, WidgetCourseFilterBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetCourseFilterBinding {
        return WidgetCourseFilterBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: CourseFilterWidgetModel): WidgetHolder {
        val binding = holder.binding
        holder.itemView.setOnClickListener {
            performAction(
                ShowFilterOption(
                    FilterData(
                        model.data?.title.orEmpty(),
                        model.data?.items?.map { filterItem ->
                            FilterItem(
                                filterItem.id,
                                filterItem.title.orEmpty(),
                                false
                            )
                        }.orEmpty()
                    )
                )
            )

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.LIVE_CLASS_FILTER_CLICK,
                    hashMapOf(
                        EventConstants.WIDGET to "CourseFilterWidget"
                    ), ignoreSnowplow = true
                )
            )
        }

        val selectedItem = model.data?.items?.firstOrNull { it.id == model.data.selectedId }
        binding.textView.text = selectedItem?.title.orEmpty()
        return holder
    }

    class WidgetHolder(binding: WidgetCourseFilterBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseFilterBinding>(binding, widget)

}