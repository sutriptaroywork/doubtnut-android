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
import com.doubtnutapp.databinding.ItemCategoryV2Binding
import com.doubtnutapp.databinding.WidgetCategoryV2Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.loadImageEtx
import com.doubtnut.core.utils.ViewUtils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.doubtnutapp.widgets.SpaceItemDecoration
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CategoryWidget(context: Context) : BaseBindingWidget<CategoryWidget.WidgetHolder,
    CategoryWidget.Model, WidgetCategoryV2Binding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE
            .daggerAppComponent
            .forceUnWrap()
            .inject(this)
    }

    override fun getViewBinding(): WidgetCategoryV2Binding {
        return WidgetCategoryV2Binding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val binding = holder.binding
        val data: Data = model.data
        binding.rv.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.rv.adapter = Adapter(
            data, holder.itemView.context,
            deeplinkAction, analyticsPublisher,
            model.extraParams ?: HashMap()
        )
        if (binding.rv.itemDecorationCount == 0)
            binding.rv.addItemDecoration(
                SpaceItemDecoration(
                    ViewUtils.dpToPx(
                        8f,
                        context
                    ).toInt()
                )
            )
        return holder
    }

    class Adapter(
        val data: Data,
        val context: Context,
        val deeplinkAction: DeeplinkAction,
        val analyticsPublisher: AnalyticsPublisher,
        val extraParams: HashMap<String, Any>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemCategoryV2Binding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = data.items[position]
            holder.binding.imageView.loadImageEtx(item.imageUrl.orEmpty())
            holder.binding.cardView.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.CATEGORY_WIDGET_CLICKED,
                        hashMapOf(EventConstants.CATEGORY to data.items[position].id.orEmpty()),
                        ignoreSnowplow = true
                    )
                )
                deeplinkAction.performAction(context, item.deeplink)
            }
        }

        override fun getItemCount(): Int = data.items.size

        class ViewHolder(val binding: ItemCategoryV2Binding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(binding: WidgetCategoryV2Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCategoryV2Binding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("items") val items: List<Item>
    ) : WidgetData()

    @Keep
    data class Item(
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("id") val id: String?,
        @SerializedName("deeplink") val deeplink: String?
    )
}
