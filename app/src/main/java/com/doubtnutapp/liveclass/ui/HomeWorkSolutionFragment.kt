package com.doubtnutapp.liveclass.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.OnNotesClosed
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.HomeWorkSolutionData
import com.doubtnutapp.databinding.FragmentHomeworkSolutionBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.liveclass.adapter.HomeWorkAdapter
import com.doubtnutapp.liveclass.viewmodel.HomeworkViewModel
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.statusbarColor
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import javax.inject.Inject

class HomeWorkSolutionFragment :
    BaseBindingFragment<HomeworkViewModel, FragmentHomeworkSolutionBinding>() {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var screenNavigator: Navigator

    companion object {
        const val TAG = "HomeWorkSolutionFragment"
        const val ID = "hw_id"

        fun newInstance(id: String?): HomeWorkSolutionFragment =
            HomeWorkSolutionFragment().apply {
                arguments = Bundle().apply {
                    putString(ID, id)
                }
            }
    }

    private fun setUpObserver() {
        viewModel.homeworkSolutionLiveData.observeK(
            viewLifecycleOwner,
            ::onResultSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )
    }


    private fun onResultSuccess(data: HomeWorkSolutionData) {
        with(binding) {
            mBinding?.tvCompletionStatus?.text = data.completionStatus.orEmpty()
            mBinding?.ivCompletionStatus?.loadImageEtx(data.completionStatusUrl.orEmpty())
            rvSolutions.adapter = HomeWorkAdapter(
                null,
                deeplinkAction,
                TAG
            ).apply { updateList(data.questions.orEmpty()) }
        }
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(childFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(requireContext())) {
            toast(getString(R.string.somethingWentWrong))
        } else {
            toast(getString(R.string.string_noInternetConnection))
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        mBinding?.progressBar?.isVisible = state
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeworkSolutionBinding {
        return FragmentHomeworkSolutionBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): HomeworkViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        statusbarColor(requireActivity(), R.color.white_20)
        setUpObserver()
        setupListeners()
        viewModel.getHomeworkSolutions(arguments?.getString(ID) ?: "0", true)
    }

    private fun setupListeners() {
        mBinding?.ivClose?.setOnClickListener {
            (activity as? ActionPerformer)?.performAction(OnNotesClosed())
        }
    }
}