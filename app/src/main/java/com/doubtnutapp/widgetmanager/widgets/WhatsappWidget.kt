package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.Keep
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetWhatsappBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class WhatsappWidget(
    context: Context
) : BaseBindingWidget<WhatsappWidget.WhatsappWidgetHolder,
        WhatsappWidget.WhatsappWidgetModel, WidgetWhatsappBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetWhatsappBinding {
        return WidgetWhatsappBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WhatsappWidgetHolder(getViewBinding(), this)
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun bindWidget(
        holder: WhatsappWidgetHolder,
        model: WhatsappWidgetModel
    ): WhatsappWidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.tvTitle.text = Utils.getSpannableNumberString(model.data.title)
        binding.imageView.loadImage(model.data.imageUrl)

        binding.buttonWhatsapp.setOnClickListener {
            if (AppUtils.appInstalledOrNot(Constants.WHATSAPP_PACKAGE_NAME, context)) {
                deeplinkAction.performAction(context, model.data.deeplink)
            } else {
                ToastUtils.makeText(context, R.string.string_install_whatsApp, Toast.LENGTH_SHORT)
                    .show()
            }
        }

        trackingViewId = model.data.id
        return holder
    }

    class WhatsappWidgetHolder(binding: WidgetWhatsappBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetWhatsappBinding>(binding, widget)

    class WhatsappWidgetModel : WidgetEntityModel<WhatsappWidgetData, WidgetAction>()

    @Keep
    data class WhatsappWidgetData(
        @SerializedName("_id") val id: String,
        @SerializedName("title") val title: String,
        @SerializedName("image_url") val imageUrl: String,
        @SerializedName("deeplink") val deeplink: String?
    ) : WidgetData()
}