package com.doubtnutapp.matchquestion.ui.fragment.bottomsheet

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.airbnb.lottie.LottieDrawable
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.CoreRemoteConfigUtils
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.toast
import com.doubtnut.core.view.NestedWebView
import com.doubtnutapp.*
import com.doubtnutapp.Constants.SCALE_DOWN_IMAGE_AREA
import com.doubtnutapp.Constants.SCALE_DOWN_IMAGE_QUALITY
import com.doubtnutapp.R
import com.doubtnutapp.base.extension.getDatabase
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.databinding.FragmentMatchBottomSheetBinding
import com.doubtnutapp.doubtpecharcha.ui.fragment.P2pHostIntroductionFragment
import com.doubtnutapp.librarylisting.ui.LibraryListingActivity
import com.doubtnutapp.matchquestion.listener.*
import com.doubtnutapp.matchquestion.model.MatchQuestion
import com.doubtnutapp.matchquestion.ui.activity.NoInternetRetryActivity
import com.doubtnutapp.matchquestion.ui.adapter.MatchQuestionViewPagerAdapter
import com.doubtnutapp.matchquestion.ui.fragment.MatchPageCarousalsFragment
import com.doubtnutapp.matchquestion.ui.fragment.MatchQuestionFragment
import com.doubtnutapp.matchquestion.ui.fragment.MatchQuestionWebViewFragment
import com.doubtnutapp.matchquestion.ui.fragment.dialog.BlurQuestionImageDialogFragment
import com.doubtnutapp.matchquestion.ui.fragment.dialog.HandWrittenQuestionDialogFragment
import com.doubtnutapp.matchquestion.ui.fragment.dialog.MatchQuestionBookFeedbackDialogFragment
import com.doubtnutapp.matchquestion.ui.fragment.dialog.MatchQuestionP2pDialogFragment
import com.doubtnutapp.matchquestion.viewmodel.MatchQuestionViewModel
import com.doubtnutapp.screennavigator.NavigationModel
import com.doubtnutapp.screennavigator.VideoScreen
import com.doubtnutapp.textsolution.ui.TextSolutionActivity
import com.doubtnutapp.ui.ask.SelectImageDialog
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.doubtnutapp.widgets.viewpagerbottomsheetbehavior.BottomSheetUtils
import com.doubtnutapp.widgets.viewpagerbottomsheetbehavior.LockableViewPagerBottomSheetBehavior
import com.doubtnutapp.widgets.viewpagerbottomsheetbehavior.ViewPagerBottomSheetBehavior
import com.doubtnutapp.workmanager.workers.MatchesByFileNameWorker
import com.google.android.material.tabs.TabLayout
import com.uxcam.UXCam
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MatchBottomSheetFragment :
    BaseBindingFragment<MatchQuestionViewModel, FragmentMatchBottomSheetBinding>(),
    View.OnClickListener,
    OnImageSelectListener,
    DialogInterface.OnDismissListener,
    DialogInterface.OnShowListener,
    MatchPageFilterListener,
    FilterDataListener,
    NestedWebView.OnOverScrolledCallback {

    @Inject
    lateinit var networkUtil: NetworkUtil

    @Inject
    lateinit var userPreference: UserPreference

    private lateinit var matchQuestionViewModel: MatchQuestionViewModel

    private lateinit var matchQuestionPagerAdapter: MatchQuestionViewPagerAdapter

    private var questionText: String = ""
    private var questionIdFromNotification: String = ""
    private var isMatchFromInApp: Boolean = false
    private var askedQuestionImageUri: String? = null
    private var askedQuestionImageUrl: String? = null
    private var uploadedImageQuestionId: Long? = null
    private var bottomNavigationBottomMargin: Int = 0

    private var questionId: String = ""
    private var imageUrl: String = ""

    var didWatchVideo: Boolean = false
    var tabDefaultSelectedOnce = false

    private var isMatchedQuestionFetched = false

    private var isMatchPageLoading = true

    private var alertDialog: AlertDialog? = null

    private val imageFileName: String by lazy {
        "uploads_" + getStudentId() + "_" + System.currentTimeMillis() / 1000 + ".png"
    }

    private var refreshResultOnFacetUnselect: Boolean = false

    private var autoPlay: Boolean? = null

    private var cropExperimentVariant = -1

    private var canShowAdvancedSearch: Boolean = false

    private lateinit var bottomSheetBehavior: LockableViewPagerBottomSheetBehavior<View>

    private var mWebViewOldScrollY: Int = 0

    private var mMatchBottomSheetFragmentCallbacks: MatchBottomSheetFragmentCallbacks? = null

    private var matchResult: MatchQuestion? = null

    private var screenTime: Long? = null
    private var selectedTab: String? = null

    private var isMatchPageScrollable: Boolean = false

    companion object {

        const val TAG = "MatchBottomSheet"

        const val EXTRA_TYPED_QUESTION_TEXT = "typed_question_text"
        const val EXTRA_ASKED_QUE_URI = "ask_que_uri"
        const val NO_MATCH_FOUND_ERROR_DIALOG = "NoMatchFoundErrorDialog"
        const val BLUR_QUESTION_IMAGE_ERROR_DIALOG = "BlurQuestionImageError"
        const val SOURCE = "SOURCE"
        const val IMAGE_FILE_NAME = "image_file_name"
        const val EXTRA_FROM_IN_APP = "match_from_in_app"
        const val EXTRA_ASKED_QUE_IMAGE_URL = "ask_que_url"
        const val EXTRA_UPLOADED_IMAGE_QUESTION_ID = "uploaded_image_question_id"
        const val EXTRA_BOTTOM_NAVIGATION_BOTTOM_MARGIN = "bottom_navigation_bottom_margin"
        const val AUTOPLAY_STATE = "autoplay_state"
        const val BOTTOM_SHEET_SLIDE_THRESHOLD = .80

        fun newInstance(
            questionImageUri: String?,
            questionText: String,
            source: String,
            imageUrl: String? = null,
            uploadedImageQuestionId: Long? = null,
            bottomNavigationBottomMargin: Int,
        ): MatchBottomSheetFragment = MatchBottomSheetFragment().apply {
            arguments = bundleOf(
                EXTRA_ASKED_QUE_URI to questionImageUri,
                EXTRA_TYPED_QUESTION_TEXT to questionText,
                SOURCE to source,
                EXTRA_ASKED_QUE_IMAGE_URL to imageUrl,
                EXTRA_UPLOADED_IMAGE_QUESTION_ID to uploadedImageQuestionId,
                EXTRA_BOTTOM_NAVIGATION_BOTTOM_MARGIN to bottomNavigationBottomMargin
            )
        }
    }

    private fun setupBackPressAction() {
        view?.apply {
            isFocusableInTouchMode = true
            isFocusable = true
            requestFocus()
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    return@setOnKeyListener onBackPressed()
                }
                return@setOnKeyListener false
            }
        }
    }

    private fun hideBottomSheet() {
        finish()
    }

    private fun initBottomSheetBehavior() {
        bottomSheetBehavior = LockableViewPagerBottomSheetBehavior.from(mBinding?.matchContainer!!)
        bottomSheetBehavior.setLocked(true)
        view?.post {
            bottomSheetBehavior.halfExpandedRatio =
                (bottomSheetBehavior.peekHeight / requireView().height.toFloat()).coerceIn(
                    .001F,
                    .999F
                )
        }

        bottomSheetBehavior.addBottomSheetCallback(object :
            ViewPagerBottomSheetBehavior.BottomSheetCallback() {

            override fun onSlide(p0: View, slideOffset: Float) {
                if (cropExperimentVariant == 1) mBinding?.buttonCropAgain?.isVisible =
                    slideOffset < BOTTOM_SHEET_SLIDE_THRESHOLD
                else mBinding?.buttonCropAgain1?.isVisible = slideOffset <= 0
            }

            override fun onStateChanged(p0: View, p1: Int) {
                when (p1) {
                    ViewPagerBottomSheetBehavior.STATE_COLLAPSED -> {
                        lifecycleScope.launchWhenResumed {
                            mBinding?.questionMatchViewPager?.apply {
                                currentItem = 0
                                setSwipePagingEnabled(false)
                            }
                            matchQuestionViewModel.playerState.postValue(MatchQuestionViewModel.PlayerState.PAUSE)
                            setVisibilityOfBottomLayerAndTabs(isVisible = false)
                            bottomSheetBehavior.setLocked(false)
                            mBinding?.matchResultsContainer?.updatePadding(0, 45.dpToPx(), 0, 0)
                        }
                    }

                    ViewPagerBottomSheetBehavior.STATE_DRAGGING -> {
                    }

                    ViewPagerBottomSheetBehavior.STATE_EXPANDED -> {
                        lifecycleScope.launchWhenResumed {
                            mBinding?.matchResultsContainer?.updatePadding(0, 0, 0, 0)
                            mBinding?.appBarLayout?.isVisible = true
                            if (isMatchPageScrollable) {
                                setVisibilityOfBottomLayerAndTabs(isVisible = mBinding?.questionMatchViewPager?.currentItem != 0)
                            } else {
                                setVisibilityOfBottomLayerAndTabs(isVisible = true)
                            }
                            mBinding?.questionMatchViewPager?.setSwipePagingEnabled(true)
                            matchQuestionViewModel.playerState.postValue(MatchQuestionViewModel.PlayerState.RESUME)
                            setUpBottomSheetLockState()
                        }
                    }

                    ViewPagerBottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        bottomSheetBehavior.state = ViewPagerBottomSheetBehavior.STATE_COLLAPSED
                        setVisibilityOfBottomLayerAndTabs(isVisible = false)
                    }

                    ViewPagerBottomSheetBehavior.STATE_HIDDEN -> {
                        finish()
                    }

                    ViewPagerBottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }
        })
        mMatchBottomSheetFragmentCallbacks?.onMatchBottomSheetCreated(bottomSheetBehavior)
    }

    private fun sendEventRemoteConfigVariants() {
        matchQuestionViewModel.sendEvent(
            EventConstants.EVENT_RETRY_ASK_FLOW_COUNT,
            hashMapOf<String, Any>().apply {
                put(
                    EventConstants.EVENT_VARIANT_VALUE,
                    CoreRemoteConfigUtils.getAskFlowRetryCount()
                )
            },
            true
        )

        matchQuestionViewModel.sendEvent(
            EventConstants.EVENT_ASK_FLOW_TIME_OUT,
            hashMapOf<String, Any>().apply {
                put(EventConstants.EVENT_VARIANT_VALUE, CoreRemoteConfigUtils.getAskFlowTimeOut())
            },
            true
        )
    }

    private fun showFilterBottomSheet() {
        val dialog = AdvanceSearchBottomSheetFragment.newInstance()
        dialog.show(childFragmentManager, AdvanceSearchBottomSheetFragment.TAG)
        onShow(null)
    }

    override fun onImageSelected(selectedBitmap: Bitmap, rotationAngle: Int) {
        toGreyScaleAndGetMatches(selectedBitmap)
        mMatchBottomSheetFragmentCallbacks?.onImageSelected(selectedBitmap, rotationAngle)
    }

    override fun showMatchFilterFragment() {
        matchQuestionViewModel.showFilterBottomSheet.postValue(true)
        matchQuestionViewModel.sendEvent(EventConstants.EVENT_ALL_FILTERS_SELECTED, hashMapOf())
    }

    override fun onUpdate(
        topicPosition: Int,
        isTopicSelected: Boolean,
        toRefresh: Boolean,
        facetPosition: Int
    ) {
        matchQuestionViewModel.updateMatchFilter(
            topicPosition,
            isTopicSelected,
            toRefresh,
            facetPosition
        )
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding?.askQuestion -> {
                CameraActivity.getStartIntent(requireContext(), TAG).also {
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(it)
                }
                matchQuestionViewModel.sendMatchPageExitEvent("camera")
            }

            mBinding?.buttonCropAgain -> {
                matchQuestionViewModel.sendEvent(
                    EventConstants.EVENT_CROP_AGAIN_CLICKED,
                    hashMapOf()
                )
                matchQuestionViewModel.sendMatchPageExitEvent("crop_again")
                hideBottomSheet()
            }

            mBinding?.buttonCropAgain1 -> {
                matchQuestionViewModel.sendEvent(
                    EventConstants.EVENT_CROP_AGAIN_CLICKED,
                    hashMapOf()
                )
                openCropScreen()
            }

            mBinding?.tvBottomTitle -> goToLibraryScreen()
        }
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        bottomSheetBehavior.setLocked(scrollY != 0)
        mWebViewOldScrollY = scrollY
    }

    private fun init() {

        UXCam.tagScreenName(TAG)

        getDataFromArguments()

        mBinding?.bottomNavigationView?.menu?.setGroupCheckable(0, false, true)

        mBinding?.matchContainer?.updatePadding(bottom = bottomNavigationBottomMargin)

        loadLoaderAnimation(1)
        mBinding?.loaderAnimation?.addAnimatorListener(loaderAnimatorListener)

        Log.d(EventConstants.EVENT_NAME_REACHED_MATCH_PAGE)

        getMatchResults()

        setupViewPager()

        setListeners()
        setLibraryTabText()

        matchQuestionViewModel.sendEvent(
            EventConstants.EVENT_NAME_REACHED_MATCH_PAGE,
            hashMapOf(),
            true
        )
    }

    fun setLibraryTabText() {
        if (context != null) {
            val bottomLibraryText = Utils.getLibraryBottomText(requireContext())
            if (!bottomLibraryText.isNullOrBlank()) {
                val menu = mBinding?.bottomNavigationView?.menu
                menu?.findItem(R.id.libraryFragment)?.title = bottomLibraryText
            }
        }
    }

    private fun convertImageToString() {
        if (askedQuestionImageUri.isNullOrEmpty()) {
            toast(R.string.msg_invalid_question)
        } else {
            matchQuestionViewModel.getImageAsByteArray(askedQuestionImageUri!!)
        }
    }

    override fun clearFilter() {
        matchQuestionViewModel.clearMatchFilter()
    }

    private fun getDataFromArguments() {
        arguments?.run {
            askedQuestionImageUri = getString(EXTRA_ASKED_QUE_URI)
            askedQuestionImageUrl = getString(EXTRA_ASKED_QUE_IMAGE_URL)
            questionText = getString(EXTRA_TYPED_QUESTION_TEXT).orEmpty()
            matchQuestionViewModel.source = getString(SOURCE).orEmpty()
            questionIdFromNotification = getString(Constants.QUESTION_ID).orEmpty()
            isMatchFromInApp = getBoolean(EXTRA_FROM_IN_APP, false)
            uploadedImageQuestionId = getLong(EXTRA_UPLOADED_IMAGE_QUESTION_ID, 0L)
            bottomNavigationBottomMargin = getInt(EXTRA_BOTTOM_NAVIGATION_BOTTOM_MARGIN, 0)
        }
    }

    private fun getMatchResults() {

        matchQuestionViewModel.setMatchesFromInApp(isMatchFromInApp)

        if (questionIdFromNotification.isNotBlank()) {
            matchQuestionViewModel.setMatchesFromNotification()
            matchQuestionViewModel.getMatchResultsFromDb(questionIdFromNotification)
        } else if (questionText.isNotBlank()) {
            matchQuestionViewModel.getTextQuestionMatchResults(questionText = questionText)
        } else if (!askedQuestionImageUrl.isNullOrBlank()) {
            showQuestionImageProgress()
            getResultsForImageQuestionAskedHistory(askedQuestionImageUrl = askedQuestionImageUrl)
        } else {
            convertImageToString()
        }
    }

    private fun showQuestionImageProgress() {
        /* no-op */
    }

    private fun toGreyScaleAndGetMatches(bitmap: Bitmap) {
        BitmapUtils.greyScaleBitmap(bitmap) { greyedBitmap ->
            greyedBitmap?.let {
                if (isVisible) getMatchResults(it)
            }
        }
    }

    private fun getMatchResults(bitmap: Bitmap) {
        val imageQuality = SCALE_DOWN_IMAGE_QUALITY
        val imageArea = SCALE_DOWN_IMAGE_AREA
        BitmapUtils.scaleDownBitmap(bitmap, imageQuality, imageArea) { imageData ->
            imageData?.first?.let {
                matchQuestionViewModel.scaleDownImageData = imageData
                matchQuestionViewModel.getSignedUrl(imageFileName)
            }
        }
    }

    private fun getResultsForImageQuestionAskedHistory(askedQuestionImageUrl: String? = null) {
        matchQuestionViewModel.makeParallelRequestsToGetImageResultsAndOtherWidgets(
            uploadedImageName = getCroppedImageName(croppedImageUrl = askedQuestionImageUrl)
                ?: imageFileName,
            uploadedImageQuestionId = uploadedImageQuestionId.toString(),
            croppedImageUrl = askedQuestionImageUrl,
            retryCounter = 1
        )
    }

    private fun getCroppedImageName(croppedImageUrl: String?): String? {
        return croppedImageUrl?.let {
            val uploadStrIndex = it.indexOf("uploads_")
            if (uploadStrIndex != -1) {
                it.substring(uploadStrIndex)
            } else {
                val lastIndex = it.lastIndexOf("/")
                if (lastIndex != -1) {
                    "${it.substring(lastIndex + 1)}.png"
                } else null
            }
        }
    }

    private fun setListeners() {

        mBinding?.askQuestion?.setOnClickListener(this)

        mBinding?.tvBottomTitle?.setOnClickListener(this)

        mBinding?.buttonCropAgain?.setOnClickListener(this)
        mBinding?.buttonCropAgain1?.setOnClickListener(this)

        mBinding?.questionMatchTab?.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            var unselected: Int = -1

            override fun onTabReselected(p0: TabLayout.Tab?) {}

            override fun onTabUnselected(p0: TabLayout.Tab?) {
                viewModel.addScreenTime(p0?.text.toString(), screenTime)
                // Reset screen time
                screenTime = null

                unselected = p0?.position ?: -1
                if (p0?.position == 0) {
                    binding.matchFilterFacetListView.hide()
                }
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                selectedTab = p0?.text?.toString()
                screenTime = System.currentTimeMillis()
                if (tabDefaultSelectedOnce) {
                    viewModel.sendTabClickEvent(source = p0?.text?.toString().orEmpty())
                } else {
                    tabDefaultSelectedOnce = true
                }
                if (p0?.position == 0) {
                    if (unselected !in -1..0 && canShowAdvancedSearch) {
                        binding.matchFilterFacetListView.show()
                    }
                } else {
                    binding.appBarLayout.setExpanded(true)
                }

                // set bottom text to navigate to some other screen
                val bottomTextData = viewModel.bottomTextData ?: hashMapOf()
                val bottomTextTitle = bottomTextData[selectedTab]?.title
                binding.tvBottomTitle.isVisible = bottomTextTitle.isNotNullAndNotEmpty()
                binding.tvBottomTitle.text = bottomTextTitle
            }
        })

        mBinding?.bottomNavigationView?.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.homeFragment -> {
                    if (!checkForFeedbackDialog(Constants.MATCH_PAGE_SOURCE_HOME)) {
                        matchQuestionViewModel.sendMatchPageExitEvent("home")
                        goToMainActivity()
                    }
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.libraryFragment -> {
                    if (!checkForFeedbackDialog(Constants.MATCH_PAGE_SOURCE_LIBRARY)) {
                        matchQuestionViewModel.sendMatchPageExitEvent("library")
                        goToLibraryScreen()
                    }
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.forumFragment -> {
                    if (!checkForFeedbackDialog(Constants.MATCH_PAGE_SOURCE_FORUM)) {
                        matchQuestionViewModel.sendMatchPageExitEvent("friends")
                        goToForumScreen()
                    }
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.userProfileFragment -> {
                    if (!checkForFeedbackDialog(Constants.MATCH_PAGE_SOURCE_PROFILE)) {
                        matchQuestionViewModel.sendMatchPageExitEvent("profile")
                        goToProfileScreen()
                    }
                    return@setOnNavigationItemSelectedListener true
                }

                else -> {
                    if (!checkForFeedbackDialog(Constants.MATCH_PAGE_SOURCE_OTHER)) {
                        goToMainActivity()
                    }
                    return@setOnNavigationItemSelectedListener true
                }
            }
        }

    }

    private fun goToMainActivity() {
        startActivity(Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    private fun goToLibraryScreen() {
        startActivity(Intent(requireContext(), MainActivity::class.java).apply {
            action = Constants.OPEN_LIBRARY_FROM_BOTTOM
        })
    }

    private fun goToForumScreen() {
        startActivity(Intent(requireContext(), MainActivity::class.java).apply {
            action = Constants.OPEN_FORUM_FROM_BOTTOM
        })
    }

    private fun goToProfileScreen() {
        startActivity(Intent(requireContext(), MainActivity::class.java).apply {
            action = Constants.OPEN_PROFILE_FROM_BOTTOM
        })
    }

    private fun setupViewPager() {
        matchQuestionPagerAdapter =
            MatchQuestionViewPagerAdapter(childFragmentManager, emptyList(), emptyList())
        mBinding?.questionMatchViewPager?.adapter = matchQuestionPagerAdapter
        mBinding?.questionMatchViewPager?.offscreenPageLimit = 5
        mBinding?.questionMatchTab?.setupWithViewPager(mBinding?.questionMatchViewPager)
        BottomSheetUtils.setupViewPager(mBinding?.questionMatchViewPager!!)
    }

    override fun setupObservers() {
        super.setupObservers()
        matchQuestionViewModel.matchResultsLiveData.observeK(
            viewLifecycleOwner,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        matchQuestionViewModel.imageBitmapLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Success -> {
                    it.data?.let { bitmap ->
                        onImageSuccess(bitmap)
                    }
                }

                is Outcome.Failure -> {
                    fileNotFoundException()
                }
                else -> {}
            }
        }

        matchQuestionViewModel.navigateLiveData.observeEvent(
            viewLifecycleOwner,
            this::openScreen
        )

        matchQuestionViewModel.message.observe(viewLifecycleOwner) {
            if (it.isNotNullAndNotEmpty()) showToast(requireContext(), it.orEmpty())
        }

        matchQuestionViewModel.showFilterBottomSheet.observe(viewLifecycleOwner) {
            if (it) {
                showFilterBottomSheet()
            }
        }

        // This live data only sets value when flagr for bottom layout experiment enabled
        matchQuestionViewModel.matchPageScrollDirection.observe(viewLifecycleOwner) {

            /**
             * change visibility of bottom layout based on scrolling events
             * @see MatchQuestionViewModel.ScrollDirection
             */

            if (bottomSheetBehavior.state == ViewPagerBottomSheetBehavior.STATE_EXPANDED) {
                when (it) {
                    MatchQuestionViewModel.ScrollDirection.SCROLL_DOWN -> {
                        setVisibilityOfBottomLayerAndTabs(isVisible = false)
                    }
                    MatchQuestionViewModel.ScrollDirection.SCROLL_UP,
                    MatchQuestionViewModel.ScrollDirection.SCROLL_DOWN_NONE,
                    MatchQuestionViewModel.ScrollDirection.SCROLL_UP_NONE,
                    -> {
                        setVisibilityOfBottomLayerAndTabs(isVisible = true)
                    }
                    else -> {
                        setVisibilityOfBottomLayerAndTabs(isVisible = false)
                    }
                }
            }

        }

        matchQuestionViewModel.advancedSearchOptionsLiveData.observe(viewLifecycleOwner) {
            if (it.displayFilter && it.facetList.isNotEmpty()) {
                canShowAdvancedSearch = true
                mBinding?.matchFilterFacetListView?.apply {
                    show()
                    setMatchPageFilterListener(this@MatchBottomSheetFragment)
                    setFilterDataListener(this@MatchBottomSheetFragment)
                    bindData(it)
                }
            }
        }

        matchQuestionViewModel.showNoInternetActivity.observe(viewLifecycleOwner) {
            if (it.first) {
                startActivityForResult(
                    NoInternetRetryActivity.getStartIntent(requireContext()),
                    it.second
                )
                requireActivity().overridePendingTransition(R.anim.slide_up_in, R.anim.slide_up_out)
            }
        }

        matchQuestionViewModel.uploadImageError.observe(viewLifecycleOwner) {
            toast(it)
        }

        matchQuestionViewModel.shouldLockBottomSheet.observe(viewLifecycleOwner) {
            bottomSheetBehavior.setLocked(it)
        }
    }

    /**
     * change visibility of bottom layout
     * @param isVisible - true/false
     */
    private fun setVisibilityOfBottomLayerAndTabs(isVisible: Boolean, hideTabs: Boolean = true) {
        mBinding?.askQuestion?.setVisibleState(isVisible)
        mBinding?.bottomNavigationView?.setVisibleState(isVisible)
        if (hideTabs) {
            setTabsVisibility(isVisible)
        }
    }

    private fun setTabsVisibility(isVisible: Boolean) {
        mBinding?.appBarLayout?.setExpanded(isVisible, true)
        mBinding?.tvTitle?.isVisible =
            !isVisible && mBinding?.questionMatchViewPager?.currentItem == 0
        mBinding?.separatorView?.isVisible =
            !isVisible && mBinding?.questionMatchViewPager?.currentItem == 0
    }

    private fun onImageSuccess(quesBitmap: Bitmap) {
        if ((quesBitmap.width / quesBitmap.height) <= 0.5 && !refreshResultOnFacetUnselect) {
            showImageSelectDialog(quesBitmap)
        } else {
            toGreyScaleAndGetMatches(quesBitmap)
        }
    }

    private fun fileNotFoundException() {
        toast(getString(R.string.string_fileNotPresent))
    }

    private fun showImageSelectDialog(questionBitmap: Bitmap) {
        matchQuestionViewModel.sendEvent(
            EventConstants.CAMERA_CHOOSE_IMAGE_ORIENTATION_DIALOG,
            hashMapOf(),
            true
        )
        childFragmentManager.commit(true) {
            val selectImageDialog = SelectImageDialog.newInstance(questionBitmap).apply {
                setOnImageSelectListener(this@MatchBottomSheetFragment)
            }
            add(selectImageDialog, SelectImageDialog.TAG)
        }
    }

    private fun onSuccess(matchQuestion: MatchQuestion) {

        hideAnimation()

        bottomSheetBehavior.state = ViewPagerBottomSheetBehavior.STATE_EXPANDED

        mBinding?.questionMatchViewPager?.show()

        isMatchedQuestionFetched = true
        isMatchPageLoading = false
        isMatchPageScrollable = matchQuestion.matchedQuestions.size > 2

        updateSearchResult(matchQuestion)

        viewModel.sendEvent(
            EventConstants.EVENT_NAME_REACHED_MATCH_PAGE,
            hashMapOf<String, Any>().apply {
                put(EventConstants.PARAM_PARENT_QUESTION_ID, matchQuestion.questionId)
            })
    }

    private fun updateSearchResult(matchQuestion: MatchQuestion) {

        // show pop ups in each case of handwritten, blur or empty list, screen should not be blank
        // Only exception is when user get empty list after selecting advanced search filter
        when {
            matchQuestion.matchedCount > 0 ||
                    matchQuestionViewModel.isAdvancedSearchEnabled.value == true -> setUpMatchPage(
                matchQuestion
            )

            matchQuestion.isBlur == true && matchQuestionViewModel.isAdvancedSearchEnabled.value == false -> showDialogForBlurImage(
                matchQuestion
            )

            else -> showDialogForHandwritten()
        }
    }

    private fun setUpMatchPage(matchQuestion: MatchQuestion) {
        matchResult = matchQuestion
        matchQuestionViewModel.isMatchResponseFetched = true
        autoPlay = matchQuestion.autoPlay
        matchQuestion.questionImage?.let {
            imageUrl = it
        }
        questionId = matchQuestion.questionId

        setupViewPager(
            matchQuestion.ocrText,
            matchQuestion.questionId,
            autoPlay,
            matchQuestion.autoPlayDuration,
            matchQuestion.autoPlayInitiation
        )

        setUpBottomSheetLockState()
    }

    private fun setUpBottomSheetLockState() {
        matchResult?.let {
            bottomSheetBehavior.setLocked(it.matchedCount >= 2)
            mBinding?.appBarLayout?.setExpanded(true, true)
            mBinding?.tvTitle?.isVisible =
                it.matchedCount > 2 && mBinding?.questionMatchViewPager?.currentItem == 0
            mBinding?.separatorView?.isVisible =
                it.matchedCount > 2 && mBinding?.questionMatchViewPager?.currentItem == 0
        } ?: bottomSheetBehavior.setLocked(true)
    }

    private fun showDialogForBlurImage(matchQuestion: MatchQuestion) {
        matchQuestionViewModel.isMatchResponseFetched = false
        val dialog = BlurQuestionImageDialogFragment.newInstance(
            matchQuestion.questionImage,
            matchQuestion.questionId
        )
        dialog.show(childFragmentManager, BLUR_QUESTION_IMAGE_ERROR_DIALOG)
    }

    private fun showDialogForHandwritten() {
        matchQuestionViewModel.isMatchResponseFetched = false
        val dialog = HandWrittenQuestionDialogFragment.newInstance()
        dialog.show(childFragmentManager, NO_MATCH_FOUND_ERROR_DIALOG)
        matchQuestionViewModel.sendEvent(
            EventConstants.EVENT_NAME_NO_MATCH_FOUND,
            hashMapOf()
        )
    }

    private fun setupViewPager(
        ocrText: String,
        parentQuestionId: String,
        autoPlay: Boolean?,
        autoPlayDuration: Long?,
        autoPlayInitiation: Long?
    ) {
        var questionOcrText = ocrText
        val isAutoPlayToggleEnabled = defaultPrefs().getBoolean(AUTOPLAY_STATE, true)

        matchQuestionViewModel.sendEvent(
            EventConstants.AUTOPLAY_TOGGLE_STATE, hashMapOf(
                Constants.ASKED_QUESTION_ID to parentQuestionId,
                Constants.CURRENT_STATE to isAutoPlayToggleEnabled
            ), true
        )

        val titleList = mutableListOf(
            Constants.DOUBTNUT,
            Constants.GOOGLE,
            Constants.YOUTUBE,
            Constants.ONLINE_CLASSES
        )

        val matchQuestionFragment = MatchQuestionFragment.newInstance(
            askedQuestionId = parentQuestionId,
            ocrText = ocrText,
            autoPlay = autoPlay ?: false,
            autoPlayDuration = autoPlayDuration,
            autoPlayInitiation = autoPlayInitiation
        )
        matchQuestionFragment.apply {
            setP2pConnectListener(object : P2pConnectListener {
                override fun showP2pHostScreenToConnect() {
                    showP2pHostAnimationFragment()
                }
            })

            setBookFeedbackListener(object : BookFeedbackListener {
                override fun openBookFeedbackDialog() {
                    showMatchQuestionP2pDialog()
                }
            })
        }

        val fragmentList = mutableListOf(
            matchQuestionFragment,
            MatchQuestionWebViewFragment.newInstance(
                urlToLoad = Constants.GOOGLE_URL + URLEncoder.encode(
                    questionOcrText,
                    Constants.SUPPORTED_CHARACTER_ENCODING
                ), pageName = Constants.GOOGLE, questionId = parentQuestionId
            ),
            MatchQuestionWebViewFragment.newInstance(
                urlToLoad = Constants.YOUTUBE_URL + URLEncoder.encode(
                    questionOcrText,
                    Constants.SUPPORTED_CHARACTER_ENCODING
                ), pageName = Constants.YOUTUBE, questionId = parentQuestionId
            ),
            MatchPageCarousalsFragment.newInstance(tabName = Constants.ONLINE_CLASSES)
        )

        val userClass = userPreference.getUserClass()
        val classesToAvoid = listOf("6", "7", "8", "14")
        if (userClass !in classesToAvoid) {
            titleList.add(4, Constants.VIP)
            fragmentList.add(4, MatchPageCarousalsFragment.newInstance(tabName = Constants.VIP))
        }

        if (questionOcrText.contains("`")) {
            questionOcrText = questionOcrText.replace("`", "")
            fragmentList.add(
                1,
                MatchQuestionWebViewFragment.newInstance(
                    Constants.CYMATH_URL + URLEncoder.encode(
                        questionOcrText,
                        "UTF-8"
                    ), Constants.CYMATH, questionId = parentQuestionId
                )
            )
            titleList.add(1, Constants.CYMATH)
        }

        fragmentList.also {
            it.filterIsInstance<MatchQuestionWebViewFragment>().forEach { fragment ->
                fragment.setWebViewOverScrolledCallback(this@MatchBottomSheetFragment)
            }
        }

        matchQuestionPagerAdapter.updateSearchResultList(titleList, fragmentList)
        matchQuestionPagerAdapter.notifyDataSetChanged()
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(childFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {

        hideAnimation()

        // Loading complete, set false
        isMatchPageLoading = false

        apiErrorToast(e)
        matchQuestionViewModel.sendEvent(EventConstants.ASK_QUESTION_API_ERROR, hashMapOf())
    }

    private fun hideAnimation() {
        mBinding?.loaderAnimation?.pauseAnimation()
        mBinding?.loaderAnimation?.clearAnimation()
        mBinding?.loaderAnimation?.setVisibleState(false)
        mBinding?.loaderAnimationTextSwitcher?.setVisibleState(false)
    }

    private fun updateProgressBarState(state: Boolean) {

        mBinding?.loaderAnimation?.setVisibleState(state)
        mBinding?.loaderAnimationTextSwitcher?.setVisibleState(state)

        if (!state) {
            mBinding?.tvTitle?.text = getString(R.string.solutions)
            mBinding?.loaderAnimation?.pauseAnimation()
            mBinding?.loaderAnimation?.clearAnimation()
        } else {
            loadLoaderAnimation(1)
            mBinding?.loaderAnimation?.addAnimatorListener(loaderAnimatorListener)
        }

        mBinding?.questionMatchViewPager?.setVisibleState(!state)
    }

    private fun ioExceptionHandler() {
        isMatchPageLoading = false

        if (NetworkUtils.isConnected(requireContext())) {
            toast(getString(R.string.somethingWentWrong))
        } else {
            toast(getString(R.string.string_noInternetConnection))
        }

        matchQuestionViewModel.sendEvent(EventConstants.ASK_QUESTION_API_ERROR, hashMapOf())
    }

    private fun showP2pHostAnimationFragment() {
        matchQuestionViewModel.setDataToRequestP2p(
            questionImageUrl = imageUrl,
            questionText = questionText,
            questionId = questionId
        )
        val doubtP2pHostAnimationFragment = P2pHostIntroductionFragment.newInstance()
        doubtP2pHostAnimationFragment.show(
            childFragmentManager,
            P2pHostIntroductionFragment.TAG
        )
    }

    private fun onBackPressed(): Boolean {

        // If user presses back button while loading, show dialog for confirmation
        if (isMatchPageLoading) {
            matchQuestionViewModel.sendEvent(
                EventConstants.ASK_QUESTION_BACK_PRESS_BEFORE_MATCHES,
                hashMapOf()
            )

            showAlertDialogForUserConfirmation()
            return true
        } else if (mBinding?.questionMatchViewPager?.currentItem != 0) {
            lifecycleScope.launchWhenResumed {
                mBinding?.questionMatchViewPager?.setCurrentItem(0, false)
            }
            return true
        } else {
            bottomSheetBehavior.apply {
                if (state == ViewPagerBottomSheetBehavior.STATE_EXPANDED) {
                    state = ViewPagerBottomSheetBehavior.STATE_COLLAPSED
                    return true
                }
            }
            if (checkForFeedbackDialog(Constants.MATCH_PAGE_SOURCE_BACK_PRESS)) {
                return true
            }

            matchQuestionViewModel.sendMatchPageExitEvent("back_press")
            hideBottomSheet()
            return true
        }
    }

    private fun showAlertDialogForUserConfirmation() {
        alertDialog = this.let {
            val builder = AlertDialog.Builder(requireContext())
            builder.apply {
                setPositiveButton(getString(R.string.yes_i_will_wait)) { dialog, id ->
                    matchQuestionViewModel.setPreventMatchExit()
                    matchQuestionViewModel.sendEvent(
                        EventConstants.PREVENT_MATCH_PAGE_EXIT_YES,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.TIME_STAMP, System.currentTimeMillis())
                        }
                    )
                }
                setNegativeButton(getString(R.string.skip_results)) { dialog, id ->
                    matchQuestionViewModel.sendEvent(
                        EventConstants.PREVENT_MATCH_PAGE_EXIT_NO,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.TIME_STAMP, System.currentTimeMillis())
                        }
                    )
                    matchQuestionViewModel.sendMatchPageExitEvent("exit_pop_up")
                    CameraActivity.getStartIntent(requireContext(), TAG).also { intent ->
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                    }

                    // Store match page result in DB and show notification
                    if (isMatchPageLoading) {
                        isMatchPageLoading = false
                        checkForMatchResultForNotification(imageFileName, askedQuestionImageUri)
                    }
                    finish()
                }
            }.setTitle(getString(R.string.match_page_alert_dialog_title))
                .setMessage(R.string.match_page_alert_dialog_sub_title)

            builder.create()
        }

        matchQuestionViewModel.sendEvent(
            EventConstants.PREVENT_MATCH_PAGE_EXIT_VISIBLE,
            hashMapOf<String, Any>().apply {
                put(EventConstants.TIME_STAMP, System.currentTimeMillis())
            }
        )

        alertDialog?.setOnShowListener {
            alertDialog?.getButton(AlertDialog.BUTTON_NEGATIVE)
                ?.setTextColor(resources.getColor(R.color.black_50))
            onShow(it)
        }
        alertDialog?.setOnDismissListener(this)

        alertDialog?.show()
    }

    private fun checkForFeedbackDialog(source: String): Boolean {
        if (matchQuestionViewModel.isMatchResponseFetched && !matchQuestionViewModel.isAnySolutionWatched && !matchQuestionViewModel.feedbackSubmitted) {
            when (viewModel.backPressExperimentVariant) {
                1, 2, 3 -> showMatchQuestionP2pDialog()
                else -> {
                    val dialog =
                        MatchQuestionBookFeedbackDialogFragment.newInstance(source = source)
                    childFragmentManager.beginTransaction()
                        .add(dialog, MatchQuestionBookFeedbackDialogFragment.TAG).commit()
                    onShow(null)
                }
            }
            return true
        } else {
            return false
        }
    }

    private fun showMatchQuestionP2pDialog(): Boolean {
        val backPressDialogFragment = MatchQuestionP2pDialogFragment.newInstance()
        backPressDialogFragment.setUpP2pListener(object :
            MatchQuestionP2pDialogFragment.P2pConnectListener {
            override fun openP2pHostAnimationFragment() {
                backPressDialogFragment.dismissAllowingStateLoss()
                showP2pHostAnimationFragment()
            }
        })
        childFragmentManager.beginTransaction()
            .add(backPressDialogFragment, MatchQuestionP2pDialogFragment.TAG).commit()
        return true
    }

    override fun onResume() {
        super.onResume()
        // Reset time onResume
        screenTime = System.currentTimeMillis()
    }

    override fun onStop() {
        super.onStop()
        if (isMatchPageLoading) {
            matchQuestionViewModel.sendEvent(
                EventConstants.ASK_QUESTION_BACK_EXIT_BEFORE_MATCHES,
                hashMapOf()
            )
        }
        if (!didWatchVideo && matchQuestionViewModel.isMatchResponseFetched) {
            var counter = defaultPrefs().getInt(Constants.VIDEO_NOT_WATCHED_COUNTER, 0)
            counter++
            defaultPrefs().edit {
                putInt(Constants.VIDEO_NOT_WATCHED_COUNTER, counter)
            }
            DoubtnutApp.INSTANCE.runOnDifferentThread {
                val count = getDatabase()?.feedbackDao()
                    ?.getCountForType(Constants.FEEDBACK_TYPE_ASKED_NOT_WATCHED)

                if (count != null && count != 0 && counter >= count) {
                    getDatabase()?.feedbackDao()?.updateFeedbackStatus(
                        "active",
                        Constants.FEEDBACK_TYPE_ASKED_NOT_WATCHED
                    )
                    defaultPrefs().edit {
                        putInt(Constants.VIDEO_NOT_WATCHED_COUNTER, 0)
                    }
                }
            }
        }

        matchQuestionViewModel.addScreenTime(selectedTab, screenTime)
        // Reset screen time
        screenTime = null

        matchQuestionViewModel.sendEventForTabScreenTime()

    }

    override fun onShow(dialog: DialogInterface?) {
        matchQuestionViewModel.isDialogShowing = true
    }

    override fun onDismiss(dialog: DialogInterface?) {
        matchQuestionViewModel.isDialogShowing = false
    }

    private fun checkForMatchResultForNotification(
        imageFileName: String,
        askedQuestionImageUri: String?
    ) {

        if (askedQuestionImageUri == null) return

        val constraint = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()

        val blurRequest = OneTimeWorkRequestBuilder<MatchesByFileNameWorker>()
            .setConstraints(constraint)
            .setInitialDelay(5, TimeUnit.SECONDS)
            .setInputData(createInputDataForUri(imageFileName, askedQuestionImageUri))
            .build()
        WorkManager.getInstance(requireContext()).enqueue(blurRequest)
    }

    /**
     * Creates the input data bundle which includes the Image file name to operate on
     * @return Data which contains the Image File Name as a String
     */
    private fun createInputDataForUri(imageFileName: String, askedQuestionImageUri: String): Data {
        val builder = Data.Builder()
        builder.putString(IMAGE_FILE_NAME, imageFileName)
        builder.putString(EXTRA_ASKED_QUE_URI, askedQuestionImageUri)
        return builder.build()
    }

    private val loaderAnimatorListener = object : Animator.AnimatorListener {
        private var loader2Loaded = false

        override fun onAnimationRepeat(animation: Animator?) {}

        override fun onAnimationEnd(animation: Animator?) {
            if (!loader2Loaded) {
                loadLoaderAnimation(2)
                loader2Loaded = true
            }
        }

        override fun onAnimationCancel(animation: Animator?) {}

        override fun onAnimationStart(animation: Animator?) {}
    }

    private fun loadLoaderAnimation(loaderNumber: Int) {
        val animationJsonPath = "lottie_match_page_loader_$loaderNumber.zip"

        mBinding?.loaderAnimation?.setAnimation(animationJsonPath)
        mBinding?.loaderAnimation?.playAnimation()
        setUpLoaderTextAnimation(loaderNumber)

        when (loaderNumber) {
            1 -> {
                mBinding?.loaderAnimation?.repeatCount = 0
            }
            2 -> {
                mBinding?.loaderAnimation?.repeatCount = LottieDrawable.INFINITE
            }
        }
    }

    private fun setUpLoaderTextAnimation(loaderNumber: Int) {
        var textMap = mutableMapOf<Int, CharSequence>()
        var animationDuration = 0

        when (loaderNumber) {
            1 -> {
                textMap = getLoaderAnimationTextMap(loaderNumber)
                animationDuration = 10016 // 10 s 16 ms
            }
            2 -> {
                textMap = getLoaderAnimationTextMap(loaderNumber)
                animationDuration = 0
            }
        }
        mBinding?.loaderAnimationTextSwitcher?.setInAnimation(
            requireContext(),
            R.anim.slide_in_from_bottom
        )
        mBinding?.loaderAnimationTextSwitcher?.setOutAnimation(
            requireContext(),
            R.anim.slide_out_to_top
        )

        ValueAnimator.ofInt(0, animationDuration).apply {
            duration = animationDuration.toLong()
            interpolator = LinearInterpolator()

            addUpdateListener {
                if ((it.animatedValue as Int) / 100 in textMap) {
                    mBinding?.loaderAnimationTextSwitcher?.setText(textMap.remove(it.animatedValue as Int / 100))
                }
            }
            start()
        }
    }

    private fun getLoaderAnimationTextMap(loaderNumber: Int): MutableMap<Int, CharSequence> =
        when (loaderNumber) {
            1 -> {
                mutableMapOf(
                    0 to getString(R.string.searching_among_10_lakh_solution_videos),
                    21 to getString(R.string.found_most_relevant_videos),
                    64 to getString(R.string.arranging_it_in_order_of_relevance)
                )
            }
            2 -> {
                mutableMapOf(0 to getString(R.string.loading_solutions))
            }
            else -> mutableMapOf()
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {
            Activity.RESULT_OK -> {
                handleRequestCodes(requestCode)
            }
            else -> {
                finish()
            }
        }
    }

    private fun handleRequestCodes(requestCode: Int) {
        when (requestCode) {

            Constants.REQUEST_CODE_ASK_IMAGE_SEARCH -> {
                val signedUrlEntity = viewModel.signedUrlEntity ?: return
                viewModel.makeParallelRequestsToGetImageResultsAndOtherWidgets(
                    uploadedImageName = signedUrlEntity.fileName,
                    uploadedImageQuestionId = signedUrlEntity.questionId
                )
            }

            Constants.REQUEST_CODE_UPLOAD_ASK_IMAGE -> {
                viewModel.retryUploadImage()
            }

            Constants.REQUEST_CODE_ASK_TEXT_SEARCH -> viewModel.getTextQuestionMatchResults(
                questionText = questionText
            )

            Constants.REQUEST_CODE_GET_SIGNED_URL -> {
                viewModel.getSignedUrl(fileName = imageFileName)
            }

            Constants.REQUEST_CODE_FILTER_RESULT -> {
                viewModel.clearMatchFilter()
            }
        }
    }

    private fun openScreen(navigationModel: NavigationModel) {
        val args: Bundle? = navigationModel.hashMap?.toBundle()
        val intent: Intent = when (navigationModel.screen) {
            VideoScreen -> {
                VideoPageActivity.startActivity(
                    context = requireContext(),
                    questionId = args?.getString(Constants.QUESTION_ID).orDefaultValue(),
                    playlistId = args?.getString(Constants.PLAYLIST_ID),
                    mcId = "",
                    page = args?.getString(Constants.PAGE).orDefaultValue(),
                    mcClass = args?.getString(Constants.MC_CLASS),
                    isMicroConcept = args?.getBoolean("isMicroconcept"),
                    referredStudentId = "",
                    parentId = args?.getString(Constants.PARENT_ID) ?: "0",
                    fromNotificationVideo = false,
                    preLoadVideoData = args?.getParcelable(Constants.VIDEO_DATA),
                    ocr = args?.getString(Constants.OCR_TEXT),
                    source = args?.getString(Constants.PAGE).orDefaultValue(),
                    parentPage = args?.getString(Constants.PARENT_PAGE)
                )
            }
            else -> {
                TextSolutionActivity.startActivity(
                    context = requireContext(),
                    questionId = args?.getString(Constants.QUESTION_ID).orDefaultValue(),
                    playlistId = args?.getString(Constants.PLAYLIST_ID),
                    mcId = "",
                    page = args?.getString(Constants.PAGE).orDefaultValue(),
                    mcClass = args?.getString(Constants.MC_CLASS),
                    isMicroConcept = args?.getBoolean("isMicroconcept"),
                    referredStudentId = "",
                    parentId = args?.getString(Constants.PARENT_ID) ?: "0",
                    fromNotificationVideo = false,
                    resourceType = args?.getString(Constants.RESOURCE_TYPE),
                    resourceData = args?.getString(Constants.RESOURCE_DATA),
                    ocrText = args?.getString(Constants.OCR_TEXT)
                )
            }
        }
        startActivity(intent)
    }

    private fun finish() {
        if (cropExperimentVariant == 1) {
            openCropScreen()
        } else {
            openCamera()
        }
    }

    private fun openCropScreen() {
        requireActivity().supportFragmentManager.commit {
            setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            remove(this@MatchBottomSheetFragment)
        }
        mMatchBottomSheetFragmentCallbacks?.onMatchBottomSheetFragmentClosed()
    }

    private fun openCamera() {
        CameraActivity.getStartIntent(
            requireContext(), LibraryListingActivity.TAG
        ).also {
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(it)
        }
    }

    fun setMatchBottomSheetCreatedCallback(callback: MatchBottomSheetFragmentCallbacks) {
        mMatchBottomSheetFragmentCallbacks = callback
    }

    interface MatchBottomSheetFragmentCallbacks {
        fun onMatchBottomSheetCreated(bottomSheetBehavior: ViewPagerBottomSheetBehavior<View>)

        fun onMatchBottomSheetFragmentClosed() {}

        fun onImageSelected(selectedBitmap: Bitmap, rotationAngle: Int) {}
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMatchBottomSheetBinding {
        return FragmentMatchBottomSheetBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): MatchQuestionViewModel {
        return ViewModelProvider(this, viewModelFactory)[MatchQuestionViewModel::class.java]
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        // create ViewModel here as it is needed for sending events as per Flagr configuration
        init()
        initBottomSheetBehavior()

        sendEventRemoteConfigVariants()

        defaultPrefs().edit {
            putLong(
                Constants.QUESTION_ASK_COUNT,
                defaultPrefs().getLong(Constants.QUESTION_ASK_COUNT, 0) + 1
            )
        }
        setupBackPressAction()
    }
}
