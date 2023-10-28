package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.RemoteConfigUtils
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemPackageDetailWidgetV3Binding
import com.doubtnutapp.databinding.WidgetPackageDetailBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class PackageDetailWidgetV3(context: Context) :
    BaseBindingWidget<PackageDetailWidgetV3.WidgetHolder,
            PackageDetailWidgetModelV3, WidgetPackageDetailBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    companion object {
        const val TAG = "PackageDetailWidgetV3"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: PackageDetailWidgetModelV3): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val data: PackageDetailWidgetDataV3 = model.data
        val binding = widgetViewHolder.binding

        binding.recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL, false
        )

        binding.recyclerView.adapter = Adapter(
            data.items.orEmpty(),
            actionPerformer,
            analyticsPublisher,
            deeplinkAction,
            model.extraParams ?: HashMap()
        )
        return holder
    }

    class Adapter(
        val items: List<PackageDetailWidgetItemV3>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val extraParams: HashMap<String, Any>,
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemPackageDetailWidgetV3Binding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ),
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
            binding.tvOldPrice.text = item.oldPrice.orEmpty()
            binding.tvOldPrice.textSize = item.oldPriceSize?.toFloat() ?: 13f
            binding.tvOldPrice.setTextColor(Utils.parseColor(item.oldPriceColor))
            binding.tvNewPrice.text = item.newPrice.orEmpty()
            binding.tvNewPrice.textSize = item.newPriceSize?.toFloat() ?: 18f
            binding.tvNewPrice.setTextColor(Utils.parseColor(item.newPriceColor))
            binding.tvOldPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

            holder.itemView.setOnClickListener {
                val event = AnalyticsEvent(
                    EventConstants.BUNDLE_SHEET_ITEM_CLICK,
                    extraParams, ignoreMoengage = false
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
                deeplinkAction.performAction(holder.itemView.context, item.deeplink)
            }

            binding.noteTv.text = item.noteTitle.orEmpty()
            binding.noteTv.setTextColor(Utils.parseColor(item.noteTitleColor ?: "#808080"))
            binding.noteTextTv.text = item.noteText.orEmpty()
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemPackageDetailWidgetV3Binding) :
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

class PackageDetailWidgetModelV3 : WidgetEntityModel<PackageDetailWidgetDataV3, WidgetAction>()

@Keep
data class PackageDetailWidgetDataV3(
    @SerializedName("items") val items: List<PackageDetailWidgetItemV3>?,
) : WidgetData()

@Keep
data class PackageDetailWidgetItemV3(
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
    @SerializedName("source") val source: String?,
    @SerializedName("note_title") val noteTitle: String?,
    @SerializedName("note_text") val noteText: String?,
    @SerializedName("note_title_color") val noteTitleColor: String?
)
