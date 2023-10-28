package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher

import com.doubtnutapp.base.ChannelSubTabFilterSelected
import com.doubtnutapp.databinding.ItemChannelFilterBinding
import com.doubtnutapp.databinding.WidgetChannelFilterBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ChannelSubTabFilterWidget(
    context: Context
) : BaseBindingWidget<ChannelSubTabFilterWidget.WidgetHolder,
        ChannelSubTabFilterWidget.Model,
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

        val binding = holder.binding
        binding.rvChannelFilters.adapter =
            Adapter(model.data.items, analyticsPublisher, actionPerformer, source.orEmpty())
        binding.rvChannelFilters.scrollToPosition(getSelectedFilterPosition(model.data.items))

        return holder
    }

    private fun getSelectedFilterPosition(items: java.util.ArrayList<ChannelFilterData>): Int {
        var position = 0
        for(item in items){
            if(item.isActive == 1){
                return position
            }
            position++
        }
        return 0
    }

    class WidgetHolder(
        binding: WidgetChannelFilterBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetChannelFilterBinding>(binding, widget)

    class Model : WidgetEntityModel<WidgetChannelFilterSubTabData, WidgetAction>()

    @Keep
    data class WidgetChannelFilterSubTabData(@SerializedName("items") val items: ArrayList<ChannelFilterData>) :
        WidgetData()

    @Keep
    data class ChannelFilterData(
        @SerializedName("key") val key: String,
        @SerializedName("value") val value: String,
        @SerializedName("is_active") var isActive: Int
    )

    class Adapter(
        private var items: List<ChannelFilterData>,
        private val analyticsPublisher: AnalyticsPublisher,
        private val actionPerformer: ActionPerformer?,
        private val source: String
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemChannelFilterBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), actionPerformer, source
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]
            holder.bind(data)

        }

        override fun getItemCount(): Int = items.size

        fun updateItems(items: List<ChannelFilterData>) {
            this.items = items
            notifyDataSetChanged()
        }

        class ViewHolder(
            val binding: ItemChannelFilterBinding,
            val actionPerformer: ActionPerformer?,
            val source: String
        ) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(data: ChannelFilterData) {
                binding.tvFilter.text = data.value
                binding.tvFilter.isSelected = data.isActive == 1

                binding.root.setOnClickListener {
                    actionPerformer?.performAction(
                        ChannelSubTabFilterSelected(
                            data,
                            absoluteAdapterPosition
                        )
                    )
                }
            }
        }
    }

}
