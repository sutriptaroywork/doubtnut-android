package com.doubtnutapp.matchquestion.ui.viewholder

import android.view.View
import androidx.core.content.ContextCompat
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.ItemMatchFilterTopicV2Binding
import com.doubtnutapp.matchquestion.listener.FilterTopicClickListener
import com.doubtnutapp.matchquestion.model.MatchFilterTopicViewItem
import javax.inject.Inject

/**
 * Created by devansh on 09/09/20.
 */

class MatchFilterTopicV2ViewHolder(
    containerView: View,
    private val facetPosition: Int,
    private val isFromFragment: Boolean,
    private val isMultiSelect: Boolean,
    private val questionId: String,
    topicClickListener: FilterTopicClickListener
) : MatchFilterTopicBaseViewHolder(containerView, topicClickListener) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    val binding = ItemMatchFilterTopicV2Binding.bind(itemView)

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun bind(data: MatchFilterTopicViewItem) {
        binding.tvTopicName.apply {
            text = data.display

            isEnabled = true
            isSelected = data.isSelected
            when (data.isSelected) {
                true -> setTextColor(ContextCompat.getColor(context, R.color.white))
                false -> setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.fragment_match_filter_unselected_text
                    )
                )
            }

            setOnClickListener {
                if (data.isAllTopic.not() || (data.isAllTopic && data.isSelected.not())) {
                    topicClickListener.onTopicClick(
                        adapterPosition,
                        data.isSelected,
                        !isFromFragment,
                        facetPosition,
                        isMultiSelect
                    )
                }

                // For sending this event, assumption has been made that language facets are the only
                // facet item in facetV2List. This assumption must be discarded after experiment
                // has been completed
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.LANGUAGE_BAR_CLICKED, hashMapOf(
                            Constants.LANGUAGES to data.display,
                            Constants.QUESTION_ID to questionId
                        )
                    )
                )
            }
        }
    }
}