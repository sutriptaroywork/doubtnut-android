package com.doubtnutapp

import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OpenLibraryPlayListActivity
import com.doubtnutapp.base.OpenLibraryVideoPlayListScreen
import com.doubtnutapp.base.PublishEvent
import com.doubtnutapp.databinding.ItemNcertItemBottomViewBinding
import com.doubtnutapp.similarVideo.model.NcertViewItemEntity

/**
 * Created by Anand Gaurav on 2019-10-14.
 */
class NcertViewItemBottomViewHolder(private val binding: ItemNcertItemBottomViewBinding) : BaseViewHolder<NcertViewItemEntity>(binding.root) {
    override fun bind(data: NcertViewItemEntity) {
        binding.executePendingBindings()
        binding.textViewTitle.text = data.name
        binding.root.setOnClickListener {
            performAction(
                    PublishEvent(
                            AnalyticsEvent(
                                    EventConstants.NCERT_RE_ENTRY_HOME_CLICK,
                                    hashMapOf(EventConstants.EVENT_NAME_ID to data.id.orDefaultValue(),
                                            EventConstants.EVENT_NAME_TITLE to data.name.orDefaultValue())
                            )
                    )
            )
            if (data.isLast == "0") {
                performAction(OpenLibraryPlayListActivity(data.id.orDefaultValue(), data.name.orDefaultValue()))
            } else {
                performAction(OpenLibraryVideoPlayListScreen(data.id, data.name.orDefaultValue("Unknown")))
            }
        }
    }
}