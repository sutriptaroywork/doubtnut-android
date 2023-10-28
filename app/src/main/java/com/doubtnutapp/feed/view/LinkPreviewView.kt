package com.doubtnutapp.feed.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.hide
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.YouTubeHelper
import com.doubtnutapp.videoPage.ui.YoutubeTypeVideoActivity
import io.github.ponnamkarthik.richlinkpreview.MetaData
import io.github.ponnamkarthik.richlinkpreview.ResponseListener
import kotlinx.android.synthetic.main.link_preview.view.*
import javax.inject.Inject

class LinkPreviewView(ctx: Context, attrs: AttributeSet) : LinearLayout(ctx, attrs) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var isDetached: Boolean = false

    companion object {
        val cachedLinkPreviews = hashMapOf<String, MetaData>()

        fun linkClickAction(context: Context, link: String): Boolean {

            if (YouTubeHelper.isYoutubeUrl(link)) {
                val youtubeId = YouTubeHelper.extractVideoIdFromUrl(link)
                if (youtubeId != null) {
                    context.startActivity(
                        YoutubeTypeVideoActivity.getStartIntent(
                            context,
                            youtubeId
                        )
                    )
                    return true
                }
            }

            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
            return true
        }

    }

    init {
        LayoutInflater.from(context).inflate(R.layout.link_preview, this, true)
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    fun setLinkPreview(linkText: String) {
        isDetached = false
        var formattedLink = linkText
        if (!linkText.contains("https") && linkText.contains("http")) {
            formattedLink = linkText.replace("http", "https")
        }

        setOnClickListener {
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_FEED_POST_LINK_CLICK, ignoreSnowplow = true))
            linkClickAction(context, formattedLink)
        }

        if (cachedLinkPreviews.containsKey(formattedLink)) {
            setLinkMetadata(cachedLinkPreviews[formattedLink]!!)
            return
        }

        val richPreview = com.doubtnutapp.utils.RichPreview(object : ResponseListener {
            override fun onData(metaData: MetaData) {
                cachedLinkPreviews[formattedLink] = metaData
                setLinkMetadata(metaData)
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
            }
        })

        richPreview.getPreview(formattedLink)
    }

    private fun setLinkMetadata(metaData: MetaData) {

        if (isDetached) return

        if (metaData.title == null || metaData.title.isEmpty()) {
            hide()
            return
        }
        rich_link_image.loadImage(metaData.imageurl, null)
        rich_link_title.text = metaData.title
        rich_link_desp.text = metaData.description
    }

    fun onDetached() {
        this.isDetached = true
    }
}