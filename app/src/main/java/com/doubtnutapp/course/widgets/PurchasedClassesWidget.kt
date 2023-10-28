package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.data.remote.models.PurchasedClassesResource
import com.doubtnutapp.data.remote.models.PurchasedClassesWidgetData
import com.doubtnutapp.data.remote.models.PurchasedClassesWidgetItem
import com.doubtnutapp.data.remote.models.PurchasedClassesWidgetModel
import com.doubtnutapp.databinding.ItemPurchasedClassesBinding
import com.doubtnutapp.databinding.ItemPurchasedResourcesBinding
import com.doubtnutapp.databinding.WidgetPurchasedClassesBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import javax.inject.Inject

class PurchasedClassesWidget(context: Context) :
    BaseBindingWidget<PurchasedClassesWidget.PurchasedClassesViewHolder,
        PurchasedClassesWidgetModel, WidgetPurchasedClassesBinding>(context) {

    companion object {
        const val TAG = "PurchasedClassesWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetPurchasedClassesBinding {
        return WidgetPurchasedClassesBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = PurchasedClassesViewHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: PurchasedClassesViewHolder, model: PurchasedClassesWidgetModel):
        PurchasedClassesViewHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data: PurchasedClassesWidgetData = model.data
        if (data.items.isNullOrEmpty()) {
            binding.root.visibility = View.GONE
            return holder
        }
        binding.recyclerView.adapter = PurchasedClassesAdapter(
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

    class PurchasedClassesAdapter(
        val items: List<PurchasedClassesWidgetItem>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val extraParams: HashMap<String, Any>
    ) : RecyclerView.Adapter<PurchasedClassesAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemPurchasedClassesBinding
                    .inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding
            val data = items[position]
            if (items.isNotEmpty()) {
                Utils.setWidthBasedOnPercentage(
                    binding.root.context, binding.root, "2.5", R.dimen.spacing_zero
                )
            }
            binding.imageViewBg.loadImageEtx(data.backgroundUrl.orEmpty())
            binding.tvTimestamp.text = data.timestamp.orEmpty()
            binding.tvTimestamp.setTextColor(Color.parseColor(data.textColor))
            binding.planName.text = data.title.orEmpty()
            binding.planName.setTextColor(Color.parseColor(data.textColor))
            val progressValue = data.progress?.toInt() ?: 0
            binding.courseProgress.progress = progressValue
            binding.courseProgressTv.text = "$progressValue%"
            binding.rvResources.adapter = ResourceAdapter(data.resources)
            binding.rvResources.layoutManager = LinearLayoutManager(
                binding.root.context, RecyclerView.HORIZONTAL,
                false
            )
            binding.rvResources.isLayoutFrozen = true
            binding.layoutPurchasedClasses.setOnClickListener {
                val deeplinkUri = Uri.parse(data.deeplink)
                val id = deeplinkUri.getQueryParameter(Constants.ID)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        TAG + EventConstants.EVENT_ITEM_CLICK,
                        hashMapOf<String, Any>(
                            EventConstants.ASSORTMENT_ID to id.orEmpty(),
                            EventConstants.WIDGET to TAG
                        ),
                        ignoreSnowplow = true
                    )
                )
                deeplinkAction.performAction(binding.root.context, data.deeplink)
            }
        }

        class ViewHolder(val binding: ItemPurchasedClassesBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun getItemCount(): Int {
            return items.size
        }
    }

    class ResourceAdapter(
        val items: List<PurchasedClassesResource?>
    ) : RecyclerView.Adapter<ResourceAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemPurchasedResourcesBinding
                    .inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding
            val data = items[position]
            binding.resourceImage.loadImageEtx(data?.iconUrl.orEmpty())
            binding.resourceCount.text = data?.count.toString()
            binding.resourceCount.setTextColor(Color.parseColor(data?.textColor))
            binding.resourceText.text = data?.text.orEmpty()
            binding.resourceText.setTextColor(Color.parseColor(data?.textColor))
        }

        class ViewHolder(val binding: ItemPurchasedResourcesBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun getItemCount(): Int = items.size
    }

    class PurchasedClassesViewHolder(
        binding: WidgetPurchasedClassesBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetPurchasedClassesBinding>(binding, widget)
}
