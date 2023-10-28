package com.doubtnutapp.feed.view

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.TopOptionClicked
import com.doubtnutapp.data.remote.models.feed.TopOptionWidgetItem
import com.doubtnutapp.databinding.ItemHomeTopOptionNewBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.utils.dpToPx
import com.doubtnutapp.loadImage
import com.doubtnutapp.widgetmanager.WidgetTypes
import javax.inject.Inject

/**
 * Created by Sachin Saxena on 08/10/20.
 */

class TopOptionViewHolder(containerView: View) :
    BaseViewHolder<TopOptionWidgetItem>(containerView) {

    val binding = ItemHomeTopOptionNewBinding.bind(containerView)

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun bind(data: TopOptionWidgetItem) {
        binding.apply {
            topOptionImage.loadImage(data.icon, null)
            tvTitle.text = data.title
            cardView.updatePadding(5, 5, 5, 5)
            tvTitle.updateLayoutParams { width = ViewGroup.LayoutParams.WRAP_CONTENT }
            cardView.updateLayoutParams { width = 90.dpToPx() }
            root.setOnClickListener {
                if (actionPerformer != null) {
                    actionPerformer?.performAction(TopOptionClicked(data))
                } else {
                    deeplinkAction.performAction(itemView.context, data.deepLink)

                    // Send clicked data to Snowplow
                    analyticsPublisher.publishEvent(
                        StructuredEvent(
                            category = WidgetTypes.TYPE_TOP_OPTION,
                            action = EventConstants.TOP_OPTION_CLICK,
                            eventParams = hashMapOf(
                                Constants.ID to data.id,
                                Constants.CLICKED_ITEM_NAME to data.title
                            )
                        )
                    )

                    // Send clicked data to Apxor
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.TOP_OPTION_CLICK,
                            hashMapOf(
                                Constants.ID to data.id,
                                Constants.CLICKED_ITEM_NAME to data.title
                            )
                        )
                    )
                    (itemView.context as? Activity)?.finish()
                }
            }
        }
    }

    private fun getTopOptionClickRequestBody(id: Int, title: String): HashMap<String, Any> {
        return HashMap<String, Any>().apply {
            this["id"] = id
            this["title"] = title
        }
    }
}