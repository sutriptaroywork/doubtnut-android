package com.doubtnutapp.course.widgets

import android.content.ActivityNotFoundException
import android.content.Context
import android.graphics.Paint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnCourseCarouselChildWidgetItmeClicked
import com.doubtnutapp.base.OneTapBuy
import com.doubtnutapp.base.RefreshUI
import com.doubtnutapp.databinding.ItemCourseResourcesBinding
import com.doubtnutapp.databinding.WidgetCourseResourcesBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.ui.browser.CustomTabActivityHelper
import com.doubtnutapp.ui.browser.WebViewFallback
import com.doubtnutapp.ui.pdfviewer.PdfViewerActivity
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseResourceWidget(context: Context) :
    BaseBindingWidget<CourseResourceWidget.WidgetViewHolder,
        CourseResourceWidget.CourseResourceWidgetModel, WidgetCourseResourcesBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    companion object {
        const val TAG = "CourseResourceWidget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun getViewBinding(): WidgetCourseResourcesBinding {
        return WidgetCourseResourcesBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun bindWidget(
        holder: WidgetViewHolder,
        model: CourseResourceWidgetModel
    ): WidgetViewHolder {
        super.bindWidget(holder, model)
        val data: CourseResourceWidgetData = model.data
        val binding = holder.binding
        with(binding) {
            tvTitle.text = data.title.orEmpty()
            tvSeeAll.text = data.linkText.orEmpty()
            tvSeeAll.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.COURSE_RESOURCE_CTA_CLICKED,
                        hashMapOf<String, Any>(EventConstants.CTA_TITLE to data.linkText.orEmpty()).apply {
                            putAll(model.extraParams ?: hashMapOf())
                        }
                    )
                )
                deeplinkAction.performAction(context, data.deeplink.orEmpty())
            }
            if (data.scrollDirection == "grid") {
                rvResource.layoutManager = GridLayoutManager(context, 2)
            } else {
                rvResource.layoutManager = LinearLayoutManager(
                    context,
                    RecyclerView.HORIZONTAL, false
                )
            }
            rvResource.adapter = Adapter(
                data.items.orEmpty(),
                actionPerformer,
                analyticsPublisher,
                deeplinkAction
            )
            if(data.bottomRightText.isNotNullAndNotEmpty()){
                tvViewAll.visibility = View.VISIBLE
                tvViewAll.apply {
                    text = data.bottomRightText.orEmpty()
                    textSize = data.bottomRightTextSize ?: 12f
                    setTextColor(Utils.parseColor(data.bottomRightTextColor))
                    setOnClickListener {
                        deeplinkAction.performAction(context, data.bottomRightDeeplink.orEmpty())
                    }
                }
            } else {
                tvViewAll.visibility = View.GONE
            }

            if (data.bgColor.isNotNullAndNotEmpty()) {
                constraintLayout.setBackgroundColor(Utils.parseColor(data.bgColor))
            }
        }


        return holder
    }

    class Adapter(
        val items: List<CourseResourceWidgetItems>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val extraParams: HashMap<String, Any>? = null
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        class ViewHolder(val binding: ItemCourseResourcesBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemCourseResourcesBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding
            val item = items[position]
            with(binding) {
                Utils.setWidthBasedOnPercentage(
                    binding.root.context,
                    binding.root,
                    "2.25",
                    R.dimen.spacing
                )
                textViewResourceTitle.text = item.resourceText.orEmpty()
                textViewTitleInfo.text = item.title.orEmpty()
                textViewFacultyInfo.text = item.title2.orEmpty()
                textViewAmountToPay.text = item.amountToPay.orEmpty()
                textViewBuy.text = item.buyText.orEmpty()
                textViewAmountStrikeThrough.text = item.amountStrikeThrough.orEmpty()
                textViewAmountStrikeThrough.paintFlags =
                    textViewAmountStrikeThrough.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                root.setOnClickListener {
                    actionPerformer?.performAction(
                        OnCourseCarouselChildWidgetItmeClicked(
                            item.title.orEmpty(),
                            item.id.orEmpty(),
                            -1,
                            ""
                        )
                    )
                    if (item.isPremium == true && item.isVip != true) {
                        if (item.isOneTapPayment == true && item.variantId != null) {
                            actionPerformer?.performAction(OneTapBuy(item.variantId))
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
                            loadPDF(binding, item, extraParams)
                            actionPerformer?.performAction(RefreshUI())
                        }
                    } else {
                        loadPDF(binding, item, extraParams)
                    }
                }
            }
        }

        private fun loadPDF(
            binding: ItemCourseResourcesBinding,
            item: CourseResourceWidgetItems,
            extraParams: HashMap<String, Any>?
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
            } else {
                deeplinkAction.performAction(binding.root.context, item.paymentDeeplink)
            }
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.COURSE_RESOURCE_CTA_CLICKED,
                    hashMapOf<String, Any>(
                        EventConstants.CTA_TITLE to item.title.orEmpty(),
                        EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty()
                    ).apply {
                        putAll(extraParams ?: hashMapOf())
                    }
                )
            )
        }

        override fun getItemCount(): Int {
            return items.size
        }
    }

    class WidgetViewHolder(binding: WidgetCourseResourcesBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseResourcesBinding>(binding, widget)

    class CourseResourceWidgetModel : WidgetEntityModel<CourseResourceWidgetData, WidgetAction>()

    @Keep
    data class CourseResourceWidgetData(
        @SerializedName("title") val title: String?,
        @SerializedName("link") val linkText: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("scroll_direction") val scrollDirection: String?,
        @SerializedName("bg_color") val bgColor: String?,
        @SerializedName("bottom_right_text") val bottomRightText: String?,
        @SerializedName("bottom_right_text_color") val bottomRightTextColor: String?,
        @SerializedName("bottom_right_text_size") val bottomRightTextSize: Float?,
        @SerializedName("bottom_right_deeplink") val bottomRightDeeplink: String?,
        @SerializedName("items") val items: List<CourseResourceWidgetItems>?,
    ) : WidgetData()

    @Keep
    data class CourseResourceWidgetItems(
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
        @SerializedName("pdf_url") val pdfUrl: String?,
        @SerializedName("resource_type") val resourceType: String?,
        @SerializedName("is_premium") val isPremium: Boolean?,
        @SerializedName("is_vip") val isVip: Boolean?,
        @SerializedName("payment_deeplink") val paymentDeeplink: String?,
        @SerializedName("show_emi_dialog") val showEMIDialog: Boolean?,
        @SerializedName("is_onetap_payment") val isOneTapPayment: Boolean?,
        @SerializedName("variant_id") val variantId: String?
    ) : WidgetData()
}
