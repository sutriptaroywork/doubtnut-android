package com.doubtnutapp.matchquestion.ui.fragment.dialog

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.*
import com.doubtnut.core.utils.ViewGroupUtils.applyWidthConstraint
import com.doubtnutapp.*
import com.doubtnutapp.databinding.DialogMatchQuestionP2pBinding
import com.doubtnutapp.domain.base.SolutionResourceType
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.matchquestion.model.ApiMatchedQuestionItem
import com.doubtnutapp.matchquestion.model.UiConfiguration
import com.doubtnutapp.matchquestion.ui.MatchPageConstants
import com.doubtnutapp.matchquestion.viewmodel.MatchQuestionViewModel
import com.doubtnutapp.textsolution.ui.TextSolutionActivity
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.google.android.material.imageview.ShapeableImageView

class MatchQuestionP2pDialogFragment :
    BaseBindingDialogFragment<DummyViewModel, DialogMatchQuestionP2pBinding>(),
    View.OnClickListener,
    View.OnTouchListener {

    companion object {
        const val TAG = "MatchQuestionP2pDialogFragment"
        fun newInstance() = MatchQuestionP2pDialogFragment()
    }

    private var p2pConnectListener: P2pConnectListener? = null

    private lateinit var matchQuestionViewModel: MatchQuestionViewModel

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogMatchQuestionP2pBinding =
        DialogMatchQuestionP2pBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DummyViewModel = viewModelProvider(viewModelFactory)

    override fun provideActivityViewModel() {
        matchQuestionViewModel =
            ViewModelProvider(immediateParentViewModelStoreOwner)[MatchQuestionViewModel::class.java]
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setLayout(getScreenWidth().toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.white)
        setUpUi()
        setClickListeners()
        matchQuestionViewModel.sendEvent(
            EventConstants.MATCH_PAGE_BACK_PRESS_POPUP_VIEWED,
            ignoreSnowplow = true
        )
    }

    private fun setUpUi() {
        mBinding?.tvTitle?.text = String.format(
            resources.getString(R.string.match_page_back_press_title),
            generateRandom().toString() + "%"
        )
        val d0UserData = matchQuestionViewModel.d0UserData
        if (d0UserData != null) {
            mBinding?.p2pLayout?.show()
            mBinding?.p2pMemberImageLayout?.hide()
            mBinding?.tvBottomTitle2?.show()
            mBinding?.tvBottomTitle2?.text = d0UserData.backPressDialogCta
        } else {
            setUpStudentImages(forP2pMembers = false)
            setUpDoubtP2p()
        }
    }

    fun setUpP2pListener(p2pConnectListener: P2pConnectListener) {
        this.p2pConnectListener = p2pConnectListener
    }

    private fun setUpDoubtP2p() {
        val isDoubtP2pEnabled =
            matchQuestionViewModel.getDoubtP2pData() != null && !matchQuestionViewModel.isDoubtP2PConnected
        if (isDoubtP2pEnabled) {
            mBinding?.tvBottomTitle1?.show()
            mBinding?.p2pLayout?.show()
            setUpStudentImages(forP2pMembers = true)
        } else {
            mBinding?.tvBottomTitle1?.hide()
            mBinding?.p2pLayout?.hide()
        }
    }

    private fun generateRandom(): Int = (90..98).random()

    private fun setUpStudentImages(forP2pMembers: Boolean) {
        val studentImages = defaultPrefs().getString(Constants.LOGIN_STUDENT_IMAGES, "")
        if (!studentImages.isNullOrEmpty()) {
            val listOfImages = studentImages.split(",")
            try {
                if (forP2pMembers) {
                    mBinding?.ivp2pMember1?.loadImage(listOfImages[0])
                    mBinding?.ivp2pMember2?.loadImage(listOfImages[1])
                    mBinding?.ivp2pMember3?.loadImage(listOfImages[2])
                } else {
                    mBinding?.ivStudent1?.loadImage(listOfImages[0])
                    mBinding?.ivStudent2?.loadImage(listOfImages[1])
                    mBinding?.ivStudent3?.loadImage(listOfImages[2])
                }
            } catch (e: ArrayIndexOutOfBoundsException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getScreenWidth(): Double {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        return displayMetrics.widthPixels / 1.1
    }

    override fun setupObservers() {
        matchQuestionViewModel.firstMatchLiveData.observe(viewLifecycleOwner) {
            setUpQuestionData(it)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setUpQuestionData(matchedQuestion: ApiMatchedQuestionItem) {
        mBinding?.apply {
            ivPlayVideo.isVisible = matchedQuestion.resourceType == SOLUTION_RESOURCE_TYPE_VIDEO
            when {
                !matchedQuestion.html.isNullOrBlank() && matchedQuestion.resourceType != SolutionResourceType.SOLUTION_RESOURCE_TYPE_DYNAMIC_TEXT -> {
                    mathView.show()
                    dmathView.hide()
                    ivMatch.hide()
                    val webSettings = mathView.settings
                    webSettings.javaScriptEnabled = true
                    mathView.setBackgroundColor(0)
                    val html = matchedQuestion.html.replace("color:white", "color:black")
                    mathView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                }

                else -> decideOnImageOrOcr(matchedQuestion)
            }

            setUiConfiguration(
                textView = tvBottomLeft,
                imageView = ivBottomLeft,
                uiConfiguration = matchedQuestion.source?.bottomLeft
            )
            setUiConfiguration(
                textView = tvBottomCenter,
                imageView = ivBottomCenter,
                uiConfiguration = matchedQuestion.source?.bottomCenter
            )
            setUiConfiguration(
                textView = tvBottomRight,
                imageView = ivBottomRight,
                uiConfiguration = matchedQuestion.source?.bottomRight
            )

            bottomLeftLayout.applyWidthConstraint(
                parentConstraintLayout = bottomLayout,
                widthPercentage = matchedQuestion.source?.bottomLeft?.widthPercentage?.toFloat()
            )

            bottomCenterLayout.applyWidthConstraint(
                parentConstraintLayout = bottomLayout,
                widthPercentage = matchedQuestion.source?.bottomCenter?.widthPercentage?.toFloat()
            )

            bottomRightLayout.applyWidthConstraint(
                parentConstraintLayout = bottomLayout,
                widthPercentage = matchedQuestion.source?.bottomRight?.widthPercentage?.toFloat()
            )
        }
    }

    private fun setUiConfiguration(
        textView: TextView,
        imageView: ShapeableImageView? = null,
        uiConfiguration: UiConfiguration?
    ) {
        if (uiConfiguration == null) {
            textView.gone()
            imageView?.gone()
            return
        }
        textView.apply {
            uiConfiguration.text?.let {
                visible()
                text = uiConfiguration.text
                applyTextColor(uiConfiguration.textColor)
                applyTextGravity(uiConfiguration.textGravity?.toFloat())
                applyAutoSizeTextTypeUniformWithConfiguration(uiConfiguration.textSize)
                applyTypeface(uiConfiguration.isBold)
                updatePadding(
                    (uiConfiguration.padding?.left
                        ?: MatchPageConstants.DEFAULT_TEXT_VIEW_PADDING).dpToPx(),
                    (uiConfiguration.padding?.top
                        ?: MatchPageConstants.DEFAULT_TEXT_VIEW_PADDING).dpToPx(),
                    (uiConfiguration.padding?.right
                        ?: MatchPageConstants.DEFAULT_TEXT_VIEW_PADDING).dpToPx(),
                    (uiConfiguration.padding?.bottom
                        ?: MatchPageConstants.DEFAULT_TEXT_VIEW_PADDING).dpToPx()
                )
                setMargins(
                    (uiConfiguration.margin?.left
                        ?: MatchPageConstants.DEFAULT_TEXT_VIEW_MARGIN).dpToPx(),
                    (uiConfiguration.margin?.top
                        ?: MatchPageConstants.DEFAULT_TEXT_VIEW_MARGIN).dpToPx(),
                    (uiConfiguration.margin?.right
                        ?: MatchPageConstants.DEFAULT_TEXT_VIEW_MARGIN).dpToPx(),
                    (uiConfiguration.margin?.bottom
                        ?: MatchPageConstants.DEFAULT_TEXT_VIEW_MARGIN).dpToPx()
                )
                val backgroundColor: String? = uiConfiguration.backgroundColor
                val gradient: GradientDrawable = Utils.getShape(
                    colorString = backgroundColor
                        ?: MatchPageConstants.DEFAULT_BOTTOM_TITLE_BG_COLOR,
                    strokeColor = uiConfiguration.strokeColor
                        ?: MatchPageConstants.DEFAULT_BOTTOM_TITLE_BG_COLOR,
                    topLeftRadius = (uiConfiguration.cornerRadius?.topLeft
                        ?: MatchPageConstants.DEFAULT_CORNER_RADIUS).toFloat(),
                    topRightRadius = (uiConfiguration.cornerRadius?.topRight
                        ?: MatchPageConstants.DEFAULT_CORNER_RADIUS).toFloat(),
                    bottomRightRadius = (uiConfiguration.cornerRadius?.bottomRight
                        ?: MatchPageConstants.DEFAULT_CORNER_RADIUS).toFloat(),
                    bottomLeftRadius = (uiConfiguration.cornerRadius?.bottomLeft
                        ?: MatchPageConstants.DEFAULT_CORNER_RADIUS).toFloat(),
                    strokeWidth = uiConfiguration.strokeWidth
                        ?: MatchPageConstants.DEFAULT_STROKE_WIDTH
                )
                background = gradient
            } ?: gone()
        }

        imageView?.apply {
            uiConfiguration.iconLink?.let {
                layoutParams.height =
                    (uiConfiguration.iconHeight ?: MatchPageConstants.DEFAULT_ICON_HEIGHT).dpToPx()
                layoutParams.width =
                    (uiConfiguration.iconWidth ?: MatchPageConstants.DEFAULT_ICON_WIDTH).dpToPx()
                requestLayout()
                visible()
                loadImageEtx(it)
            } ?: gone()
        }
    }

    private fun decideOnImageOrOcr(matchedQuestion: ApiMatchedQuestionItem) {
        loadOcr(matchedQuestion.source?.ocrText)
        if (matchedQuestion.questionThumbnailLocalized != null) {
            setImageUrl(matchedQuestion.questionThumbnailLocalized)
        }
    }

    private fun loadOcr(ocrText: String?) {
        mBinding?.apply {
            dmathView.show()
            mathView.hide()
            dmathView.text = ocrText
        }
    }

    private fun setImageUrl(thumbnailUrl: String?) {
        val imageUrl = thumbnailUrl ?: ""
        mBinding?.apply {
            Glide.with(requireContext())
                .load(imageUrl)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?, model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean,
                    ): Boolean {
                        mBinding?.apply {
                            ivMatch.hide()
                            dmathView.show()
                        }

                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable?, model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?, isFirstResource: Boolean,
                    ): Boolean {
                        mBinding?.apply {
                            ivMatch.show()
                            dmathView.hide()
                            mathView.hide()
                        }
                        return false
                    }
                })
                .into(ivMatch)
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setClickListeners() {
        mBinding?.apply {
            ivClose.setOnClickListener(this@MatchQuestionP2pDialogFragment)
            tvBottomTitle2.setOnClickListener(this@MatchQuestionP2pDialogFragment)
            firstSolutionLayout.setOnClickListener(this@MatchQuestionP2pDialogFragment)
            mathView.setOnTouchListener(this@MatchQuestionP2pDialogFragment)
            dmathView.setOnTouchListener(this@MatchQuestionP2pDialogFragment)
            ivMatch.setOnClickListener(this@MatchQuestionP2pDialogFragment)
            ivPlayVideo.setOnClickListener(this@MatchQuestionP2pDialogFragment)
            bottomLayout.setOnClickListener(this@MatchQuestionP2pDialogFragment)
            firstSolutionLayout.setOnClickListener(this@MatchQuestionP2pDialogFragment)
            p2pLayout.setOnClickListener(this@MatchQuestionP2pDialogFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding?.ivClose -> {
                matchQuestionViewModel.sendEvent(
                    event = EventConstants.MATCH_PAGE_BACK_PRESS_POPUP_CROSS_CLICKED,
                    ignoreSnowplow = true
                )
                dismissAllowingStateLoss()
            }

            mBinding?.ivMatch, mBinding?.ivPlayVideo, mBinding?.bottomLayout, mBinding?.firstSolutionLayout -> {
                matchQuestionViewModel.sendEvent(
                    event = EventConstants.MATCH_PAGE_BACK_PRESS_POPUP_VIDEO_PLAYED,
                    ignoreSnowplow = true
                )
                openVideoScreen()
                dismissAllowingStateLoss()
            }

            mBinding?.tvBottomTitle2, mBinding?.p2pLayout -> {
                p2pConnectListener?.openP2pHostAnimationFragment()
            }
        }
    }

    private fun openVideoScreen() {

        val matchQuestion = matchQuestionViewModel.firstMatchLiveData.value ?: return
        val page = if (matchQuestion.resourceType == Constants.DYNAMIC_TEXT) {
            Constants.PAGE_WOLFRAM
        } else {
            Constants.PAGE_SRP
        }

        val questionId = matchQuestion.id
        val parentId = matchQuestionViewModel.parentQuestionId

        if (matchQuestion.resourceType == SOLUTION_RESOURCE_TYPE_VIDEO) {
            VideoPageActivity.startActivity(
                context = requireContext(),
                questionId = questionId,
                playlistId = "",
                mcId = "",
                page = page,
                mcClass = "",
                isMicroConcept = false,
                referredStudentId = "",
                parentId = parentId,
                fromNotificationVideo = false
            ).apply {
                startActivity(this)
            }
        } else {
            TextSolutionActivity.startActivity(
                context = requireContext(),
                questionId = questionId, playlistId = "",
                mcId = "",
                page = page,
                mcClass = "",
                isMicroConcept = false,
                referredStudentId = "",
                parentId = parentId,
                fromNotificationVideo = false
            ).apply {
                startActivity(this)
            }
        }
    }

    interface P2pConnectListener {
        fun openP2pHostAnimationFragment()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if ((v == mBinding?.dmathView || v == mBinding?.mathView) && event?.action == MotionEvent.ACTION_DOWN) {
            openVideoScreen()
            dismissAllowingStateLoss()
        }
        return false
    }
}