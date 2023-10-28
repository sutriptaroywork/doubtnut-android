package com.doubtnutapp.widgetmanager.widgets

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.webkit.URLUtil
import androidx.annotation.Keep
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.SgChildWidgetLongClick
import com.doubtnutapp.databinding.WidgetPdfViewBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.studygroup.ui.fragment.SgChatFragment
import com.doubtnutapp.studygroup.ui.fragment.SgPersonalChatFragment
import com.doubtnutapp.ui.browser.CustomTabActivityHelper
import com.doubtnutapp.ui.browser.WebViewFallback
import com.doubtnutapp.ui.pdfviewer.PdfViewerActivity
import com.doubtnutapp.utils.showToast
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class PdfViewWidget(context: Context) : BaseBindingWidget<
        PdfViewWidget.WidgetHolder,
        PdfViewWidget.Model,
        WidgetPdfViewBinding>(context) {

    companion object {
        private const val TAG = "PdfViewWidget"
        private const val PDF_VIEW_WIDGET_CLICK = "pdf_view_widget_click"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var source: String? = null

    init {
        DoubtnutApp.INSTANCE
            .daggerAppComponent
            .forceUnWrap()
            .inject(this)
    }

    override fun getViewBinding(): WidgetPdfViewBinding =
        WidgetPdfViewBinding.inflate(LayoutInflater.from(context), this, true)

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        model.layoutConfig = WidgetLayoutConfig(
            marginTop = 0,
            marginBottom = 0,
        )
        super.bindWidget(holder, model)

        val data = model.data
        val binding = holder.binding

        holder.itemView.apply {
            binding.tvPdfTitle.text = data.title
            binding.ivEnd.isVisible = data.showForwardArrow == true
            if (source == SgChatFragment.STUDY_GROUP || source == SgPersonalChatFragment.SOURCE_PERSONAL_CHAT) {

                binding.rootLayout.updatePadding(
                    left = 4.dpToPx(),
                    top = 8.dpToPx(),
                    right = 0.dpToPx(),
                    bottom = 0.dpToPx()
                )

                val lastTouchDownXY = FloatArray(2)
                setOnTouchListener(OnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        lastTouchDownXY[0] = event.x
                        lastTouchDownXY[1] = event.y - holder.itemView.height
                    }
                    return@OnTouchListener false
                })

                setOnLongClickListener {
                    actionPerformer?.performAction(
                        SgChildWidgetLongClick(
                            model.type,
                            lastTouchDownXY
                        )
                    )
                    true
                }
            } else {
                binding.rootLayout.updatePadding(
                    left = 4.dpToPx(),
                    top = 8.dpToPx(),
                    right = 4.dpToPx(),
                    bottom = 8.dpToPx()
                )
            }

            binding.ivPdf.apply {
                isVisible = !(data.hideIcon == true)
                data.imageUrl?.let {
                    loadImage(it)
                }
            }

            setOnClickListener {
                if (data.deeplink.isNotNullAndNotEmpty()) {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            PDF_VIEW_WIDGET_CLICK,
                            hashMapOf<String, Any>(
                                EventConstants.CLICKED_ITEM_ID to data.id.orEmpty(),
                                EventConstants.WIDGET to TAG,
                                EventConstants.ITEM_POSITION to widgetViewHolder.absoluteAdapterPosition,
                                EventConstants.SOURCE to source.orEmpty(),
                                EventConstants.WIDGET_TITLE to data.title
                            ).apply {
                                putAll(model.extraParams ?: HashMap())
                            }
                        )
                    )
                    deeplinkAction.performAction(context, data.deeplink)
                } else {
                    try {
                        if (!URLUtil.isValidUrl(data.link)) {
                            showToast(
                                holder.itemView.context,
                                holder.itemView.context.resources.getString(R.string.notAvalidLink)
                            )
                        } else {
                            if (data.link.contains(".html")) {
                                val customTabsIntent = CustomTabsIntent.Builder().build()
                                CustomTabActivityHelper.openCustomTab(
                                    holder.itemView.context,
                                    customTabsIntent,
                                    Uri.parse(data.link),
                                    WebViewFallback()
                                )
                            } else {
                                PdfViewerActivity.previewPdfFromTheUrl(
                                    holder.itemView.context,
                                    data.link
                                )
                            }
                        }
                    } catch (error: ActivityNotFoundException) {
                        showToast(
                            holder.itemView.context,
                            holder.itemView.context.resources.getString(R.string.donothaveanybrowser)
                        )
                    }
                }
            }
        }

        return holder
    }

    class WidgetHolder(binding: WidgetPdfViewBinding, baseWidget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetPdfViewBinding>(binding, baseWidget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String?,
        @SerializedName("title") val title: String,
        @SerializedName("link") val link: String,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("show_forward_arrow") val showForwardArrow: Boolean?,
        @SerializedName("hide_icon") val hideIcon: Boolean? = false,
    ) : WidgetData()
}