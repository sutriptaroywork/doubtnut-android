package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.data.remote.models.LiveClassCategoryWidgetData
import com.doubtnutapp.data.remote.models.LiveClassCategoryWidgetItem
import com.doubtnutapp.data.remote.models.LiveClassCategoryWidgetModel
import com.doubtnutapp.databinding.ItemCategoryBinding
import com.doubtnutapp.databinding.WidgetCategoryBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.liveclass.ui.CourseCategoryActivity
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import javax.inject.Inject

class LiveClassCategoryWidget(context: Context) :
    BaseBindingWidget<LiveClassCategoryWidget.CategoryWidgetViewHolder,
        LiveClassCategoryWidgetModel, WidgetCategoryBinding>(context) {

    companion object {
        const val TAG = "LiveClassCategoryWidget"
        const val CATEGORY = "category"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = CategoryWidgetViewHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: CategoryWidgetViewHolder, model: LiveClassCategoryWidgetModel):
        CategoryWidgetViewHolder {
        super.bindWidget(holder, model)
        val data: LiveClassCategoryWidgetData = model.data
        val binding = holder.binding
        if (data.items.isNullOrEmpty()) {
            widgetViewHolder.itemView.visibility = View.GONE
            return holder
        }

        binding.recyclerView.adapter = LiveClassCategoryWidgetAdapter(
            data.items.orEmpty(),
            actionPerformer,
            analyticsPublisher, deeplinkAction,
            model.extraParams ?: HashMap()
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL, false
        )
        return holder
    }

    class LiveClassCategoryWidgetAdapter(
        val items: List<LiveClassCategoryWidgetItem>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val extraParams: HashMap<String, Any>
    ) : RecyclerView.Adapter<LiveClassCategoryWidgetAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemCategoryBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]
            val binding = holder.binding
            if (items.isNotEmpty()) {
                if (items.size > 4) {
                    Utils.setWidthBasedOnPercentage(
                        holder.itemView.context, holder.itemView, "4.1", R.dimen.spacing
                    )
                } else {
                    Utils.setWidthBasedOnPercentage(
                        holder.itemView.context, holder.itemView, "4", R.dimen.spacing_zero
                    )
                }
            }
            binding.categoryName.text = data.title.orEmpty()
            binding.categoryImage.loadImageEtx(data.imageUrl.orEmpty())
            binding.root.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EXPLORE_CAROUSEL_ITEM_CLICK,
                        hashMapOf<String, Any>(
                            EventConstants.CATEGORY_ID to data.categoryId.toString(),
                            EventConstants.PARENT_TITLE to data.title.toString(),
                            EventConstants.WIDGET to TAG,
                            EventConstants.WIDGET_TYPE to CATEGORY,
                            EventConstants.ITEM_POSITION to position
                        ).apply {
                            putAll(extraParams)
                        }
                    )
                )
                CourseCategoryActivity.startActivity(
                    holder.itemView.context,
                    true,
                    data.categoryId.toString(),
                    data.title.toString(),
                    null
                )
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)
    }

    class CategoryWidgetViewHolder(
        binding: WidgetCategoryBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetCategoryBinding>(binding, widget)

    override fun getViewBinding(): WidgetCategoryBinding {
        return WidgetCategoryBinding.inflate(LayoutInflater.from(context), this, true)
    }
}
