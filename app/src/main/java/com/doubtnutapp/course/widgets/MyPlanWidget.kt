package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.ItemMyPlanBinding
import com.doubtnutapp.databinding.WidgetRvListBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class MyPlanWidget(context: Context) : BaseBindingWidget<MyPlanWidget.WidgetHolder,
    MyPlanWidgetModel, WidgetRvListBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    companion object {
        const val TAG = "MyPlanWidget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: MyPlanWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        setMargins(WidgetLayoutConfig(0, 0, 0, 0))
        binding.rvItems.layoutManager = LinearLayoutManager(
            context, RecyclerView.VERTICAL, false
        )
        binding.rvItems.adapter = Adapter(
            model.data.items.orEmpty(),
            deeplinkAction, analyticsPublisher
        )

        return holder
    }

    class Adapter(val items: List<MyPlanItem>, val deeplinkAction: DeeplinkAction, val analyticsPublisher: AnalyticsPublisher) :
        RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemMyPlanBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding
            binding.root.background =
                Utils.getShape("#0a2537", "#5ba4d2")
            val item = items[position]
            binding.textViewSubTitle.text = item.subTitle.orEmpty()
            binding.textViewTitle.text = item.title.orEmpty()
            binding.root.setOnClickListener {
                deeplinkAction.performAction(holder.itemView.context, item.deeplink)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        TAG +
                            EventConstants.UNDERSCORE +
                            EventConstants.WIDGET_ITEM_CLICK,
                        hashMapOf(
                            EventConstants.EVENT_NAME_ID to item.assortmentId.toString(),
                            EventConstants.WIDGET to TAG
                        )
                    )
                )
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemMyPlanBinding) : RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(
        binding: WidgetRvListBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetRvListBinding>(binding, widget)

    override fun getViewBinding(): WidgetRvListBinding {
        return WidgetRvListBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

class MyPlanWidgetModel : WidgetEntityModel<MyPlanWidgetData, WidgetAction>()

@Keep
data class MyPlanWidgetData(
    @SerializedName("items") val items: List<MyPlanItem>?
) : WidgetData()

@Keep
data class MyPlanItem(
    @SerializedName("name") val title: String?,
    @SerializedName("sub_title") val subTitle: String?,
    @SerializedName("assortment_id") val assortmentId: String?,
    @SerializedName("deeplink") val deeplink: String?
)
