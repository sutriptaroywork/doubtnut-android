package com.doubtnutapp.course.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemBulletListBinding
import com.doubtnutapp.databinding.WidgetBulletListBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.loadImage
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class BulletListWidget @JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<BulletListWidget.WidgetHolder, BulletListWidgetModel,
    WidgetBulletListBinding>(context) {
    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    override fun getViewBinding(): WidgetBulletListBinding {
        return WidgetBulletListBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: BulletListWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)

        val binding = holder.binding

        binding.tvTitle.text = model.data.title
        binding.tvTitle.isVisible = model.data.title.isNullOrEmpty().not()
        binding.tvTitle.applyTextColor(model.data.titleTextColor)
        binding.tvTitle.applyTextSize(model.data.titleTextSize)

        binding.rvMain.adapter = BulletListAdapter(
            items = model.data.items.orEmpty(),
        )
        return holder
    }

    class WidgetHolder(binding: WidgetBulletListBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetBulletListBinding>(binding, widget)

    companion object {
        const val TAG = "BulletListWidget"
    }
}

@Keep
class BulletListWidgetModel :
    WidgetEntityModel<BulletListWidgetData, WidgetAction>()

@Keep
data class BulletListWidgetData(
    @SerializedName("title")
    val title: String?,
    @SerializedName("title_text_color")
    val titleTextColor: String?,
    @SerializedName("title_text_size")
    val titleTextSize: String?,
    @SerializedName("items")
    val items: List<BulletListWidgetDataItem>?
) : WidgetData()

@Keep
data class BulletListWidgetDataItem(
    @SerializedName("title")
    val title: String?,
    @SerializedName("title_text_color")
    val titleTextColor: String?,
    @SerializedName("title_text_size")
    val titleTextSize: String?,
    @SerializedName("icon")
    val icon: String?,
    @SerializedName("icon_size")
    val iconSize: String?,
    @SerializedName("layout_config")
    var layoutConfig: WidgetLayoutConfig? = null,
)

class BulletListAdapter(
    private val items: List<BulletListWidgetDataItem>,
) :
    ListAdapter<BulletListWidgetDataItem, BulletListAdapter.ViewHolder>(
        DIFF_UTILS
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BulletListAdapter.ViewHolder {
        return ViewHolder(
            ItemBulletListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: BulletListAdapter.ViewHolder,
        position: Int
    ) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: ItemBulletListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            val item = items[bindingAdapterPosition]

            if (item.layoutConfig == null) {
                item.layoutConfig = WidgetLayoutConfig(
                    marginLeft = 0,
                    marginTop = 0,
                    marginRight = 0,
                    marginBottom = 17,
                )
            }
            item.layoutConfig?.let {
                binding.root.setMargins(
                    left = it.marginLeft.dpToPx(),
                    top = it.marginTop.dpToPx(),
                    right = it.marginRight.dpToPx(),
                    bottom = it.marginBottom.dpToPx(),
                )
            }

            TextViewUtils.setTextFromHtml(
                binding.tvTitle,
                item.title.orEmpty()
            )
            binding.tvTitle.applyTextColor(item.titleTextColor)
            binding.tvTitle.applyTextSize(item.titleTextSize)

            binding.ivImage.loadImage(item.icon)
            binding.ivImage.layoutParams?.apply {
                width = item.iconSize?.toInt()?.dpToPx() ?: 16.dpToPx()
                height = item.iconSize?.toInt()?.dpToPx() ?: 16.dpToPx()
            }
            binding.ivImage.requestLayout()
        }
    }

    companion object {

        val DIFF_UTILS = object :
            DiffUtil.ItemCallback<BulletListWidgetDataItem>() {
            override fun areContentsTheSame(
                oldItem: BulletListWidgetDataItem,
                newItem: BulletListWidgetDataItem
            ) =
                oldItem == newItem

            override fun areItemsTheSame(
                oldItem: BulletListWidgetDataItem,
                newItem: BulletListWidgetDataItem
            ) =
                false
        }
    }
}
