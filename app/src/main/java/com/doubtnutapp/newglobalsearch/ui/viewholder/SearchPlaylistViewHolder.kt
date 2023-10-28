package com.doubtnutapp.newglobalsearch.ui.viewholder

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.webkit.URLUtil
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.*
import com.doubtnutapp.base.*
import com.doubtnutapp.databinding.ItemSearchPlaylistBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO
import com.doubtnutapp.newglobalsearch.model.SearchPlaylistViewItem
import com.doubtnutapp.utils.DateUtils
import com.doubtnutapp.utils.Utils
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit





class SearchPlaylistViewHolder(
    val binding: ItemSearchPlaylistBinding,
    private var deeplinkAction: DeeplinkAction?,
    private val resultCount: Int
) :
    BaseViewHolder<SearchPlaylistViewItem>(binding.root) {

    override fun bind(data: SearchPlaylistViewItem) {
        binding.searchPlaylist = data
        if (data.isVip) binding.tvVipBadge.show() else binding.tvVipBadge.hide()
        val width = Resources.getSystem().displayMetrics.widthPixels / 3
        val layoutParams = binding.imageView.layoutParams
        binding.buttonSearchGetAdmissionPdf.hide()
        binding.buttonSearchGetAdmission.hide()
        binding.containerBook.hide()
        binding.playlistLayout.hide()
        binding.containerTeacher.hide()
        if ("pdf".equals(data.type, true)) {
            binding.searchResultDisplayName.minLines = 1
            binding.imageView.visibility = View.VISIBLE
            if (data.isBooksPdf) {
                binding.imageView.setImageResource(R.drawable.ic_pdf_book)
            } else if (data.isLiveClassPdf) {
                binding.imageView.setImageResource(R.drawable.ic_pdf_liveclass)
            } else {
                binding.imageView.setImageResource(R.drawable.ic_pdf_ias)
            }

            if (data.buttonDetails?.button_text.isNullOrEmpty()) {
                binding.buttonSearchGetAdmissionPdf.hide()
                if (data.coursePrice.isNullOrEmpty()) {
                    binding.searchResultPrice.hide()
                } else {
                    binding.searchResultPrice.show()
                }
            } else {
                binding.buttonSearchGetAdmissionPdf.show()
                binding.buttonSearchGetAdmissionPdf.setBackgroundColor(Color.parseColor(data.buttonDetails?.button_bg_color))
                binding.searchResultDisplayName.minLines = 1
                if (data.coursePrice.isNullOrEmpty()) {
                    binding.searchResultPrice.invisible()
                } else {
                    binding.searchResultPrice.show()
                }
            }
            binding.containerBook.hide()
            binding.playlistLayout.show()
            binding.containerTeacher.hide()

        } else if ("video".equals(data.tabType, true)) {
            binding.searchResultDisplayName.minLines = 1
            binding.imageView.visibility = View.VISIBLE
            binding.imageView.setImageResource(R.drawable.ic_play_lecture_small)
            binding.containerBook.hide()
            binding.playlistLayout.show()
            binding.containerTeacher.hide()
        } else {
            if ("book".equals(data.tabType, true)) {
                binding.searchResultDisplayName.minLines = 1
            } else {
                if(data.imageUrl != null && data.imageUrl.isNotEmpty()) {
                    if(data.premiumMetaContent != null){
                        binding.searchResultDisplayName.minLines = 1
                    }else {
                        binding.searchResultDisplayName.minLines = 3
                    }
                }else {
                    binding.searchResultDisplayName.minLines = 1
                }
            }


            if (data.viewTypeUi.equals("grid") && data.tabType != "All") {
                if (!data.imageUrl.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load(data.imageUrl)
                        .into(binding.imageView)
                }
                binding.containerBook.show()
                binding.playlistLayout.hide()
                binding.containerTeacher.hide()
            } else if(data.tabType.equals("teacher")){
                binding.containerTeacher.show()
                binding.containerBook.hide()
                binding.playlistLayout.hide()

                binding.tvName.text = data.teacherName.orEmpty()
                binding.ivProfileImage.loadImage(data.imageUrl,R.drawable.ic_profile_placeholder)
                binding.llDetails.removeAllViews()
                for (item in data.teacherDetails!!) {
                    val vi =  LayoutInflater.from(itemView.context)
                    val layoutToInflate = vi.inflate(R.layout.teacher_details_item,
                            null)
                    val tvLabel = layoutToInflate.findViewById<TextView>(R.id.tvLabel);
                    tvLabel.text = item.key
                    val tvValue = layoutToInflate.findViewById<TextView>(R.id.tvValue);
                    tvValue.text = item.value

                    binding.llDetails.addView(layoutToInflate)
                }

                if (data.buttonDetails?.button_text.isNullOrEmpty()) {
                    binding.buttonSubscribe.hide()
                } else {
                    binding.buttonSubscribe.show()
                    binding.buttonSubscribe.text = data.buttonDetails?.button_text.orEmpty()
                    binding.buttonSubscribe.setBackgroundColor(Color.parseColor(data.buttonDetails?.button_bg_color))
                }
            }else {
                binding.containerBook.hide()
                binding.playlistLayout.show()
                binding.containerTeacher.hide()
                if (!data.imageUrl.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load(data.imageUrl)
                        .into(binding.imageView)
                }

                binding.imageView.visibility =
                    if (data.imageUrl != null && data.imageUrl.isNotEmpty()) View.VISIBLE else View.GONE
            }
            if (data.buttonDetails?.button_text.isNullOrEmpty()) {
                binding.buttonSearchGetAdmission.hide()
            } else {
                binding.buttonSearchGetAdmission.show()
                binding.buttonSearchGetAdmission.setBackgroundColor(Color.parseColor(data.buttonDetails?.button_bg_color))
                binding.searchResultDisplayName.minLines = 1
            }
            if (data.coursePrice.isNullOrEmpty()) {
                binding.buttonSearchGetAdmissionPdf.hide()
                binding.searchResultPrice.hide()
            } else {
                binding.searchResultPrice.show()
                binding.buttonSearchGetAdmissionPdf.invisible()
            }

        }

        if (data.vipContentLock.isNullOrEmpty()) {
            binding.imageViewLock.hide()
        } else {
            binding.imageViewLock.show()
            binding.imageViewLock.loadImage(data.vipContentLock)
        }

        binding.subPlaylistData.visibility =
            if (data.subData.isNotEmpty()) View.VISIBLE else View.GONE
        setLiveClassView(data)
        layoutParams.width = when (data.imageParamsDecider) {
            Constants.ETOOS_FACULTY -> 300
            Constants.ETOOS_CHAPTER -> 300
            Constants.PDF -> Utils.convertDpToPixel(40.0f).toInt()
            Constants.TAB_VIDEO -> Utils.convertDpToPixel(40.0f).toInt()
            Constants.BOOK -> 100
            else -> width
        }
        layoutParams.height = when (data.imageParamsDecider) {
            Constants.ETOOS_FACULTY -> 300
            Constants.ETOOS_CHAPTER -> 300
            Constants.PDF -> Utils.convertDpToPixel(40.0f).toInt()
            Constants.TAB_VIDEO -> Utils.convertDpToPixel(40.0f).toInt()
            Constants.BOOK -> 100
            else -> getHeight(width)
        }
        binding.imageView.layoutParams = layoutParams
        binding.mvsVideoThumbnail.layoutParams = layoutParams

        if (data.imageUrl.isNullOrEmpty() && data.resourceType == Constants.ETOOS_VIDEO
            && !TextUtils.isEmpty(data.bgColor) && !"video".equals(data.tabType, true)
        ) {

            binding.mvsVideoThumbnail.text = data.display
            setBackgroundColor(data.bgColor)
            binding.mvsVideoThumbnail.show()
            binding.ivVideoPlay.show()
        } else {
            binding.mvsVideoThumbnail.hide()
            binding.ivVideoPlay.hide()
        }

        if (data.premiumMetaContent != null) {
            binding.tvPremium.show()
            binding.tvPremium.text = data.premiumMetaContent.title
            binding.tvPremium.background = Utils.getGradientView(
                data.premiumMetaContent.gradient_bg_color[0],
                data.premiumMetaContent.gradient_bg_color[0],
                data.premiumMetaContent.gradient_bg_color[1],
                GradientDrawable.Orientation.BL_TR,
                5f
            )
            if (!data.premiumMetaContent.image_url.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(data.premiumMetaContent.image_url)
                    .into(object : CustomTarget<Drawable?>(100, 100) {

                        override fun onLoadCleared(@Nullable placeholder: Drawable?) {
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable?>?
                        ) {
                            binding.tvPremium.setCompoundDrawablesWithIntrinsicBounds(
                                resource,
                                null,
                                null,
                                null
                            )
                        }
                    })
            }
        } else {
            binding.tvPremium.hide()
        }

        binding.tvRecommended.isVisible = data.isRecommended

        binding.root.setOnClickListener {
            performAction(
                SearchPlaylistClickedEvent(
                    Gson().toJson(data),
                    data.display,
                    data.id,
                    data.fakeType,
                    data.type,
                    adapterPosition,
                    data.isRecommended,
                    resultCount = resultCount,
                    data.assortmentId.orEmpty()
                )
            )
            performAction(SearchPlaylistClicked(data, adapterPosition))
            if (!data.deeplinkUrl.isNullOrEmpty()) {
                deeplinkAction?.performAction(
                    itemView.context,
                    data.deeplinkUrl,
                    Constants.IN_APP_SEARCH_SOURCE
                )
            } else {
                if (data.isVip) {
                    performAction(SearchVipPlaylistClicked(data))
                } else {
                    if (data.paymentDeeplink.isNullOrEmpty())
                        performAction(getAction(data))
                    else
                        deeplinkAction?.performAction(
                            itemView.context,
                            data.paymentDeeplink,
                            Constants.IN_APP_SEARCH_SOURCE
                        )
                }
            }
            performAction(SearchTopResultClicked(data, adapterPosition, resultCount))
        }

        if(data.tabType.equals("teacher")){
            binding.containerTeacher.show()
            binding.containerBook.hide()
            binding.playlistLayout.hide()
        }

    }

    private fun getHeight(width: Int): Int {
        val rat1 = 16
        val rat2 = 9
        val ratio = width / rat1
        return ratio * rat2
    }

    private fun getAction(searchItem: SearchPlaylistViewItem): Any {
        return when (searchItem.type) {

            "video" -> {
                val currentDate =
                    if (searchItem.currentTime != null) DateUtils.stringToDate(searchItem.currentTime) else Date()
                if (searchItem.liveAt != null && DateUtils.stringToDate(searchItem.liveAt)
                        .after(currentDate)
                ) {
                    UpcomingLiveVideo()
                } else
                    PlayVideo(
                        searchItem.id, searchItem.page,
                        "", "", searchItem.playerType ?: SOLUTION_RESOURCE_TYPE_VIDEO
                    )
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
                        ToastUtils.makeText(
                            binding.root.context,
                            R.string.notAvalidLink,
                            Toast.LENGTH_SHORT
                        ).show()
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
            binding.textViewLiveClassTitle.isVisible = false
            binding.textViewLive.isVisible = false
            return
        }

        binding.textViewLiveClassTitle.isVisible = !data.liveClassTitle.isNullOrEmpty()
        binding.textViewLiveClassTitle.text = data.liveClassTitle

        try {
            val liveAtDate = DateUtils.stringToDate(data.liveAt)
            val currentDate =
                if (data.currentTime == null) Date() else DateUtils.stringToDate(data.currentTime)
            when {
                liveAtDate.after(currentDate) -> {
                    val dateTest = getDateText(currentDate, liveAtDate)
                    binding.textViewLive.isVisible = true
                    binding.textViewLive.setBackgroundResource(R.drawable.bg_capsule_light_yellow_solid)
                    binding.textViewLive.text = binding.root.context.getString(
                        R.string.live_class_at,
                        dateFormat.format(liveAtDate),
                        dateTest
                    )
                    binding.textViewLive.setTextColor(colorUpcomingText)
                }
                isVideoLive(data) -> {
                    binding.textViewLive.isVisible = true
                    binding.textViewLive.setBackgroundResource(R.drawable.bg_live_video_tag)
                    binding.textViewLive.text =
                        binding.root.context.getString(R.string.live_now_dot)
                    binding.textViewLive.setTextColor(colorLiveText)
                }
                else -> {
                    binding.textViewLive.isVisible = false
                    binding.textViewLiveClassTitle.isVisible = false
                }
            }
        } catch (e: Exception) {

        }
    }

    private fun setBackgroundColor(bgColor: String) {
        if (bgColor.isNotEmpty())
            binding.mvsVideoThumbnail.setBackgroundColor(Color.parseColor(bgColor))
    }

    private fun isVideoLive(data: SearchPlaylistViewItem): Boolean {
        if (data.liveAt.isNullOrEmpty())
            return false
        return try {
            val liveAtDate = DateUtils.stringToDate(data.liveAt)
            val currentDate =
                if (data.currentTime == null) Date() else DateUtils.stringToDate(data.currentTime)
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