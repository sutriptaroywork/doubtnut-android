package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.events.Dismiss
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.RemoteConfigUtils
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemPackageDetailV2Binding
import com.doubtnutapp.databinding.WidgetPackageDetailBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.liveclass.ui.BundleFragmentV2
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class PackageDetailWidgetV2(context: Context) :
    BaseBindingWidget<PackageDetailWidgetV2.WidgetHolder,
            PackageDetailWidgetModelV2, WidgetPackageDetailBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    companion object {
        const val TAG = "PackageDetailWidgetV2"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: PackageDetailWidgetModelV2): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val data: PackageDetailWidgetDataV2 = model.data

        holder.binding.recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL, false
        )

        holder.binding.recyclerView.adapter = Adapter(
            data.items.orEmpty(),
            actionPerformer,
            analyticsPublisher,
            deeplinkAction,
            model.extraParams ?: HashMap()
        )
        return holder
    }

    class Adapter(
        val items: List<PackageDetailWidgetItemV2>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val extraParams: HashMap<String, Any>,
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Adapter.ViewHolder {
            return ViewHolder(
                ItemPackageDetailV2Binding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val binding = holder.binding
            binding.textViewTitleOne.text = item.title.orEmpty()
            binding.textViewTitleOne.textSize = item.titleSize?.toFloat() ?: 18f
            binding.textViewTitleOne.setTextColor(Utils.parseColor(item.titleColor))
            binding.textViewTitleTwo.text = item.subtitle.orEmpty()
            binding.textViewTitleTwo.textSize = item.subtitleSize?.toFloat() ?: 14f
            binding.textViewTitleTwo.setTextColor(Utils.parseColor(item.subtitleColor))
            binding.tvBestSeller.text = item.bestsellerText.orEmpty()
            binding.tvBestSeller.textSize = item.bestsellerSize?.toFloat() ?: 13f
            binding.tvBestSeller.setTextColor(Utils.parseColor(item.bestsellerColor))
            binding.tvBestSeller.isVisible = item.bestseller ?: false
            binding.tvOldPrice.text = item.oldPrice.orEmpty()
            binding.tvOldPrice.textSize = item.oldPriceSize?.toFloat() ?: 13f
            binding.tvOldPrice.setTextColor(Utils.parseColor(item.oldPriceColor))
            binding.tvNewPrice.text = item.newPrice.orEmpty()
            binding.tvNewPrice.textSize = item.newPriceSize?.toFloat() ?: 18f
            binding.tvNewPrice.setTextColor(Utils.parseColor(item.newPriceColor))
            binding.viewBestSeller.isVisible = item.bestseller ?: false
            binding.tvOldPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

            binding.root.setOnClickListener {
                val event = AnalyticsEvent(
                    EventConstants.BUNDLE_SHEET_ITEM_CLICK,
                    extraParams,ignoreMoengage = false
                )
                analyticsPublisher.publishEvent(event)
                val countToSendEvent: Int = Utils.getCountToSend(
                    RemoteConfigUtils.getEventInfo(),
                    EventConstants.BUNDLE_SHEET_ITEM_CLICK
                )
                MoEngageUtils.setUserAttribute(holder.itemView.context, "dn_bnb_clicked",true)

                val eventCopy = event.copy()
                repeat((0 until countToSendEvent).count()) {
                    analyticsPublisher.publishBranchIoEvent(eventCopy)
                }
                DoubtnutApp.INSTANCE.bus()?.send(Dismiss(BundleFragmentV2.TAG))
                deeplinkAction.performAction(holder.itemView.context, item.deeplink)
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemPackageDetailV2Binding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(
        binding: WidgetPackageDetailBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetPackageDetailBinding>(binding, widget)

    override fun getViewBinding(): WidgetPackageDetailBinding {
        return WidgetPackageDetailBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

class PackageDetailWidgetModelV2 : WidgetEntityModel<PackageDetailWidgetDataV2, WidgetAction>()

@Keep
data class PackageDetailWidgetDataV2(
    @SerializedName("items") val items: List<PackageDetailWidgetItemV2>?,
) : WidgetData()

@Keep
data class PackageDetailWidgetItemV2(
    @SerializedName("duration") val title: String?,
    @SerializedName("duration_color") val titleColor: String?,
    @SerializedName("title_size") val titleSize: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("subtitle_color") val subtitleColor: String?,
    @SerializedName("subtitle_size") val subtitleSize: String?,
    @SerializedName("bestseller_text") val bestsellerText: String?,
    @SerializedName("bestseller") val bestseller: Boolean?,
    @SerializedName("bestseller_text_color") val bestsellerColor: String?,
    @SerializedName("bestseller_size") val bestsellerSize: Int?,
    @SerializedName("amount_strike_through") val oldPrice: String?,
    @SerializedName("amount_strike_through_color") val oldPriceColor: String?,
    @SerializedName("amount_strike_through_size") val oldPriceSize: String?,
    @SerializedName("amount_to_pay") val newPrice: String?,
    @SerializedName("amount_to_pay_color") val newPriceColor: String?,
    @SerializedName("amount_to_pay_size") val newPriceSize: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("source") val source: String?
)
