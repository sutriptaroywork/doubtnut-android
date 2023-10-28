package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.FilterSelectAction
import com.doubtnutapp.data.remote.models.CourseClassTabWidgetModel
import com.doubtnutapp.databinding.WidgetCourseClassTabBinding
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.material.tabs.TabLayout
import javax.inject.Inject

class CourseClassTabWidget(
    context: Context
) :
    BaseBindingWidget<CourseClassTabWidget.WidgetHolder,
        CourseClassTabWidgetModel, WidgetCourseClassTabBinding>(context) {

    companion object {
        private const val TAG = "CourseClassTabWidget"
    }

    override fun getViewBinding(): WidgetCourseClassTabBinding {
        return WidgetCourseClassTabBinding.inflate(LayoutInflater.from(context), this, true)
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: CourseClassTabWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        setMargins(WidgetLayoutConfig(8, 8, 0, 0))
        val data = model.data
        holder.binding.tabLayout.run {
            clearOnTabSelectedListeners()
            removeAllTabs()

            data.tabs.forEach {
                addTab(
                    newTab()
                        .apply {
                            text = it.title
                        }
                )
            }

            data.tabs.forEachIndexed { index, courseClassTabData ->
                if (courseClassTabData.isSelected == true) {
                    holder.binding.tabLayout.getTabAt(index)?.select()
                }
            }
        }

        holder.binding.tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    val filterId = data.tabs.getOrNull(tab.position)?.id ?: return
                    actionPerformer?.performAction(FilterSelectAction(null, filterId, "course_class"))
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            TAG + EventConstants.EVENT_ITEM_CLICK,
                            hashMapOf<String, Any>(
                                EventConstants.EVENT_NAME_ID to data.tabs.getOrNull(tab.position)?.id.toString(),
                                EventConstants.COURSE_TYPE to data.tabs.getOrNull(tab.position)?.title.orEmpty(),
                                EventConstants.WIDGET to TAG
                            ).apply {
                                putAll(model.extraParams ?: HashMap())
                            },
                            ignoreSnowplow = true
                        )
                    )
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                }
            }
        )

        return holder
    }

    class WidgetHolder(binding: WidgetCourseClassTabBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseClassTabBinding>(binding, widget)
}
