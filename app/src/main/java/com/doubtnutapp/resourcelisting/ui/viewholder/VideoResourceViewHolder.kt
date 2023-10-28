package com.doubtnutapp.resourcelisting.ui.viewholder

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentManager
import com.airbnb.lottie.LottieDrawable
import com.apxor.androidsdk.core.Attributes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.setMargins
import com.doubtnutapp.*
import com.doubtnutapp.base.AutoplayVideoViewHolder
import com.doubtnutapp.base.PlayVideo
import com.doubtnutapp.base.WatchLaterRequest
import com.doubtnutapp.databinding.ItemVideoResourceBinding
import com.doubtnutapp.resourcelisting.model.QuestionMetaDataModel
import com.doubtnutapp.sharing.VIDEO_CHANNEL
import com.doubtnutapp.utils.ApxorUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.Utils.getSecondsToString
import com.doubtnutapp.utils.Utils.screenWidth

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
class VideoResourceViewHolder(
    fm: FragmentManager,
    private val binding: ItemVideoResourceBinding,
    private val playListId: String,
    private val isFromVideoTag: Boolean,
    private val page: String?
) : AutoplayVideoViewHolder<QuestionMetaDataModel>(fm, binding.root, page, playListId) {

    private lateinit var watchVideoModel: QuestionMetaDataModel

    companion object {
        const val TAG = "VideoPlaylist"
    }

    @SuppressLint("RestrictedApi")
    override fun bind(watchVideo: QuestionMetaDataModel) {

        if (watchVideo.isFullWidthCard.not()) {
            binding.root.updateLayoutParams {
                binding.root.setMargins(left = 0, top = 0.dpToPx(), right = 16.dpToPx(), bottom = 0)
                width = Utils.getWidthFromScrollSize(binding.root.context, "1.2") -
                        (binding.root.marginStart + binding.root.marginEnd)
                height = ((9f / 16) * width).toInt()
            }
            binding.rootCardView.cardElevation = 6f.dpToPx()
        } else {
            binding.root.updateLayoutParams {
                binding.root.setMargins(left = 0, top = 0.dpToPx(), right = 0, bottom = 8.dpToPx())
                width = ViewGroup.LayoutParams.MATCH_PARENT

                height = if (watchVideo.heightRatio == null) {
                    ViewGroup.LayoutParams.WRAP_CONTENT
                } else {
                    (watchVideo.heightRatio!! * screenWidth).toInt()
                }
            }
            binding.rootCardView.cardElevation = 0f
        }

        this.watchVideoModel = watchVideo

        binding.videoPlaylist = watchVideo

        binding.executePendingBindings()

        binding.timeDurationTextView.text = getSecondsToString(watchVideo.videoDuration)
        if (!watchVideo.bgColor.isNullOrBlank()) binding.cardContainer.setBackgroundColor(
            Color.parseColor(
                watchVideo.bgColor
            )
        ) else binding.cardContainer.setBackgroundColor(Color.parseColor("#ffffff"))

        handleView(watchVideo)

        binding.clickHelperView.setOnClickListener {
            checkInternetConnection(binding.root.context) {
                performAction(
                    PlayVideo(
                        watchVideoModel.questionId,
                        getCurrentPage(),
                        playListId,
                        "",
                        watchVideoModel.resourceType,
                        adapterPosition
                    )
                )
            }
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
                    when (menuItem.itemId) {
                        R.id.itemWatchLaterSimilar -> {
                            performAction(WatchLaterRequest(watchVideo.questionId))
                        }
                        R.id.itemShareSimilar -> {
                            ApxorUtils.logAppEvent(
                                EventConstants.SHARE_VIDEO_CLICK,
                                Attributes().apply {
                                    putAttribute(
                                        EventConstants.QUESTION_ID, watchVideo.questionId
                                            ?: ""
                                    )
                                    putAttribute(EventConstants.SOURCE, TAG)
                                })

                            checkInternetConnection(binding.root.context) {
                                performAction(
                                    ShareOnWhatApp(
                                        VIDEO_CHANNEL,
                                        "video",
                                        watchVideo.questionThumbnailImage,
                                        getControlParams(watchVideo),
                                        watchVideo.bgColor ?: "#000000",
                                        watchVideo.sharingMessage,
                                        watchVideo.questionId
                                    )
                                )
                            }
                        }
                    }
                    true
                }
                menuHelper.show()
            }
        }

        if (watchVideo.label != null) {
            binding.tvLabel.apply {
                show()
                text = watchVideo.label
                setTextColor(Utils.parseColor(watchVideo.labelColor, Color.BLACK))
            }
        } else {
            binding.tvLabel.hide()
        }

        when (watchVideo.isPlaying) {
            true -> {
                binding.autoPlayAnim.show()
                binding.autoPlayAnim.setAnimation("lottie_autoplay_animation.zip")
                binding.autoPlayAnim.repeatCount = LottieDrawable.INFINITE
                binding.autoPlayAnim.playAnimation()
                binding.viewLayer.show()
            }
            else -> {
                binding.autoPlayAnim.pauseAnimation()
                binding.autoPlayAnim.clearAnimation()
                binding.autoPlayAnim.hide()
                binding.viewLayer.hide()
            }
        }

        when (watchVideo.isPlaying) {
            true -> {
                binding.autoPlayAnim.show()
                binding.autoPlayAnim.setAnimation("lottie_autoplay_animation.zip")
                binding.autoPlayAnim.repeatCount = LottieDrawable.INFINITE
                binding.autoPlayAnim.playAnimation()
                binding.viewLayer.show()
            }
            else -> {
                binding.autoPlayAnim.pauseAnimation()
                binding.autoPlayAnim.clearAnimation()
                binding.autoPlayAnim.hide()
                binding.viewLayer.hide()
            }
        }

        super.bind(watchVideo)
    }

    private fun getControlParams(watchVideo: QuestionMetaDataModel): HashMap<String, String> {

        return hashMapOf(
            Constants.PAGE to "SRP",
            Constants.Q_ID to watchVideo.questionId,
            Constants.PLAYLIST_ID to playListId,
            Constants.SOLUTION_RESOURCE_TYPE to watchVideo.resourceType
        )
    }

    private fun setImageUrl(thumbnailUrl: String?) {
        val imageUrl = thumbnailUrl ?: ""
        Glide.with(binding.root.context)
            .load(imageUrl)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.hide()
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.hide()
                    return false
                }

            })
            .into(binding.ivMatch)
    }

    override fun toggleShowAutoplayVideo(showVideo: Boolean, data: QuestionMetaDataModel) {
        binding.videoContainer.isVisible = showVideo
        binding.textViewVideoDetails.isVisible = showVideo
        if (showVideo) {
            binding.progressBar.visibility = View.GONE
            binding.dmathView.visibility = View.GONE
            binding.ivMatch.visibility = View.GONE
            binding.playVideoImageView.visibility = View.GONE
            binding.overflowMenuSimilar.hide()
        } else {
            handleView(data)
        }
    }

    private fun handleView(watchVideo: QuestionMetaDataModel) {
        binding.overflowMenuSimilar.isVisible = watchVideo.hideOverflowMenu.not()
        binding.playVideoImageView.visibility = View.VISIBLE
        binding.overflowMenuSimilar.isVisible = watchVideo.hideOverflowMenu.not()
        binding.playVideoImageView.isVisible = watchVideo.isPlaying != true
        if (!watchVideo.ocrText.isNullOrBlank()) {
            binding.dmathView.show()
            watchVideo.ocrTextFontSize?.let {
                binding.dmathView.setFontSize(it)
            }
            binding.dmathView.text = watchVideo.ocrText
            binding.ivMatch.hide()
            binding.progressBar.hide()
            binding.viewBottom.show()
            binding.playVideoImageView.bringToFront()
        } else {
            binding.dmathView.hide()
            binding.ivMatch.show()
            binding.viewBottom.hide()
            setImageUrl(watchVideo.questionThumbnailImage)
            binding.progressBar.show()
            binding.playVideoImageView.bringToFront()
        }
    }

    override fun getAutoplayVideoPage(): String {
        return page ?: getCurrentPage() + "_AUTO"
    }

    private fun getCurrentPage(): String {
        var page = Constants.PAGE_LIBRARY
        if (isFromVideoTag) {
            page = Constants.PAGE_BROWSE
        }
        if (this.page == Constants.PAGE_SEARCH_SRP) {
            page = this.page
        }
        return page
    }
}