package com.doubtnutapp.matchquestion.ui.viewholder

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewStub
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.doubtnut.core.utils.*
import com.doubtnut.core.utils.ViewGroupUtils.applyWidthConstraint
import com.doubtnutapp.*
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.PlayMatchedSolutionVideo
import com.doubtnutapp.databinding.ItemMatchResultBinding
import com.doubtnutapp.domain.base.SolutionResourceType
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO
import com.doubtnutapp.matchquestion.model.MatchedQuestionsList
import com.doubtnutapp.matchquestion.model.UiConfiguration
import com.doubtnutapp.matchquestion.ui.MatchPageConstants
import com.doubtnutapp.matchquestion.ui.MatchPageConstants.DEFAULT_BOTTOM_TITLE_BG_COLOR
import com.doubtnutapp.matchquestion.ui.MatchPageConstants.DEFAULT_CARD_VIEW_MARGIN
import com.doubtnutapp.matchquestion.ui.MatchPageConstants.DEFAULT_CARD_VIEW_PADDING
import com.doubtnutapp.matchquestion.ui.MatchPageConstants.DEFAULT_CORNER_RADIUS
import com.doubtnutapp.matchquestion.ui.MatchPageConstants.DEFAULT_ICON_HEIGHT
import com.doubtnutapp.matchquestion.ui.MatchPageConstants.DEFAULT_ICON_WIDTH
import com.doubtnutapp.matchquestion.ui.MatchPageConstants.DEFAULT_STROKE_WIDTH
import com.doubtnutapp.matchquestion.ui.MatchPageConstants.DEFAULT_TEXT_VIEW_MARGIN
import com.doubtnutapp.matchquestion.ui.MatchPageConstants.DEFAULT_TEXT_VIEW_PADDING
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgets.mathview.MathViewSimilar
import com.doubtnutapp.widgets.mathview.OnMathViewRenderListener
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.crashlytics.FirebaseCrashlytics

class MatchQuestionListItemViewHolder(
    val itemView: View,
    val autoPlay: Boolean? = false,
) : BaseViewHolder<MatchedQuestionsList>(itemView) {

    private val binding = ItemMatchResultBinding.bind(itemView)

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    // region view stub
    private var imageViewStub: View? = null
    private var webViewStub: View? = null
    private var mathViewStub: View? = null
    // end region

    @SuppressLint("SetJavaScriptEnabled", "RestrictedApi")
    override fun bind(data: MatchedQuestionsList) {

        with(binding) {

            cardView.apply {
                background = Utils.getShape(
                    colorString = data.canvas?.backgroundColor ?: "#ffffff",
                    strokeColor = data.canvas?.strokeColor ?: "#ffffff",
                    topLeftRadius = (data.canvas?.cornerRadius?.topLeft
                        ?: DEFAULT_CORNER_RADIUS).toFloat(),
                    topRightRadius = (data.canvas?.cornerRadius?.topRight
                        ?: DEFAULT_CORNER_RADIUS).toFloat(),
                    bottomRightRadius = (data.canvas?.cornerRadius?.bottomRight
                        ?: DEFAULT_CORNER_RADIUS).toFloat(),
                    bottomLeftRadius = (data.canvas?.cornerRadius?.bottomLeft
                        ?: DEFAULT_CORNER_RADIUS).toFloat(),
                    strokeWidth = data.canvas?.strokeWidth ?: DEFAULT_STROKE_WIDTH
                )
                updatePadding(
                    (data.canvas?.padding?.left ?: DEFAULT_CARD_VIEW_PADDING).dpToPx(),
                    (data.canvas?.padding?.top ?: DEFAULT_CARD_VIEW_PADDING).dpToPx(),
                    (data.canvas?.padding?.right ?: DEFAULT_CARD_VIEW_PADDING).dpToPx(),
                    (data.canvas?.padding?.bottom ?: DEFAULT_CARD_VIEW_PADDING).dpToPx(),
                )
                updateMargins2(
                    (data.canvas?.margin?.left ?: DEFAULT_CARD_VIEW_MARGIN).dpToPx(),
                    (data.canvas?.margin?.top ?: DEFAULT_CARD_VIEW_MARGIN).dpToPx(),
                    (data.canvas?.margin?.right ?: DEFAULT_CARD_VIEW_MARGIN).dpToPx(),
                    (data.canvas?.margin?.bottom ?: DEFAULT_CARD_VIEW_MARGIN).dpToPx()
                )
            }

            setUiConfiguration(
                textView = tvTopLeft,
                imageView = null,
                uiConfiguration = data.source?.topLeft,
                cardBackgroundColor = data.canvas?.backgroundColor
            )
            setUiConfiguration(
                textView = tvTopRight,
                imageView = null,
                uiConfiguration = data.source?.topRight,
                cardBackgroundColor = data.canvas?.backgroundColor
            )
            setUiConfiguration(
                textView = tvBottomLeft,
                imageView = ivBottomLeft,
                uiConfiguration = data.source?.bottomLeft,
                cardBackgroundColor = data.canvas?.backgroundColor
            )
            setUiConfiguration(
                textView = tvBottomCenter,
                imageView = ivBottomCenter,
                uiConfiguration = data.source?.bottomCenter,
                cardBackgroundColor = data.canvas?.backgroundColor
            )
            setUiConfiguration(
                textView = tvBottomRight,
                imageView = ivBottomRight,
                uiConfiguration = data.source?.bottomRight,
                cardBackgroundColor = data.canvas?.backgroundColor
            )

            bottomLeftLayout.applyWidthConstraint(
                parentConstraintLayout = bottomLayout,
                widthPercentage = data.source?.bottomLeft?.widthPercentage?.toFloat()
            )
            bottomCenterLayout.applyWidthConstraint(
                parentConstraintLayout = bottomLayout,
                widthPercentage = data.source?.bottomCenter?.widthPercentage?.toFloat()
            )
            bottomRightLayout.applyWidthConstraint(
                parentConstraintLayout = bottomLayout,
                widthPercentage = data.source?.bottomRight?.widthPercentage?.toFloat()
            )

            // dynamic_text has HTML but it is the full solution and we do not want to show it at first, so we'll show OCR
            when {
                data.isHtmlPresent() && data.resourceType != SolutionResourceType.SOLUTION_RESOURCE_TYPE_DYNAMIC_TEXT -> {
                    imageViewStub?.gone()
                    mathViewStub?.gone()
                    viewStubWebview.apply {
                        setOnInflateListener { _, inflated ->
                            webViewStub = inflated
                            setQuestionHtml(data.html)
                        }
                    }
                    val viewStub = binding.root.findViewById<ViewStub>(R.id.view_stub_webview)
                    if (viewStub == null) {
                        webViewStub?.visible()
                    } else {
                        if (viewStub.parent != null) {
                            viewStub.inflate()
                        }
                    }
                }

                else -> {
                    webViewStub?.gone()
                    if (data.imageLoadingOrder?.isNotEmpty() == true) {
                        renderAsPerOrder(data)
                    } else {
                        decideOnImageOrOcr(data)
                    }
                    setVisibilityOfPlayIcon(data.resourceType)
                }
            }

            clickHelperView.setOnClickListener {
                playMatchSolution(data)
            }
        }
    }

    private fun playMatchSolution(matchedQuestion: MatchedQuestionsList) {
        checkInternetConnection(binding.root.context) {
            val page = if (matchedQuestion.resourceType == Constants.DYNAMIC_TEXT) {
                Constants.PAGE_WOLFRAM
            } else {
                Constants.PAGE_SRP
            }
            performAction(
                PlayMatchedSolutionVideo(
                    id = matchedQuestion.id,
                    answerId = matchedQuestion.answerId,
                    viewType = matchedQuestion.viewType,
                    currentPosition = matchedQuestion.currentPosition,
                    showContinueWatching = matchedQuestion.showContinueWatching,
                    videoResource = matchedQuestion.videoResource,
                    resourceType = matchedQuestion.resourceType,
                    html = matchedQuestion.html,
                    ocrText = matchedQuestion.source?.ocrText,
                    page = page,
                    position = absoluteAdapterPosition
                )
            )
        }
    }

    private fun setUiConfiguration(
        textView: TextView,
        imageView: ShapeableImageView? = null,
        uiConfiguration: UiConfiguration?,
        cardBackgroundColor: String?
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
                    (uiConfiguration.padding?.left ?: DEFAULT_TEXT_VIEW_PADDING).dpToPx(),
                    (uiConfiguration.padding?.top ?: DEFAULT_TEXT_VIEW_PADDING).dpToPx(),
                    (uiConfiguration.padding?.right ?: DEFAULT_TEXT_VIEW_PADDING).dpToPx(),
                    (uiConfiguration.padding?.bottom ?: DEFAULT_TEXT_VIEW_PADDING).dpToPx()
                )
                setMargins(
                    (uiConfiguration.margin?.left ?: DEFAULT_TEXT_VIEW_MARGIN).dpToPx(),
                    (uiConfiguration.margin?.top ?: DEFAULT_TEXT_VIEW_MARGIN).dpToPx(),
                    (uiConfiguration.margin?.right ?: DEFAULT_TEXT_VIEW_MARGIN).dpToPx(),
                    (uiConfiguration.margin?.bottom ?: DEFAULT_TEXT_VIEW_MARGIN).dpToPx()
                )
                val backgroundColor: String? =
                    uiConfiguration.backgroundColor ?: cardBackgroundColor
                val gradient: GradientDrawable = Utils.getShape(
                    colorString = backgroundColor ?: DEFAULT_BOTTOM_TITLE_BG_COLOR,
                    strokeColor = uiConfiguration.strokeColor ?: DEFAULT_BOTTOM_TITLE_BG_COLOR,
                    topLeftRadius = (uiConfiguration.cornerRadius?.topLeft
                        ?: DEFAULT_CORNER_RADIUS).toFloat(),
                    topRightRadius = (uiConfiguration.cornerRadius?.topRight
                        ?: DEFAULT_CORNER_RADIUS).toFloat(),
                    bottomRightRadius = (uiConfiguration.cornerRadius?.bottomRight
                        ?: DEFAULT_CORNER_RADIUS).toFloat(),
                    bottomLeftRadius = (uiConfiguration.cornerRadius?.bottomLeft
                        ?: DEFAULT_CORNER_RADIUS).toFloat(),
                    strokeWidth = uiConfiguration.strokeWidth ?: DEFAULT_STROKE_WIDTH
                )
                background = gradient
            } ?: gone()
        }

        imageView?.apply {
            uiConfiguration.iconLink?.let {
                layoutParams.height = (uiConfiguration.iconHeight ?: DEFAULT_ICON_HEIGHT).dpToPx()
                layoutParams.width = (uiConfiguration.iconWidth ?: DEFAULT_ICON_WIDTH).dpToPx()
                requestLayout()
                visible()
                loadImageEtx(it)
            } ?: gone()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setQuestionHtml(html: String?) {
        val webview: WebView? = webViewStub?.findViewById(R.id.webview)
        webview?.apply {
            val webSettings = settings
            webSettings.javaScriptEnabled = true
            setBackgroundColor(0)
            val htmlString = html?.replace("color:white", "color:black")
            loadDataWithBaseURL(null, htmlString.orEmpty(), "text/html", "UTF-8", null)
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    binding.progressBar.visible()
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.progressBar.gone()
                }
            }
        }
    }

    /**
     * This method tries to render between localized image, default image and ocr as order
     * defined in api response using recursion
     */
    private fun renderAsPerOrder(matchedQuestion: MatchedQuestionsList) {
        load(0, matchedQuestion)
    }

    private fun load(nextIndex: Int, matchedQuestion: MatchedQuestionsList) {
        if (binding.root.context?.isRunning() != true) return
        val imageLoadingOrder = matchedQuestion.imageLoadingOrder ?: return
        if (nextIndex >= imageLoadingOrder.size) { // Base condition
            binding.progressBar.gone()
            return
        }
        when (imageLoadingOrder[nextIndex]) {
            MatchPageConstants.LOCALIZED_IMAGE -> {
                if (matchedQuestion.questionThumbnailLocalized.isNotNullAndNotEmpty()) {
                    setUpImageViewStub(
                        thumbnailUrl = matchedQuestion.questionThumbnailLocalized,
                        nextIndex = nextIndex + 1,
                        matchedQuestion = matchedQuestion
                    )
                } else {
                    imageViewStub?.gone()
                    load(
                        nextIndex = nextIndex + 1,
                        matchedQuestion = matchedQuestion
                    )
                }
            }
            MatchPageConstants.DEFAULT_IMAGE -> {
                if (matchedQuestion.questionThumbnail.isNotNullAndNotEmpty()) {
                    setUpImageViewStub(
                        thumbnailUrl = matchedQuestion.questionThumbnail,
                        nextIndex = nextIndex + 1,
                        matchedQuestion = matchedQuestion
                    )
                } else {
                    imageViewStub?.gone()
                    load(
                        nextIndex = nextIndex + 1,
                        matchedQuestion = matchedQuestion
                    )
                }
            }
            MatchPageConstants.OCR -> {
                if (matchedQuestion.source?.ocrText.isNotNullAndNotEmpty2()) {
                    setUpMathViewStub(ocrText = matchedQuestion.source?.ocrText)
                } else {
                    mathViewStub?.gone()
                    load(
                        nextIndex = nextIndex + 1,
                        matchedQuestion = matchedQuestion
                    )
                }
            }
        }
    }

    private fun setUpImageViewStub(
        thumbnailUrl: String?,
        nextIndex: Int? = null,
        matchedQuestion: MatchedQuestionsList? = null,
    ) {
        binding.viewStubImageview.apply {
            setOnInflateListener { _, inflated ->
                imageViewStub = inflated
                loadQuestionImage(thumbnailUrl, nextIndex, matchedQuestion)
            }
        }
        val viewStub = binding.root.findViewById<ViewStub>(R.id.view_stub_imageview)
        if (viewStub == null) {
            imageViewStub?.visible()
            loadQuestionImage(thumbnailUrl, nextIndex, matchedQuestion)
        } else {
            if (viewStub.parent != null) {
                viewStub.inflate()
            }
        }
    }

    private fun loadQuestionImage(
        thumbnailUrl: String?,
        nextIndex: Int? = null,
        matchedQuestion: MatchedQuestionsList? = null,
    ) {
        if (binding.root.context?.isRunning() != true) return
        val imageView = imageViewStub?.findViewById<ImageView>(R.id.imageview) ?: return
        imageView.visible()
        Glide.with(binding.root.context)
            .load(thumbnailUrl.orEmpty())
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?, model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean,
                ): Boolean {
                    imageViewStub?.gone()
                    if (nextIndex != null && matchedQuestion != null) {
                        Handler(Looper.getMainLooper()).post {
                            load(
                                nextIndex = nextIndex,
                                matchedQuestion = matchedQuestion
                            )
                        }
                    }
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?, model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?, isFirstResource: Boolean,
                ): Boolean {
                    mathViewStub?.gone()
                    binding.progressBar.gone()
                    return false
                }
            })
            .into(imageView)
    }

    private fun decideOnImageOrOcr(matchedQuestion: MatchedQuestionsList) {
        setUpMathViewStub(matchedQuestion.source?.ocrText)
        if (matchedQuestion.questionThumbnailLocalized != null) {
            setUpImageViewStub(matchedQuestion.questionThumbnailLocalized)
        } else {
            imageViewStub?.gone()
        }
    }

    private fun setUpMathViewStub(ocrText: String?) {
        binding.viewStubMathView.apply {
            setOnInflateListener { _, inflated ->
                mathViewStub = inflated
                setOcrText(ocrText)
            }
        }
        val viewStub = binding.root.findViewById<ViewStub>(R.id.view_stub_math_view)
        if (viewStub == null) {
            mathViewStub?.visible()
            setOcrText(ocrText)
        } else {
            if (viewStub.parent != null) {
                viewStub.inflate()
            }
        }
    }

    private fun setOcrText(ocrText: String?) {
        mathViewStub?.findViewById<MathViewSimilar>(R.id.mathView)?.apply {
            visible()
            setText(ocrText, (object :
                OnMathViewRenderListener {
                override fun onRenderStarted() {
                    binding.progressBar.visible()
                }

                override fun onRenderEnd() {
                    binding.progressBar.gone()
                }

                override fun onReceiveError(
                    errorCode: Int,
                    description: String,
                    failingUrl: String
                ) {
                    binding.progressBar.gone()
                    FirebaseCrashlytics.getInstance()
                        .recordException(Throwable("${MatchPageConstants.ERROR_TAG}_${errorCode}-$description"))
                }
            }))
        }
    }

    private fun setVisibilityOfPlayIcon(resourceType: String) {
        if (resourceType == SOLUTION_RESOURCE_TYPE_VIDEO) {
            binding.ivPlayVideo.visible()
            binding.ivPlayVideo.bringToFront()
        } else {
            binding.ivPlayVideo.gone()
        }
    }
}