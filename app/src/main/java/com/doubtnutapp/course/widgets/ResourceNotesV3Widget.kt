package com.doubtnutapp.course.widgets

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.annotation.Keep
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.ItemResourceNotesV3Binding
import com.doubtnutapp.databinding.WidgetResourceNotesV3Binding
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

class ResourceNotesV3Widget(context: Context) :
    BaseBindingWidget<ResourceNotesV3Widget.WidgetHolder,
            ResourceNotesV3WidgetModel, WidgetResourceNotesV3Binding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var source: String? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetResourceNotesV3Binding {
        return WidgetResourceNotesV3Binding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: ResourceNotesV3WidgetModel): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
        })
        val data: ResourceNotesV3WidgetData = model.data
        val binding = holder.binding
        binding.tvMainTitle.text = data.title.orEmpty()
        if (data.imageUrl.isNotNullAndNotEmpty()) {
            binding.imageView.loadImageEtx(data.imageUrl)
        }

        binding.tvMainTitle2.isVisible = data.titleTwo.isNullOrBlank().not()
        binding.tvMainTitle2.text = data.titleTwo.orEmpty()

        binding.recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.adapter =
            ResourceNotesV3WidgetAdapter(
                data.items.orEmpty(),
                deeplinkAction,
                model.extraParams,
                model.type,
                analyticsPublisher
            )
        return holder
    }

    class ResourceNotesV3WidgetAdapter(
        val items: List<ResourceNotesV3Item>,
        val deeplinkAction: DeeplinkAction,
        val extraParams: HashMap<String, Any>?,
        val type: String,
        val analyticsPublisher: AnalyticsPublisher
    ) : RecyclerView.Adapter<ResourceNotesV3WidgetAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemResourceNotesV3Binding
                    .inflate(LayoutInflater.from(parent.context), parent, false)

            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Utils.setWidthBasedOnPercentage(
                holder.itemView.context,
                holder.itemView,
                "1.1",
                R.dimen.spacing
            )

            val data = items[position]
            holder.binding.tvTitle.text = data.title.orEmpty()
            holder.binding.tvSubTitle.text = data.text1.orEmpty()
            holder.binding.tvDesc.text = data.text2.orEmpty()
            holder.binding.tvTitle.setTextColor(Utils.parseColor(data.color, R.color.grey))
            holder.binding.sideBar.setBackgroundColor(Utils.parseColor(data.color))
            holder.binding.root.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        type + "_" + EventConstants.CLICKED,
                        hashMapOf<String, Any>().apply {
                            putAll(extraParams.orEmpty())
                        }
                    )
                )
                if (!data.deeplink.isNullOrBlank()) {
                    deeplinkAction.performAction(holder.itemView.context, data.deeplink)
                } else if (!data.pdfUrl.isNullOrBlank()) {
                    checkPDFUrl(holder.itemView.context, data.pdfUrl, data.id.orEmpty())
                }
            }

        }

        private fun checkPDFUrl(context: Context, pdfUrl: String, id: String) {
            try {
                if (!URLUtil.isValidUrl(pdfUrl)) {
                    showToast(context, context.resources.getString(R.string.notAvalidLink))
                } else {
                    var isDownloaded = 1
                    if (pdfUrl.contains(".html")) {
                        isDownloaded = 0
                        val customTabsIntent = CustomTabsIntent.Builder().build()
                        CustomTabActivityHelper.openCustomTab(
                            context,
                            customTabsIntent,
                            Uri.parse(pdfUrl),
                            WebViewFallback()
                        )
                    } else {
                        PdfViewerActivity.previewPdfFromTheUrl(context, pdfUrl)
                    }
                    markNotesRead(id, isDownloaded)
                }
            } catch (error: ActivityNotFoundException) {
                showToast(context, context.resources.getString(R.string.donothaveanybrowser))
            }
        }

        private fun markNotesRead(resourceId: String?, isDownloaded: Int) {
            DataHandler.INSTANCE.courseRepository.markNotesRead(resourceId.orEmpty(), isDownloaded)
                .applyIoToMainSchedulerOnCompletable()
                .subscribeToCompletable({})
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemResourceNotesV3Binding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(binding: WidgetResourceNotesV3Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetResourceNotesV3Binding>(binding, widget)
}


class ResourceNotesV3WidgetModel : WidgetEntityModel<ResourceNotesV3WidgetData, WidgetAction>()

@Keep
data class ResourceNotesV3WidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("title2") val titleTwo: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("items") val items: List<ResourceNotesV3Item>?
) : WidgetData()

@Keep
data class ResourceNotesV3Item(
    @SerializedName("title") val title: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("text1") val text1: String?,
    @SerializedName("text2") val text2: String?,
    @SerializedName("pdf_url") val pdfUrl: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("id") val id: String?
)