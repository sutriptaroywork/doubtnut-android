package com.doubtnutapp.course.widgets

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.webkit.URLUtil
import androidx.annotation.Keep
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetHomeWorkBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.ui.browser.CustomTabActivityHelper
import com.doubtnutapp.ui.browser.WebViewFallback
import com.doubtnutapp.ui.pdfviewer.PdfViewerActivity
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class HomeWorkWidget(context: Context) : BaseBindingWidget<HomeWorkWidget.WidgetHolder,
    HomeWorkWidgetModel, WidgetHomeWorkBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: HomeWorkWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val data = model.data
        val binding = holder.binding
        binding.tvLectureName.text = data.title.orEmpty()
        binding.lectureDescription.text = data.subtitle.orEmpty()
        binding.pdfButton.setVisibleState(data.status == 0 && !data.pdfUrl.isNullOrEmpty())
        binding.tickIv.loadImageEtx(data.checkImageUrl.orEmpty())
        binding.statusTv.text = data.statusMessage.orEmpty()
        binding.statusTv.setTextColor(Utils.parseColor(data.color))
        binding.viewResultButton.text = data.button?.title
        binding.ivBackground.loadImageEtx(data.bgImageUrl.orEmpty())
        binding.viewResultButton.setOnClickListener {
            deeplinkAction.performAction(context, data.button?.deeplink.orEmpty())
        }
        binding.pdfButton.setOnClickListener {
            checkPDFUrl(holder, data.pdfUrl.orEmpty())
        }
        return holder
    }

    private fun checkPDFUrl(holder: RecyclerView.ViewHolder, url: String) {
        try {
            if (!URLUtil.isValidUrl(url)) {
                showToast(holder.itemView.context, holder.itemView.context.resources.getString(R.string.notAvalidLink))
            } else {
                if (url.contains(".html")) {
                    val customTabsIntent = CustomTabsIntent.Builder().build()
                    CustomTabActivityHelper.openCustomTab(
                        holder.itemView.context,
                        customTabsIntent,
                        Uri.parse(url),
                        WebViewFallback()
                    )
                } else {
                    PdfViewerActivity.previewPdfFromTheUrl(holder.itemView.context, url)
                }
            }
        } catch (error: ActivityNotFoundException) {
            showToast(holder.itemView.context, holder.itemView.context.resources.getString(R.string.donothaveanybrowser))
        }
    }

    class WidgetHolder(binding: WidgetHomeWorkBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetHomeWorkBinding>(binding, widget)

    override fun getViewBinding(): WidgetHomeWorkBinding {
        return WidgetHomeWorkBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

class HomeWorkWidgetModel : WidgetEntityModel<HomeWorkWidgetData, WidgetAction>()

@Keep
data class HomeWorkWidgetData(
    @SerializedName("title1") val title: String?,
    @SerializedName("title2") val subtitle: String?,
    @SerializedName("status") val status: Int?,
    @SerializedName("status_message") val statusMessage: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("pdf_button") val pdfButtonText: String?,
    @SerializedName("image_url") val bgImageUrl: String?,
    @SerializedName("pdf_url") val pdfUrl: String?,
    @SerializedName("button") val button: Button?,
    @SerializedName("tick_image_url") val checkImageUrl: String?
) : WidgetData() {
    @Keep
    data class Button(
        @SerializedName("title") val title: String?,
        @SerializedName("deeplink") val deeplink: String?
    )
}
