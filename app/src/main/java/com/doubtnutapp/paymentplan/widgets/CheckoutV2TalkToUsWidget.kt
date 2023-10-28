package com.doubtnutapp.paymentplan.widgets

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetCheckoutV2TalkToUsBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


class CheckoutV2TalkToUsWidget(context: Context) :
    BaseBindingWidget<CheckoutV2TalkToUsWidget.WidgetHolder, CheckoutV2TalkToUsWidgetModel, WidgetCheckoutV2TalkToUsBinding>(
        context
    ) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var disposable: CompositeDisposable

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetCheckoutV2TalkToUsBinding {
        return WidgetCheckoutV2TalkToUsBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.forceUnWrap()?.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: CheckoutV2TalkToUsWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(8, 8, 0, 0)
        })
        val binding = holder.binding
        val data = model.data

        val text = data.title.orEmpty() + data.subtitle.orEmpty()

        val string = SpannableString(text)
        string.setSpan(
            UnderlineSpan(),
            data.title.orEmpty().length,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvCall.text = string

        binding.root.setOnClickListener {
            deeplinkAction.performAction(binding.root.context, data.deeplink)
        }

        return holder
    }

    class WidgetHolder(
        binding: WidgetCheckoutV2TalkToUsBinding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<WidgetCheckoutV2TalkToUsBinding>(binding, widget)
}

class CheckoutV2TalkToUsWidgetModel :
    WidgetEntityModel<CheckoutV2TalkToUsWidgetData, WidgetAction>()

@Keep
data class CheckoutV2TalkToUsWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("deeplink") val deeplink: String?
) : WidgetData()