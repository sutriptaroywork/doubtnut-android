package com.doubtnutapp.similarplaylist.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.ViewUtils
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.addtoplaylist.AddToPlaylistFragment
import com.doubtnutapp.base.FilterSubject
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.FragmentSimilarPlaylistBinding
import com.doubtnutapp.domain.similarVideo.entities.SimilarPlaylistEntity
import com.doubtnutapp.domain.similarVideo.entities.SimilarPlaylistTabEntity
import com.doubtnutapp.domain.similarVideo.entities.SimilarPlaylistVideoEntity
import com.doubtnutapp.eventmanager.CommonEventManager
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.similarplaylist.viewmodel.SimilarPlaylistFragmentViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.playlist.AddPlaylistFragment
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.widgets.SpaceItemDecoration
import com.doubtnutapp.widgets.itemdecorator.GridDividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * Called from VideoPageActivity for similar overlapping playlist
 */
class SimilarPlaylistFragment :
    BaseBindingFragment<SimilarPlaylistFragmentViewModel, FragmentSimilarPlaylistBinding>(),
    ActionPerformer {

    companion object {
        private const val PARAM_KEY_QUESTION_ID = "question_id"
        private const val TAG = "SimilarPlaylistFragment"

        fun newInstance(questionId: String): Fragment {
            val fragment = SimilarPlaylistFragment()
            val args = Bundle()
            args.putString(PARAM_KEY_QUESTION_ID, questionId)
            fragment.arguments = args
            return fragment
        }
    }

    private var adapter: SimilarPlaylistAdapter? = null
    private var filterAdapter: SimilarPlaylistFilterAdapter? = null

    private var listener: OnInteractionListener? = null
    private var questionId: String = ""

    @Inject
    lateinit var screenNavigator: Navigator

    @Inject
    lateinit var commonEventManager: CommonEventManager

    private var matchResultList: RecyclerView? = null

    override fun performAction(action: Any) {
        activity?.let {
            if (action is FilterSubject) {
                filterAdapter?.updateTagSelection(action.position)
                adapter?.filter?.filter(action.subjectName)
                commonEventManager.eventWith(EventConstants.RELATED_CONCEPT_TABS_SELECTED)
            } else {
                viewModel.handleAction(action)
            }
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        if (context is OnInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnInteractionListener")
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSimilarPlaylistBinding {
        return FragmentSimilarPlaylistBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): SimilarPlaylistFragmentViewModel {
        return activityViewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        matchResultList = view.findViewById(R.id.rvSimilar)
        getDataIntent()
        mBinding?.buttonRetry?.setOnClickListener {
            getDataIntent()
            mBinding?.buttonRetry?.hide()
        }
        mBinding?.layoutParentSimilar?.setOnClickListener {
        }
    }

    private fun getDataIntent() {
        questionId = arguments?.getString(PARAM_KEY_QUESTION_ID).orEmpty()
        getSimilarVideo(questionId)
    }

    private fun getSimilarVideo(questionId: String) {
        viewModel.getSimilarResults(questionId)
    }

    private fun setUpFilterView(tab: List<SimilarPlaylistTabEntity>?) {
        if (tab.isNullOrEmpty()) {
            mBinding?.recyclerViewFilter?.hide()
            return
        }
        mBinding?.recyclerViewFilter?.show()
        filterAdapter = SimilarPlaylistFilterAdapter(tab, this, mBinding?.rvSimilar ?: return)
        mBinding?.recyclerViewFilter?.adapter = filterAdapter
        mBinding?.recyclerViewFilter?.addItemDecoration(
            SpaceItemDecoration(
                ViewUtils.dpToPx(
                    8f,
                    requireContext()
                ).toInt()
            )
        )
        val selectedFilter = tab.firstOrNull()
        if (selectedFilter != null) {
            adapter?.filter?.filter(selectedFilter.type)
        }
        commonEventManager.eventWith(EventConstants.RELATED_CONCEPT_TABS_VISIBLE)
    }

    private fun updateUi(similarVideoItem: List<SimilarPlaylistVideoEntity>) {
        adapter = SimilarPlaylistAdapter(this, commonEventManager, TAG)
        mBinding?.rvSimilar?.layoutManager = LinearLayoutManager(requireActivity())
        mBinding?.rvSimilar?.adapter = adapter
        mBinding?.rvSimilar?.addItemDecoration(
            GridDividerItemDecoration(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.grey_light
                )
            )
        )
        adapter?.updateData(similarVideoItem)
        mBinding?.rvSimilar?.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                commonEventManager.eventWith(EventConstants.RELATED_CONCEPT_SCROLL)
            }
        })
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.similarVideoLiveData.observeK(
            viewLifecycleOwner,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.whatsAppShareableData.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let {
                val (deepLink, imagePath, sharingMessage) = it
                deepLink?.let { shareOnWhatsApp(deepLink, imagePath, sharingMessage) }
                    ?: showBranchLinkError()
            }
        }

        viewModel.showProgressLiveData.observe(viewLifecycleOwner, Observer(this::updateProgress))

        viewModel.showWhatsappProgressLiveData.observe(
            viewLifecycleOwner,
            Observer(this::updateProgress)
        )

        viewModel.navigateToSameScreenLiveData.observe(viewLifecycleOwner) { screenNavigationArgu ->
            val param = screenNavigationArgu.getContentIfNotHandled()
            if (param != null) {
                listener?.onResourceSelected(param.second.getValue(Constants.QUESTION_ID) as String)
            }
        }

        viewModel.navigateToNewScreenLiveData.observe(viewLifecycleOwner) { screenNavigation ->
            val param = screenNavigation.getContentIfNotHandled()
            if (param != null) {
                screenNavigator.startActivityForResultFromFragment(
                    this,
                    param.first,
                    param.second.toBundle(),
                    0
                )
            }
        }

        viewModel.navigateScreenLiveData.observe(viewLifecycleOwner) {
            activity?.let { context ->
                screenNavigator.startActivityFromActivity(context, it.first, null)
            }
        }


        viewModel.navigateLiveData.observe(viewLifecycleOwner) {
            val navigationData = it.getContentIfNotHandled()
            if (navigationData != null) {
                val args: Bundle? = navigationData.hashMap?.toBundle()
                context?.let { activityContext ->
                    screenNavigator.startActivityFromActivity(
                        activityContext,
                        navigationData.screen,
                        args
                    )
                }
            }
        }

        viewModel.eventLiveData.observe(viewLifecycleOwner) {
            sendEvent(it)
        }

        viewModel.addToPlayListLiveData.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let {
                addToPlayList(it)
            }
        }

        viewModel.onAddToWatchLater.observe(viewLifecycleOwner, EventObserver {
            onWatchLaterSubmit(it)
        })
    }

    private fun onWatchLaterSubmit(id: String) {
        showAddedToWatchLaterSnackBar(
            R.string.video_saved_to_watch_later,
            R.string.change,
            Snackbar.LENGTH_LONG,
            id
        ) { idToPost ->
            viewModel.removeFromPlaylist(idToPost, "1")
            AddToPlaylistFragment.newInstance(idToPost)
                .show(childFragmentManager, AddToPlaylistFragment.TAG)
        }
    }

    private fun showAddedToWatchLaterSnackBar(
        message: Int,
        actionText: Int,
        duration: Int,
        id: String,
        action: ((idToPost: String) -> Unit)? = null
    ) {
        activity?.let {
            Snackbar.make(
                it.findViewById(android.R.id.content),
                message,
                duration
            ).apply {
                setAction(actionText) {
                    action?.invoke(id)
                }
                this.view.background = context.getDrawable(R.drawable.bg_capsule_dark_blue)
                setActionTextColor(ContextCompat.getColor(it, R.color.redTomato))
                val textView =
                    this.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                textView.setTextColor(ContextCompat.getColor(it, R.color.white))
                show()
            }
        }
    }

    private fun addToPlayList(videoId: String) {
        AddPlaylistFragment.newInstance(videoId).show(childFragmentManager, "AddPlaylist")
    }

    private fun onSuccess(data: SimilarPlaylistEntity) {
        mBinding?.buttonRetry?.hide()
        if (data.similarVideo.isNullOrEmpty()) {
            return
        }
        updateUi(data.similarVideo)
        setUpFilterView(data.tabs)
    }

    interface OnInteractionListener {
        fun onResourceSelected(qid: String)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireFragmentManager(), "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        mBinding?.buttonRetry?.show()
        apiErrorToast(e)
    }

    private fun updateProgress(state: Boolean) {
        mBinding?.pbSimilarQuestions?.setVisibleState(state)
    }

    private fun updateProgressBarState(state: Boolean) {
        mBinding?.pbSimilarQuestions?.setVisibleState(state)
    }

    private fun ioExceptionHandler() {
        mBinding?.buttonRetry?.show()
        if (NetworkUtils.isConnected(requireActivity()).not()) {
            toast(getString(R.string.string_noInternetConnection))
        }
    }

    private fun shareOnWhatsApp(deepLink: String, imageFilePath: String?, sharingMessage: String?) {
        Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            `package` = "com.whatsapp"
            putExtra(Intent.EXTRA_TEXT, "$sharingMessage $deepLink")

            if (imageFilePath == null) {
                type = "text/plain"
            } else {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, Uri.parse(imageFilePath))
            }
        }.also {
            if (AppUtils.isCallable(context, it)) {
                startActivity(it)
            } else {
                context?.let { it1 ->
                    ToastUtils.makeText(
                        it1,
                        R.string.string_install_whatsApp,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showBranchLinkError() {
        context?.getString(R.string.error_branchLinkNotFound)?.let { msg ->
            toast(msg)
        }
    }

    fun sendEvent(eventName: String) {
        activity?.apply {
            (activity?.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.SCREEN_SIMILAR_VIDEO_FRAGMENT)
                .track()
        }
    }

    private fun sendEventByQid(eventName: String, qid: String) {
        activity?.apply {
            (activity?.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_VIDEO_VIEW_ACTIVITY)
                .addEventParameter(Constants.QUESTION_ID, qid)
                .track()
        }
    }

}
