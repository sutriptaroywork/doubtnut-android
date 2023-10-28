package com.doubtnutapp.matchquestion.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.matchquestion.listener.FilterTopicClickListener
import com.doubtnutapp.matchquestion.model.MatchFilterTopicViewItem

/**
 * Created by devansh on 09/09/20.
 */

abstract class MatchFilterTopicBaseViewHolder(
    protected val containerView: View,
    protected val topicClickListener: FilterTopicClickListener
) : BaseViewHolder<MatchFilterTopicViewItem>(containerView)