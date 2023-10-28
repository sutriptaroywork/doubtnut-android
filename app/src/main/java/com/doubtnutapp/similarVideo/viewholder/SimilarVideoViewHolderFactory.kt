package com.doubtnutapp.similarVideo.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.eventmanager.CommonEventManager
import com.doubtnutapp.pCBanner.PCBannerViewHolder

class SimilarVideoViewHolderFactory {

    fun getViewHolderFor(
        parent: ViewGroup,
        viewType: Int,
        commonEventManager: CommonEventManager,
        sourceTag: String
    ): BaseViewHolder<*> {
        return when (viewType) {
            R.layout.item_similar_result -> SimilarVideoListItemViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_similar_result,
                    parent,
                    false
                )
            )
            R.layout.item_similar_questions_feedback -> FeedBackSimilarVideoViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_similar_questions_feedback,
                    parent,
                    false
                )
            )
            R.layout.item_similar_video_askquestion -> AskNowViewHolder(
                LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            )

            R.layout.item_similar_video_postcommunity -> PostOnCommunityViewHolder(
                LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            )

            R.layout.item_whatsapp_feed -> SimilarVideoWhatsappViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_whatsapp_feed,
                    parent,
                    false
                )
            )

            R.layout.item_similar_search_topic -> SimilarTopicSearchViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_similar_search_topic,
                    parent,
                    false
                )
            )

            R.layout.item_scratch_card -> ScratchCardViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_scratch_card, parent,
                    false
                )
            )

            R.layout.item_sale_timer -> SaleTimerViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_sale_timer, parent,
                    false
                )
            )

            R.layout.concept_video_view -> ConceptVideoViewHolder(
                LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            )
            R.layout.pc_banner_view -> PCBannerViewHolder(
                LayoutInflater.from(parent.context).inflate(viewType, parent, false),
                commonEventManager,
                sourceTag
            )
            R.layout.item_similar_header -> SimilarHeaderViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_similar_header,
                    parent,
                    false
                )
            )
            R.layout.item_similar_topic_booster -> SimilarTopicBoosterViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_similar_topic_booster, parent, false)
            )

            R.layout.item_ncert_view -> NcertViewViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_ncert_view,
                    parent,
                    false
                )
            )
            R.layout.item_similar_show_more -> SimilarShowMoreViewHolder(
                LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            )
            else -> throw IllegalArgumentException()
        }
    }

    fun getViewHolderFor(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            R.layout.item_similar_result -> LandscapeSimilarVideoListItemViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_similar_result,
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException()
        }
    }

    fun getViewHolderFor(
        parent: ViewGroup,
        viewType: Int,
        actionPerformer: ActionPerformer
    ): BaseViewHolder<*> {
        return when (viewType) {
            R.layout.item_similar_topic_booster_option -> SimilarTopicBoosterOptionViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_similar_topic_booster_option,
                    parent,
                    false
                ),
                actionPerformer
            )

            else -> throw IllegalArgumentException()
        }
    }

}