package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Keep
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetPaymentHistoryBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.transactionhistory.TransactionHistoryActivityV2
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class PaymentHistoryWidget(context: Context) :
    BaseBindingWidget<PaymentHistoryWidget.WidgetHolder,
        PaymentHistoryWidgetModel, WidgetPaymentHistoryBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    companion object {
        private const val TAG = "PaymentHistoryWidget"
    }

    override fun getView(): View {
        return View.inflate(context, R.layout.widget_payment_history, this)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: PaymentHistoryWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val data = model.data
        holder.binding.purchasedDateTv.setVisibleState(!data.purchasedDate.isNullOrEmpty())
        holder.binding.paymentHistoryTv.setVisibleState(!data.paymentHistoryText.isNullOrEmpty())
        holder.binding.purchasedDateTv.text = data.purchasedDate.orEmpty()
        holder.binding.paymentHistoryTv.text = data.paymentHistoryText.orEmpty()
        holder.binding.paymentHistoryTv.setOnClickListener {
            holder.binding.root.context.startActivity(TransactionHistoryActivityV2.getStartIntent(holder.binding.root.context))
        }
        return holder
    }

    class WidgetHolder(
        binding: WidgetPaymentHistoryBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetPaymentHistoryBinding>(binding, widget)

    override fun getViewBinding(): WidgetPaymentHistoryBinding {
        return WidgetPaymentHistoryBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

class PaymentHistoryWidgetModel : WidgetEntityModel<PaymentHistoryWidgetData, WidgetAction>()

@Keep
data class PaymentHistoryWidgetData(
    @SerializedName("title") val purchasedDate: String?,
    @SerializedName("link_text") val paymentHistoryText: String?,
    @SerializedName("deeplink") val deeplink: String?
) : WidgetData()
