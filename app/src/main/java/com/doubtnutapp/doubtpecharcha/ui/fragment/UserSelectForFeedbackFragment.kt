package com.doubtnutapp.doubtpecharcha.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.DoubtP2PMember
import com.doubtnutapp.databinding.FragmentSelectUserFeedbackBinding
import com.doubtnutapp.doubtpecharcha.ui.activity.UserFeedbackActivity
import com.doubtnutapp.doubtpecharcha.ui.activity.UserFeedbackActivityViewModel
import com.doubtnutapp.doubtpecharcha.ui.adapter.UserSelectForFeedbackAdapter
import com.doubtnutapp.ui.base.BaseBindingFragment
import java.util.ArrayList

class UserSelectForFeedbackFragment :
    BaseBindingFragment<UserFeedbackActivityViewModel, FragmentSelectUserFeedbackBinding>() {

    companion object {
        const val TAG = "user_select_feedback_fragment"
        const val MEMBERS = "Members"
        const val ROOM_ID = "room_id"
        fun newInstance(membersList: ArrayList<DoubtP2PMember>, roomId: String) =
            UserSelectForFeedbackFragment().apply {
                val argumentsBundle = arguments
                argumentsBundle?.putParcelableArrayList(MEMBERS, membersList)
                argumentsBundle?.putString(UserFeedbackActivity.ROOM_ID, roomId)
                arguments = argumentsBundle
            }

    }

    private var selectedMemberFromList = ""

    private val membersList: List<DoubtP2PMember>? by lazy {
        arguments?.getParcelableArrayList(UserFeedbackActivity.MEMBERS)
    }

    val roomId: String? by lazy {
        arguments?.getString(UserFeedbackActivity.ROOM_ID)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSelectUserFeedbackBinding {
        return FragmentSelectUserFeedbackBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): UserFeedbackActivityViewModel {
        return activityViewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        val recyclerview = binding.rvMain
        recyclerview.layoutManager = GridLayoutManager(requireContext(), 3)

        val navHostFragment = requireActivity().supportFragmentManager
            .findFragmentById(R.id.frameLayoutFragment)

        val navController = navHostFragment?.findNavController()

        binding.tvConfirm.setOnClickListener {
            if (selectedMemberFromList.isEmpty()) {
                return@setOnClickListener
            }
            navController?.let { controller ->
                val bundle = bundleOf(
                    UserFeedbackActivity.ROOM_ID to roomId,
                    UserFeedbackActivity.STUDENT_ID to selectedMemberFromList
                )

                controller.navigate(
                    R.id.action_userSelectForFeedbackFragment_to_doubtPeCharchaUserFeedbackRatingFragment2,
                    bundle
                )

            }
        }

        membersList?.let {
            val adapter = UserSelectForFeedbackAdapter(membersList!!) {
                selectedMemberFromList = it
                if (it.isNotEmpty()) {
                    binding.tvConfirm.isEnabled = true
                    binding.cardViewSubmit.setCardBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorPrimary
                        )
                    )
                } else {
                    binding.tvConfirm.isEnabled = false
                    binding.cardViewSubmit.setCardBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.grey_808080
                        )
                    )
                }
            }
            recyclerview.adapter = adapter;
        }

    }
}