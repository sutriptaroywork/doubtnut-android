package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.FilterSelectedEvent
import com.doubtnutapp.R

import com.doubtnutapp.base.FilterSelectAction
import com.doubtnutapp.databinding.ItemFilterChipBinding
import com.doubtnutapp.databinding.WidgetFilterTabsBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.utils.ViewUtils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgets.SpaceItemDecoration
import com.google.gson.annotations.SerializedName

class FilterTabsWidget(context: Context) :
    BaseBindingWidget<FilterTabsWidget.FilterTabsWidgetHolder,
            FilterTabsWidget.FilterTabsWidgetModel, WidgetFilterTabsBinding>(context) {

    override fun getViewBinding(): WidgetFilterTabsBinding {
        return WidgetFilterTabsBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = FilterTabsWidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: FilterTabsWidgetHolder,
        model: FilterTabsWidgetModel
    ): FilterTabsWidgetHolder {
        super.bindWidget(holder, model)

        val data: FilterTabWidgetData = model.data!!

        holder.binding.rvFilters.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        holder.binding.rvFilters.adapter =
            FilterTabsAdapter(data.items, data.selectedFilterId, actionPerformer)
        if (holder.binding.rvFilters.itemDecorationCount == 0)
            holder.binding.rvFilters.addItemDecoration(
                SpaceItemDecoration(
                    ViewUtils.dpToPx(
                        8f,
                        context!!
                    ).toInt()
                )
            )
        return holder
    }

    class FilterTabsAdapter(
        val items: List<FilterTabItem>, selectedFilterId: Int, val actionPerformer:
        ActionPerformer? = null
    ) : RecyclerView.Adapter<FilterTabsAdapter.ViewHolder>() {

        private var selectedFilterId = selectedFilterId

        init {
            if (selectedFilterId == -1) {
                this.selectedFilterId = items[0].filterId
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemFilterChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.filterChip.apply {
                text = items[position].text
                isCheckable = false
                chipCornerRadius = 6f
                if (items[position].filterId == selectedFilterId) {
                    setChipBackgroundColorResource(R.color.tomato)
                    setTextColor(context.resources.getColor(R.color.white))
                } else {
                    setChipBackgroundColorResource(R.color.colorGreyLight)
                    setTextColor(context.resources.getColor(R.color.tomato))
                }

                setOnClickListener {
                    if (items[position].filterId == selectedFilterId) {
                        return@setOnClickListener
                    }
                    selectedFilterId = items[position].filterId
                    notifyDataSetChanged()
                    (context.applicationContext as DoubtnutApp).bus()
                        ?.send(FilterSelectedEvent(items[position].filterId, items[position].text))
                    actionPerformer?.performAction(
                        FilterSelectAction(
                            items[position].filterId,
                            items[position].text
                        )
                    )
                }
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemFilterChipBinding) : RecyclerView.ViewHolder(binding.root)
    }

    class FilterTabsWidgetHolder(binding: WidgetFilterTabsBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetFilterTabsBinding>(binding, widget)

    class FilterTabsWidgetModel : WidgetEntityModel<FilterTabWidgetData, WidgetAction>()

    @Keep
    data class FilterTabWidgetData(
        @SerializedName("tabs") val items: List<FilterTabItem>,
        var selectedFilterId: Int = -1
    ) : WidgetData()

    @Keep
    data class FilterTabItem(
        @SerializedName("text") val text: String,
        @SerializedName("filter_id") val filterId: Int
    )
}