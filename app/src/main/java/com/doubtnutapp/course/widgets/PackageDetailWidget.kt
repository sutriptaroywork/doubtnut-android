package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.RequestCheckout
import com.doubtnutapp.databinding.ItemPackageDetailBinding
import com.doubtnutapp.databinding.WidgetPackageDetailBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.doubtnutapp.widgets.PointerTextView
import com.doubtnutapp.widgets.RowTextView
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 28/09/20.
 */
class PackageDetailWidget(context: Context) :
    BaseBindingWidget<PackageDetailWidget.WidgetHolder,
        PackageDetailWidgetModel, WidgetPackageDetailBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    companion object {
        const val TAG = "PackageDetailWidget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: PackageDetailWidgetModel): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val data: PackageDetailWidgetData = model.data
        val binding = holder.binding

        binding.recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL, false
        )

        binding.recyclerView.adapter = Adapter(
            data.items.orEmpty(),
            actionPerformer,
            analyticsPublisher,
            deeplinkAction,
            model.extraParams ?: HashMap(),
            holder.adapterPosition.toString()
        )
        return holder
    }

    class Adapter(
        val items: List<PackageDetailWidgetItem>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val extraParams: HashMap<String, Any>,
        val parentPosition: String
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemPackageDetailBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item: PackageDetailWidgetItem = items[position]
            val binding = holder.binding
            binding.cardContainer.setBackgroundColor(Utils.parseColor(item.bgColor))
            binding.textViewTitle.text = item.title.orEmpty()
            binding.textViewTitle.loadBackgroundImage(item.bgImage, R.color.white)

            if (item.emi?.installments?.any { it.isCompleted == true } == true) {
                binding.groupEmiOngoingNot.hide()
                binding.groupEmiOngoing.show()

                binding.textViewPaid.text = item.amountPaid.orEmpty()
                binding.textViewAmountDue.text = item.amountDue.orEmpty()
                binding.textViewRemaining.text = item.amountRemaining.orEmpty()
            } else {
                binding.groupEmiOngoingNot.show()
                binding.groupEmiOngoing.hide()

                binding.textViewAmountToPay.text = item.amountToPay.orEmpty()
                binding.textViewAmountStrikeThrough.text = item.amountStrikeThrough.orEmpty()
                binding.textViewAmountStrikeThrough.paintFlags =
                    binding.textViewAmountStrikeThrough.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.textViewAmountSaving.text = item.amountSaving.orEmpty()
            }

            binding.textViewPay.text = item.payText.orEmpty()
            binding.textViewPayInstallment.text = item.payInstallmentText.orEmpty()

            if (item.pointers?.size ?: 0 > 2) {
                binding.textViewSeeMore.show()
                item.pointers?.take(2)?.forEach {
                    val textView = PointerTextView(holder.itemView.context)
                    textView.setViews("•", it)
                    binding.layoutPointers.addView(textView)
                }
                var isShowingAll = false
                binding.textViewSeeMore.setOnClickListener {
                    if (isShowingAll) {
                        isShowingAll = false
                        setPointerView(
                            item.pointers, holder.itemView.context, binding.layoutPointers,
                            2
                        )
                        binding.textViewSeeMore
                            .setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_drop_down, 0)
                        binding.textViewSeeMore.text = "See More"
                    } else {
                        isShowingAll = true
                        setPointerView(
                            item.pointers, holder.itemView.context, binding.layoutPointers,
                            item.pointers?.size ?: 0
                        )
                        binding.textViewSeeMore
                            .setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_up_arrow, 0)
                        binding.textViewSeeMore.text = "See Less"
                    }
                }
            } else {
                binding.textViewSeeMore.hide()
                setPointerView(
                    item.pointers, holder.itemView.context, binding.layoutPointers,
                    item.pointers?.size ?: 0
                )
            }

            binding.textViewEmiTitle.text = item.emi?.title.orEmpty()
            binding.textViewEmiTitleMin.text = item.emi?.title.orEmpty()

            binding.textViewEmiSubTitle.text = item.emi?.subTitle.orEmpty()
            binding.textViewEmiDescription.text = item.emi?.description.orEmpty()
            binding.textViewMonth.text = item.emi?.monthLabel.orEmpty()
            binding.textViewInstallment.text = item.emi?.installmentLabel.orEmpty()
            binding.textViewEmiTotal.text = item.emi?.totalLabel.orEmpty()
            binding.textViewTotalAmount.text = item.emi?.totalAmount.orEmpty()

            binding.layoutInstallment.removeAllViews()
            item.emi?.installments?.forEach {
                val textView = RowTextView(holder.itemView.context)
                textView.setViews(
                    it.title.orEmpty(), it.amount.orEmpty(),
                    if (it.isCompleted == true) {
                        R.drawable.ic_tick_vip
                    } else {
                        R.drawable.ic_bg_rank_yellow
                    }
                )
                binding.layoutInstallment.addView(textView)
            }

            if (item.type != null && item.type == "emi") {
                if (item.emi?.showMore == true) {
                    binding.layoutEmi.show()
                    binding.layoutEmiMin.hide()
                } else {
                    binding.layoutEmi.hide()
                    binding.layoutEmiMin.show()
                }
                binding.layoutEmiParent.show()
                binding.textViewPayInstallment.show()
                if (item.payText.isNullOrBlank() || item.variantId.isNullOrBlank()) {
                    binding.textViewPay.hide()
                    binding.viewPaymentDivider.hide()
                } else {
                    binding.textViewPay.show()
                    binding.viewPaymentDivider.show()
                }
            } else {
                binding.layoutEmiParent.hide()
                binding.layoutEmi.hide()
                binding.layoutEmiMin.hide()
                binding.textViewPay.show()
                binding.textViewPayInstallment.hide()
                binding.viewPaymentDivider.hide()
            }

            binding.textViewKnowMore.setOnClickListener {
                item.emi?.showMore = false
                binding.layoutEmi.hide()
                binding.layoutEmiMin.show()
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.BUNDLE_CLICK_KNOW_LESS,
                        hashMapOf(
                            EventConstants.ITEM_POSITION to position,
                            EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty(),
                            EventConstants.PACKAGE_ID to item.id.orEmpty(),
                            EventConstants.VARIANT_ID to item.variantId.toString()
                        )
                    )
                )
            }

            binding.textViewKnowMoreMin.setOnClickListener {
                item.emi?.showMore = true
                binding.layoutEmi.show()
                binding.layoutEmiMin.hide()
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.BUNDLE_CLICK_KNOW_MORE,
                        hashMapOf(
                            EventConstants.ITEM_POSITION to position,
                            EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty(),
                            EventConstants.PACKAGE_ID to item.id.orEmpty(),
                            EventConstants.VARIANT_ID to item.variantId.toString()
                        )
                    )
                )
            }

            binding.textViewPay.setOnClickListener {
                val event = AnalyticsEvent(
                    EventConstants.BUNDLE_CLICK_BUY_NOW,
                    hashMapOf(
                        EventConstants.ITEM_POSITION to position,
                        EventConstants.ITEM_PARENT_POSITION to parentPosition,
                        EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty(),
                        EventConstants.PACKAGE_ID to item.id.orEmpty(),
                        EventConstants.VARIANT_ID to item.variantId.orEmpty()
                    ), ignoreMoengage = false
                )
                val event2 = AnalyticsEvent(
                    EventConstants.BUNDLE_CLICK_BUY_NOW + "_v2",
                    hashMapOf(
                        EventConstants.ITEM_POSITION to position,
                        EventConstants.ITEM_PARENT_POSITION to parentPosition,
                        EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty(),
                        EventConstants.PACKAGE_ID to item.id.orEmpty(),
                        EventConstants.VARIANT_ID to item.variantId.orEmpty()
                    )
                )
                analyticsPublisher.publishEvent(event)
                val countToSendEvent: Int = Utils.getCountToSend(
                    RemoteConfigUtils.getEventInfo(),
                    EventConstants.BUNDLE_CLICK_BUY_NOW
                )
                MoEngageUtils.setUserAttribute(holder.itemView.context, "dn_bnb_clicked",true)

                val eventCopy = event.copy()
                val event2Copy = event2.copy()
                repeat((0 until countToSendEvent).count()) {
                    analyticsPublisher.publishBranchIoEvent(eventCopy)
                    analyticsPublisher.publishBranchIoEvent(event2Copy)
                }
                actionPerformer?.performAction(RequestCheckout(item.variantId.orEmpty(), null))
            }

            binding.textViewPayInstallment.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.BUNDLE_CLICK_EMI,
                        hashMapOf(
                            EventConstants.ITEM_POSITION to position,
                            EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty(),
                            EventConstants.PACKAGE_ID to item.id.orEmpty(),
                            EventConstants.VARIANT_ID to item.variantIdInstallment.orEmpty()
                        )
                    )
                )
                actionPerformer?.performAction(RequestCheckout(item.variantIdInstallment.orEmpty(), null))
            }
        }

        private fun setPointerView(pointers: List<String>?, context: Context, pointerLayout: LinearLayout, take: Int) {
            pointerLayout.removeAllViews()
            if (pointers?.size ?: 0 > 0 && take > 0) {
                pointers?.take(take)?.forEach {
                    val textView = PointerTextView(context)
                    textView.setViews("•", it)
                    pointerLayout.addView(textView)
                }
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemPackageDetailBinding) : RecyclerView.ViewHolder(binding.root)
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

class PackageDetailWidgetModel : WidgetEntityModel<PackageDetailWidgetData, WidgetAction>()

@Keep
data class PackageDetailWidgetData(
    @SerializedName("items") val items: List<PackageDetailWidgetItem>?
) : WidgetData()

@Keep
data class PackageDetailWidgetItem(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("amount_to_pay") val amountToPay: String?,
    @SerializedName("paid") val amountPaid: String?,
    @SerializedName("amount_due") val amountDue: String?,
    @SerializedName("remaining") val amountRemaining: String?,
    @SerializedName("amount_strike_through") val amountStrikeThrough: String?,
    @SerializedName("amount_saving") val amountSaving: String?,
    @SerializedName("pointers") val pointers: List<String>?,
    @SerializedName("emi") val emi: Emi?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("pay_text") val payText: String?,
    @SerializedName("pay_installment_text") val payInstallmentText: String?,
    @SerializedName("upfront_variant") val variantId: String?,
    @SerializedName("emi_variant") val variantIdInstallment: String?,
    @SerializedName("bg_image") val bgImage: String?,
    @SerializedName("bg_color") val bgColor: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("assortment_id") val assortmentId: String?
) {
    @Keep
    data class Emi(
        @SerializedName("title") val title: String?,
        @SerializedName("sub_title") val subTitle: String?,
        @SerializedName("description") val description: String?,
        @SerializedName("month_label") val monthLabel: String?,
        @SerializedName("installment_label") val installmentLabel: String?,
        @SerializedName("total_label") val totalLabel: String?,
        @SerializedName("total_amount") val totalAmount: String?,
        @SerializedName("installments") val installments: List<Installment>?,
        @SerializedName("show_more") var showMore: Boolean? = false
    ) {
        @Keep
        data class Installment(
            @SerializedName("title") val title: String?,
            @SerializedName("amount") val amount: String?,
            @SerializedName("is_completed") var isCompleted: Boolean? = false
        )
    }
}
