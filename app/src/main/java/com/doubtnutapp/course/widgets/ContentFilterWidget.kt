package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ViewUtils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnContentFilterSelect
import com.doubtnutapp.databinding.ItemFilterChipContentBinding
import com.doubtnutapp.databinding.WidgetContentFilterBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.doubtnutapp.widgets.SpaceItemDecoration
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ContentFilterWidget(context: Context) : BaseBindingWidget<ContentFilterWidget.WidgetHolder,
    ContentFilterWidget.Model, WidgetContentFilterBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun getViewBinding(): WidgetContentFilterBinding {
        return WidgetContentFilterBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val binding = holder.binding
        val data: Data = model.data
        binding.rvFilters.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.rvFilters.adapter = Adapter(
            data, holder.itemView.context,
            actionPerformer, analyticsPublisher, model.extraParams ?: HashMap()
        )
        if (binding.rvFilters.itemDecorationCount == 0)
            binding.rvFilters.addItemDecoration(
                SpaceItemDecoration(
                    ViewUtils.dpToPx(
                        8f,
                        context
                    ).toInt()
                )
            )
        return holder
    }

    class Adapter(
        val data: Data,
        val context: Context,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val extraParams: HashMap<String, Any>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemFilterChipContentBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = data.items[position]
            holder.binding.filterChip.apply {
                holder.binding.tvFilter.text = item.displayName
                if (item.isSelected == true) {
                    background = Utils.getShape(
                        "#ea532c",
                        "#ffffff",
                        12f
                    )
                    holder.binding.tvFilter.setTextColor(Utils.parseColor("#ffffff"))
                } else {
                    background = Utils.getShape(
                        "#ffffff",
                        "#ea532c",
                        12f
                    )
                    holder.binding.tvFilter.setTextColor(Utils.parseColor("#ea532c"))
                }
                setOnClickListener {
                    if (item.isSelected == true) {
                        return@setOnClickListener
                    }
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.NCP_CONTENT_FILTER_ITEM_CLICKED,
                            hashMapOf<String, Any>(
                                EventConstants.NAME to item.displayName
                            ).apply {
                                putAll(extraParams)
                            },
                            ignoreBranch = false
                        )
                    )
                    actionPerformer?.performAction(OnContentFilterSelect(item.filterId))
                }
            }
        }

        override fun getItemCount(): Int = data.items.size

        class ViewHolder(val binding: ItemFilterChipContentBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(binding: WidgetContentFilterBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetContentFilterBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("items") val items: List<Item>
    ) : WidgetData()

    @Keep
    data class Item(
        @SerializedName("filter_id") val filterId: String,
        @SerializedName("text") val displayName: String,
        @SerializedName("is_selected") val isSelected: Boolean?
    )
}
