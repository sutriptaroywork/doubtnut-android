package com.doubtnutapp.similarVideo.viewholder

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.doubtnut.core.utils.gone
import com.doubtnut.core.utils.visible
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.PlayVideo
import com.doubtnutapp.databinding.ItemSimilarResultBinding
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_ETOOS_VIDEO
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO
import com.doubtnutapp.similarVideo.model.SimilarVideoList
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.vipplan.ui.VipPlanActivity
import com.doubtnutapp.widgets.mathview.OnMathViewRenderListener
import javax.inject.Inject

class LandscapeSimilarVideoListItemViewHolder(val binding: ItemSimilarResultBinding) :
    BaseViewHolder<SimilarVideoList>(binding.root) {

    @SuppressLint("SetJavaScriptEnabled")

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    companion object {
        const val TAG = "LandscapeSimilarVideoListItemViewHolder"
    }

    @SuppressLint("SetJavaScriptEnabled")
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

        binding.bottomLayout.hide()
        binding.tagsRecyclerView.hide()
        binding.bottomLine.hide()
        binding.overflowMenuSimilar.hide()
        binding.overflowMenuSimilarView.hide()

        binding.dmathView.isVisible = data.ocrTextSimilar.isNotEmpty()
        if (data.ocrTextSimilar.isNotEmpty()) {
            loadOcr(data.ocrTextSimilar)
        }
        if (data.thumbnailImageSimilar.isNotBlank()) {
            setImageUrl(data.thumbnailImageSimilar)
        }

        binding.clickHelperView.setOnClickListener {
            checkInternetConnection(binding.root.context) {
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
                                binding.root.context,
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

}