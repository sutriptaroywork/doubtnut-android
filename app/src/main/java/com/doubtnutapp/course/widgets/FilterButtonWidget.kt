package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnFilterButtonClicked
import com.doubtnutapp.base.OnMultiSelectFilterButtonClicked
import com.doubtnutapp.course.widgets.FilterButtonWidget.FilterButtonWidgetViewHolder
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

/**
* Used inside FilterDialogFragment
* */
class FilterButtonWidget(context: Context) :
    BaseBindingWidget<FilterButtonWidgetViewHolder,
            FilterButtonWidgetModel, WidgetFilterButtonBinding>(context) {

    companion object {
        const val TAG = "FilterButtonWidget"
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
        this.widgetViewHolder = FilterButtonWidgetViewHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: FilterButtonWidgetViewHolder,
        model: FilterButtonWidgetModel
    ):
            FilterButtonWidgetViewHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
        })
        val binding = holder.binding
        if (isCreated) {
            binding.flexLayout.removeAllViews()
        }
        isCreated = true
        val data = model.data
        binding.tvTitle.text = data.filterText
        binding.tvTitle.textSize = data.filterTextSize?.toFloat() ?: 18f
        binding.tvTitle.setTextColor(
            Utils.parseColor(data.filterTextColor ?: "#2f2f2f")
        )
        binding.flexLayout.flexDirection = FlexDirection.ROW
        binding.flexLayout.flexWrap = FlexWrap.WRAP
        binding.flexLayout.justifyContent = JustifyContent.FLEX_START
        data.filterItems?.forEachIndexed { index, filterItem ->
            val textView =
                LayoutInflater.from(context)
                    .inflate(R.layout.item_filter_button, this, false) as? TextView
            textView?.text = filterItem.text
            textView?.id = index
            if (filterItem.isSelected == true) {
                textView?.setTextColor(ContextCompat.getColor(context, R.color.white))
                textView?.background =
                    (ContextCompat.getDrawable(context, R.drawable.bg_orange_rectangle))
            } else {
                textView?.setTextColor(ContextCompat.getColor(context, R.color.color_969696))
                textView?.background =
                    (ContextCompat.getDrawable(context, R.drawable.bg_grey_rectangle))
            }
            textView?.setOnClickListener {
                if (data.isMultiSelect) {
                    actionPerformer?.performAction(
                        OnMultiSelectFilterButtonClicked(
                            data.filterKey.orEmpty(),
                            filterItem.filterId.orEmpty()
                        )
                    )
                } else {
                    actionPerformer?.performAction(
                        OnFilterButtonClicked(
                            data.filterKey.orEmpty(),
                            filterItem.filterId.orEmpty()
                        )
                    )
                }
                handleBackgroundColor(binding, data.filterItems, it)
            }
            binding.flexLayout.addView(textView)
        }
        return holder
    }

    private fun handleBackgroundColor(
        binding: WidgetFilterButtonBinding,
        filterItems: List<FilterButtonItems>,
        view: View
    ) {
        filterItems.forEachIndexed { index, filterButtonItems ->
            val button = binding.flexLayout.getFlexItemAt(index) as? Button
            if (index == view.id) {
                button?.setTextColor(ContextCompat.getColor(context, R.color.white))
                button?.setBackgroundColor(ContextCompat.getColor(context, R.color.orange_eb532c))
            } else {
                button?.setTextColor(ContextCompat.getColor(context, R.color.color_969696))
                button?.setBackgroundColor(ContextCompat.getColor(context, R.color.color_f5f5f5))
            }
        }
    }

    class FilterButtonWidgetViewHolder(
        binding: WidgetFilterButtonBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetFilterButtonBinding>(binding, widget)

    override fun getViewBinding(): WidgetFilterButtonBinding {
        return WidgetFilterButtonBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

class FilterButtonWidgetModel :
    WidgetEntityModel<FilterButtonWidgetData, WidgetAction>()

@Keep
data class FilterButtonWidgetData(
    @SerializedName("filter_text") val filterText: String?,
    @SerializedName("filter_text_color") val filterTextColor: String?,
    @SerializedName("filter_text_size") val filterTextSize: String?,
    @SerializedName("filter_key") val filterKey: String?,
    @SerializedName("filter_items") val filterItems: List<FilterButtonItems>?,
    @SerializedName("is_multi_select") val isMultiSelect: Boolean = false
) : WidgetData()

@Keep
data class FilterButtonItems(
    @SerializedName("filter_id") val filterId: String?,
    @SerializedName("text") val text: String?,
    @SerializedName("is_selected") val isSelected: Boolean?,
)