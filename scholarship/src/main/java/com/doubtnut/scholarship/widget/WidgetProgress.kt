package com.doubtnut.scholarship.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.CoreBindingWidget
import com.doubtnut.core.widgets.ui.CoreWidget
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnut.scholarship.R
import com.doubtnut.scholarship.databinding.ItemWidgetProgressBinding
import com.doubtnut.scholarship.databinding.WidgetProgressBinding
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class WidgetProgress(
    context: Context
) : CoreBindingWidget<WidgetProgress.WidgetHolder, ProgressWidgetModel, WidgetProgressBinding>(
    context
) {

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    override fun getViewBinding(): WidgetProgressBinding {
        return WidgetProgressBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        CoreApplication.INSTANCE.androidInjector().inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: ProgressWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        binding.rvMain.layoutManager = GridLayoutManager(context, model.data.items?.size ?: 0)
        binding.rvMain.adapter = ProgressAdapter(
            context,
            model.data
        )
        return holder
    }

    class WidgetHolder(binding: WidgetProgressBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<WidgetProgressBinding>(binding, widget)

    companion object {
        const val TAG = "WidgetProgress"
        const val EVENT_TAG = "progress_widget"
    }
}

class ProgressAdapter(
    val context: Context,
    val data: ProgressWidgetData
) :
    RecyclerView.Adapter<ProgressAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemWidgetProgressBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return data.items?.size ?: 0
    }

    inner class ViewHolder(val binding: ItemWidgetProgressBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            data.items?.get(bindingAdapterPosition)?.let { item ->
                binding.tvTitle.text = item.title
                binding.tvTitle.applyTextSize(data.titleTextSize)
                binding.tvTitle.applyTextColor(data.titleTextColor)

                when {
                    item.isSelected == true -> {
                        binding.ivTick.load2(R.drawable.ic_progress_check)
                        binding.ivTick.applyBackgroundColor(data.highlightColor)
                        binding.ivTick.applyStrokeColor(data.highlightColor)
                        binding.viewLeft.applyBackgroundColor(data.highlightColor)
                        binding.viewRight.applyBackgroundColor(data.highlightColor)
                    }
                    item.isCurrent == true -> {
                        binding.ivTick.setImageDrawable(null)
                        binding.ivTick.applyBackgroundColor("#ffffff")
                        binding.ivTick.applyStrokeColor("#979797")
                        binding.viewLeft.applyBackgroundColor(data.highlightColor)
                        binding.viewRight.applyBackgroundColor("#979797")
                    }
                    else -> {
                        binding.ivTick.setImageDrawable(null)
                        binding.ivTick.applyBackgroundColor("#d8d8d8")
                        binding.ivTick.applyStrokeColor("#979797")
                        binding.viewLeft.applyBackgroundColor("#979797")
                        binding.viewRight.applyBackgroundColor("#979797")
                    }
                }
                binding.viewLeft.isVisible = item.isFirst?.not() ?: true
                binding.viewRight.isVisible = item.isLast?.not() ?: true
            }
        }
    }
}

@Keep
class ProgressWidgetModel :
    WidgetEntityModel<ProgressWidgetData, WidgetAction>()

@Keep
data class ProgressWidgetData(
    @SerializedName("highlight_color")
    var highlightColor: String?,
    @SerializedName("title_text_color")
    var titleTextColor: String?,
    @SerializedName("title_text_size")
    var titleTextSize: String?,
    @SerializedName("items")
    val items: List<ProgressWidgetItem>?
) : WidgetData()

@Keep
data class ProgressWidgetItem(
    @SerializedName("title")
    val title: String?,
    @SerializedName("is_selected")
    val isSelected: Boolean?,
    @SerializedName("is_current")
    var isCurrent: Boolean?,
    @SerializedName("is_first")
    var isFirst: Boolean?,
    @SerializedName("is_last")
    var isLast: Boolean?,
) : WidgetData()
