package com.doubtnutapp.newglobalsearch.ui.viewholder

import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.base.*
import com.doubtnutapp.databinding.ItemSearchPlaylistFullWidthBinding
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO
import com.doubtnutapp.hide
import com.doubtnutapp.newglobalsearch.model.SearchPlaylistViewItem
import com.doubtnutapp.show
import com.doubtnutapp.utils.DateUtils
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class SearchPlaylistFullWidthViewHolder(val binding: ItemSearchPlaylistFullWidthBinding, val resultCount: Int) :
        BaseViewHolder<SearchPlaylistViewItem>(binding.root) {

    override fun bind(data: SearchPlaylistViewItem) {
        binding.searchPlaylist = data
        if (data.isVip) binding.tvVipBadge.show() else binding.tvVipBadge.hide()
        binding.imageView.visibility = if (data.imageUrl != null && data.imageUrl.isNotEmpty()) View.VISIBLE else View.GONE
        setLiveClassView(data)
        if (data.imageUrl.isNullOrEmpty() && data.resourceType == Constants.ETOOS_VIDEO
                && !TextUtils.isEmpty(data.bgColor)) {
            binding.mvsVideoThumbnail.text = data.display
            setBackgroundColor(data.bgColor)
            binding.mvsVideoThumbnail.show()
        } else {
            binding.mvsVideoThumbnail.hide()
        }
        binding.tvRecommended.isVisible = data.isRecommended
        binding.tvTitle.text = data.display

        binding.cardView.setOnClickListener {
            performAction(SearchPlaylistClickedEvent(Gson().toJson(data), data.display, data.id,
                data.fakeType, data.type, adapterPosition, resultCount = resultCount,
                assortmentId = data.assortmentId.orEmpty()))
            performAction(SearchPlaylistClicked(data, adapterPosition))
            if (data.isVip) {
                performAction(SearchVipPlaylistClicked(data))
            } else {
                performAction(getAction(data))
            }
        }

    }

    private fun getAction(searchItem: SearchPlaylistViewItem): Any {
        return when (searchItem.type) {

            "video" -> {
                val currentDate = if (searchItem.currentTime != null) DateUtils.stringToDate(searchItem.currentTime) else Date()
                if (searchItem.liveAt != null && DateUtils.stringToDate(searchItem.liveAt).after(currentDate)) {
                    UpcomingLiveVideo()
                } else
                    PlayVideo(searchItem.id, searchItem.page,
                            "", "", searchItem.playerType ?: SOLUTION_RESOURCE_TYPE_VIDEO)
            }

            "playlist" -> {
                if (searchItem.isLast == "0")
                    OpenLibraryPlayListActivity(searchItem.id, searchItem.display)
                else
                    OpenLibraryVideoPlayListScreen(searchItem.id, searchItem.display)
            }

            "pdf" -> {
                if (searchItem.isLast == "1") {
                    if (!URLUtil.isValidUrl(searchItem.resourcesPath)) {
                        ToastUtils.makeText(binding.root.context, R.string.notAvalidLink, Toast.LENGTH_SHORT).show()
                        OpenLibraryPlayListActivity(searchItem.id, searchItem.display)
                    } else {
                        OpenPDFViewScreen(pdfUrl = searchItem.resourcesPath)
                    }
                } else
                    OpenLibraryPlayListActivity(searchItem.id, searchItem.display)
            }

            else -> throw IllegalArgumentException("Wrong type for search playlist")
        }
    }

    private fun setLiveClassView(data: SearchPlaylistViewItem) {
        val colorLiveText = ContextCompat.getColor(binding.root.context, R.color.pink_dark)
        val colorUpcomingText = ContextCompat.getColor(binding.root.context, R.color.text_black)
        val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        if (data.liveAt == null) {
            binding.textViewLive.isVisible = false
            return
        }


        try {
            val liveAtDate = DateUtils.stringToDate(data.liveAt)
            val currentDate = if (data.currentTime == null) Date() else DateUtils.stringToDate(data.currentTime)
            when {
                liveAtDate.after(currentDate) -> {
                    val dateTest = getDateText(currentDate, liveAtDate)
                    binding.textViewLive.isVisible = true
                    binding.textViewLive.setBackgroundResource(R.drawable.bg_capsule_light_yellow_solid)
                    binding.textViewLive.text = binding.root.context.getString(R.string.live_class_at, dateFormat.format(liveAtDate), dateTest)
                    binding.textViewLive.setTextColor(colorUpcomingText)
                }
                isVideoLive(data) -> {
                    binding.textViewLive.isVisible = true
                    binding.textViewLive.setBackgroundResource(R.drawable.bg_live_video_tag)
                    binding.textViewLive.text = binding.root.context.getString(R.string.live_now_dot)
                    binding.textViewLive.setTextColor(colorLiveText)
                }
                else -> {
                    binding.textViewLive.isVisible = false
                }
            }
        } catch (e: Exception) {

        }
    }

    private fun setBackgroundColor(bgColor: String) {
        if (bgColor.isNotEmpty()) {
            binding.thumbView.setBackgroundColor(Color.parseColor(bgColor))
        }
    }

    private fun isVideoLive(data: SearchPlaylistViewItem): Boolean {
        if (data.liveAt.isNullOrEmpty())
            return false
        return try {
            val liveAtDate = DateUtils.stringToDate(data.liveAt)
            val currentDate = if (data.currentTime == null) Date() else DateUtils.stringToDate(data.currentTime)
            val minDif = TimeUnit.MILLISECONDS.toMinutes(currentDate.time - liveAtDate.time)
            minDif >= 0 && minDif < data.liveLengthMin ?: 60
        } catch (e: Exception) {
            false
        }
    }

    private fun getDateText(currentDate: Date, compareDate: Date): String {
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        val currentCal = Calendar.getInstance().apply {
            time = currentDate
        }
        val compareCal = Calendar.getInstance().apply {
            time = compareDate
        }
        return if (currentCal.get(Calendar.YEAR) == compareCal.get(Calendar.YEAR)) {
            when (compareCal.get(Calendar.DAY_OF_YEAR) - currentCal.get(Calendar.DAY_OF_YEAR)) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> dateFormat.format(compareDate)
            }
        } else {
            return dateFormat.format(compareDate)
        }
    }
}