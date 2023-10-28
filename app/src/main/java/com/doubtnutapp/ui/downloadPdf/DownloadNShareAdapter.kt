package com.doubtnutapp.ui.downloadPdf

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.*
import com.doubtnutapp.data.remote.models.DownloadDataList
import com.doubtnutapp.databinding.ItemDownloadpdfBinding
import com.doubtnutapp.ui.mypdf.SharePDFListener
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId

class DownloadNShareAdapter(
    val activity: Activity,
    val eventTracker: Tracker,
    val sharePDFListener: SharePDFListener?,
    val updateTextListener: UpdateButtonText?
) : RecyclerView.Adapter<DownloadNShareAdapter.ViewHolder>() {

    var downloadDataList = mutableListOf<DownloadDataList>()

    private var filterType: String = ""
    private var checkBoxVisibility = false
    private var itemStateArray: HashMap<Int, Boolean> = hashMapOf()
    private var pdfListToShare = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_downloadpdf, parent, false
            ), sharePDFListener, updateTextListener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            downloadDataList[position],
            filterType,
            checkBoxVisibility,
            itemStateArray,
            pdfListToShare,
            position,
            activity
        )
        sendEventItem(downloadDataList[position].packageNameData.toString())

    }

    override fun getItemCount(): Int {
        return downloadDataList.size
    }

    class ViewHolder(
        var binding: ItemDownloadpdfBinding,
        sharePDFListener: SharePDFListener?,
        val updateTextListener: UpdateButtonText?
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            sharePDFListener?.let {
                binding.sharePDFListener = it
            }
        }

        fun bind(
            downloadDataList: DownloadDataList,
            filterType: String,
            checkBoxVisibility: Boolean,
            itemStateArray: HashMap<Int, Boolean>,
            pdfListToShare: MutableList<String>,
            position: Int,
            activity: Activity
        ) {
            binding.chapter = downloadDataList
            binding.executePendingBindings()

            if (!downloadDataList.downloadPath.isNullOrBlank()) {
                binding.imageViewDownloadpdfItemDownload.show()
                binding.imageViewDownloadpdfItemMore.hide()
                binding.imageViewSharePdfFile.show()
            }

            when {
                filterType.contentEquals(Constants.FILTER_PACKAGE) -> binding.textViewDownloadpdfItem.text =
                    downloadDataList.packageNameData
                filterType.contentEquals(Constants.FILTER_LEVEL_ONE) -> binding.textViewDownloadpdfItem.text =
                    downloadDataList.levelOneData
                filterType.contentEquals(Constants.FILTER_LEVEL_TWO) -> binding.textViewDownloadpdfItem.text =
                    downloadDataList.levelTwoData
            }

            when {
                checkBoxVisibility -> {
                    binding.cbSelectItem.show()
                    binding.imageViewDownloadpdfItemDownload.hide()
                    binding.imageViewSharePdfFile.hide()
                }
                else -> binding.cbSelectItem.hide()
            }

            when {
                itemStateArray.get(position) ?: false -> binding.cbSelectItem.setChecked(true)
                else -> binding.cbSelectItem.setChecked(false)
            }

            when {
                pdfListToShare.size > 0 -> updateTextListener?.changeButtonText(
                    binding.root.context.getString(
                        R.string.string_share_selected_pdfs
                    )
                )
                else -> updateTextListener?.changeButtonText(binding.root.context.getString(R.string.string_share_multiple_pdfs))
            }

            if (checkBoxVisibility) {
                binding.overlayView.visibility = View.VISIBLE
                binding.overlayView.setOnClickListener {
                    selectUnselectItem(
                        itemStateArray,
                        downloadDataList,
                        pdfListToShare,
                        activity,
                        position,
                        binding.root.context
                    )
                }
            } else {
                binding.overlayView.visibility = View.GONE
            }
        }

        private fun selectUnselectItem(
            itemStateArray: HashMap<Int, Boolean>, downloadDataList: DownloadDataList,
            pdfListToShare: MutableList<String>, activity: Activity, position: Int, context: Context
        ) {
            itemStateArray.get(position)?.let {
                when {
                    it -> {

                        downloadDataList.downloadPath?.let { it1 -> pdfListToShare.remove(it1) }
                        itemStateArray.remove(adapterPosition)
                        sendEvent(context, EventConstants.EVENT_NAME_UNSELECT_PDF_SHARE_ITEM)


                        binding.cbSelectItem.setChecked(false)


                    }
                    else -> {

                        binding.cbSelectItem.setChecked(true)
                        itemStateArray.put(adapterPosition, true)
                        sendEvent(context, EventConstants.EVENT_NAME_SELECT_PDF_SHARE_ITEM)
                        downloadDataList.downloadPath?.let { it1 -> pdfListToShare.add(it1) }
                    }
                }
            } ?: run {

                when (pdfListToShare.size) {
                    10 -> {

                        binding.cbSelectItem.setChecked(false)
                        ToastUtils.makeText(
                            activity,
                            binding.root.context.getString(R.string.string_sharing_limit_message),
                            Toast.LENGTH_LONG
                        ).show()

                    }
                    else -> {

                        binding.cbSelectItem.setChecked(true)
                        downloadDataList.downloadPath?.let { it1 -> pdfListToShare.add(it1) }
                        sendEvent(context, EventConstants.EVENT_NAME_SELECT_PDF_SHARE_ITEM)
                        itemStateArray.put(adapterPosition, true)
                    }
                }
            }


            when {
                pdfListToShare.size > 0 -> updateTextListener?.changeButtonText(
                    binding.root.context.getString(
                        R.string.string_share_selected_pdfs
                    )
                )
                else -> updateTextListener?.changeButtonText(binding.root.context.getString(R.string.string_share_multiple_pdfs))
            }
        }

        private fun sendEvent(context: Context, eventName: String) {
            context.apply {
                (context.applicationContext as DoubtnutApp).getEventTracker()
                    .addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(context).toString())
                    .addStudentId(getStudentId())
                    .addScreenName(EventConstants.PAGE_DOWNLOAD_N_SHARE_ACTIVITY)
                    .track()
            }

        }
    }

    fun updateData(downloadDataList: List<DownloadDataList>, filterType: String) {
        this.downloadDataList.addAll(downloadDataList)
        this.filterType = filterType
        notifyDataSetChanged()
    }

    fun updateCheckbox(status: Boolean) {
        checkBoxVisibility = status
        notifyDataSetChanged()
    }

    fun resetSelectedItems() {
        pdfListToShare.clear()
        itemStateArray.clear()
        notifyDataSetChanged()
    }

    fun getCheckBoxStatus(): Boolean {
        return checkBoxVisibility
    }

    fun getCheckedPdf(): List<String>? {
        return pdfListToShare
    }

    interface UpdateButtonText {
        fun changeButtonText(sharingMessage: String)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        sendEvent(EventConstants.EVENT_NAME_SCREEN_ADAPTER_ATTACH)

    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        sendEvent(EventConstants.EVENT_NAME_SCREEN_ADAPTER_DETACH)
    }

    private fun sendEvent(eventName: String) {
        activity.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(activity).toString())
                .addScreenState(
                    EventConstants.EVENT_PRAMA_SCREEN_STATE,
                    EventConstants.PAGE_DOWNLOAD_N_SHARE_ADAPTER
                )
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_DOWNLOAD_N_SHARE_ACTIVITY)
                .track()
        }
    }

    private fun sendEventItem(eventName: String) {
        activity.apply {
            eventTracker.addEventNames(EventConstants.EVENT_NAME_ITEM_ENTITY)
                .addScreenState(EventConstants.EVENT_PRAMA_ADAPTER_SCREEN_ITEM, eventName)
                .addNetworkState(NetworkUtils.isConnected(activity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_DOWNLOAD_N_SHARE_ACTIVITY)
                .track()
        }
    }


}

