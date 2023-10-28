package com.doubtnutapp.librarylisting.viewholder

import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.*
import com.doubtnutapp.databinding.ItemNextVideoBinding
import com.doubtnutapp.librarylisting.model.NextVideoViewItem
import com.doubtnutapp.utils.Utils

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
class NextVideoViewHolder(private val binding: ItemNextVideoBinding) :
    BaseViewHolder<NextVideoViewItem>(binding.root) {

    override fun bind(data: NextVideoViewItem) {
        binding.nextVideoViewItem = data
        binding.executePendingBindings()
        binding.timeDurationTextView.text = Utils.getSecondsToString(
            data.videoData?.duration?.toIntOrNull()
                ?: 0
        )

        if (!data.videoData?.ocrText.isNullOrBlank()) {
            binding.dmathView.show()
            binding.dmathView.text = data.videoData?.ocrText
            binding.progressBar.hide()
            binding.playVideoImageView.bringToFront()
        } else {
            binding.dmathView.hide()
        }

        binding.clickHelperView.setOnClickListener {
            checkInternetConnection(binding.root.context) {
                performAction(
                    PublishEvent(
                        AnalyticsEvent(
                            EventConstants.NCERT_CONTINUE_WATCHING,
                            hashMapOf()
                        )
                    )
                )
                performAction(
                    PlayVideo(
                        data.videoData.questionId,
                        Constants.PAGE_LIBRARY,
                        data.videoData.playlistId.orDefaultValue(),
                        "",
                        data.videoData.resourceType
                    )
                )
            }
        }

        binding.cardViewPlaylist.setOnClickListener {
            if (data.playlistData.isLast == "0") {
                performAction(
                    OpenLibraryPlayListActivity(
                        data.playlistData.playlistId.orDefaultValue(),
                        data.playlistData.title.orDefaultValue()
                    )
                )
            } else {
                performAction(
                    OpenLibraryVideoPlayListScreen(
                        data.playlistData.playlistId,
                        data.playlistData.title.orDefaultValue("Unknown")
                    )
                )
            }
        }
    }

}