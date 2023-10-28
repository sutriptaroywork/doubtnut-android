package com.doubtnutapp.doubtfeed2.leaderboard.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.base.ViewModelFactory
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.databinding.FragmentDfLeaderboardListBinding
import com.doubtnutapp.doubtfeed2.leaderboard.data.model.LeaderboardStudent
import com.doubtnutapp.doubtfeed2.leaderboard.data.model.StudentData
import com.doubtnutapp.doubtfeed2.leaderboard.ui.adapter.LeaderboardAdapter
import com.doubtnutapp.doubtfeed2.leaderboard.viewmodel.LeaderboardListViewModel
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * Created by devansh on 12/7/21.
 */

class LeaderboardListFragment : Fragment(R.layout.fragment_df_leaderboard_list) {

    companion object {
        private const val PARAM_KEY_ID = "tab_id"

        fun newInstance(id: Int): LeaderboardListFragment =
            LeaderboardListFragment().apply {
                arguments = bundleOf(PARAM_KEY_ID to id)
            }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val binding by viewBinding(FragmentDfLeaderboardListBinding::bind)
    private val viewModel by viewModels<LeaderboardListViewModel> { viewModelFactory }

    private val leaderboardAdapter by lazy { LeaderboardAdapter() }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.getInt(PARAM_KEY_ID)?.let {
            viewModel.setListId(it)
        }
        setupObservers()
        binding.rvLeaderboard.adapter = leaderboardAdapter
    }

    private fun setupObservers() {
        viewModel.leaderboardLiveData.observe(viewLifecycleOwner) {
            leaderboardAdapter.submitList(it)
        }

        viewModel.topThreeLeadersLiveData.observe(viewLifecycleOwner) {
            updateTopThreeLeadersUi(it.orEmpty())
        }

        viewModel.studentDataLiveData.observe(viewLifecycleOwner) {
            updateStudentData(it)
        }
    }

    private fun updateTopThreeLeadersUi(data: List<LeaderboardStudent>) {
        with(binding) {
            val first = data.firstOrNull()
            if (first != null) {
                groupWinner1.show()
                ivRank1.loadImage(first.image)
                tvNameRank1.text = first.name
                tvWinsRank1.text = first.subtitle

                ivRank1.setOnClickListener {
                    openStudentProfile(first.studentId)
                }
            } else {
                groupWinner1.hide()
            }

            val second = data.getOrNull(1)
            if (second != null) {
                groupWinner2.show()
                ivRank2.loadImage(second.image)
                tvNameRank2.text = second.name
                tvWinsRank2.text = second.subtitle

                ivRank2.setOnClickListener {
                    openStudentProfile(second.studentId)
                }
            } else {
                groupWinner2.hide()
            }

            val third = data.getOrNull(2)
            if (third != null) {
                groupWinner3.show()
                ivRank3.loadImage(third.image)
                tvNameRank3.text = third.name
                tvWinsRank3.text = third.subtitle

                ivRank3.setOnClickListener {
                    openStudentProfile(third.studentId)
                }
            } else {
                groupWinner3.invisible()
            }
        }
    }

    private fun updateStudentData(data: StudentData?) {
        with(binding) {
            if (data == null) {
                containerStudentData.hide()
            } else {
                containerStudentData.show()
                tvSelfRank.text = data.rank
                tvSelfName.text = data.name
                tvSelfWins.text = data.score

                ivSelf.loadImage(data.image)
            }
        }
    }

    private fun openStudentProfile(studentId: String) {
        FragmentWrapperActivity.userProfile(binding.root.context, studentId, "daily_goal")
        viewModel.sendEvent(EventConstants.DG_PROFILE_VISIT_LEADERBOARD, ignoreSnowplow = true)
    }
}
