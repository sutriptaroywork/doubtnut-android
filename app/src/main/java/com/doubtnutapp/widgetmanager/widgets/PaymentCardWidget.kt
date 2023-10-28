package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher

import com.doubtnutapp.base.PublishEvent
import com.doubtnutapp.data.remote.models.PaymentCardWidgetModel
import com.doubtnutapp.databinding.WidgetPaymentCardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.forceUnWrap
import javax.inject.Inject

class PaymentCardWidget(
        context: Context) : BaseBindingWidget<PaymentCardWidget.PaymentCardWidgetHolder,
        PaymentCardWidgetModel,WidgetPaymentCardBinding>(context) {


    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    companion object {
        const val TAG = "PaymentCardWidget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun getView(): View {
        return View.inflate(context, R.layout.widget_payment_card, this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = PaymentCardWidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: PaymentCardWidgetHolder, model: PaymentCardWidgetModel): PaymentCardWidgetHolder {
        holder.binding.paymentTitle.text = model.data.textOne.orEmpty()
        holder.binding.paymentTitle2.text = model.data.textTwo.orEmpty()
        holder.binding.paymentButton.text = model.data.buttonText.orEmpty()
        holder.binding.root.setOnClickListener {
            actionPerformer?.performAction(
                    PublishEvent(
                            AnalyticsEvent(
                                    EventConstants.INITIATE_VIP_PLAN_PAGE,
                                    hashMapOf(
                                            EventConstants.WIDGET to EventConstants.PAYMENT_CARD,
                                            EventConstants.TYPE to model.data.eventType.orEmpty()
                                    ), ignoreSnowplow = true
                            )
                    )
            )
            deeplinkAction.performAction(holder.itemView.context, model.data.paymentDeeplink)
        }
        return holder
    }

    class PaymentCardWidgetHolder(
        binding: WidgetPaymentCardBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetPaymentCardBinding>(binding, widget)

    override fun getViewBinding(): WidgetPaymentCardBinding {
        return WidgetPaymentCardBinding.inflate(LayoutInflater.from(context), this, true)
    }

}