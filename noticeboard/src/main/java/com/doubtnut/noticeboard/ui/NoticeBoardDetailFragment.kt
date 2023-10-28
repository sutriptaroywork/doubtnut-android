package com.doubtnut.noticeboard.ui

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.dummy.CoreDummyVM
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.ui.base.CoreBindingFragment
import com.doubtnut.core.utils.CoreUserUtils
import com.doubtnut.core.utils.isRunning
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnut.noticeboard.NoticeBoardConstants
import com.doubtnut.noticeboard.data.entity.NoticeBoardItem
import com.doubtnut.noticeboard.databinding.FragmentNoticeBoardDetailBinding
import javax.inject.Inject

class NoticeBoardDetailFragment :
    CoreBindingFragment<CoreDummyVM, FragmentNoticeBoardDetailBinding>() {

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    private val noticeBoardItem: NoticeBoardItem
        get() = requireArguments().getParcelable(KEY_NOTICE_BOARD_ITEM)!!

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNoticeBoardDetailBinding {
        return FragmentNoticeBoardDetailBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): CoreDummyVM {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        init()
        initListener()
    }

    private fun init() {
        binding.tvTitle.text = noticeBoardItem.title
        binding.tvTitle.isVisible = !noticeBoardItem.title.isNullOrEmpty()

        binding.tvSubtitle.text = noticeBoardItem.subtitle
        binding.tvSubtitle.isVisible = !noticeBoardItem.subtitle.isNullOrEmpty()


        if (noticeBoardItem.imageLink.isNullOrEmpty()) {
            isImageLoading = false
            resumeStatus()
        } else {
            isImageLoading = true
            pauseStatus()
            Glide.with(requireContext()).load(noticeBoardItem.imageLink ?: return)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        isImageLoading = false
                        resumeStatus()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        isImageLoading = false
                        resumeStatus()
                        return false
                    }
                })
                .into(binding.ivImage)
        }

        binding.tvCaption.text = noticeBoardItem.caption
        binding.tvCaption.isVisible = !noticeBoardItem.caption.isNullOrEmpty()

        binding.tvCta.text = noticeBoardItem.ctaText
        binding.tvCta.isVisible = !noticeBoardItem.ctaText.isNullOrEmpty()
    }

    private fun resumeStatus() {
        if (activity is NoticeBoardDetailActivity && activity.isRunning()) {
            (activity as? NoticeBoardDetailActivity)?.resumeStatus()
        }
    }

    private fun pauseStatus() {
        if (activity is NoticeBoardDetailActivity && activity.isRunning()) {
            (activity as? NoticeBoardDetailActivity)?.pauseStatus()
        }
    }

    private fun initListener() {
        binding.tvCta.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    name = EventConstants.DN_NB_CTA_CLICKED,
                    params = hashMapOf(
                        EventConstants.TYPE to noticeBoardItem.type.orEmpty(),
                        NoticeBoardConstants.NB_ID to noticeBoardItem.id.orEmpty(),
                        EventConstants.CTA_TEXT to noticeBoardItem.ctaText.orEmpty(),
                        EventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                        EventConstants.STUDENT_CLASS to CoreUserUtils.getStudentClass(),
                        EventConstants.BOARD to CoreUserUtils.getUserBoard(),
                    ),
                    ignoreFacebook = true
                )
            )
            if (!noticeBoardItem.deepLink.isNullOrEmpty()) {
                deeplinkAction.performAction(requireContext(), noticeBoardItem.deepLink)
            }
        }
    }

    companion object {

        var isImageLoading = false

        private const val KEY_NOTICE_BOARD_ITEM = "notice_board_item"
        private const val TAG = "NoticeBoardDetailFragment"

        fun newInstance(noticeBoardItem: NoticeBoardItem) = NoticeBoardDetailFragment().apply {
            arguments = Bundle().apply {
                putParcelable(KEY_NOTICE_BOARD_ITEM, noticeBoardItem)
            }

        }
    }

}