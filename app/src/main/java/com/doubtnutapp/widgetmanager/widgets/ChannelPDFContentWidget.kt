package com.doubtnutapp.widgetmanager.widgets

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.annotation.Keep
import androidx.browser.customtabs.CustomTabsIntent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx

import com.doubtnutapp.base.DownloadPDF
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.ItemChannelPdfContentBinding
import com.doubtnutapp.databinding.WidgetChannelContentBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.ui.browser.CustomTabActivityHelper
import com.doubtnutapp.ui.browser.WebViewFallback
import com.doubtnutapp.ui.pdfviewer.PdfViewerActivity
import com.doubtnutapp.utils.showToast
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ChannelPDFContentWidget(
    context: Context
) : BaseBindingWidget<ChannelPDFContentWidget.WidgetHolder,
        ChannelPDFContentWidget.Model,
        WidgetChannelContentBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var source: String? = ""

    override fun getViewBinding(): WidgetChannelContentBinding {
        return WidgetChannelContentBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: Model
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        if (model.data.items.size == 1) {
            model.data.listOrientation = Constants.ORIENTATION_TYPE_HORIZONTAL_LIST
        }

        binding.rvChannelContent.setLayoutOrientation(
            holder.itemView.context,
            model.data.listOrientation ?: Constants.ORIENTATION_TYPE_GRID, 2
        )

        if (model.data.listOrientation != Constants.ORIENTATION_TYPE_VERTICAL_LIST) {
            val padding16Dp = 16.dpToPx()
            binding.rvChannelContent.setPadding(padding16Dp, padding16Dp, padding16Dp, padding16Dp)
        }

        binding.rvChannelContent.adapter =
            Adapter(
                model.data.items,
                model.data.listOrientation,
                analyticsPublisher,
                actionPerformer,
                deeplinkAction,
                source.orEmpty()
            )
        return holder
    }

    class WidgetHolder(
        binding: WidgetChannelContentBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetChannelContentBinding>(binding, widget)

    class Model : WidgetEntityModel<WidgetChannelPDFData, WidgetAction>()

    @Keep
    data class WidgetChannelPDFData(
        @SerializedName("title") val title: String,
        @SerializedName("items") val items: ArrayList<ChannelPDFContentData>,
        @SerializedName("list_orientation") var listOrientation: Int?
    ) :
        WidgetData()

    @Keep
    data class ChannelPDFContentData(
        @SerializedName("teacher_id") val teacherId: String?,
        @SerializedName("title1") val title: String?,
        @SerializedName("title2") val subTitle: String?,
        @SerializedName("chapter") val chapter: String?,
        @SerializedName("subject") val subject: String?,
        @SerializedName("category") val category: String?,
        @SerializedName("description") val description: String?,
        @SerializedName("button_text") val ctaText: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("pdf_url") val pdfUrl: String?,
        @SerializedName("deeplink") val deeplink: String,
        @SerializedName("question_id") val questionId: String?,
        @SerializedName("course_resource_id") val resourceId: String?,
        @SerializedName("type") val type: String?,
        @SerializedName("card_width") val cardWidth: String?,
        @SerializedName("card_ratio") val cardRatio: String?
    )

    class Adapter(
        private var items: List<ChannelPDFContentData>,
        private val listOrientation: Int?,
        private val analyticsPublisher: AnalyticsPublisher,
        private val actionPerformer: ActionPerformer?,
        private val deeplinkAction: DeeplinkAction,
        private val source: String
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemChannelPdfContentBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), analyticsPublisher, actionPerformer, deeplinkAction, source
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]
            if (listOrientation == Constants.ORIENTATION_TYPE_HORIZONTAL_LIST) {
                holder.itemView.setWidthFromScrollSize("match_parent")
            }

            holder.binding.contentLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
                if (listOrientation != Constants.ORIENTATION_TYPE_GRID) {
                    dimensionRatio = "16:9"
                }
            }

            holder.bind(data)
        }

        override fun getItemCount(): Int = items.size

        fun updateItems(items: List<ChannelPDFContentData>) {
            this.items = items
            notifyDataSetChanged()
        }

        class ViewHolder(
            val binding: ItemChannelPdfContentBinding,
            val analyticsPublisher: AnalyticsPublisher,
            private val actionPerformer: ActionPerformer?,
            val deeplinkAction: DeeplinkAction,
            val source: String
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(data: ChannelPDFContentData) {
                binding.apply {
                    root.setOnClickListener {
                        checkPDFUrl(itemView.context, data)
                        val eventParams = hashMapOf<String, Any>()
                        eventParams[Constants.SOURCE] = source
                        eventParams[EventConstants.RESOURCE_ID] = data.resourceId.orEmpty()
                        eventParams[Constants.QUESTION_ID] = data.questionId.orEmpty()
                        eventParams[Constants.RESOURCE_TYPE] = Constants.PDF
                        eventParams[Constants.TEACHER_ID] = data.teacherId.orEmpty()
                        analyticsPublisher?.publishEvent(
                            AnalyticsEvent(
                                EventConstants.TEACHER_PAGE_RESOURCE_CLICKED,
                                eventParams
                            )
                        )
                    }

                    tvTitle.isVisible = !data.title.isNullOrEmpty()
                    tvTitle.text = data.title

                    tvChapter.isVisible = !data.chapter.isNullOrEmpty()
                    tvChapter.text = data.chapter

                    tvDescription.isVisible = !data.subTitle.isNullOrEmpty()
                    tvDescription.text = data.subTitle

                    tvButtonText.text = data.ctaText
                        ?: if (data.pdfUrl.isNullOrEmpty()) "View" else "Download"

                    if (!data.deeplink.isNullOrEmpty()) {
                        ivDownload.hide()
                    } else {
                        ivDownload.show()
                    }

                    button.setOnClickListener {
                        if (data.deeplink.isNullOrEmpty()) {
                            actionPerformer?.performAction(DownloadPDF(data.pdfUrl.orEmpty()))
                        } else {
                            checkPDFUrl(itemView.context, data)
                        }
                    }

                }

            }

            private fun checkPDFUrl(context: Context, data: ChannelPDFContentData) {
                try {
                    if (!URLUtil.isValidUrl(data.pdfUrl) || data.pdfUrl.isNullOrEmpty()) {
                        showToast(
                            context,
                            context.resources.getString(R.string.notAvalidLink)
                        )
                    } else {
                        var isDownloaded = 1
                        if (data.pdfUrl.contains(".html")) {
                            isDownloaded = 0
                            val customTabsIntent = CustomTabsIntent.Builder().build()
                            CustomTabActivityHelper.openCustomTab(
                                context,
                                customTabsIntent,
                                Uri.parse(data.pdfUrl),
                                WebViewFallback()
                            )
                        } else {
                            PdfViewerActivity.previewPdfFromTheUrl(
                                context,
                                data.pdfUrl.orEmpty()
                            )
                        }
                        markNotesRead(data.resourceId, isDownloaded)
                    }
                } catch (error: ActivityNotFoundException) {
                    showToast(
                        context, context.resources.getString(R.string.donothaveanybrowser)
                    )
                }
            }

            private fun markNotesRead(resourceId: String?, isDownloaded: Int) {
                DataHandler.INSTANCE.courseRepository.markNotesRead(
                    resourceId.orEmpty(),
                    isDownloaded
                )
                    .applyIoToMainSchedulerOnCompletable()
                    .subscribeToCompletable({})
            }
        }
    }
}
