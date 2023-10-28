package com.doubtnutapp.libraryhome.liveclasses.viewholder

import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.base.BannerClickEvent
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.common.promotional.model.PromotionalActionDataViewItem
import com.doubtnutapp.data.remote.models.BannerActionData
import com.doubtnutapp.databinding.ItemDetailLiveClassBannerBinding
import com.doubtnutapp.libraryhome.liveclasses.model.BannerViewItem
import com.doubtnutapp.utils.BannerActionUtils

class BannerViewHolder(private val binding: ItemDetailLiveClassBannerBinding) :
    BaseViewHolder<BannerViewItem>(binding.root) {

    override fun bind(data: BannerViewItem) {
        binding.banner = data

        binding.bannerLayout.setOnClickListener {
            actionPerformer?.performAction(
                BannerClickEvent(EventConstants.CRASH_COURSE_SCORE_BANNER_CLICK)
            )
            BannerActionUtils.navigateToPage(
                binding.root.context,
                data.actionActivity,
                mapToBannerActionData(data.actionData!!),
                (binding.root.context.applicationContext as DoubtnutApp).getEventTracker()
            )
        }
    }

    private fun mapToBannerActionData(actionData: PromotionalActionDataViewItem): BannerActionData =
        actionData.run {
            BannerActionData(
                "",
                "",
                actionData.playlistId,
                actionData.playlistTitle,
                actionData.isLast,
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                actionData.facultyId,
                ecmId,
                subject
            )
        }
}