package com.doubtnutapp.libraryhome.course.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.view.View
import android.webkit.URLUtil
import androidx.browser.customtabs.CustomTabsIntent
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemScheduleResourceBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.libraryhome.course.data.Schedule
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.ui.browser.CustomTabActivityHelper
import com.doubtnutapp.ui.browser.WebViewFallback
import com.doubtnutapp.ui.mockTest.MockTestSubscriptionActivity
import com.doubtnutapp.ui.pdfviewer.PdfViewerActivity
import com.doubtnutapp.utils.DateUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import javax.inject.Inject

class ScheduleResourceViewHolder(itemView: View) : BaseViewHolder<Schedule.Resource>(itemView) {

    companion object {
        const val FUTURE = 0
        const val LIVE = 1
        const val PAST = 2
        const val TAG = "ScheduleResourceViewHolder"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    val binding = ItemScheduleResourceBinding.bind(itemView)

    override fun bind(data: Schedule.Resource) {
        binding.textViewTitle.text = data.title.orEmpty()
        binding.textViewDescription.text = data.description.orEmpty()
        if (data.duration.isNullOrEmpty()) {
            binding.textViewDuration.visibility = View.GONE
        } else {
            binding.textViewDuration.visibility = View.VISIBLE
            binding.textViewDuration.text = data.duration
        }
        binding.imageView.loadImageEtx(data.imageUrl.orEmpty())

        binding.textViewTitle.setTextColor(Utils.parseColor(data.color, R.color.grey))
        binding.sideBar.setBackgroundColor(Utils.parseColor(data.color))

        itemView.setOnClickListener {
            if (data.isPremium == true && data.isVip != true) {
                deeplinkAction.performAction(itemView.context, data.paymentDeeplink)
            } else {
                if (data.resourceType == "pdf") {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.TIME_TABLE_PDF,
                            hashMapOf(
                                EventConstants.SOURCE to data.id.orEmpty(),
                                EventConstants.WIDGET to TAG
                            ), ignoreSnowplow = true
                        )
                    )
                    try {
                        if (!URLUtil.isValidUrl(data.pdfUrl)) {
                            showToast(
                                itemView.context,
                                itemView.context.resources.getString(R.string.notAvalidLink)
                            )
                        } else {
                            if (data.pdfUrl.orEmpty().contains(".html")) {
                                val customTabsIntent = CustomTabsIntent.Builder().build()
                                CustomTabActivityHelper.openCustomTab(
                                    itemView.context,
                                    customTabsIntent,
                                    Uri.parse(data.pdfUrl.orEmpty()),
                                    WebViewFallback()
                                )
                            } else {
                                PdfViewerActivity.previewPdfFromTheUrl(
                                    itemView.context,
                                    data.pdfUrl.orEmpty()
                                )
                            }
                        }
                    } catch (error: ActivityNotFoundException) {
                        showToast(
                            itemView.context,
                            itemView.context.resources.getString(R.string.donothaveanybrowser)
                        )
                    }
                } else if (data.resourceType == "test") {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.TIME_TABLE_TEST,
                            hashMapOf(
                                EventConstants.SOURCE to data.id.orEmpty(),
                                EventConstants.WIDGET to TAG
                            ), ignoreSnowplow = true
                        )
                    )
                    if (data.state == FUTURE) {
                        showToast(
                            itemView.context,
                            itemView.context.getString(R.string.coming_soon)
                        )
                    } else {
                        itemView.context.startActivity(
                            MockTestSubscriptionActivity.getStartIntent(
                                itemView.context,
                                data.testId?.toIntOrNull() ?: 0, false
                            )
                        )
                    }
                } else {
                    val currentContext = itemView.context
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.PLAY_VIDEO_CLICK,
                            hashMapOf(
                                EventConstants.SOURCE to data.id.orEmpty(),
                                EventConstants.WIDGET to TAG
                            )
                        )
                    )
                    if (data.state == LIVE
                        || DateUtils.isBeforeCurrentTime(data.liveAt?.toLongOrNull())
                        || data.state == PAST
                    ) {
                        //allow to watch video
                        openVideoPage(currentContext, data.id, data.page)
                    } else {
                        showToast(currentContext, currentContext.getString(R.string.coming_soon))
                    }
                }
            }
        }
    }

    private fun openVideoPage(context: Context, id: String?, page: String?) {
        context.startActivity(
            VideoPageActivity.startActivity(
                context = context,
                questionId = id.orEmpty(),
                page = page.orEmpty()
            )
        )
    }

}