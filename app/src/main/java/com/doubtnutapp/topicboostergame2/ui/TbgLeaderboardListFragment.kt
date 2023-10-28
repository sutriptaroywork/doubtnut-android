package com.doubtnutapp.topicboostergame2.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.ViewModelFactory
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.data.remote.models.topicboostergame2.LeaderboardStudent
import com.doubtnutapp.data.remote.models.topicboostergame2.StudentData
import com.doubtnutapp.databinding.FragmentTbgLeaderboardListBinding
import com.doubtnutapp.topicboostergame2.ui.adapter.LeaderboardAdapter
import com.doubtnutapp.topicboostergame2.viewmodel.TbgLeaderboardListViewModel
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * Created by devansh on 23/06/21.
 */

class TbgLeaderboardListFragment : Fragment(R.layout.fragment_tbg_leaderboard_list) {

    companion object {
        private const val PARAM_KEY_ID = "tab_id"

        fun newInstance(id: Int): TbgLeaderboardListFragment =
            TbgLeaderboardListFragment().apply {
                arguments = bundleOf(PARAM_KEY_ID to id)
            }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private val binding by viewBinding(FragmentTbgLeaderboardListBinding::bind)
    private val viewModel by viewModels<TbgLeaderboardListViewModel> { viewModelFactory }

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
                ivRank1.apply {
                    loadImage(first.image)
                    setOnClickListener {
                        openStudentProfile(first.studentId)
                    }
                }
                tvNameRank1.apply {
                    text = first.name
                    setOnClickListener {
                        openStudentProfile(first.studentId)
                    }
                }
                tvWinsRank1.text = first.subtitle
                tvRewardRank1.text = first.label
            } else {
                groupWinner1.hide()
            }

            val second = data.getOrNull(1)
            if (second != null) {
                groupWinner2.show()
                ivRank2.apply {
                    loadImage(second.image)
                    setOnClickListener {
                        openStudentProfile(second.studentId)
                    }
                }
                tvNameRank2.apply {
                    text = second.name
                    setOnClickListener {
                        openStudentProfile(second.studentId)
                    }
                }
                tvWinsRank2.text = second.subtitle
                tvRewardRank2.text = second.label
            } else {
                groupWinner2.hide()
            }

            val third = data.getOrNull(2)
            if (third != null) {
                groupWinner3.show()
                ivRank3.apply {
                    loadImage(third.image)
                    setOnClickListener {
                        openStudentProfile(third.studentId)
                    }
                }
                tvNameRank3.apply {
                    text = third.name
                    setOnClickListener {
                        openStudentProfile(third.studentId)
                    }
                }
                tvWinsRank3.text = third.subtitle
                tvRewardRank3.text = third.label
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

        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.TOPIC_BOOSTER_GAME_PROFILE_VISIT_LEADERBOARD, ignoreSnowplow = true))

        FragmentWrapperActivity.userProfile(
            binding.root.context,
            studentId, "khelo_jeeto"
        )
    }
}