package com.doubtnut.referral.widgets

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.Keep
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.applyBackgroundColor
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.CoreBindingWidget
import com.doubtnut.core.widgets.ui.CoreWidget
import com.doubtnut.core.widgets.ui.CoreWidgetVH
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnut.referral.databinding.WidgetReferralCodeBinding
import com.doubtnut.referral.ui.ReferAndEarnHomeFragment
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ReferralCodeWidget(context: Context) :
    CoreBindingWidget<ReferralCodeWidget.WidgetHolder, ReferralCodeWidget.WidgetModel, WidgetReferralCodeBinding>(
        context
    ) {

    companion object{
        val EVENT_TAG_COPY_CLIKED="ReferQA_copy_click"
    }

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    override fun getViewBinding(): WidgetReferralCodeBinding {
        return WidgetReferralCodeBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
        CoreApplication.INSTANCE.androidInjector().inject(this)
    }

    override fun bindWidget(holder: WidgetHolder, model: WidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data = model.data

        binding.apply {
            tvCopy.text = data.copyText.orEmpty()
            tvReferralCode.text = data.referralCode
            rootContainer.applyBackgroundColor(data.bgColor)
            tvCopy.setOnClickListener {
                copyClipData(context, data.referralCode.orEmpty())
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EVENT_TAG_COPY_CLIKED
                    )
                )
            }
        }

        return holder
    }

    private fun copyClipData(context: Context, referralCodeString: String) {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("referral_code", referralCodeString)
        clipboardManager.setPrimaryClip(clip)
        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
    }

    class WidgetHolder(binding: WidgetReferralCodeBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<WidgetReferralCodeBinding>(binding, widget) {

    }

    @Keep
    class WidgetModel : WidgetEntityModel<ReferralCodeData, WidgetAction>()

    data class
    ReferralCodeData(
        @SerializedName("referral_code") val referralCode: String?,
        @SerializedName("bg_color") val bgColor: String?,
        @SerializedName("copy_text") val copyText: String?
    ) : WidgetData()


}