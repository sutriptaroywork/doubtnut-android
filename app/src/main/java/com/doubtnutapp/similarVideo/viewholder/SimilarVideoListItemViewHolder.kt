package com.doubtnutapp.similarVideo.viewholder

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.Gravity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import com.apxor.androidsdk.core.Attributes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnut.core.utils.gone
import com.doubtnut.core.utils.visible
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OpenPCPopup
import com.doubtnutapp.base.PlayVideo
import com.doubtnutapp.base.WatchLaterRequest
import com.doubtnutapp.databinding.ItemSimilarResultBinding
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_ETOOS_VIDEO
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO
import com.doubtnutapp.sharing.VIDEO_CHANNEL
import com.doubtnutapp.similarVideo.model.SimilarVideoList
import com.doubtnutapp.utils.ApxorUtils
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.vipplan.ui.VipPlanActivity
import com.doubtnutapp.widgets.mathview.OnMathViewRenderListener
import com.doubtnutapp.youtubeVideoPage.ui.adapter.VideoTagListAdapter
import javax.inject.Inject

class SimilarVideoListItemViewHolder(val binding: ItemSimilarResultBinding) :
    BaseViewHolder<SimilarVideoList>(binding.root) {

    @SuppressLint("SetJavaScriptEnabled")

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    companion object {
        const val TAG = "SimilarVideo"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    @SuppressLint("SetJavaScriptEnabled", "RestrictedApi")
    override fun bind(data: SimilarVideoList) {

        binding.similarVideoData = data
        binding.executePendingBindings()

        if (data.resourceType.isBlank()
                .not() && data.resourceType == SOLUTION_RESOURCE_TYPE_ETOOS_VIDEO
        ) {
            binding.overflowMenuSimilar.hide()
        } else {
            binding.overflowMenuSimilar.show()
        }

        if (data.views != null) {
            binding.questionAskedCount.show()
            binding.questionAskedCount.text = (data.views + "+ " + data.viewsText)
        } else {
            binding.questionAskedCount.hide()
        }

        binding.dmathView.isVisible = data.ocrTextSimilar.isNotEmpty()
        if (data.ocrTextSimilar.isNotEmpty()) {
            loadOcr(data.ocrTextSimilar)
        }
        if (data.thumbnailImageSimilar.isNotBlank()) {
            setImageUrl(data.thumbnailImageSimilar)
        }

        if (data.questionTag != null) {
            binding.tvQuestionTag.show()
            binding.tvQuestionTag.text = data.questionTag
        } else {
            binding.tvQuestionTag.hide()
        }

        binding.clickHelperView.setOnClickListener {
            checkInternetConnection(binding.root.context) {
                if (data.contentLock.isLocked != null && data.contentLock.isLocked) {
                    performAction(OpenPCPopup())
                } else {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.SIMILAR_ITEM_CLICK, hashMapOf(
                                EventConstants.ITEM_POSITION to bindingAdapterPosition
                            ), ignoreSnowplow = true
                        )
                    )
                    if (data.resourceType.isBlank()
                            .not() && data.resourceType == SOLUTION_RESOURCE_TYPE_ETOOS_VIDEO
                    ) {
                        if (data.isVip) {
                            performAction(
                                PlayVideo(
                                    data.questionIdSimilar,
                                    Constants.PAGE_SIMILAR,
                                    "",
                                    "",
                                    SOLUTION_RESOURCE_TYPE_VIDEO
                                )
                            )
                        } else {
                            binding.root.context.startActivity(
                                VipPlanActivity.getStartIntent(
                                    context = binding.root.context,
                                    source = "SimilarVideoList",
                                    assortmentId = data.assortmentId,
                                    variantId = data.variantId
                                )
                            )
                        }
                    } else {
                        performAction(
                            PlayVideo(
                                data.questionIdSimilar,
                                Constants.PAGE_SIMILAR,
                                "",
                                "",
                                data.resourceType
                            )
                        )
                    }
                }
            }
        }


        if (data.tagsList.isNotEmpty()) {
            binding.tagsRecyclerView.show()
            val adapter = VideoTagListAdapter(actionPerformer)
            binding.tagsRecyclerView.adapter = adapter
            adapter.updateFeeds(data.tagsList)
        } else {
            binding.tagsRecyclerView.hide()
        }

        binding.overflowMenuSimilar.setOnClickListener {
            PopupMenu(
                ContextThemeWrapper(binding.root.context, R.style.PopupMenuStyle),
                binding.overflowMenuSimilarView,
                Gravity.END
            ).also { popupMenu ->
                popupMenu.menuInflater.inflate(R.menu.menu_popup_similar, popupMenu.menu)

                val menuHelper = MenuPopupHelper(
                    binding.root.context,
                    popupMenu.menu as MenuBuilder,
                    binding.overflowMenuSimilarView
                )
                menuHelper.gravity = Gravity.END
                menuHelper.setForceShowIcon(true)

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when {
                        menuItem.itemId == R.id.itemWatchLaterSimilar -> {
                            performAction(WatchLaterRequest(data.questionIdSimilar))
                        }

                        menuItem.itemId == R.id.itemShareSimilar -> {
                            ApxorUtils.logAppEvent(
                                EventConstants.SHARE_VIDEO_CLICK,
                                Attributes().apply {
                                    putAttribute(EventConstants.QUESTION_ID, data.questionIdSimilar)
                                    putAttribute(EventConstants.SOURCE, TAG)
                                })
                            checkInternetConnection(binding.root.context) {
                                if (FeaturesManager.isFeatureEnabled(
                                        binding.root.context,
                                        Features.SIMILAR_VIDEO_THUMBNAIL
                                    )
                                ) {
                                    if (data.localeThumbnailImageSimilar.isNullOrBlank()) {
                                        analyticsPublisher.publishEvent(
                                            AnalyticsEvent(
                                                EventConstants.EVENT_SIMILAR_VIDEO_THUMBNAIL,
                                                hashMapOf(
                                                    EventConstants.VIDEO_THUMBNAIL_FEATURE_STATUS to FeaturesManager.isFeatureEnabled(
                                                        binding.root.context,
                                                        Features.SIMILAR_VIDEO_THUMBNAIL
                                                    ),
                                                    EventConstants.VIDEO_THUMBNAIL_SHOWN to "english_thumbnail",
                                                    EventConstants.LOCALE_VIDEO_THUMBNAIL_STATUS to data.localeThumbnailImageSimilar.toString(),
                                                    EventConstants.STUDENT_ID to UserUtil.getStudentId()
                                                ), ignoreSnowplow = true
                                            )
                                        )
                                        performAction(
                                            ShareOnWhatApp(
                                                VIDEO_CHANNEL,
                                                "video",
                                                data.thumbnailImageSimilar,
                                                getControlParams(data),
                                                data.bgColorSimilar,
                                                data.sharingMessage,
                                                data.questionIdSimilar
                                            )
                                        )
                                    } else {
                                        analyticsPublisher.publishEvent(
                                            AnalyticsEvent(
                                                EventConstants.EVENT_SIMILAR_VIDEO_THUMBNAIL,
                                                hashMapOf(
                                                    EventConstants.VIDEO_THUMBNAIL_FEATURE_STATUS to FeaturesManager.isFeatureEnabled(
                                                        binding.root.context,
                                                        Features.SIMILAR_VIDEO_THUMBNAIL
                                                    ),
                                                    EventConstants.VIDEO_THUMBNAIL_SHOWN to "locale_thumbnail",
                                                    EventConstants.LOCALE_VIDEO_THUMBNAIL_STATUS to data.localeThumbnailImageSimilar.toString(),
                                                    EventConstants.STUDENT_ID to UserUtil.getStudentId()
                                                ), ignoreSnowplow = true
                                            )
                                        )
                                        performAction(
                                            ShareOnWhatApp(
                                                VIDEO_CHANNEL,
                                                "video",
                                                data.localeThumbnailImageSimilar,
                                                getControlParams(data),
                                                data.bgColorSimilar,
                                                data.sharingMessage,
                                                data.questionIdSimilar
                                            )
                                        )
                                    }
                                } else {
                                    analyticsPublisher.publishEvent(
                                        AnalyticsEvent(
                                            EventConstants.EVENT_SIMILAR_VIDEO_THUMBNAIL, hashMapOf(
                                                EventConstants.VIDEO_THUMBNAIL_FEATURE_STATUS to FeaturesManager.isFeatureEnabled(
                                                    binding.root.context,
                                                    Features.SIMILAR_VIDEO_THUMBNAIL
                                                ),
                                                EventConstants.VIDEO_THUMBNAIL_SHOWN to "english_thumbnail",
                                                EventConstants.LOCALE_VIDEO_THUMBNAIL_STATUS to data.localeThumbnailImageSimilar.toString(),
                                                EventConstants.STUDENT_ID to UserUtil.getStudentId()
                                            ), ignoreSnowplow = true
                                        )
                                    )
                                    performAction(
                                        ShareOnWhatApp(
                                            VIDEO_CHANNEL,
                                            "video",
                                            data.thumbnailImageSimilar,
                                            getControlParams(data),
                                            data.bgColorSimilar,
                                            data.sharingMessage,
                                            data.questionIdSimilar
                                        )
                                    )
                                }

                            }
                        }
                    }
                    true
                }
                menuHelper.show()
            }
        }

    }

    private fun loadOcr(ocrText: String) {
        binding.dmathView.setText(ocrText, object : OnMathViewRenderListener {
            override fun onRenderStarted() {
                binding.progressBar.visible()
            }

            override fun onRenderEnd() {
                binding.progressBar.gone()
            }
        })
    }

    private fun setImageUrl(thumbnailUrl: String) {

        if (!Utils.isValidContextForGlide(binding.root.context)) return

        Glide.with(binding.root.context)
            .load(thumbnailUrl)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?, model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.hide()
                    binding.dmathView.show()
                    binding.ivMatch.hide()
                    binding.ivPlayVideo.bringToFront()

                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?, model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?, isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.hide()
                    binding.dmathView.hide()
                    binding.ivPlayVideo.bringToFront()
                    return false
                }
            })
            .into(binding.ivMatch)
    }

    private fun getControlParams(similarVideoList: SimilarVideoList): HashMap<String, String> {

        return hashMapOf(
            Constants.PAGE to "SRP",
            Constants.Q_ID to similarVideoList.questionIdSimilar,
            Constants.PLAYLIST_ID to "",
            Constants.SOLUTION_RESOURCE_TYPE to similarVideoList.resourceType
        )
    }

}