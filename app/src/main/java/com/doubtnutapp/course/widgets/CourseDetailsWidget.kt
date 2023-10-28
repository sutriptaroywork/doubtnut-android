package com.doubtnutapp.course.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.utils.applyTextColor
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemCourseDetailsBinding
import com.doubtnutapp.databinding.WidgetCourseDetailsBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.loadImage
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseDetailsWidget @JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<CourseDetailsWidget.WidgetHolder, CourseDetailsWidgetModel,
    WidgetCourseDetailsBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetCourseDetailsBinding {
        return WidgetCourseDetailsBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: CourseDetailsWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)

        val binding = holder.binding

        binding.tvTitle.text = model.data.title
        binding.rvItems.adapter = CourseDetailsWidgetItemAdapter(model.data.items.orEmpty())

        return holder
    }

    class WidgetHolder(binding: WidgetCourseDetailsBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseDetailsBinding>(binding, widget)
}

@Keep
class CourseDetailsWidgetModel :
    WidgetEntityModel<CourseDetailsWidgetData, WidgetAction>()

@Keep
data class CourseDetailsWidgetData(
    @SerializedName("title")
    val title: String?,
    @SerializedName("items")
    val items: List<CourseDetailsWidgetDataItem>?,
) : WidgetData()

/**
 * Adding as an object instead of string to support future data.
 */
@Keep
data class CourseDetailsWidgetDataItem(
    @SerializedName("title")
    val title: String?,
    @SerializedName("title_text_color")
    val titleTextColor: String?,
    @SerializedName("icon")
    val icon: String?
) : WidgetData()

class CourseDetailsWidgetItemAdapter(
    private val items: List<CourseDetailsWidgetDataItem>,
    private val onBindUI: (ItemCourseDetailsBinding) -> Unit = {}
) :
    ListAdapter<CourseDetailsWidgetDataItem, CourseDetailsWidgetItemAdapter.ViewHolder>(
        DIFF_UTILS
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CourseDetailsWidgetItemAdapter.ViewHolder {
        return ViewHolder(
            ItemCourseDetailsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: CourseDetailsWidgetItemAdapter.ViewHolder,
        position: Int
    ) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: ItemCourseDetailsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            val item = items[bindingAdapterPosition]
            binding.tvTitle.text = item.title
            binding.tvTitle.applyTextColor(item.titleTextColor)
            if (item.icon.isNotNullAndNotEmpty()) {
                binding.ivImage.loadImage(item.icon)
            }
            onBindUI(binding)
        }
    }

    companion object {

        val DIFF_UTILS = object : DiffUtil.ItemCallback<CourseDetailsWidgetDataItem>() {
            override fun areContentsTheSame(
                oldItem: CourseDetailsWidgetDataItem,
                newItem: CourseDetailsWidgetDataItem
            ) =
                oldItem == newItem

            override fun areItemsTheSame(
                oldItem: CourseDetailsWidgetDataItem,
                newItem: CourseDetailsWidgetDataItem
            ) =
                false
        }
    }
}
