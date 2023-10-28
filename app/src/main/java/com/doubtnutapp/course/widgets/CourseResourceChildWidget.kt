package com.doubtnutapp.course.widgets

import android.content.ActivityNotFoundException
import android.content.Context
import android.graphics.Paint
import android.net.Uri
import android.view.LayoutInflater
import android.webkit.URLUtil
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.OnCourseCarouselChildWidgetItmeClicked
import com.doubtnutapp.base.OneTapBuy
import com.doubtnutapp.base.RefreshUI
import com.doubtnutapp.databinding.WidgetCourseResourceChildBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.ui.browser.CustomTabActivityHelper
import com.doubtnutapp.ui.browser.WebViewFallback
import com.doubtnutapp.ui.mockTest.MockTestSubscriptionActivity
import com.doubtnutapp.ui.pdfviewer.PdfViewerActivity
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 02/10/20.
 */
class CourseResourceChildWidget(context: Context) :
    BaseBindingWidget<CourseResourceChildWidget.WidgetHolder,
        CourseResourceChildWidget.CourseResourceChildWidgetModel, WidgetCourseResourceChildBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    companion object {
        const val TAG = "CourseResourceChildWidget"
        const val FUTURE = 0
        const val LIVE = 1
        const val PAST = 2
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun getViewBinding(): WidgetCourseResourceChildBinding {
        return WidgetCourseResourceChildBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: CourseResourceChildWidgetModel
    ): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val binding = holder.binding
        val item: CourseResourceChildWidgetData = model.data
        if (item.setWidth == true) {
            Utils.setWidthBasedOnPercentage(
                binding.root.context,
                holder.itemView,
                "1.8",
                R.dimen.spacing
            )
        }
        binding.textViewResourceTitle.text = item.resourceText
        binding.textViewTitleInfo.text = item.title
        binding.textViewFacultyInfo.text = item.title2

        when (item.lockState) {
            1 -> {
                binding.imageViewLock.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.ic_tag_light_locked
                    )
                )
            }
            2 -> {
                binding.imageViewLock.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.ic_tag_light_unlocked
                    )
                )
            }
            else -> {
                binding.imageViewLock.setImageDrawable(null)
            }
        }

        if (item.buttonState == "payment") {
            binding.layoutPaymentInfo.show()
            binding.textViewBottom.hide()

            binding.textViewAmountToPay.text = item.amountToPay.orEmpty()
            binding.textViewAmountStrikeThrough.text = item.amountStrikeThrough.orEmpty()
            binding.textViewDiscount.text = item.discount.orEmpty()
            binding.textViewBuy.text = item.buyText.orEmpty()
            binding.textViewBottom.text = ""
        } else {
            binding.layoutPaymentInfo.hide()
            binding.textViewBottom.show()

            binding.textViewAmountToPay.text = ""
            binding.textViewAmountStrikeThrough.text = ""
            binding.textViewDiscount.text = ""
            binding.textViewBuy.text = ""

            binding.textViewBottom.text = item.bottomText.orEmpty()
        }

        binding.textViewAmountStrikeThrough.paintFlags =
            binding.textViewAmountStrikeThrough.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        binding.root.setOnClickListener {
            actionPerformer?.performAction(
                OnCourseCarouselChildWidgetItmeClicked(
                    item.title.orEmpty(),
                    item.id.orEmpty(),
                    -1,
                    source.orEmpty()
                )
            )

            if (item.isPremium == true && item.isVip != true) {
                if (item.isOneTapPayment == true && item.variantId != null) {
                    performAction(OneTapBuy(item.variantId))
                } else {
                    deeplinkAction.performAction(binding.root.context, item.paymentDeeplink)
                }
            } else if (item.showEMIDialog == true) {
                val dialog =
                    EMIReminderDialog.newInstance(Integer.parseInt(item.assortmentId.orEmpty()))
                dialog.show(
                    (binding.root.context as AppCompatActivity).supportFragmentManager,
                    EMIReminderDialog.TAG
                )
                (binding.root.context as AppCompatActivity).supportFragmentManager.executePendingTransactions()
                dialog.dialog?.setOnDismissListener {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.EMI_REMINDER_CLOSE,
                            hashMapOf<String, Any>(
                                EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty()
                            )
                        )
                    )
                    dialog.dismiss()
                    loadPDF(binding, item, model)
                    actionPerformer?.performAction(RefreshUI())
                }
            } else {
                loadPDF(binding, item, model)
            }
        }

        return holder
    }

    private fun loadPDF(
        binding: WidgetCourseResourceChildBinding,
        item: CourseResourceChildWidgetData,
        model: CourseResourceChildWidgetModel
    ) {
        if (item.resourceType == "pdf") {
            try {
                if (!URLUtil.isValidUrl(item.pdfUrl)) {
                    showToast(
                        binding.root.context,
                        binding.root.context.resources.getString(R.string.notAvalidLink)
                    )
                } else {
                    if (item.pdfUrl.orEmpty().contains(".html")) {
                        val customTabsIntent = CustomTabsIntent.Builder().build()
                        CustomTabActivityHelper.openCustomTab(
                            binding.root.context,
                            customTabsIntent,
                            Uri.parse(item.pdfUrl.orEmpty()),
                            WebViewFallback()
                        )
                    } else {
                        PdfViewerActivity.previewPdfFromTheUrl(
                            binding.root.context,
                            item.pdfUrl.orEmpty()
                        )
                    }
                }
            } catch (error: ActivityNotFoundException) {
                showToast(
                    binding.root.context,
                    binding.root.context.resources.getString(R.string.donothaveanybrowser)
                )
            }
        } else if (item.resourceType == "test") {
            if (item.state == FUTURE) {
                showToast(
                    binding.root.context,
                    binding.root.context.getString(R.string.coming_soon)
                )
            } else {
                binding.root.context.startActivity(
                    MockTestSubscriptionActivity.getStartIntent(
                        binding.root.context,
                        item.testId?.toIntOrNull() ?: 0, false
                    )
                )
            }
        } else {
            deeplinkAction.performAction(binding.root.context, item.paymentDeeplink)
        }

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.EXPLORE_CAROUSEL +
                    "_" + EventConstants.WIDGET_ITEM_CLICK,
                hashMapOf<String, Any>(
                    EventConstants.EVENT_NAME_ID to item.id.orEmpty(),
                    EventConstants.TYPE to item.resourceType.orEmpty(),
                    EventConstants.WIDGET to TAG
                ).apply {
                    putAll(model.extraParams ?: HashMap())
                }
            )
        )
    }

    class WidgetHolder(binding: WidgetCourseResourceChildBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseResourceChildBinding>(binding, widget)

    class CourseResourceChildWidgetModel :
        WidgetEntityModel<CourseResourceChildWidgetData, WidgetAction>()

    @Keep
    data class CourseResourceChildWidgetData(
        @SerializedName("id") val id: String?,
        @SerializedName("assortment_id") val assortmentId: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("title2") val title2: String?,
        @SerializedName("resource_text") val resourceText: String?,
        @SerializedName("button_state") val buttonState: String?,
        @SerializedName("amount_to_pay") val amountToPay: String?,
        @SerializedName("amount_strike_through") val amountStrikeThrough: String?,
        @SerializedName("discount") val discount: String?,
        @SerializedName("buy_text") val buyText: String?,
        @SerializedName("bottom_text") val bottomText: String?,
        @SerializedName("pdf_url") val pdfUrl: String?,
        @SerializedName("resource_type") val resourceType: String?,
        @SerializedName("test_id") val testId: String?,
        @SerializedName("state") val state: Int?,
        @SerializedName("is_premium") val isPremium: Boolean?,
        @SerializedName("is_vip") val isVip: Boolean?,
        @SerializedName("payment_deeplink") val paymentDeeplink: String?,
        @SerializedName("lock_state") val lockState: Int?,
        @SerializedName("set_width") val setWidth: Boolean?,
        @SerializedName("show_emi_dialog") val showEMIDialog: Boolean?,
        @SerializedName("is_onetap_payment") val isOneTapPayment: Boolean?,
        @SerializedName("variant_id") val variantId: String?,
    ) : WidgetData()
}
