package com.doubtnutapp.course.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent

import com.doubtnutapp.base.FilterSelectAction
import com.doubtnutapp.databinding.ItemFilterChipBinding
import com.doubtnutapp.databinding.WidgetFilterCourseTypeBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.utils.ViewUtils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.doubtnutapp.widgets.SpaceItemDecoration
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class FilterCourseTypeWidget(context: Context) :
    BaseBindingWidget<FilterCourseTypeWidget.FilterTabsWidgetHolder,
        FilterCourseTypeWidget.FilterTabsWidgetModel, WidgetFilterCourseTypeBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetFilterCourseTypeBinding {
        return WidgetFilterCourseTypeBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = FilterTabsWidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: FilterTabsWidgetHolder,
        model: FilterTabsWidgetModel
    ): FilterTabsWidgetHolder {
        super.bindWidget(holder, model)
        setMargins(WidgetLayoutConfig(0, 0, 0, 0))
        val data: FilterTabWidgetData = model.data

        holder.binding.textViewTitleMain.text = data.title.orEmpty()
        holder.binding.rvFilters.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        holder.binding.rvFilters.adapter = FilterTabsAdapter(
            data.items, data.selected.orEmpty(),
            actionPerformer, analyticsPublisher
        )
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
        val items: List<FilterTabItem>,
        private var selected: String,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher
    ) :
        RecyclerView.Adapter<FilterTabsAdapter.FilterCourseTypeViewHolder>() {

        init {
            if (selected.isBlank()) {
                this.selected = items.getOrNull(0)?.filterId.orEmpty()
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): FilterCourseTypeViewHolder {
            return FilterCourseTypeViewHolder(
                ItemFilterChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: FilterCourseTypeViewHolder, position: Int) {
            holder.binding.filterChip.apply {
                text = items[position].display
                isCheckable = false
                chipCornerRadius = 6f
                chipStrokeWidth = 2f
                if (items[position].filterId == selected) {
                    chipBackgroundColor = ColorStateList.valueOf(Utils.parseColor("#ea532e"))
                    chipStrokeColor = ColorStateList.valueOf(Utils.parseColor("#ea532e"))
                } else {
                    chipBackgroundColor = ColorStateList.valueOf(Utils.parseColor("#0a2537"))
                    chipStrokeColor = ColorStateList.valueOf(Utils.parseColor("#ffffff"))
                }
                setTextColor(context.resources.getColor(R.color.white))
                setOnClickListener {
                    if (items[position].filterId == selected) {
                        return@setOnClickListener
                    }
                    selected = items[position].filterId
                    notifyDataSetChanged()
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.PAYMENT_COURSE_TYPE_FILTER_ITEM_CLICK,
                            hashMapOf(
                                EventConstants.EVENT_NAME_ID to selected,
                                EventConstants.WIDGET to "FilterCourseTypeWidget"
                            )
                        )
                    )
                    actionPerformer?.performAction(
                        FilterSelectAction(
                            -1,
                            items[position].filterId, "course_type"
                        )
                    )
                }
            }
        }

        override fun getItemCount(): Int = items.size

        class FilterCourseTypeViewHolder(val binding: ItemFilterChipBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class FilterTabsWidgetHolder(binding: WidgetFilterCourseTypeBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetFilterCourseTypeBinding>(binding, widget)

    class FilterTabsWidgetModel : WidgetEntityModel<FilterTabWidgetData, WidgetAction>()

    @Keep
    data class FilterTabWidgetData(
        @SerializedName("items") val items: List<FilterTabItem>,
        @SerializedName("title") val title: String?,
        var selected: String? = ""
    ) : WidgetData()

    @Keep
    data class FilterTabItem(
        @SerializedName("display") val display: String,
        @SerializedName("id") val filterId: String
    )
}
