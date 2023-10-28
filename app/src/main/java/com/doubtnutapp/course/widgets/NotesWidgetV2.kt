package com.doubtnutapp.course.widgets

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.setMargins

import com.doubtnutapp.base.RefreshUI
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.NotesWidgetData
import com.doubtnutapp.data.remote.models.NotesWidgetItem
import com.doubtnutapp.data.remote.models.NotesWidgetModel
import com.doubtnutapp.databinding.ItemNotesV2Binding
import com.doubtnutapp.databinding.WidgetNotesV2Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.ui.browser.CustomTabActivityHelper
import com.doubtnutapp.ui.browser.WebViewFallback
import com.doubtnutapp.ui.pdfviewer.PdfViewerActivity
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import javax.inject.Inject

class NotesWidgetV2(context: Context) :
    BaseBindingWidget<NotesWidgetV2.WidgetViewHolder,
        NotesWidgetModel, WidgetNotesV2Binding>(context) {

    companion object {
        const val TAG = "NotesWidgetV2"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetViewHolder, model: NotesWidgetModel): WidgetViewHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(12, 12, 16, 16)
            }
        )
        val binding = holder.binding
        val data: NotesWidgetData = model.data
        if (data.items.isNullOrEmpty()) {
            widgetViewHolder.itemView.visibility = View.GONE
            return holder
        }
        widgetViewHolder.itemView.setVisibleState(!data.items.isNullOrEmpty())
        if (!model.data.title.isNullOrEmpty()) {
            binding.titleTv.text = model.data.title
            binding.titleTv.visibility = View.VISIBLE
        } else {
            binding.titleTv.visibility = View.GONE
        }
        val setWidth: Boolean
        when (model.data.scrollDirection) {
            "grid" -> {
                setWidth = false
                binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
            }
            "horizontal" -> {
                setWidth = true
                binding.recyclerView.layoutManager = LinearLayoutManager(
                    context,
                    RecyclerView.HORIZONTAL, false
                )
            }
            else -> {
                setWidth = false
                binding.recyclerView.layoutManager = LinearLayoutManager(
                    context,
                    RecyclerView.VERTICAL, false
                )
            }
        }

        binding.recyclerView.adapter = NotesAdapter(
            data.items.orEmpty(),
            actionPerformer,
            analyticsPublisher, deeplinkAction,
            model.extraParams ?: HashMap(),
            setWidth
        )
        return holder
    }

    class NotesAdapter(
        val items: List<NotesWidgetItem>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val extraParams: HashMap<String, Any>,
        val setWidth: Boolean,
    ) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemNotesV2Binding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]
            val binding = holder.binding
            if (setWidth && items.isNotEmpty()) {
                Utils.setWidthBasedOnPercentage(
                    holder.itemView.context, holder.itemView, "1.75", R.dimen.spacing_zero
                )
            }

            binding.tvPDFHeading.text = data.title.orEmpty()
            binding.tvNotesText.text = data.text.orEmpty()
            binding.layoutPDF.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        TAG + EventConstants.EVENT_ITEM_CLICK,
                        hashMapOf<String, Any>(
                            EventConstants.EVENT_NAME_ID to items[position].id.orEmpty(),
                            EventConstants.SOURCE_ID to items[position].link,
                            EventConstants.WIDGET to TAG
                        ).apply {
                            putAll(extraParams)
                        }
                    )
                )

                if (data.isPremium && !data.isVip) {
                    deeplinkAction.performAction(holder.itemView.context, data.paymentDeeplink)
                } else if (data.showEMIDialog == true) {
                    val dialog = EMIReminderDialog.newInstance(Integer.parseInt(data.assortmentId.orEmpty()))
                    dialog.show((holder.itemView.context as AppCompatActivity).supportFragmentManager, EMIReminderDialog.TAG)
                    (holder.itemView.context as AppCompatActivity).supportFragmentManager.executePendingTransactions()
                    dialog.dialog?.setOnDismissListener {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.EMI_REMINDER_CLOSE,
                                hashMapOf<String, Any>(
                                    EventConstants.ASSORTMENT_ID to data.assortmentId.orEmpty()
                                )
                            )
                        )
                        dialog.dismiss()
                        checkPDFUrl(holder, data)
                        actionPerformer?.performAction(RefreshUI())
                    }
                } else {
                    checkPDFUrl(holder, data)
                }
            }
            if (items.size > 1) {
                binding.notesCard.setMargins(
                    Utils.convertDpToPixel(4f).toInt(),
                    Utils.convertDpToPixel(12f).toInt(),
                    Utils.convertDpToPixel(4f).toInt(),
                    Utils.convertDpToPixel(12f).toInt()
                )
            } else {
                binding.notesCard.setMargins(
                    0,
                    0,
                    0,
                    0
                )
            }

            binding.imageViewPDF.loadImageEtx(data.imageUrl.orEmpty())
        }

        private fun checkPDFUrl(holder: RecyclerView.ViewHolder, data: NotesWidgetItem) {
            try {
                if (!URLUtil.isValidUrl(data.link)) {
                    showToast(holder.itemView.context, holder.itemView.context.resources.getString(R.string.notAvalidLink))
                } else {
                    var isDownloaded = 1
                    if (data.link.contains(".html")) {
                        isDownloaded = 0
                        val customTabsIntent = CustomTabsIntent.Builder().build()
                        CustomTabActivityHelper.openCustomTab(
                            holder.itemView.context,
                            customTabsIntent,
                            Uri.parse(data.link),
                            WebViewFallback()
                        )
                    } else {
                        PdfViewerActivity.previewPdfFromTheUrl(holder.itemView.context, data.link)
                    }
                    markNotesRead(data.id, isDownloaded)
                }
            } catch (error: ActivityNotFoundException) {
                showToast(holder.itemView.context, holder.itemView.context.resources.getString(R.string.donothaveanybrowser))
            }
        }

        private fun markNotesRead(resourceId: String?, isDownloaded: Int) {
            DataHandler.INSTANCE.courseRepository.markNotesRead(resourceId.orEmpty(), isDownloaded)
                .applyIoToMainSchedulerOnCompletable()
                .subscribeToCompletable({})
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemNotesV2Binding) : RecyclerView.ViewHolder(binding.root)
    }

    class WidgetViewHolder(
        binding: WidgetNotesV2Binding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetNotesV2Binding>(binding, widget)

    override fun getViewBinding(): WidgetNotesV2Binding {
        return WidgetNotesV2Binding.inflate(LayoutInflater.from(context), this, true)
    }
}
