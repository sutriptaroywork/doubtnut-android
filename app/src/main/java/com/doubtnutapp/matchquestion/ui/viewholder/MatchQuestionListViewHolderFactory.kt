package com.doubtnutapp.matchquestion.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.doubtnut.core.DnException
import com.doubtnut.core.constant.ErrorConstants
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.matchquestion.model.MatchQuestionViewItem
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Created by akshaynandwana on
 * 05, March, 2019
 **/
class MatchQuestionListViewHolderFactory() {

    fun getViewHolderFor(
        parent: ViewGroup,
        viewType: Int,
        autoPlay: Boolean? = false,
        autoPlayTime: Long? = null
    ): BaseViewHolder<out MatchQuestionViewItem> {
        return when (viewType) {
            R.layout.item_match_result -> MatchQuestionListItemViewHolder(
                LayoutInflater.from(parent.context).inflate(viewType, parent, false),
                autoPlay
            )
            R.layout.item_autoplay_match_result -> AutoPlayMatchResultViewHolder(
                LayoutInflater.from(parent.context).inflate(viewType, parent, false),
                autoPlay,
                autoPlayTime
            )
            R.layout.item_show_more_youtube_video -> ShowMoreYoutubeVideoViewHolder(
                LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            )
            R.layout.item_youtube_header -> YoutubeHeaderViewHolder(
                LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            )
            R.layout.item_youtube_video -> YoutubeVideoViewHolder(
                LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            )
            else -> {
                with(FirebaseCrashlytics.getInstance()) {
                    this.setCustomKey(ErrorConstants.DN_FATAL, true)
                    recordException(DnException("Inconsistent Match page view type - $viewType"))
                }
                throw IllegalArgumentException()
            }
        }
    }
}