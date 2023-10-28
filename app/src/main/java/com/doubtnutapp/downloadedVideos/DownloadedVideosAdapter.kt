package com.doubtnutapp.downloadedVideos

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.ItemDownloadedVideoBinding
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.model.Video
import com.doubtnutapp.ui.mediahelper.ExoUtils
import com.doubtnutapp.ui.mediahelper.MEDIA_TYPE_DASH_OFFLINE
import com.doubtnutapp.video.VideoFragment
import com.doubtnutapp.videoPage.model.VideoResource
import com.doubtnutapp.videoPage.ui.FullScreenVideoPageActivity
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class DownloadedVideosAdapter(
    val context: Context,
    val items: ArrayList<OfflineMediaItem>,
    val analyticsPublisher: AnalyticsPublisher
) : RecyclerView.Adapter<DownloadedVideosAdapter.DownloadedVideoViewHolder>(), ActionMode.Callback {

    private var multiSelect = false
    private val selectedItems = arrayListOf<OfflineMediaItem>()

    private val tracker: ExoDownloadTracker = ExoDownloadTracker.getInstance(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadedVideoViewHolder {
        return DownloadedVideoViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_downloaded_video, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: DownloadedVideoViewHolder, position: Int) {
        val data = items[position]

        holder.binding.imageViewBackground.loadImageEtx(data.thumbUrl.orEmpty())
        holder.binding.videoSeekbar.setPadding(0, 0, 0, 0)
        observeDownloading(data.videoUrl, holder)

        val expireDate = Date()
        expireDate.time = data.licenceExpireDate

        val subscriptionDate = Date()
        subscriptionDate.time = data.subscriptionExpireDate

        setSectionMode(holder)
        holder.binding.tvExpire.isVisible = false
        holder.binding.tvExpire.text = getDateText(expireDate)

        when (data.status) {
            OfflineMediaStatus.DOWNLOADED -> {
                holder.binding.gradView.setBackgroundResource(R.drawable.bg_grad_right_dark)
                holder.binding.videoSeekbar.isVisible = false
                holder.binding.tvExpire.isVisible = true
            }
            OfflineMediaStatus.FAILURE -> {
            }
            OfflineMediaStatus.DOWNLOADING -> {
                holder.binding.gradView.setBackgroundResource(R.drawable.bg_grad_bottom_dark)
                holder.binding.gradView.isVisible = true
                holder.binding.videoSeekbar.isIndeterminate = true
                holder.binding.videoSeekbar.isVisible = true
            }
            OfflineMediaStatus.EXPIRED -> {
                holder.binding.gradView.setBackgroundColor(Color.parseColor("#CC000000"))
                holder.binding.gradView.isVisible = true
                holder.binding.videoSeekbar.isVisible = false
                holder.binding.tvExpire.isVisible = true
                holder.binding.tvExpire.text = "Subscription Expired"
            }
            OfflineMediaStatus.INITIAL -> {
            }
        }

        holder.itemView.setOnLongClickListener {
            if (!multiSelect) {
                multiSelect = true
                (holder.itemView.context as AppCompatActivity).startSupportActionMode(this)
                selectItem(holder, data)
                notifyDataSetChanged()
                true
            } else {
                false
            }
        }
        holder.binding.selectionCheckbox.setImageResource(if (data.isSelected) R.drawable.ic_done_tomato else R.drawable.bg_circle_white)

        holder.itemView.setOnClickListener {
            if (multiSelect) {
                selectItem(holder, data)
            } else {
                when (data.status) {
                    OfflineMediaStatus.DOWNLOADED -> {
                        holder.binding.videoSeekbar.isVisible = false
                        if (expireDate.after(Date()) && subscriptionDate.after(Date()))
                            openVideoPage(holder.itemView.context, data)
                        else
                            ToastUtils.makeText(
                                holder.itemView.context,
                                "Video or subscription is expired",
                                Toast.LENGTH_LONG
                            ).show()
                    }
                    OfflineMediaStatus.FAILURE -> {
                        ToastUtils.makeText(
                            holder.itemView.context,
                            "Unable to download ${data.title}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    OfflineMediaStatus.DOWNLOADING -> {
                        ToastUtils.makeText(
                            holder.itemView.context,
                            "Downloading in progress, please wait",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    OfflineMediaStatus.INITIAL -> {
                        ToastUtils.makeText(
                            holder.itemView.context,
                            "Downloading in queue please wait...",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    OfflineMediaStatus.EXPIRED -> {
                        Snackbar.make(
                            holder.itemView,
                            "This video or subscription is expired",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
                val event = AnalyticsEvent(
                    EventConstants.EVENT_OFFLINE_DOWNLOAD_ITEM_CLICK,
                    ignoreFacebook = false,
                    ignoreFirebase = false,
                    params = hashMapOf(
                        EventConstants.QUESTION_ID to data.questionId,
                        EventConstants.EVENT_NAME_TITLE to data.title.orEmpty()
                    ),
                    ignoreSnowplow = true
                )
                analyticsPublisher.publishEvent(event)
            }
        }
    }

    private fun getDateText(expiredate: Date): String {
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        val currentCal = Calendar.getInstance().apply {
            time = Date()
        }
        val compareCal = Calendar.getInstance().apply {
            time = expiredate
        }
        return if (currentCal.get(Calendar.YEAR) == compareCal.get(Calendar.YEAR)) {
            when (compareCal.get(Calendar.DAY_OF_YEAR) - currentCal.get(Calendar.DAY_OF_YEAR)) {
                0 -> "Expires Today"
                1 -> "Expires Tomorrow"
                else -> "Expire ${dateFormat.format(expiredate)}"
            }
        } else {
            return "Expire" + dateFormat.format(expiredate)
        }
    }

    private fun setSectionMode(holder: DownloadedVideoViewHolder) {
        holder.binding.gradView.isVisible = multiSelect
        holder.binding.selectionCheckbox.isVisible = multiSelect
    }

    @SuppressLint("CheckResult")
    private fun observeDownloading(url: String, holder: DownloadedVideoViewHolder) {
        DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe {
            if (it is ExoUtils.ExoDownloadInfo && it.videoUrl == url) {
                holder.binding.videoSeekbar.progress = it.percentage.toInt()
                if (it.percentage == 100f)
                    holder.binding.videoSeekbar.isVisible = false
            }
        }
    }

    private fun selectItem(holder: DownloadedVideoViewHolder, offlineMediaItem: OfflineMediaItem) {
        if (offlineMediaItem.isSelected) {
            offlineMediaItem.isSelected = false
            holder.binding.selectionCheckbox.setImageResource(R.drawable.bg_circle_white)
            selectedItems.remove(offlineMediaItem)
        } else {
            offlineMediaItem.isSelected = true
            holder.binding.selectionCheckbox.setImageResource(R.drawable.ic_done_tomato)
            selectedItems.add(offlineMediaItem)
        }
    }

    private fun openVideoPage(context: Context, offlineMediaItem: OfflineMediaItem) {
        val videoResource = VideoResource(
            offlineMediaItem.videoUrl,
            "widevine",
            offlineMediaItem.drmLicenseUrl,
            MEDIA_TYPE_DASH_OFFLINE,
            false,
            null,
            null,
            0
        )
        val video = Video(
            questionId = offlineMediaItem.questionId.toString(),
            autoPlay = true,
            showFullScreen = false,
            videoResources = listOf(videoResource),
            viewId = null,
            videoPosition = 0,
            videoPage = "DOWNLOAD",
            aspectRatio = VideoFragment.DEFAULT_ASPECT_RATIO
        )
        FullScreenVideoPageActivity.startActivity(context, video, usePageWithoutAppend = true)
            .apply {
                context.startActivity(this)
            }
    }

    class DownloadedVideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemDownloadedVideoBinding.bind(itemView)
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_delete) {
            deleteSelectedVideos(context, mode)
//            mode?.finish()
        }
        return true
    }

    private fun deleteSelectedVideos(context: Context, mode: ActionMode?) {
        if (selectedItems.size > 0) {
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle("Delete Downloaded Videos")
            alertDialog.setMessage("Selected ${selectedItems.size} videos will be deleted")
            alertDialog.setPositiveButton(context.getString(R.string.string_ok)) { _, _ ->
                deleteVideosFromDB()
                mode?.finish()
            }
            alertDialog.setNegativeButton(context.getString(R.string.string_notificationEducation_cancel)) { _, _ ->
                mode?.finish()
                for (item: OfflineMediaItem in items) {
                    item.isSelected = false
                }
                notifyDataSetChanged()
            }
            alertDialog.show()
        } else {
            ToastUtils.makeText(context, "Please select videos to delete", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun deleteVideosFromDB() {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.EVENT_OFFLINE_DOWNLOAD_DELETE,
                ignoreFacebook = false,
                ignoreSnowplow = true
            )
        )
        items.removeAll(selectedItems)
        for (item: OfflineMediaItem in selectedItems) {
            tracker.removeDownload(item.videoUrl)
        }
        notifyDataSetChanged()
        val message = context.resources.getQuantityString(
            R.plurals.video_deleted_successfully,
            selectedItems.size
        )
        ToastUtils.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.let {
            val inflater: MenuInflater = mode!!.menuInflater
            inflater.inflate(R.menu.menu_downloaded_video, menu)
        }
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        multiSelect = false
        selectedItems.clear()
        items.map {
            it.isSelected = false
        }
        notifyDataSetChanged()
    }
}
