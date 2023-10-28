package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.RecyclerView
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.dpToPxFloat
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.ChannelContentFilterSelected
import com.doubtnutapp.databinding.ItemChannelContentFilterBinding
import com.doubtnutapp.databinding.WidgetChannelFilterBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.hide
import com.doubtnutapp.loadImage
import com.doubtnutapp.show
import com.doubtnutapp.utils.Utils
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ChannelContentFilterWidget(
    context: Context
) : BaseBindingWidget<ChannelContentFilterWidget.WidgetHolder,
        ChannelContentFilterWidget.Model,
        WidgetChannelFilterBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var source: String? = ""

    override fun getViewBinding(): WidgetChannelFilterBinding {
        return WidgetChannelFilterBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: Model
    ): WidgetHolder {
        super.bindWidget(holder, model)

        setMargins(WidgetLayoutConfig(0, 0, 0, 8))
        val binding = holder.binding

        binding.rvChannelFilters.layoutManager =
            ChipsLayoutManager.newBuilder(binding.root.context)
                .setOrientation(ChipsLayoutManager.HORIZONTAL)
                .setMaxViewsInRow(5)
                .build()
        binding.rvChannelFilters.adapter =
            Adapter(model.data.items, analyticsPublisher, actionPerformer, source.orEmpty())

        return holder
    }

    class WidgetHolder(
        binding: WidgetChannelFilterBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetChannelFilterBinding>(binding, widget)

    class Model : WidgetEntityModel<ChannelContentFilterData, WidgetAction>()

    @Keep
    data class ChannelContentFilterData(@SerializedName("items") val items: ArrayList<ChannelFilterData>) :
        WidgetData()

    @Keep
    data class ChannelFilterData(
        @SerializedName("key") val key: String,
        @SerializedName("value") val value: String,
        @SerializedName("is_active") var isActive: Int,
        @SerializedName("icon_url") var icon: String
    )

    class Adapter(
        private var items: List<ChannelFilterData>,
        private val analyticsPublisher: AnalyticsPublisher,
        private val actionPerformer: ActionPerformer?,
        private val source: String
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemChannelContentFilterBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), actionPerformer, source
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]

            val widgetLayoutConfig = WidgetLayoutConfig(0, 8, 0, 16)
            var marginTop = 10
            var marginLeft = 0
            var marginRight = 0
            var marginBottom: Int = 0
            if (widgetLayoutConfig != null) {
                if (widgetLayoutConfig.marginTop >= 0) marginTop = widgetLayoutConfig.marginTop
                if (widgetLayoutConfig.marginLeft >= 0) marginLeft = widgetLayoutConfig.marginLeft
                if (widgetLayoutConfig.marginRight >= 0) marginRight =
                    widgetLayoutConfig.marginRight
                if (widgetLayoutConfig.marginBottom >= 0) marginBottom =
                    widgetLayoutConfig.marginBottom
            }

            val params = holder.binding.cardView.layoutParams as MarginLayoutParams
            params.topMargin = Utils.convertDpToPixel(marginTop.toFloat()).toInt()
            params.leftMargin = Utils.convertDpToPixel(marginLeft.toFloat()).toInt()
            params.rightMargin = Utils.convertDpToPixel(marginRight.toFloat()).toInt()
            params.bottomMargin = Utils.convertDpToPixel(marginBottom.toFloat()).toInt()

            holder.bind(data)

        }

        override fun getItemCount(): Int = items.size

        fun updateItems(items: List<ChannelFilterData>) {
            this.items = items
            notifyDataSetChanged()
        }

        class ViewHolder(
            val binding: ItemChannelContentFilterBinding,
            val actionPerformer: ActionPerformer?,
            val source: String
        ) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(data: ChannelFilterData) {
                binding.tvFilter.text = data.value
                binding.root.isSelected = data.isActive == 1
                binding.cardView.cardElevation =
                    if (data.isActive == 1) 8.dpToPxFloat() else 0.dpToPxFloat()

                if (!data.icon.isNullOrEmpty()) {
                    binding.ivIcon.show()
                    binding.ivIcon.loadImage(data.icon)
                } else {
                    binding.ivIcon.hide()
                }
                binding.root.setOnClickListener {
                    actionPerformer?.performAction(
                        ChannelContentFilterSelected(
                            data,
                            absoluteAdapterPosition
                        )
                    )
                }
            }
        }
    }
}