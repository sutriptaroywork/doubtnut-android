package com.doubtnutapp.doubtpecharcha.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.base.SubmitP2pFeedback
import com.doubtnutapp.databinding.FragmentDoubtPeCharchaRatingBinding
import com.doubtnutapp.doubtpecharcha.ui.adapter.DoubtPeCharchaUserAdapter
import com.doubtnutapp.doubtpecharcha.viewmodel.DoubtP2pViewModel
import com.doubtnutapp.getScreenWidth
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.utils.UserUtil.getStudentId

class DoubtPeCharchaRatingFragment :
    BaseBindingDialogFragment<DoubtP2pViewModel, FragmentDoubtPeCharchaRatingBinding>(),
    ActionPerformer {

    companion object {

        const val TAG = "DoubtPeCharchaRatingFragment"

        private const val ROOM_ID = "room_id"

        fun newInstance(roomId: String) = DoubtPeCharchaRatingFragment().apply {
            val bundle = Bundle()
            bundle.putString(ROOM_ID, roomId)
            arguments = bundle
        }
    }

    private var roomId: String? = null

    private val ratingAdapter: DoubtPeCharchaUserAdapter by lazy {
        DoubtPeCharchaUserAdapter(
            this,
            DoubtPeCharchaUserAdapter.VIEW_TYPE_RATING
        )
    }

    private var onRatingSubmitted: OnRatingSubmitted? = null

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DoubtP2pViewModel =
        viewModelProvider(viewModelFactory)

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDoubtPeCharchaRatingBinding =
        FragmentDoubtPeCharchaRatingBinding.inflate(layoutInflater)

    override fun setupView(view: View, savedInstanceState: Bundle?) {

        roomId = arguments?.getString(ROOM_ID)

        binding.rvUserRating.adapter = ratingAdapter
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
    }

    override fun onStart() {
        super.onStart()
        val params = view?.layoutParams
        params?.width = activity?.getScreenWidth()
        view?.layoutParams = params
    }

    fun setDismissListener(onRatingSubmitted: OnRatingSubmitted) {
        this.onRatingSubmitted = onRatingSubmitted
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.p2pMembers.observe(viewLifecycleOwner) {
            ratingAdapter.updateUsers(
                it.first.distinctBy { doubtP2PMember ->
                    doubtP2PMember.studentId
                }.filter { doubtP2PMember ->
                    doubtP2PMember.studentId != getStudentId()
                }
            )
        }

        viewModel.isFeedbackSubmitted.observe(viewLifecycleOwner) { isSubmitted ->
            isSubmitted.getContentIfNotHandled()?.let {
                if (it.first) {
                    it.second?.let { studentId ->
                        val member = ratingAdapter.getMembers()
                            .find { p2PMember -> p2PMember.studentId == studentId && p2PMember.isRatingSubmitted != true }
                        member?.isRatingSubmitted = true
                        ratingAdapter.notifyDataSetChanged()
                    }
                    val anyUsersWithNoRatingSubmitted =
                        ratingAdapter.getMembers().find { p2PMember ->
                            p2PMember.isRatingSubmitted != true
                        }
                    if (anyUsersWithNoRatingSubmitted == null) {
                        onRatingSubmitted?.onRatingSubmittedForAllMembers()
                    }
                }
            }
        }
    }

    override fun performAction(action: Any) {
        when (action) {
            is SubmitP2pFeedback -> {
                viewModel.submitFeedback(action.studentId, action.rating, action.reason, roomId!!)
            }
            else -> {}
        }
    }

    interface OnRatingSubmitted {
        fun onRatingSubmittedForAllMembers()
    }
}
