package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher

import com.doubtnutapp.base.OnSelectCourseTab
import com.doubtnutapp.data.remote.models.TabCourseItem
import com.doubtnutapp.data.remote.models.TabCourseWidgetData
import com.doubtnutapp.data.remote.models.TabCourseWidgetModel
import com.doubtnutapp.databinding.ItemTabCourseBinding
import com.doubtnutapp.databinding.WidgetTabCourseBinding
import com.doubtnutapp.orDefaultValue
import javax.inject.Inject

class TabCourseWidget(context: Context) : BaseBindingWidget<TabCourseWidget.WidgetHolder,
        TabCourseWidgetModel, WidgetTabCourseBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetTabCourseBinding {
        return WidgetTabCourseBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: TabCourseWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data: TabCourseWidgetData = model.data
        binding.recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL, false
        )
        val actionActivity = model.action?.actionActivity.orDefaultValue()
        binding.recyclerView.adapter = Adapter(
            data.items.orEmpty(),
            actionActivity, model.data.selectedType.orEmpty(),
            actionPerformer, analyticsPublisher
        )
        return holder
    }

    class Adapter(
        val items: List<TabCourseItem>,
        val actionActivity: String,
        private val selectedType: String,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_tab_course, parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding
            binding.layoutParent.isSelected = items[position].type == selectedType
            binding.buttonFilter.text = items[position].display.orEmpty()
            binding.buttonFilter.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.LIVE_CLASS_TAB_FILTER_CLICK,
                        hashMapOf(
                            EventConstants.WIDGET to "TabCourseWidget"
                        )
                    )
                )
                actionPerformer?.performAction(OnSelectCourseTab(items[position].type))
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val binding = ItemTabCourseBinding.bind(itemView)
        }
    }

    class WidgetHolder(binding: WidgetTabCourseBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTabCourseBinding>(binding, widget)
}