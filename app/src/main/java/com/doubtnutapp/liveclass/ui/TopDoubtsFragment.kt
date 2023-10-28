package com.doubtnutapp.liveclass.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.apiErrorToast

import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.FragmentTopDoubtBinding
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.liveclass.viewmodel.TopDoubtViewModel
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class TopDoubtsFragment : BaseBindingFragment<TopDoubtViewModel, FragmentTopDoubtBinding>(),
    ActionPerformer {

    companion object {
        private const val TAG = "TopDoubtsFragment"
        private const val ID = "id"
        private const val QUESTION = "question"
        private const val ENTITY_ID = "entity_id"
        private const val ENTITY_TYPE = "entity_type"
        fun newInstance(
                id: String, question: String, entityId: String, entityType: String,
                batchId: String?
        ): TopDoubtsFragment {
            return TopDoubtsFragment().apply {
                arguments = Bundle().apply {
                    putString(ID, id)
                    putString(QUESTION, question)
                    putString(ENTITY_ID, entityId)
                    putString(ENTITY_TYPE, entityType)
                    putString(Constants.INTENT_EXTRA_BATCH_ID, batchId)
                }
            }
        }
    }

    private lateinit var adapter: WidgetLayoutAdapter

    private lateinit var recyclerViewListing: RecyclerView

    private var id: String? = null
    private var batchId: String? = null

    private var question: String? = null

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun provideViewBinding(
            inflater: LayoutInflater,
            container: ViewGroup?
    ): FragmentTopDoubtBinding {
        return FragmentTopDoubtBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): TopDoubtViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        viewModel = viewModelProvider(viewModelFactory)
        setUpRecyclerView()
        mBinding?.layoutParent?.setOnClickListener {

        }
    }

    private fun setUpRecyclerView() {
        id = arguments?.getString(ID)
        batchId = arguments?.getString(Constants.INTENT_EXTRA_BATCH_ID)
        question = arguments?.getString(QUESTION)
        val entityId = arguments?.getString(ENTITY_ID).orEmpty()
        val entityType = arguments?.getString(ENTITY_TYPE).orEmpty()
        viewModel.extraParams.apply {
            put(ENTITY_ID, entityId)
            put(ENTITY_TYPE, entityType)
        }
        mBinding?.rvWidgets?.let {
            recyclerViewListing = it
        }
        adapter = WidgetLayoutAdapter(requireActivity(), this)
        recyclerViewListing.layoutManager = LinearLayoutManager(requireActivity())
        recyclerViewListing.adapter = adapter
        fetchList()
        mBinding?.tvQuestion?.text = question.orEmpty()
    }

    private fun fetchList() {
        viewModel.fetchTopDoubtAnswerData(id.orEmpty(), batchId)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.topDoubtData.observeK(
                viewLifecycleOwner,
                this::onWidgetListFetched,
                this::onApiError,
                this::unAuthorizeUserError,
                this::ioExceptionHandler,
                this::updateProgress
        )
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireFragmentManager(), "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        context?.let { currentContext ->
            if (NetworkUtils.isConnected(currentContext)) {
                toast(getString(R.string.somethingWentWrong))
            } else {
                toast(getString(R.string.string_noInternetConnection))
            }
        }
    }

    private fun updateProgress(state: Boolean) {
        mBinding?.progressBar?.setVisibleState(state)
    }

    private fun onWidgetListFetched(list: List<WidgetEntityModel<*, *>>) {
        adapter.setWidgets(list)
    }

    override fun performAction(action: Any) {

    }


}