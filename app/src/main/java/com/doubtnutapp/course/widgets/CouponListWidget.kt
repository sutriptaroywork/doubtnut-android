package com.doubtnutapp.course.widgets

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.Dismiss
import com.doubtnutapp.databinding.ItemCouponListBinding
import com.doubtnutapp.databinding.WidgetCouponListBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CouponListWidget(context: Context) : BaseBindingWidget<CouponListWidget.WidgetViewHolder,
    CouponListWidget.CouponListWidgetModel, WidgetCouponListBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    companion object {
        const val TAG = "CouponListWidget"
        const val EVENT_TAG = "coupon_list_widget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun getViewBinding(): WidgetCouponListBinding {
        return WidgetCouponListBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun bindWidget(
        holder: WidgetViewHolder,
        model: CouponListWidgetModel
    ): WidgetViewHolder {
        super.bindWidget(holder, model)
        val data: CouponListWidgetData = model.data
        val binding = holder.binding

        binding.tvTitle.text = data.title.orEmpty()
        binding.rvCoupon.layoutManager = LinearLayoutManager(
            context
        )
        binding.rvCoupon.adapter = Adapter(
            data.items.orEmpty(),
            actionPerformer,
            analyticsPublisher,
            deeplinkAction,
            context, model.extraParams
        )

        return holder
    }

    class Adapter(
        val items: List<CouponItem>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val context: Context,
        val extraParams: HashMap<String, Any>?
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        class ViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemCouponListBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding as ItemCouponListBinding
            val item = items[position]
            with(binding) {
                tvCouponCode.text = item.couponCode.orEmpty()
                tvDescription.text = item.description.orEmpty()
                tvDate.text = item.date.orEmpty()
                tvCopyText.text = item.couponCodeText.orEmpty()
                parentLayout.setOnClickListener {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            "${EVENT_TAG}_${EventConstants.CARD_CLICKED}",
                            hashMapOf<String, Any>().apply {
                                putAll(extraParams ?: hashMapOf())
                                EventConstants.CTA_TITLE to item.couponCode.orEmpty()
                            }
                        )
                    )

                    if (item.deeplink.isNullOrEmpty().not()) {
                        deeplinkAction.performAction(context, item.deeplink)
                        actionPerformer?.performAction(Dismiss())
                        return@setOnClickListener
                    }

                    copyCouponCode(context, item.couponCode.orEmpty())
                }
            }
        }

        private fun copyCouponCode(context: Context, couponCode: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("doubtnut_coupon_code", couponCode)
            clipboard.setPrimaryClip(clip)
            showToast(context, context.getString(R.string.coupon_code_copied))
        }

        override fun getItemCount(): Int {
            return items.size
        }
    }

    class WidgetViewHolder(binding: WidgetCouponListBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCouponListBinding>(binding, widget)

    class CouponListWidgetModel : WidgetEntityModel<CouponListWidgetData, WidgetAction>()

    @Keep
    data class CouponListWidgetData(
        @SerializedName("title") val title: String?,
        @SerializedName("items") val items: List<CouponItem>?,
    ) : WidgetData()

    @Keep
    data class CouponItem(
        @SerializedName("coupon_title") val couponCode: String?,
        @SerializedName("amount_saved") val description: String?,
        @SerializedName("validity") val date: String?,
        @SerializedName("coupon_code_text") val couponCodeText: String?,
        @SerializedName("deeplink") val deeplink: String?
    )
}
