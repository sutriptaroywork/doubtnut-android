package com.doubtnutapp

import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetPendingPaymentBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class PendingPaymentWidget(context: Context) : BaseBindingWidget<PendingPaymentWidget.WidgetHolder,
        PendingPaymentWidgetModel, WidgetPendingPaymentBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: PendingPaymentWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)

        val data = model.data
        val view = holder.binding

        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.COMPLETE_PAYMENT_VIEW,
                hashMapOf<String, Any>()
                        .apply {
                            putAll(model.extraParams ?: hashMapOf())
                        }, ignoreSnowplow = false))

        view.tvTitle.text = data.title.orEmpty()
        view.tvSubTitle.text = data.subTitle.orEmpty()
        view.imageView.loadImageEtx(data.imageUrl.orEmpty())
        view.tvPrice.text = data.price.orEmpty()
        view.tvCrossedPrice.text = data.crossedPrice.orEmpty()
        view.buttonPaymentComplete.text = data.buttonText.orEmpty()
        view.tvCouponText.setVisibleState(!data.couponText.isNullOrEmpty())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.tvCouponText.text = Html.fromHtml(data.couponText.orEmpty(), Html.FROM_HTML_MODE_LEGACY)
        } else {
            view.tvCouponText.text = Html.fromHtml(data.couponText.orEmpty())
        }

        view.root.setOnClickListener {
            deeplinkAction.performAction(view.root.context, data.deeplink.orEmpty())
        }
        view.buttonPaymentComplete.setOnClickListener {
            deeplinkAction.performAction(view.root.context, data.deeplink.orEmpty())
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.PAYMENT_COMPLETE_CLICKED,
                    hashMapOf<String, Any>()
                            .apply {
                                putAll(model.extraParams ?: hashMapOf())
                            }))
        }

        return holder
    }

    class WidgetHolder(
        binding: WidgetPendingPaymentBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetPendingPaymentBinding>(binding, widget)

    override fun getViewBinding(): WidgetPendingPaymentBinding {
        return WidgetPendingPaymentBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

class PendingPaymentWidgetModel : WidgetEntityModel<PendingPaymentWidgetData, WidgetAction>()

@Keep
data class PendingPaymentWidgetData(
        @SerializedName("title") val title: String?,
        @SerializedName("subtitle") val subTitle: String?,
        @SerializedName("price") val price: String?,
        @SerializedName("crossed_price") val crossedPrice: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("cta_button_text") val buttonText: String?,
        @SerializedName("coupon") val couponText: String?,
        @SerializedName("deeplink") val deeplink: String?
) : WidgetData()

