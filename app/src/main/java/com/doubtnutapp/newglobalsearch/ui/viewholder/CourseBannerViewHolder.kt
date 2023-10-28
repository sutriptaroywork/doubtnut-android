package com.doubtnutapp.newglobalsearch.ui.viewholder

import com.doubtnutapp.Constants
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.CourseBannerClicked
import com.doubtnutapp.databinding.ItemCourseBannerBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.loadImage
import com.doubtnutapp.newglobalsearch.model.CourseBannerViewItem

class CourseBannerViewHolder(
    val binding: ItemCourseBannerBinding,
    private var deeplinkAction: DeeplinkAction?
) :
    BaseViewHolder<CourseBannerViewItem>(binding.root) {

    override fun bind(data: CourseBannerViewItem) {
        binding.ivBanner.loadImage(data.imageUrl)
        binding.root.setOnClickListener {
            performAction(CourseBannerClicked(data.tabType, data.title))
            deeplinkAction?.performAction(
                itemView.context,
                data.deepLinkUrl,
                Constants.IN_APP_SEARCH_SOURCE
            )
        }
    }
}