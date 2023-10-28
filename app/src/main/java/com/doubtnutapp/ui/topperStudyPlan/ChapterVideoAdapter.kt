package com.doubtnutapp.ui.topperStudyPlan

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.PlayVideo
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnutapp.base.WatchLaterRequest
import com.doubtnutapp.databinding.ItemChapterVideoBinding
import com.doubtnutapp.sharing.VIDEO_CHANNEL
import com.doubtnutapp.utils.Utils
import javax.inject.Inject

class ChapterVideoAdapter(private val actionPerformer: ActionPerformer) :
    ListAdapter<ChapterDetailData.VideoDetailsData.VideoDetails, ChapterVideoAdapter.ChapterVideoViewHolder>(
        DIFF_UTILS
    ) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChapterVideoAdapter.ChapterVideoViewHolder {
        val binder = ItemChapterVideoBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ChapterVideoViewHolder(binder, actionPerformer)
    }

    override fun onBindViewHolder(
        holder: ChapterVideoAdapter.ChapterVideoViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        holder.bind(item)
    }

    companion object {
        val DIFF_UTILS =
            object : DiffUtil.ItemCallback<ChapterDetailData.VideoDetailsData.VideoDetails>() {
                override fun areContentsTheSame(
                    oldItem: ChapterDetailData.VideoDetailsData.VideoDetails,
                    newItem: ChapterDetailData.VideoDetailsData.VideoDetails
                ) = oldItem == newItem

                override fun areItemsTheSame(
                    oldItem: ChapterDetailData.VideoDetailsData.VideoDetails,
                    newItem: ChapterDetailData.VideoDetailsData.VideoDetails
                ) = oldItem.questionId == newItem.questionId
            }
    }

    inner class ChapterVideoViewHolder(
        private val binding: ItemChapterVideoBinding,
        val actionsPerformer: ActionPerformer
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("RestrictedApi")
        fun bind(item: ChapterDetailData.VideoDetailsData.VideoDetails) {
            binding.cardContainer.setBackgroundColor(Color.parseColor("#ffffff"))
            binding.timeDurationTextView.text = Utils.getSecondsToString(item.duration)
            binding.tvQuestionId.text = item.questionId.toString()
            if (!item.ocrText.isNullOrBlank()) {
                binding.dmathView.show()
                binding.dmathView.text = item.ocrText
                binding.ivMatch.hide()
                binding.progressBar.hide()
                binding.playVideoImageView.bringToFront()
            } else {
                binding.dmathView.hide()
                binding.ivMatch.show()
                binding.progressBar.show()
                binding.playVideoImageView.bringToFront()
            }

            binding.clickHelperView.setOnClickListener {
                checkInternetConnection(binding.root.context) {
                    actionsPerformer.performAction(
                        PlayVideo(
                            item.questionId.toString(),
                            "PERSONALIZATION",
                            "",
                            "",
                            ""
                        )
                    )
                }
            }

            binding.overflowMenuSimilar.setOnClickListener {
                PopupMenu(
                    ContextThemeWrapper(binding.root.context, R.style.PopupMenuStyle),
                    binding.overflowMenuSimilarView,
                    Gravity.END
                ).also { popupMenu ->
                    popupMenu.menuInflater.inflate(R.menu.menu_popup_similar, popupMenu.menu)
                    val menuHelper = MenuPopupHelper(
                        binding.root.context,
                        popupMenu.menu as MenuBuilder,
                        binding.overflowMenuSimilarView
                    )
                    menuHelper.gravity = Gravity.END
                    menuHelper.setForceShowIcon(true)

                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.itemWatchLaterSimilar -> {
                                actionsPerformer.performAction(WatchLaterRequest(item.questionId.toString()))
                            }
                            R.id.itemShareSimilar -> {
                                analyticsPublisher.publishEvent(
                                    AnalyticsEvent(
                                        EventConstants.SHARE_VIDEO_CLICK,
                                        hashMapOf(
                                            EventConstants.QUESTION_ID to item.questionId.toString(),
                                            EventConstants.SOURCE to EventConstants.EVENT_NAME_CHAPTER_DETAIL_PAGE
                                        ), ignoreSnowplow = true
                                    )
                                )
                                checkInternetConnection(binding.root.context) {
                                    actionsPerformer.performAction(
                                        ShareOnWhatApp(
                                            VIDEO_CHANNEL,
                                            "video",
                                            "",
                                            hashMapOf(),
                                            "#000000",
                                            "",
                                            item.questionId.toString()
                                        )
                                    )
                                }
                            }
                        }
                        true
                    }
                    menuHelper.show()
                }
            }
        }
    }
}