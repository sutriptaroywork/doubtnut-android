package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.FilterSelectAction
import com.doubtnutapp.data.remote.models.CourseTypeTabWidgetModel
import com.doubtnutapp.databinding.WidgetCourseTypeTabBinding
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.material.tabs.TabLayout
import javax.inject.Inject

class CourseTypeTabWidget(
    context: Context
) : BaseBindingWidget<CourseTypeTabWidget.WidgetHolder,
    CourseTypeTabWidgetModel, WidgetCourseTypeTabBinding>(context) {

    companion object {
        private const val TAG = "CourseTypeTabWidget"
    }

    override fun getViewBinding(): WidgetCourseTypeTabBinding {
        return WidgetCourseTypeTabBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: CourseTypeTabWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        setMargins(WidgetLayoutConfig(8, 8, 0, 0))
        val binding = holder.binding
        val data = model.data
        binding.tabLayout.run {
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

            data.tabs.forEachIndexed { index, courseTypeTabData ->
                if (courseTypeTabData.isSelected == true) {
                    binding.tabLayout.getTabAt(index)?.select()
                }
            }
        }

        binding.tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    val filterId = data.tabs.getOrNull(tab.position)?.id ?: return
                    actionPerformer?.performAction(
                        FilterSelectAction(
                            null,
                            filterId,
                            "course_type"
                        )
                    )
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

    class WidgetHolder(binding: WidgetCourseTypeTabBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseTypeTabBinding>(binding, widget)
}
