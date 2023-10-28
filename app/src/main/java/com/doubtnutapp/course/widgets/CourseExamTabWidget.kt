package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.FilterSelectAction
import com.doubtnutapp.data.remote.models.CourseExamTabWidgetModel
import com.doubtnutapp.databinding.WidgetCourseExamTabBinding
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.material.tabs.TabLayout
import javax.inject.Inject

class CourseExamTabWidget(
    context: Context
) : BaseBindingWidget<CourseExamTabWidget.WidgetHolder,
    CourseExamTabWidgetModel, WidgetCourseExamTabBinding>(context) {

    override fun getViewBinding(): WidgetCourseExamTabBinding {
        return WidgetCourseExamTabBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: CourseExamTabWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        setMargins(WidgetLayoutConfig(8, 8, 0, 0))
        val data = model.data
        val binding = holder.binding
        if (data.title.isNullOrBlank()) {
            binding.textViewTitleMain.hide()
        } else {
            binding.textViewTitleMain.show()
        }
        binding.textViewTitleMain.text = data.title.orEmpty()
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

            data.tabs.forEachIndexed { index, courseExamTabData ->
                if (courseExamTabData.isSelected == true) {
                    binding.tabLayout.getTabAt(index)?.select()
                }
            }
        }

        binding.tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    val filterId = data.tabs.getOrNull(tab.position)?.id ?: return
                    actionPerformer?.performAction(FilterSelectAction(filterId, null, "ecm_id"))
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.COURSE_FILTER_EXAM_CLICK,
                            hashMapOf(
                                EventConstants.WIDGET to "CourseExamTabWidget",
                                EventConstants.EXAM to data.tabs.getOrNull(tab.position)?.title.orEmpty()
                            ),
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

    class WidgetHolder(binding: WidgetCourseExamTabBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseExamTabBinding>(binding, widget)
}
