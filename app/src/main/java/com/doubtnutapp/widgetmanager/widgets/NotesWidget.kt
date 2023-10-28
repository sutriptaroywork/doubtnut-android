package com.doubtnutapp.widgetmanager.widgets

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.setMargins
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnNotesClicked
import com.doubtnutapp.base.RefreshUI
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.NotesWidgetData
import com.doubtnutapp.data.remote.models.NotesWidgetItem
import com.doubtnutapp.data.remote.models.NotesWidgetModel
import com.doubtnutapp.databinding.ItemNotesBinding
import com.doubtnutapp.databinding.WidgetNotesBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.ui.browser.CustomTabActivityHelper
import com.doubtnutapp.ui.browser.WebViewFallback
import com.doubtnutapp.ui.pdfviewer.PdfViewerActivity
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class NotesWidget(context: Context) : BaseBindingWidget<NotesWidget.NotesWidgetViewHolder,
        NotesWidgetModel, WidgetNotesBinding>(context) {

    companion object {
        const val TAG = "NotesWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var adapter: WidgetLayoutAdapter? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = NotesWidgetViewHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: NotesWidgetViewHolder,
        model: NotesWidgetModel
    ): NotesWidgetViewHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = WidgetLayoutConfig(
                0,
                Utils.convertDpToPixel(12f).toInt(),
                16,
                16
            )
        })
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
            items = data.items,
            actionPerformer = actionPerformer,
            analyticsPublisher = analyticsPublisher,
            deeplinkAction = deeplinkAction,
            extraParams = model.extraParams ?: HashMap(),
            setWidth = setWidth
        ) {
            if (model.data.items.isNullOrEmpty()) {
                adapter?.removeWidget(model)
            }
        }
        return holder
    }

    class NotesAdapter(
        val items: ArrayList<NotesWidgetItem>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val extraParams: HashMap<String, Any>,
        val setWidth: Boolean,
        private val onBookmarkRemoved: () -> Unit = {}
    ) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemNotesBinding.inflate(
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
                            EventConstants.WIDGET to TAG,
                        ).apply {
                            putAll(extraParams)
                        }
                    )
                )


                if (data.isPremium && !data.isVip) {
                    deeplinkAction.performAction(holder.itemView.context, data.paymentDeeplink)

                } else if (data.showEMIDialog == true) {
                    val dialog =
                        EMIReminderDialog.newInstance(Integer.parseInt(data.assortmentId.orEmpty()))
                    dialog.show(
                        (holder.itemView.context as AppCompatActivity).supportFragmentManager,
                        EMIReminderDialog.TAG
                    )
                    (holder.itemView.context as AppCompatActivity).supportFragmentManager.executePendingTransactions()
                    dialog.dialog?.setOnDismissListener {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.EMI_REMINDER_CLOSE,
                                hashMapOf(
                                    EventConstants.ASSORTMENT_ID to data.assortmentId.orEmpty()
                                )
                            )
                        )
                        dialog.dismiss()
                        checkPDFUrl(holder, data)
                        actionPerformer?.performAction(RefreshUI())
                    }
                } else if (items[position].isVideoPage == true) {
                    actionPerformer?.performAction(OnNotesClicked(data.link, data.id, data.iconUrl))
                } else {
                    checkPDFUrl(holder, data)
                }
            }
            binding.notesCard.strokeColor = Utils.parseColor(data.borderColor.orEmpty())
            binding.tvPDFHeading.setTextColor(Utils.parseColor(data.borderColor.orEmpty()))
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
            if (data.isBookmarked == true) {
                binding.ivBookMark.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.ic_bookmark
                    )
                )
            }
            binding.ivBookMark.isVisible = data.isBookmarked != null
            setIsSelected(binding.ivBookMark, data.isBookmarked ?: false)
            binding.ivBookMark.setOnClickListener {
                if (data.isBookmarked == true) {
                    AlertDialog.Builder(binding.root.context).apply {
                        setMessage(binding.root.context.getString(R.string.question_remove_bookmark))
                        setPositiveButton(
                            context.getString(R.string.string_yes)
                        ) { dialog, _ ->
                            dialog.dismiss()
                            toggleBookmark(data, context, binding)
                        }
                        setNegativeButton(
                            context.getString(R.string.string_no)
                        ) { dialog, _ ->
                            dialog.dismiss()
                        }
                        show()
                    }
                } else {
                    toggleBookmark(data, binding.root.context, binding)
                }
            }
        }

        private fun toggleBookmark(
            data: NotesWidgetItem,
            context: Context,
            binding: ItemNotesBinding
        ) {
            data.isBookmarked = !(data.isBookmarked ?: return)

            val eventName = if (data.isBookmarked == true) {
                EventConstants.DOUBT_BOOKMARKED
            } else {
                EventConstants.DOUBT_UNBOOKMARKED
            }

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    eventName,
                    hashMapOf(
                        EventConstants.SOURCE to TAG,
                        EventConstants.ID to data.id.orEmpty()
                    )
                )
            )
            DataHandler.INSTANCE.commentRepository.bookmark(
                data.id ?: "",
                data.courseAssortmentId ?: ""
            ).applyIoToMainSchedulerOnCompletable()
                .subscribeToCompletable({})
            items.indexOfFirst { it.id == data.id }
                .takeIf { it >= 0 }
                ?.let {
                    if (data.source == "bookmark") {
                        items.removeAt(it)
                        notifyItemRemoved(it)
                        onBookmarkRemoved()
                    } else {
                        if (data.isBookmarked == true) {
                            binding.ivBookMark.setImageDrawable(
                                ContextCompat.getDrawable(
                                    binding.root.context,
                                    R.drawable.ic_bookmark
                                )
                            )
                            showToast(context, "Bookmarked Successfully")
                        } else {
                            binding.ivBookMark.setImageDrawable(
                                ContextCompat.getDrawable(
                                    binding.root.context,
                                    R.drawable.ic_unbookmark
                                )
                            )
                            showToast(context, "Bookmarked Removed Successfully")
                        }
                    }
                }
        }

        private fun checkPDFUrl(holder: RecyclerView.ViewHolder, data: NotesWidgetItem) {
            try {
                if (!URLUtil.isValidUrl(data.link)) {
                    showToast(
                        holder.itemView.context,
                        holder.itemView.context.resources.getString(R.string.notAvalidLink)
                    )
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
                showToast(
                    holder.itemView.context,
                    holder.itemView.context.resources.getString(R.string.donothaveanybrowser)
                )
            }
        }

        private fun markNotesRead(resourceId: String?, isDownloaded: Int) {
            DataHandler.INSTANCE.courseRepository.markNotesRead(resourceId.orEmpty(), isDownloaded)
                .applyIoToMainSchedulerOnCompletable()
                .subscribeToCompletable({})
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemNotesBinding) : RecyclerView.ViewHolder(binding.root)
    }

    class NotesWidgetViewHolder(
        binding: WidgetNotesBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetNotesBinding>(binding, widget)

    override fun getViewBinding(): WidgetNotesBinding {
        return WidgetNotesBinding.inflate(LayoutInflater.from(context), this, true)
    }
}