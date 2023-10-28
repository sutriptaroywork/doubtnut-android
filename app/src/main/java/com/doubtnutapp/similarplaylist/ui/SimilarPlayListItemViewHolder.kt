package com.doubtnutapp.similarplaylist.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import com.apxor.androidsdk.core.Attributes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnutapp.*
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.PlayVideo
import com.doubtnutapp.base.WatchLaterRequest
import com.doubtnutapp.databinding.ItemSimilarPlaylistBinding
import com.doubtnutapp.domain.similarVideo.entities.SimilarPlaylistVideoEntity
import com.doubtnutapp.sharing.VIDEO_CHANNEL
import com.doubtnutapp.utils.ApxorUtils

class SimilarPlayListItemViewHolder(val binding: ItemSimilarPlaylistBinding) :
    BaseViewHolder<SimilarPlaylistVideoEntity>(binding.root) {

    @SuppressLint("SetJavaScriptEnabled")

    companion object {
        const val TAG = "SimilarVideo"
    }

    @SuppressLint("SetJavaScriptEnabled", "RestrictedApi")
    override fun bind(data: SimilarPlaylistVideoEntity) {
        binding.similarVideoData = data
        binding.executePendingBindings()
        color("#223d4e")

        if (data.views != null) {
            binding.questionAskedCount.show()
            binding.questionAskedCount.text = data.views
        } else {
            binding.questionAskedCount.hide()
        }

        if (!data.ocrTextSimilar.isBlank()) {
            binding.dmathView.show()
            binding.dmathView.text = data.ocrTextSimilar
            binding.ivMatch.hide()
            binding.progressBar.hide()
            binding.ivPlayVideo.bringToFront()
        } else {
            binding.dmathView.hide()
            binding.ivMatch.show()
            setImageUrl(data.thumbnailImageSimilar)
            binding.progressBar.show()
            binding.ivPlayVideo.bringToFront()
        }

        binding.clickHelperView.setOnClickListener {
            checkInternetConnection(binding.root.context) {
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
                    true
                }
                menuHelper.show()
            }
        }
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

    fun color(bgColor: String) {
        binding.cardContainer.setBackgroundColor(Color.parseColor("#223d4e"))
    }

    private fun getControlParams(similarPlaylistVideoEntity: SimilarPlaylistVideoEntity): HashMap<String, String> {
        return hashMapOf(
            Constants.PAGE to "SRP",
            Constants.Q_ID to similarPlaylistVideoEntity.questionIdSimilar,
            Constants.PLAYLIST_ID to "",
            Constants.SOLUTION_RESOURCE_TYPE to similarPlaylistVideoEntity.resourceType
        )
    }

}