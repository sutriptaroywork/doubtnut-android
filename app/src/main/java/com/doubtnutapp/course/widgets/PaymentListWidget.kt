package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.PublishEvent
import com.doubtnutapp.base.RequestVipTrial
import com.doubtnutapp.data.remote.models.PaymentCardListWidgetData
import com.doubtnutapp.data.remote.models.PaymentCardListWidgetModel
import com.doubtnutapp.data.remote.models.PaymentCardWidgetItem
import com.doubtnutapp.databinding.ItemPaymentCardBinding
import com.doubtnutapp.databinding.WidgetPaymentCardListBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import javax.inject.Inject

class PaymentListWidget(context: Context) : BaseBindingWidget<PaymentListWidget.WidgetHolder,
    PaymentCardListWidgetModel, WidgetPaymentCardListBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    companion object {
        const val TAG = "PaymentListWidget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: PaymentCardListWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val data: PaymentCardListWidgetData = model.data
        val binding = holder.binding

        val snapHelper = PagerSnapHelper()
        widgetViewHolder.binding.rvItems.onFlingListener = null
        snapHelper.attachToRecyclerView(widgetViewHolder.binding.rvItems)

        widgetViewHolder.binding.rvItems.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL, false
        )
        val actionActivity = model.action?.actionActivity.orDefaultValue()
        widgetViewHolder.binding.rvItems.adapter = Adapter(
            data.items, actionActivity,
            deeplinkAction, actionPerformer
        )
        if (data.items.size < 2) {
            widgetViewHolder.binding.circleIndicator.hide()
        } else {
            widgetViewHolder.binding.circleIndicator.show()
        }
        widgetViewHolder.binding.circleIndicator.attachToRecyclerView(widgetViewHolder.binding.rvItems, snapHelper)
        return holder
    }

    class Adapter(
        val items: List<PaymentCardWidgetItem>,
        val actionActivity: String,
        val deeplinkAction: DeeplinkAction,
        val actionPerformer: ActionPerformer? = null
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemPaymentCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.binding.paymentTitle.text = item.textOne.orEmpty()
            holder.binding.paymentButton.text = item.buttonText.orEmpty()
            holder.binding.root.setOnClickListener {
                if (item.action?.actionActivity.isNullOrBlank().not() && item.action?.actionActivity == "trial") {
                    actionPerformer?.performAction(RequestVipTrial(item.action.actionData?.categoryId.orEmpty()))
                } else if (item.action?.actionActivity.isNullOrBlank().not() && item.action?.actionActivity == "no_action") {
                } else {
                    actionPerformer?.performAction(
                        PublishEvent(
                            AnalyticsEvent(
                                EventConstants.INITIATE_VIP_PLAN_PAGE,
                                hashMapOf(
                                    EventConstants.WIDGET to EventConstants.PAYMENT_CARD,
                                    EventConstants.VARIANT_ID to item.variantId.orEmpty(),
                                    EventConstants.TYPE to item.eventType.orEmpty()
                                ),
                                ignoreSnowplow = true
                            )
                        )
                    )
                    deeplinkAction.performAction(holder.binding.root.context, item.action?.actionData?.paymentDeeplink)
                }
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemPaymentCardBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(
        binding: WidgetPaymentCardListBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetPaymentCardListBinding>(binding, widget)

    override fun getViewBinding(): WidgetPaymentCardListBinding {
        return WidgetPaymentCardListBinding.inflate(LayoutInflater.from(context), this, true)
    }
}
