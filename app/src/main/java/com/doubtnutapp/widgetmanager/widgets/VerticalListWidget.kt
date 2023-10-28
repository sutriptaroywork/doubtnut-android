package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemWidgetVerticalListBinding
import com.doubtnutapp.databinding.WidgetVerticalListBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class VerticalListWidget(
    context: Context
) : BaseBindingWidget<VerticalListWidget.WidgetHolder,
        VerticalListWidget.VerticalListWidgetModel, WidgetVerticalListBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetVerticalListBinding {
        return WidgetVerticalListBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: VerticalListWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val data: VerticalListWidgetData = model.data

        widgetViewHolder.binding.tvWidgetTitle.text = data.title
        widgetViewHolder.binding.rvItems.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        widgetViewHolder.binding.rvItems.adapter =
            VerticalListAdapter(model, analyticsPublisher, deeplinkAction)

        if (data.showViewAll.toBoolean()) {
            widgetViewHolder.binding.tvViewAll.show()
            widgetViewHolder.binding.tvViewAll.setOnClickListener {
                deeplinkAction.performAction(it.context, data.deeplink, model.type)
            }
        } else {
            widgetViewHolder.binding.tvViewAll.hide()
        }

        trackingViewId = data.id
        return holder
    }

    class VerticalListAdapter(
        val model: VerticalListWidgetModel,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction
    ) : RecyclerView.Adapter<VerticalListAdapter.ViewHolder>() {

        private val items = model.data.items

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_widget_vertical_list, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data: VerticalListItemData = items[position]

            holder.binding.ivImage.loadImage(data.imageUrl)
            holder.binding.tvTitle.text = data.title
            holder.binding.tvSubtitle.toggleVisibilityAndSetText(data.subtitle)

            holder.itemView.setOnClickListener {
                deeplinkAction.performAction(it.context, data.deeplink, model.type)

            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val binding = ItemWidgetVerticalListBinding.bind(itemView)
        }
    }

    class WidgetHolder(binding: WidgetVerticalListBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetVerticalListBinding>(binding, widget)

    class VerticalListWidgetModel : WidgetEntityModel<VerticalListWidgetData, WidgetAction>()

    @Keep
    data class VerticalListWidgetData(
        @SerializedName("_id") val id: String,
        @SerializedName("title") val title: String,
        @SerializedName("items") val items: List<VerticalListItemData>,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("show_view_all") val showViewAll: Int
    ) : WidgetData()

    @Keep
    data class VerticalListItemData(
        @SerializedName("title") val title: String,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("image_url") val imageUrl: String,
        @SerializedName("deeplink") val deeplink: String?
    )
}