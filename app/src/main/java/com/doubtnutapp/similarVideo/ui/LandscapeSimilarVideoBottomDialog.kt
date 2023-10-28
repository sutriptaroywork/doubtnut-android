package com.doubtnutapp.similarVideo.ui

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.FragmentLandscapeSimilarVideoBinding
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_TEXT
import com.doubtnutapp.matchquestion.model.SubjectTabViewItem
import com.doubtnutapp.show
import com.doubtnutapp.similarVideo.model.SimilarVideoList
import com.doubtnutapp.similarVideo.ui.adapter.LandscapeSimilarVideoAdapter
import com.doubtnutapp.similarVideo.viewmodel.SimilarVideoFragmentViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.videoPage.viewmodel.VideoPageViewModel
import com.doubtnutapp.widgets.BottomSheetBehavior
import com.doubtnutapp.widgets.itemdecorator.GridDividerItemDecoration
import dagger.android.support.AndroidSupportInjection


class LandscapeSimilarVideoBottomDialog :
    BaseBindingFragment<SimilarVideoFragmentViewModel, FragmentLandscapeSimilarVideoBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "LandscapeSimilarVideoBottomDialog"
        private const val PARAM_KEY_PLAYLIST_ID = "playlist_id"
        fun newInstance(): LandscapeSimilarVideoBottomDialog {
            val fragment = LandscapeSimilarVideoBottomDialog()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    private var adapter: LandscapeSimilarVideoAdapter? = null

    private lateinit var similarVideoFragmentViewModel: SimilarVideoFragmentViewModel
    private lateinit var videoPageViewModel: VideoPageViewModel

    private lateinit var behavior: BottomSheetBehavior<*>

    private var playlistId: String = ""

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        playlistId = if (arguments?.getString(PARAM_KEY_PLAYLIST_ID).isNullOrBlank()) "null" else arguments?.getString(PARAM_KEY_PLAYLIST_ID)
                ?: "null"
        videoPageViewModel = ViewModelProviders.of(requireActivity()).get(VideoPageViewModel::class.java)
        similarVideoFragmentViewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get(SimilarVideoFragmentViewModel::class.java)
        setUpBottomSheetBehaviour()
        setClickListeners()
        setUpObserver()
    }

    private fun setClickListeners() {
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (mBinding?.recyclerviewSimilarVideo != null) {
                    mBinding?.recyclerviewSimilarVideo?.isLayoutFrozen = slideOffset < 1
                }
            }
        })
    }

    private fun setUpBottomSheetBehaviour() {
        behavior = BottomSheetBehavior.from(binding.filterSheet)
        behavior.peekHeight = 48
        binding.executePendingBindings()
    }

    private fun setUpObserver() {
        similarVideoFragmentViewModel.similarVideoLiveData.observeK(viewLifecycleOwner,
                ::onSuccess,
                ::onApiError,
                ::unAuthorizeUserError,
                ::ioExceptionHandler,
                ::updateProgressBarState)

        videoPageViewModel.expandLandscapeBottomSheet.observe(viewLifecycleOwner, Observer {
            expandBottomSheet(it)
        })
    }

    private fun expandBottomSheet(toExpand: Boolean) {
        if (toExpand && behavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            similarVideoFragmentViewModel.sendApxorEvent(EventConstants.HORIZONTAL_SIMILAR_DRAG, hashMapOf<String, Any>().apply {
                put(EventConstants.SOURCE, "SHOW")
            }, true)
        } else if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            similarVideoFragmentViewModel.sendApxorEvent(EventConstants.HORIZONTAL_SIMILAR_DRAG, hashMapOf<String, Any>().apply {
                put(EventConstants.SOURCE, "HIDE")
            }, true)
        }
    }

    private fun onSuccess(data: Pair<List<RecyclerViewItem>, List<SubjectTabViewItem>?>) {
        updateUi(data.first)
    }

    private fun updateUi(similarVideoItem: List<RecyclerViewItem>) {

        adapter = LandscapeSimilarVideoAdapter(this, getScreenWidth() / 3, getScreenHeight() / 3)
        mBinding?.recyclerviewSimilarVideo?.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        mBinding?.recyclerviewSimilarVideo?.adapter = adapter
        mBinding?.recyclerviewSimilarVideo?.addItemDecoration(GridDividerItemDecoration(ContextCompat.getColor(requireContext(), R.color.grey_light)))
        adapter?.updateData(similarVideoItem.filter {
            it.viewType == R.layout.item_similar_result && (it as SimilarVideoList).resourceType != SOLUTION_RESOURCE_TYPE_TEXT
        })

        mBinding?.recyclerviewSimilarVideo?.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    similarVideoFragmentViewModel.sendApxorEvent(EventConstants.HORIZONTAL_SIMILAR_SCROLL, hashMapOf(), true)
                }
            }
        })

        binding.filterSheet.show()

    }

    private fun getScreenWidth(): Int {
        val disPlayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(disPlayMetrics)
        return disPlayMetrics.widthPixels
    }

    private fun getScreenHeight(): Int {
        val disPlayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(disPlayMetrics)
        return disPlayMetrics.heightPixels
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(childFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }


    private fun updateProgressBarState(state: Boolean) {}

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(requireActivity()).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    override fun performAction(action: Any) {
        activity?.let {
            if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                similarVideoFragmentViewModel.sendApxorEvent(EventConstants.HORIZONTAL_SIMILAR_DRAG, hashMapOf<String, Any>().apply {
                    put(EventConstants.SOURCE, "HIDE")
                }, true)
                similarVideoFragmentViewModel.sendApxorEvent(EventConstants.HORIZONTAL_SIMILAR_CLICK, hashMapOf(), ignoreSnowplow = true)
                similarVideoFragmentViewModel.handleAction(action, playlistId)
            } else {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                similarVideoFragmentViewModel.sendApxorEvent(EventConstants.HORIZONTAL_SIMILAR_DRAG, hashMapOf<String, Any>().apply {
                    put(EventConstants.SOURCE, "SHOW")
                }, true)
            }
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLandscapeSimilarVideoBinding {
       return FragmentLandscapeSimilarVideoBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): SimilarVideoFragmentViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

    }

}