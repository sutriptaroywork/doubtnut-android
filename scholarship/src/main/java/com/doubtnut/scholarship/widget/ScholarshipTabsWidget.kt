package com.doubtnut.scholarship.widget

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.utils.CoreUserUtils
import com.doubtnut.core.utils.addOnTabSelectedListener2
import com.doubtnut.core.widgets.IWidgetLayoutAdapter
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.CoreBindingWidget
import com.doubtnut.core.widgets.ui.CoreWidget
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnut.scholarship.data.entity.ScholarshipBottomData
import com.doubtnut.scholarship.databinding.WidgetScholorshipTabsBinding
import com.doubtnut.scholarship.event.UpdateBottomData
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ScholarshipTabsWidget(
    context: Context
) : CoreBindingWidget<ScholarshipTabsWidget.WidgetHolder, ScholarshipTabsWidgetModel, WidgetScholorshipTabsBinding>(
    context
) {

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    override fun getViewBinding(): WidgetScholorshipTabsBinding {
        return WidgetScholorshipTabsBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        CoreApplication.INSTANCE.androidInjector().inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: ScholarshipTabsWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.tabLayout.removeAllTabs()
        binding.tabLayout.clearOnTabSelectedListeners()

        model.data.items?.forEach { item ->
            binding.tabLayout.addTab(
                binding.tabLayout.newTab()
                    .apply {
                        text = item.title
                        tag = item.title
                    }
            )
        }

        binding.tabLayout.addOnTabSelectedListener2 { tab ->
            model.data.items?.forEach { it.isSelected = false }
            model.data.items?.firstOrNull { it.title == tab.tag?.toString() }
                ?.let {
                    it.isSelected = true
                    (binding.rvMain.adapter as? IWidgetLayoutAdapter)
                        ?.setWidgets(it.widgets.orEmpty())
                    actionPerformer?.performAction(UpdateBottomData(it.bottomData))
                }

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.TAB_CLICKED}",
                    hashMapOf<String, Any>(
                        EventConstants.WIDGET to TAG,
                        EventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                        EventConstants.TAB_TITLE to tab.tag?.toString().orEmpty(),
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )

        }

        model.data.items?.indexOfFirst { it.isSelected == true }
            ?.takeIf { it != -1 }
            ?.let {
                binding.tabLayout.getTabAt(it)?.select()
            }

        model.data.items?.firstOrNull { it.isSelected == true }
            ?.let {
                (binding.rvMain.adapter as? IWidgetLayoutAdapter)
                    ?.setWidgets(it.widgets.orEmpty())
                actionPerformer?.performAction(UpdateBottomData(it.bottomData))
            }

        return holder
    }

    class WidgetHolder(binding: WidgetScholorshipTabsBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<WidgetScholorshipTabsBinding>(binding, widget)

    companion object {
        const val TAG = "ScholarshipTabWidget"
        const val EVENT_TAG = "scholarship_tab_widget"
    }
}

@Keep
class ScholarshipTabsWidgetModel :
    WidgetEntityModel<ScholarshipTabsWidgetData, WidgetAction>()

@Keep
data class ScholarshipTabsWidgetData(
    @SerializedName("items")
    val items: List<ScholarshipTabsWidgetItem>?
) : WidgetData()

@Keep
data class ScholarshipTabsWidgetItem(
    @SerializedName("title")
    val title: String?,
    @SerializedName("widgets")
    val widgets: List<WidgetEntityModel<*, *>?>?,
    @SerializedName("bottom_data")
    val bottomData: ScholarshipBottomData?,

    @SerializedName("extra_params")
    var extraParams: HashMap<String, Any>?,

    @SerializedName("is_selected")
    var isSelected: Boolean?,
) : WidgetData()