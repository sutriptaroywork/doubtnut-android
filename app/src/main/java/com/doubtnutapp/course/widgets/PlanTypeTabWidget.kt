package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.FilterSelectAction
import com.doubtnutapp.databinding.WidgetPlanTypeTabBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.material.tabs.TabLayout
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class PlanTypeTabWidget(context: Context) : BaseBindingWidget<PlanTypeTabWidget.WidgetHolder,
    PlanTypeTabWidgetModel, WidgetPlanTypeTabBinding>(context) {

    override fun getViewBinding(): WidgetPlanTypeTabBinding {
        return WidgetPlanTypeTabBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: PlanTypeTabWidgetModel): WidgetHolder {
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

            data.tabs.forEachIndexed { index, planTypeTabData ->
                if (planTypeTabData.isSelected == true) {
                    binding.tabLayout.getTabAt(index)?.select()
                }
            }
        }

        binding.tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    val filterId = data.tabs.getOrNull(tab.position)?.id ?: return
                    actionPerformer?.performAction(FilterSelectAction(null, filterId, "page_type"))
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.PLAN_TYPE_CLICK,
                            hashMapOf(
                                EventConstants.WIDGET to "PlanTypeTabWidget",
                                EventConstants.PARAM_NAME to data.tabs.getOrNull(tab.position)?.title.orEmpty()
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

    class WidgetHolder(binding: WidgetPlanTypeTabBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetPlanTypeTabBinding>(binding, widget)
}

class PlanTypeTabWidgetModel : WidgetEntityModel<PlanTypeTabWidgetData, WidgetAction>()

@Keep
data class PlanTypeTabWidgetData(
    @SerializedName("items") val tabs: List<PlanTypeTabData>
) : WidgetData()

@Keep
data class PlanTypeTabData(
    @SerializedName("id") val id: String,
    @SerializedName("display") val title: String?,
    @SerializedName("is_selected") var isSelected: Boolean? = false
)
