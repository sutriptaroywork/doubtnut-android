package com.doubtnutapp.feed.view

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.data.remote.models.feed.FeedPostItem
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.feed.FeedPostTypes
import com.doubtnutapp.ui.pdfviewer.PdfViewerActivity
import com.doubtnutapp.video.SimpleVideoFragment
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.doubtnutapp.videoPage.ui.YoutubeTypeVideoActivity
import kotlinx.android.synthetic.main.item_feed_attachment_image.view.*
import kotlinx.android.synthetic.main.item_feed_attachment_pdf.view.*
import kotlinx.android.synthetic.main.view_feed_attachments.view.*
import javax.inject.Inject

class FeedAttachmentView(ctx: Context, attrs: AttributeSet) : LinearLayout(ctx, attrs) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private var isDetached: Boolean = false
    private var source: String? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_feed_attachments, this, true)
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    fun bindData(feedItem: FeedPostItem, source: String) {
        this.source = source
        when (feedItem.type) {
            FeedPostTypes.TYPE_IMAGE -> {
                show()
                hideAllViews()
                if (feedItem.attachments.size == 1) {
                    val imageUrl = feedItem.cdnPath + feedItem.attachments[0]
                    ivAttachment.show()
                    ivAttachment.loadImage(imageUrl, null)
                    ivAttachment.setOnClickListener {
                        openImageDetail(feedItem, 0, source)
                    }
                } else if (feedItem.attachments.size > 1) {
                    rvAttachments.show()
                    rvAttachments.layoutManager = GridLayoutManager(context, 3)
                    rvAttachments.adapter = ImageAttachmentsAdapter(
                        feedItem.attachments, feedItem.cdnPath
                    ) {
                        openImageDetail(feedItem, it, source)
                    }
                } else {
                    hide()
                }
            }
            FeedPostTypes.TYPE_PDF -> {
                show()
                hideAllViews()
                rvAttachments.show()
                rvAttachments.layoutManager =
                    LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                rvAttachments.adapter = PdfAttachmentsAdapter(
                    feedItem.attachments,
                    feedItem.cdnPath,
                    analyticsPublisher
                )
            }
            FeedPostTypes.TYPE_LINK -> {
                hideAllViews()
                if (feedItem.attachments.isNotEmpty()) {
                    show()
                    linkPreview.show()
                    linkPreview.setLinkPreview(feedItem.attachments[0])
                }
            }
            FeedPostTypes.TYPE_VIDEO -> {
                hideAllViews()
                if (feedItem.attachments.isNotEmpty()) {
                    show()
                    ivAttachment.setOnClickListener(null)
                    ivPlayVideo.setOnClickListener(null)
                    ivAttachment.show()
                    ivAttachment.loadImage(feedItem.cdnPath + feedItem.attachments[0], null)
                    ivPlayVideo.show()
                    ivPlayVideo.setOnClickListener {
                        navigateToVideo(feedItem.cdnPath + feedItem.attachments[0])
                    }
                    ivAttachment.setOnClickListener {
                        navigateToVideo(feedItem.cdnPath + feedItem.attachments[0])
                    }
                }
            }
            FeedPostTypes.TYPE_DN_VIDEO -> {
                hideAllViews()
                if (feedItem.videoObj != null) {
                    show()
                    if (feedItem.videoObj.autoPlay ?: false) {
                        videoContainer.show()
                        ivPlayVideo.hide()
                    } else {
                        ivAttachment.show()
                        ivAttachment.setOnClickListener(null)
                        ivPlayVideo.setOnClickListener(null)
                        ivAttachment.loadImage(feedItem.videoObj.thumbnailImage, null)
                        ivPlayVideo.show()
                        ivPlayVideo.setOnClickListener {
                            navigateToDnVideo(
                                feedItem.videoObj.questionId?.toString(),
                                feedItem.videoObj.youtubeId
                            )
                        }
                        ivAttachment.setOnClickListener {
                            navigateToDnVideo(
                                feedItem.videoObj.questionId?.toString(),
                                feedItem.videoObj.youtubeId
                            )
                        }
                    }

                }
            }
            FeedPostTypes.TYPE_LIVE -> {
                hideAllViews()
                if (feedItem.attachments != null && feedItem.attachments.isNotEmpty()) {
                    val imageUrl = feedItem.cdnPath + feedItem.attachments[0]
                    ivAttachment.show()
                    ivAttachment.loadImage(imageUrl, null)
                    ivAttachment.setOnClickListener {
                        openImageDetail(feedItem, 0, source)
                    }
                }

            }
            FeedPostTypes.TYPE_DN_ACTIVITY -> {
                hideAllViews()
                rlGamePost.show()

                if (!feedItem.imageUrl.isNullOrEmpty()) {

                    ivGameImageAttachment.show()
                    ivGameImageAttachment.loadImage(feedItem.imageUrl)
                } else {
                    ivGameImageAttachment.hide()
                }

                if (!TextUtils.isEmpty(feedItem.deeplink)) {
                    rootFeedAttachmentView.setOnClickListener {
                        deeplinkAction.performAction(context, feedItem.deeplink)
                        if (!TextUtils.isEmpty(feedItem.eventName)) {
                            analyticsPublisher.publishEvent(
                                AnalyticsEvent(
                                    feedItem.eventName, hashMapOf(
                                        feedItem.dnActivityType to (feedItem.dnActivityTitle),
                                        EventConstants.SOURCE to "${feedItem.dnActivityType}_${EventConstants.SUFFIX_SOURCE_FEED}"
                                    )
                                )
                            )
                        }
                    }
                }
            }
            else -> {
                hide()
            }
        }
    }

    private fun hideAllViews() {
        ivAttachment.hide()
        linkPreview.hide()
        rvAttachments.hide()
        videoContainer.hide()
        ivPlayVideo.hide()
        rlGamePost.hide()
    }

    private fun navigateToVideo(videoUrl: String) {
        FragmentWrapperActivity.simpleVideo(
            context, SimpleVideoFragment.Companion.VideoData(
                videoUrl = videoUrl,
                aspectRatio = "16:9",
                autoPlay = true,
                startPosition = 0
            ), source, true
        )
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_FEED_POST_VIDEO_PLAY, ignoreSnowplow = true))
    }

    private fun navigateToDnVideo(questionId: String?, youtubeId: String?) {
        if (youtubeId != null) {
            val intent = YoutubeTypeVideoActivity.getStartIntent(context, youtubeId)
            context.startActivity(intent)
        } else {
            val intent = VideoPageActivity.startActivity(
                context,
                questionId.toString(),
                "",
                "",
                Constants.PAGE_COMMUNITY,
                "",
                false,
                "",
                "",
                false
            )
            context.startActivity(intent)
        }
    }

    private fun openImageDetail(feedItem: FeedPostItem, index: Int, source: String?) {
        val images = feedItem.attachments.map { feedItem.cdnPath + it }
        context.startActivity(ImagesPagerActivity.getIntent(context, images, index, true, source))
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_FEED_POST_IMAGE_OPEN, ignoreSnowplow = true))
    }

    class ImageAttachmentsAdapter(
        val images: List<String>,
        val cdnPath: String? = "",
        val imageClickListener: (position: Int) -> Unit
    ) : RecyclerView.Adapter<ImageAttachmentsAdapter.ImageViewHolder>() {

        val MAX_GRID_IMAGES = 6

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            return ImageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_feed_attachment_image, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            holder.itemView.ivImageAttachment.loadImage(cdnPath + images[position], null)

            if (images.size > MAX_GRID_IMAGES && position == MAX_GRID_IMAGES - 1) {
                holder.itemView.viewOverlay.show()
                holder.itemView.tvExtraAttachmentCount.show()
                holder.itemView.tvExtraAttachmentCount.text = "+${images.size - MAX_GRID_IMAGES}"
            }

            holder.itemView.setOnClickListener {
                imageClickListener(position)
            }
        }

        override fun getItemCount(): Int {
            return images.size.coerceAtMost(MAX_GRID_IMAGES)
        }

        class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        }
    }

    class PdfAttachmentsAdapter(
        val pdfs: List<String>,
        val cdnPath: String? = "",
        val analyticsPublisher: AnalyticsPublisher
    ) : RecyclerView.Adapter<PdfAttachmentsAdapter.PdfViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
            return PdfViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_feed_attachment_pdf, parent, false)
            )
        }

        override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
            holder.itemView.tvPdfAttachmentTitle.text = pdfs[position]
            holder.itemView.setOnClickListener {
                PdfViewerActivity.previewPdfFromTheUrl(
                    holder.itemView.context,
                    cdnPath + pdfs[position]
                )
                analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_FEED_POST_PDF_OPEN, ignoreSnowplow = true))
            }
        }

        override fun getItemCount(): Int {
            return pdfs.size
        }

        class PdfViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        linkPreview.onDetached()
    }
}