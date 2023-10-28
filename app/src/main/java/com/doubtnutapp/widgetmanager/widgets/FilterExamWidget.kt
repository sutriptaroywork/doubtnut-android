package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.FilterSelectAction
import com.doubtnutapp.data.remote.models.FilterExamItem
import com.doubtnutapp.data.remote.models.FilterExamWidgetModel
import com.doubtnutapp.data.remote.models.FilterPromoItem
import com.doubtnutapp.databinding.WidgetFilterExamBinding
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.libraryhome.course.ui.FilterDropDownAdapter
import com.doubtnutapp.libraryhome.course.ui.FilterDropdownMenu
import com.doubtnutapp.utils.Utils
import javax.inject.Inject

class FilterExamWidget(context: Context) : BaseBindingWidget<FilterExamWidget.WidgetHolder,
        FilterExamWidgetModel, WidgetFilterExamBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetFilterExamBinding {
        return WidgetFilterExamBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: FilterExamWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        setMargins(WidgetLayoutConfig(0, 0, 0, 0))
        val selectedFilter: FilterExamItem? = if (model.data.selectedFilterId == -1) {
            model.data.items?.getOrNull(0)
        } else {
            model.data.items?.firstOrNull { it.filterId == model.data.selectedFilterId }
        }

        val selectedPromo: FilterPromoItem? = if (model.data.selectedPromoId.isNullOrBlank()) {
            model.data.itemsPromo?.getOrNull(0)
        } else {
            model.data.itemsPromo?.firstOrNull { it.filterId == model.data.selectedPromoId }
        }

        if (selectedPromo?.filterId.isNullOrBlank()) {
            holder.binding.textViewTitleFilter2.visibility = View.INVISIBLE
        } else {
            holder.binding.textViewTitleFilter2.visibility = View.VISIBLE
        }

        holder.binding.textViewTitleFilter.text = selectedFilter?.text.orEmpty()
        holder.binding.textViewTitleFilter2.text = selectedPromo?.filterId.orEmpty()

        holder.binding.textViewTitleFilter.setOnClickListener {
            val filterListPair: List<Triple<String, String, String>> = model.data.items
                ?.map { filter ->
                    Triple(filter.filterId.toString(), filter.text, "#152838")
                }.orEmpty()
            if (filterListPair.isNullOrEmpty()) return@setOnClickListener
            showFilterMenu(
                anchorView = it,
                filterableList = filterListPair,
                selectedFilterId = if (selectedFilter?.filterId == null) {
                    ""
                } else {
                    selectedFilter.filterId.toString()
                },
                type = "ecm_id"
            )
        }

        holder.binding.textViewTitleFilter2.setOnClickListener {
            val filterListPromoPair: List<Triple<String, String, String>> = model.data.itemsPromo
                ?.map { filter ->
                    Triple(filter.filterId, filter.filterId, "#152838")
                }.orEmpty()
            if (filterListPromoPair.isNullOrEmpty()) return@setOnClickListener
            showFilterMenu(
                holder.binding.view,
                filterListPromoPair, selectedPromo?.filterId.orEmpty(), "promo"
            )
        }
        return holder
    }

    private fun showFilterMenu(
        anchorView: View,
        filterableList: List<Triple<String, String, String>>,
        selectedFilterId: String, type: String
    ) {
        val menu = FilterDropdownMenu(context!!, filterableList)
        menu.height = WindowManager.LayoutParams.WRAP_CONTENT
        menu.width = Utils.convertDpToPixel(200f).toInt()
        menu.isOutsideTouchable = true
        menu.isFocusable = true
        menu.showAsDropDown(anchorView)
        menu.setCategorySelectedListener(object : FilterDropDownAdapter.FilterSelectedListener {
            override fun onFilterSelected(position: Int, triple: Triple<String, String, String>?) {
                menu.dismiss()
                val key = triple?.first ?: return
                if (key == selectedFilterId) return
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.COURSE_FILTER_ITEM_CLICK,
                        hashMapOf(
                            EventConstants.SOURCE to type,
                            EventConstants.EVENT_NAME_ID to key,
                            EventConstants.WIDGET to "FilterExamWidget"
                        )
                    )
                )
                actionPerformer?.performAction(
                    FilterSelectAction(
                        key.toIntOrNull(),
                        triple.second,
                        type
                    )
                )
            }
        })
    }

    class WidgetHolder(binding: WidgetFilterExamBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetFilterExamBinding>(binding, widget)

}