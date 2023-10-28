package com.doubtnutapp.newlibrary.viewholder

import com.apxor.androidsdk.core.Attributes
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.checkInternetConnection
import com.doubtnutapp.common.promotional.model.PromotionalActionDataViewItem
import com.doubtnutapp.common.promotional.model.PromotionalDataViewItem
import com.doubtnutapp.data.remote.models.BannerActionData
import com.doubtnutapp.databinding.ItemPromotionalViewBinding
import com.doubtnutapp.utils.ApxorUtils
import com.doubtnutapp.utils.BannerActionUtils

class LibraryBannerViewHolder(private val binding: ItemPromotionalViewBinding) :
        BaseViewHolder<PromotionalDataViewItem>(binding.root) {

    override fun bind(data: PromotionalDataViewItem) {
        binding.promotionalDataViewItem = data
        binding.cardContainer.setOnClickListener {
            checkInternetConnection(binding.root.context) {
                val context = binding.root.context
                BannerActionUtils.navigateToPage(context, data.actionActivity, mapToBannerActionData(data.actionData), eventTracker = (context.applicationContext as DoubtnutApp).getEventTracker())
                try {
                    ApxorUtils.logAppEvent(EventConstants.EVENT_BANNER_CLICK, Attributes().apply {
                        putAttribute(EventConstants.SOURCE, EventConstants.EVENT_LIBRARY)
                        putAttribute(EventConstants.ITEM, data.actionActivity.orEmpty())
                        putAttribute(EventConstants.VIEW_SOURCE, data.actionData?.playlistId.orEmpty())
                    })
                } catch (e: Exception) {

                }
            }
        }
    }

    private fun mapToBannerActionData(actionData: PromotionalActionDataViewItem): BannerActionData = actionData.run {
        BannerActionData("", "", actionData.playlistId, actionData.playlistTitle, actionData.isLast, "",
                "", "", "", "", "", "", "", "", "", "", "", "", "", facultyId, ecmId, subject)
    }
}