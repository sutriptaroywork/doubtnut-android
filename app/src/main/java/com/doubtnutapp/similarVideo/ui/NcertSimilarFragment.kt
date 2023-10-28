package com.doubtnutapp.similarVideo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.databinding.FragmentNcertSimilarVideoBinding
import com.doubtnutapp.similarVideo.viewmodel.NcertSimilarViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter

class NcertSimilarFragment :
    BaseBindingFragment<NcertSimilarViewModel, FragmentNcertSimilarVideoBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "NcertSimilarFragment"
        const val SOURCE_NCERT_PAGE = "ncert_new_page"
        const val ARG_PLAYLIST_ID = "playlist_id"
        const val ARG_QUESTION_ID = "question_id"
        const val ARG_TYPE = "type"
        const val ARG_PAGE = "page"

        fun newInstance(
            playlistId: String,
            type: String,
            questionId: String,
            page: String
        ): NcertSimilarFragment =
            NcertSimilarFragment().apply {
                val bundle = Bundle()
                bundle.putString(ARG_PLAYLIST_ID, playlistId)
                bundle.putString(ARG_QUESTION_ID, questionId)
                bundle.putString(ARG_TYPE, type)
                bundle.putString(ARG_PAGE, page)
                arguments = bundle
            }

    }

    private val similarAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(requireContext(), this, SOURCE_NCERT_PAGE)
    }

    private val playlistId: String? by lazy {
        arguments?.getString(ARG_PLAYLIST_ID)
    }
    private val questionId: String? by lazy {
        arguments?.getString(ARG_QUESTION_ID)
    }
    private val type: String? by lazy {
        arguments?.getString(ARG_TYPE)
    }
    private val page: String? by lazy {
        arguments?.getString(ARG_PAGE)
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNcertSimilarVideoBinding =
        FragmentNcertSimilarVideoBinding.inflate(layoutInflater)

    override fun provideViewModel(): NcertSimilarViewModel {
        val ncertSimilarViewModel: NcertSimilarViewModel by viewModels(
            ownerProducer = { requireActivity() }
        ) { viewModelFactory }
        return ncertSimilarViewModel
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        viewModel.questionId = questionId
        viewModel.page = page
        viewModel.sendEvent(EventConstants.NCERT_NEW_PAGE_OPEN)
        setUpNcertWidgets()
        viewModel.getNcertSimilarWidgets(playlistId!!, type!!, questionId, null)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.ncertSimilarVideoList.observe(viewLifecycleOwner) { ncertWidgets ->
            similarAdapter.setWidgets(ncertWidgets)
        }

        viewModel.bookWidgetData.observe(viewLifecycleOwner) { bookWidgets ->
            similarAdapter.addWidgets(bookWidgets)
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = it
        }

        viewModel.scrollUp.observe(viewLifecycleOwner) {
            binding.rvNcertSimilar.smoothScrollToPosition(0)
        }
    }

    private fun setUpNcertWidgets() {
        binding.rvNcertSimilar.adapter = similarAdapter
    }

    override fun performAction(action: Any) {
        viewModel.handleAction(action)
    }
}