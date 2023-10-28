package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.applyBackgroundColor
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemPackageDetailWidgetV4Binding
import com.doubtnutapp.databinding.WidgetPackageDetailBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class PackageDetailWidgetV4(context: Context) :
    BaseBindingWidget<PackageDetailWidgetV4.WidgetHolder,
            PackageDetailWidgetModelV4, WidgetPackageDetailBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    companion object {
        const val TAG = "PackageDetailWidgetV4"
    }

    var source: String = ""

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: PackageDetailWidgetModelV4): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val data: PackageDetailWidgetDataV4 = model.data

        widgetViewHolder.binding.recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL, false
        )

        widgetViewHolder.binding.recyclerView.adapter = Adapter(
            data.items.orEmpty(),
            actionPerformer,
            analyticsPublisher,
            deeplinkAction,
            model.extraParams ?: HashMap(),
            source
        )
        return holder
    }

    class Adapter(
        val items: List<PackageDetailWidgetItemV4>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val extraParams: HashMap<String, Any>,
        val source: Any,
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemPackageDetailWidgetV4Binding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val binding = holder.binding
            binding.tvHeader.applyBackgroundColor(item.headerBackgroundColor)
            binding.tvHeader.text = item.headerTitle
            binding.tvHeader.applyTextSize(item.headerTitleTextSize)
            binding.tvHeader.applyTextColor(item.headerTitleTextColor)
            binding.tvHeader.isVisible = item.headerTitle.isNullOrEmpty().not()

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
            binding.tvNewPrice.text = item.newPrice.orEmpty()
            binding.tvNewPrice.textSize = item.newPriceSize?.toFloat() ?: 24f
            binding.tvNewPrice.setTextColor(Utils.parseColor(item.newPriceColor))
            binding.tvEmi.text = item.emi.orEmpty()
            binding.tvEmi.textSize = item.emiSize?.toFloat() ?: 10f
            binding.tvEmi.setTextColor(Utils.parseColor(item.emiColor))
            binding.tvDiscount.text = item.discountText.orEmpty()
            binding.tvDiscount.textSize = item.discountSize?.toFloat() ?: 13f
            binding.tvDiscount.setTextColor(Utils.parseColor(item.discountColor))
            binding.tvBuyNow.text = item.ctaText.orEmpty()
            binding.tvBuyNow.setTextColor(Utils.parseColor(item.ctaColor))

            binding.root.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PACKAGE_DETAIL_V4_ITEM_CLICKED,
                        hashMapOf(EventConstants.SOURCE to source).apply {
                            putAll(extraParams)
                        }, ignoreMoengage = false
                    )
                )
                MoEngageUtils.setUserAttribute(holder.itemView.context, "dn_bnb_clicked",true)

                deeplinkAction.performAction(holder.itemView.context, item.deeplink)
            }
            if (item.discountText != null && item.discountText.isNotEmpty()) {
                binding.tvDiscount.visibility = View.VISIBLE
                binding.tvDiscount.text = item.discountText.orEmpty()
            } else {
                binding.tvDiscount.visibility = View.GONE
            }
            binding.ivBestSeller.loadImageEtx(item.bestsellerImageUrl.orEmpty())
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemPackageDetailWidgetV4Binding) :
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

class PackageDetailWidgetModelV4 : WidgetEntityModel<PackageDetailWidgetDataV4, WidgetAction>()

@Keep
data class PackageDetailWidgetDataV4(
    @SerializedName("items") val items: List<PackageDetailWidgetItemV4>?,
) : WidgetData()

@Keep
data class PackageDetailWidgetItemV4(
    @SerializedName("duration") val title: String?,
    @SerializedName("duration_color") val titleColor: String?,
    @SerializedName("duration_size") val titleSize: String?,
    @SerializedName("image_url") val bestsellerImageUrl: String?,
    @SerializedName("bestseller") val bestseller: Boolean?,
    @SerializedName("amount_strike_through") val oldPrice: String?,
    @SerializedName("amount_strike_through_color") val oldPriceColor: String?,
    @SerializedName("amount_strike_through_size") val oldPriceSize: String?,
    @SerializedName("amount_to_pay") val newPrice: String?,
    @SerializedName("amount_to_pay_color") val newPriceColor: String?,
    @SerializedName("amount_to_pay_size") val newPriceSize: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("source") val source: String?,
    @SerializedName("discount") val discountText: String?,
    @SerializedName("discount_size") val discountSize: String?,
    @SerializedName("discount_color") val discountColor: String?,
    @SerializedName("emi") val emi: String?,
    @SerializedName("emi_color") val emiColor: String?,
    @SerializedName("emi_size") val emiSize: String?,
    @SerializedName("cta_text") val ctaText: String?,
    @SerializedName("cta_color") val ctaColor: String?,

    @SerializedName("header_title")
    val headerTitle: String?,
    @SerializedName("header_title_text_size")
    val headerTitleTextSize: String?,
    @SerializedName("header_title_text_color")
    val headerTitleTextColor: String?,
    @SerializedName("header_background_color")
    val headerBackgroundColor: String?,
)
