package com.doubtnutapp.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.addOnTabSelectedListener
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnMediumSelected
import com.doubtnutapp.databinding.ItemCourseDetailsBinding
import com.doubtnutapp.databinding.WidgetSelectMediumBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.ifEmptyThenNull
import com.doubtnutapp.updateMargins
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SelectMediumWidget @JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<SelectMediumWidget.WidgetHolder, SelectMediumWidgetModel, WidgetSelectMediumBinding>(
    context, attrs, defStyle
) {

    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetSelectMediumBinding {
        return WidgetSelectMediumBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: SelectMediumWidgetModel
    ): WidgetHolder {
        if (model.layoutConfig == null) {
            model.layoutConfig = WidgetLayoutConfig.NONE
        }
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.tabLayout.removeAllTabs()
        binding.tabLayout.clearOnTabSelectedListeners()

        model.data.items?.forEachIndexed { index, studyMediumData ->
            binding.tabLayout.addTab(
                binding.tabLayout.newTab()
                    .apply {
                        text = studyMediumData.medium
                        tag = index
                    }
            )
        }
        binding.tabLayout.getTabAt(0)?.select()

        val adapter = CourseMediumAdapter()
        binding.rvItems.adapter = adapter
        adapter.submitList(model.data.items?.firstOrNull()?.items.orEmpty())

        binding.tabLayout.addOnTabSelectedListener { tab ->
            val index = tab.tag as Int
            adapter.submitList(model.data.items?.getOrNull(index)?.items.orEmpty())
            actionPerformer?.performAction(OnMediumSelected(model.data.items?.get(index)?.assortmentId.orEmpty()))
        }
        return holder
    }

    class WidgetHolder(binding: WidgetSelectMediumBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetSelectMediumBinding>(binding, widget)
}

@Keep
class SelectMediumWidgetModel :
    WidgetEntityModel<SelectMediumWidgetData, WidgetAction>()

@Keep
data class SelectMediumWidgetData(
    @SerializedName("items")
    val items: List<SelectMediumWidgetDataItem>?,
) : WidgetData()

@Keep
data class SelectMediumWidgetDataItem(
    @SerializedName("medium")
    val medium: String?,
    @SerializedName("medium_text")
    val mediumText: String?,
    @SerializedName("items")
    val items: List<SelectMediumWidgetDataItemItem>?,
    @SerializedName("assortment_id")
    val assortmentId: String?,
) : WidgetData()

@Keep
data class SelectMediumWidgetDataItemItem(
    @SerializedName("title")
    val title: String?,
    @SerializedName("title_text_size")
    val titleTextSize: String?,
    @SerializedName("title_text_color")
    val titleTextColor: String?,
) : WidgetData()

class CourseMediumAdapter :
    ListAdapter<SelectMediumWidgetDataItemItem, CourseMediumAdapter.ViewHolder>(
        DIFF_UTILS
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CourseMediumAdapter.ViewHolder {
        return ViewHolder(
            ItemCourseDetailsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: CourseMediumAdapter.ViewHolder,
        position: Int
    ) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    inner class ViewHolder(val binding: ItemCourseDetailsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            val item = currentList[bindingAdapterPosition]
            binding.tvTitle.text = item.title
            binding.tvTitle.applyTextSize(item.titleTextSize.ifEmptyThenNull() ?: "14")
            binding.tvTitle.applyTextColor(item.titleTextColor.ifEmptyThenNull() ?: "#494a4d")
            binding.tvTitle.updateMargins(
                bottom = 16.dpToPx()
            )
        }
    }

    companion object {

        val DIFF_UTILS = object : DiffUtil.ItemCallback<SelectMediumWidgetDataItemItem>() {
            override fun areContentsTheSame(
                oldItem: SelectMediumWidgetDataItemItem,
                newItem: SelectMediumWidgetDataItemItem
            ) =
                oldItem == newItem

            override fun areItemsTheSame(
                oldItem: SelectMediumWidgetDataItemItem,
                newItem: SelectMediumWidgetDataItemItem
            ) =
                oldItem == newItem
        }
    }
}


