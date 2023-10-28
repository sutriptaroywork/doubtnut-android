package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.annotation.Keep
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetFilterButtonBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.JustifyContent
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class FilterDropDownWidget(context: Context) :
    BaseBindingWidget<FilterDropDownWidget.FilterDropDownWidgetViewHolder,
            FilterDropDownWidgetModel, WidgetFilterButtonBinding>(context) {

    companion object {
        const val TAG = "FilterDropDownWidget"
        const val CATEGORY = "category"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    private var isCreated = false

    override fun setupViewHolder() {
        this.widgetViewHolder = FilterDropDownWidgetViewHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: FilterDropDownWidgetViewHolder,
        model: FilterDropDownWidgetModel
    ):
            FilterDropDownWidgetViewHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        if (isCreated) {
            binding.flexLayout.removeAllViews()
        }
        isCreated = true
        val data = model.data
        binding.tvTitle.text = data.filterText
        binding.tvTitle.textSize = data.filterTextSize?.toFloat() ?: 18f
        binding.tvTitle.setTextColor(
            Utils.parseColor(data.filterTextColor.orEmpty())
        )
        binding.flexLayout.flexDirection = FlexDirection.ROW
        binding.flexLayout.flexWrap = FlexWrap.WRAP
        binding.flexLayout.justifyContent = JustifyContent.FLEX_START
        data.filterItems?.forEachIndexed { index, filterItem ->
            val cardView =
                LayoutInflater.from(context)
                    .inflate(R.layout.item_category_filter_dropdown, this, false) as? CardView
            val constraintLayout =
                cardView?.findViewById<ConstraintLayout>(R.id.layoutConstraint)
            val filterTypeTv = constraintLayout?.findViewById<TextView>(R.id.tvFilterType)
            val filterValueTv = constraintLayout?.findViewById<TextView>(R.id.tvFilter)
            filterTypeTv?.text = filterItem.filterType
            filterValueTv?.text = filterItem.filterValue
            filterTypeTv?.id = index
            cardView?.setOnClickListener {
                // todo add deeplink
           /*     context.startActivity(
                    BottomSheetHolderActivity.getFiltersStartIntent(
                        context,
                        "1234"
                    )
                )*/
            }
            binding.flexLayout.addView(cardView)
        }
        return holder
    }

    class FilterDropDownWidgetViewHolder(
        binding: WidgetFilterButtonBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetFilterButtonBinding>(binding, widget)

    override fun getViewBinding(): WidgetFilterButtonBinding {
        return WidgetFilterButtonBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

class FilterDropDownWidgetModel :
    WidgetEntityModel<FilterDropDownWidgetData, WidgetAction>()

@Keep
data class FilterDropDownWidgetData(
    @SerializedName("filter_text") val filterText: String?,
    @SerializedName("filter_text_color") val filterTextColor: String?,
    @SerializedName("filter_text_size") val filterTextSize: String?,
    @SerializedName("filter_items") val filterItems: List<FilterWidgetItems>?,
) : WidgetData()

@Keep
data class FilterWidgetItems(
    @SerializedName("filter_type") val filterType: String?,
    @SerializedName("filter_value") val filterValue: String?,
    @SerializedName("filter_type_text_color") val filterTypeTextColor: String?,
    @SerializedName("filter_value_text_color") val filterValueTextColor: String?,
    @SerializedName("filter_type_text_size") val filterTypeTextSize: String?,
    @SerializedName("filter_value_text_size") val filterValueTextSize: String?,
)