package com.doubtnutapp.topicboostergame2.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.base.TbgQuizHistoryItemClicked
import com.doubtnutapp.base.TbgSubjectClicked
import com.doubtnutapp.base.ViewModelFactory
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.topicboostergame2.Leaderboard
import com.doubtnutapp.data.remote.models.topicboostergame2.TbgHomeData
import com.doubtnutapp.databinding.FragmentTopicBoosterGameHomeBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.topicboostergame2.ui.adapter.LevelGameNumberAdapter
import com.doubtnutapp.topicboostergame2.ui.adapter.QuizHistoryAdapter
import com.doubtnutapp.topicboostergame2.ui.adapter.RecentTopicAdapter
import com.doubtnutapp.topicboostergame2.ui.adapter.SubjectAdapter
import com.doubtnutapp.topicboostergame2.viewmodel.TbgHomeViewModel
import com.google.android.flexbox.FlexboxLayoutManager
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class TbgHomeFragment : Fragment(R.layout.fragment_topic_booster_game_home), ActionPerformer2 {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private val binding by viewBinding(FragmentTopicBoosterGameHomeBinding::bind)
    private val viewModel by viewModels<TbgHomeViewModel> { viewModelFactory }
    private val navController by lazy { findNavController() }

    private val levelGameNumberAdapter by lazy { LevelGameNumberAdapter() }
    private val recentTopicAdapter by lazy { RecentTopicAdapter() }
    private val subjectAdapter by lazy { SubjectAdapter(this) }
    private val quizHistoryAdapter by lazy { QuizHistoryAdapter(this) }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupObservers()
        viewModel.getTbgHomeData()
    }

    private fun setupObservers() {
        viewModel.homeLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = it.loading
                }
                is Outcome.Success -> {
                    updateUi(it.data)
                }
                is Outcome.Failure -> {
                    apiErrorToast(it.e)
                }
            }
        }

        viewModel.quizHistoryLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = it.loading
                }
                is Outcome.Success -> {
                    if (it.data.quizPlayedHistory.isNullOrEmpty()) {
                        binding.tvQuizHistoryViewMore.hide()
                    } else {
                        quizHistoryAdapter.addItems(it.data.quizPlayedHistory)
                    }
                }
                is Outcome.Failure -> {
                    apiErrorToast(it.e)
                }
            }
        }

        viewModel.leaderboardLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = it.loading
                }
                is Outcome.Success -> {
                    updateLeaderboardUi(it.data)
                }
                is Outcome.Failure -> {
                    apiErrorToast(it.e)
                }
                else -> {

                }
            }
        }
    }

    private fun updateUi(data: TbgHomeData) {
        with(binding) {
            //Levels
            rvLevelQuestions.adapter = levelGameNumberAdapter
            levelGameNumberAdapter.updateList(data.levelGames)
            tvLevel.text = data.levelTitle
            tvLevelDescription.text = data.levelDescription
            containerLevelGames.setOnClickListener {
                viewModel.sendEvent(EventConstants.TOPIC_BOOSTER_GAME_LEVEL_CLICK)
                mayNavigate {
                    navigate(R.id.actionOpenLevelsBottomSheet)
                }
            }

            //Faq
            tvFaq.text = data.faq.title.orEmpty()
            tvFaq.setOnClickListener {
                viewModel.sendEvent(EventConstants.TOPIC_BOOSTER_GAME_FAQ_CLICK)
                mayNavigate {
                    navigate(TbgHomeFragmentDirections.actionOpenFaqBottomSheet(data.faq))
                }
            }

            tvLeaderboard.setOnClickListener {
                viewModel.sendEvent(EventConstants.TOPIC_BOOSTER_GAME_VIEW_RANK_CLICK)
                deeplinkAction.performAction(
                    requireContext(),
                    data.leaderboardContainer?.leaderboardDeeplink
                )
            }

            //Recent topics
            if (data.isRecentAvailable) {
                data.recentTopics?.let {
                    groupRecentTopics.show()
                    tvTitleRecentTopics.text = it.recentTitle
                    rvRecentTopics.apply {
                        layoutManager = FlexboxLayoutManager(requireContext())
                        adapter = recentTopicAdapter
                        recentTopicAdapter.updateList(it.recentTopics)
                    }
                }
            } else {
                groupRecentTopics.hide()
            }

            //Subjects
            if (data.isSubjectAvailable) {
                data.subjects?.let {
                    groupSubjects.show()
                    tvTitleSubjects.text = it.title
                    rvSubjects.apply {
                        layoutManager = GridLayoutManager(requireContext(), 4)
                        adapter = subjectAdapter
                        subjectAdapter.updateList(it.subjects)
                    }
                }
            } else {
                groupSubjects.hide()
            }

            //Quiz history
            if (data.isQuizHistoryAvailable) {
                data.quizHistory?.let {
                    groupQuizHistory.show()
                    tvTitleQuizHistory.text = it.title
                    tvSubtitleQuizHistory.text = it.subtitle
                    rvQuizHistory.adapter = quizHistoryAdapter
                    quizHistoryAdapter.updateList(it.quizPlayedHistory)

                    tvQuizHistoryViewMore.isVisible = it.showViewMore
                    tvQuizHistoryViewMore.setOnClickListener {
                        viewModel.getQuizHistory()
                    }
                }
            } else {
                groupQuizHistory.hide()
            }

            //Leaderboard
            if (data.isLeaderboardAvailable) {
                data.leaderboardContainer?.let {
                    groupLeaderboard.show()

                    it.tabs.forEach { tabData ->
                        val newTab = tabsLeaderboard.newTab().setText(tabData.title)
                        newTab.id = tabData.id
                        tabsLeaderboard.addTab(newTab)
                        if (tabData.id == it.activeTab) {
                            newTab.select()
                        }
                    }

                    tabsLeaderboard.addOnTabSelectedListener { tab ->
                        viewModel.getLeaderboardData(tab.id, 0)
                    }

                    updateLeaderboardUi(it)
                }
            } else {
                groupLeaderboard.hide()
            }

            ivBottomImageBanner.isVisible = data.bottomBanner != null
            ivBottomImageBanner.loadImage(data.bottomBanner)

            primaryCta.apply {
                text = data.primaryCta
                setOnClickListener {
                    deeplinkAction.performAction(requireContext(), data.primaryCtaDeeplink)
                }
            }
        }
    }

    private fun updateLeaderboardUi(data: Leaderboard?) {
        with(binding) {
            tvTitleLeaderboard.text = data?.title
            tvDescriptionLeaderboard.text = data?.subtitle

            val first = data?.leaderboardData?.firstOrNull()
            if (first != null) {
                groupWinner1.show()
                ivRank1.loadImage(first.image)
                tvNameRank1.text = first.name
                tvWinsRank1.text = first.subtitle
                tvRewardRank1.text = first.label
            } else {
                groupWinner1.hide()
            }

            val second = data?.leaderboardData?.getOrNull(1)
            if (second != null) {
                groupWinner2.show()
                ivRank2.loadImage(second.image)
                tvNameRank2.text = second.name
                tvWinsRank2.text = second.subtitle
                tvRewardRank2.text = second.label
            } else {
                groupWinner2.hide()
            }

            val third = data?.leaderboardData?.getOrNull(2)
            if (third != null) {
                groupWinner3.show()
                ivRank3.loadImage(third.image)
                tvNameRank3.text = third.name
                tvWinsRank3.text = third.subtitle
                tvRewardRank3.text = third.label
            } else {
                groupWinner3.invisible()
            }

            if (data?.isRankAvailable == true) {
                tvRank.text = buildSpannedString {
                    append(data.rankText.orEmpty())
                    bold {
                        append(data.rank.orEmpty())
                    }
                    append("\n")
                    append(resources.getString(R.string.view_leaderboard))
                }
            } else {
                tvRank.text = resources.getString(R.string.view_leaderboard)
            }

            containerLeaderboard.setOnClickListener {
                viewModel.sendEvent(EventConstants.TOPIC_BOOSTER_GAME_VIEW_RANK_CLICK)
                deeplinkAction.performAction(requireContext(), data?.leaderboardDeeplink)
            }
        }
    }

    override fun performAction(action: Any) {
        when (action) {
            is TbgSubjectClicked -> {
                viewModel.sendEvent(
                    EventConstants.TOPIC_BOOSTER_GAME_SUBJECT_CLICKED,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.TYPE, action.subject)
                    }, ignoreMoengage = false
                )
                navController.navigate(
                    TbgHomeFragmentDirections.actionSelectChapter(action.subject.subjectAlias, null)
                )
            }

            is TbgQuizHistoryItemClicked -> {
                viewModel.sendEvent(EventConstants.TOPIC_BOOSTER_GAME_PERFORMANCE_HISTORY_CLICKED)
                deeplinkAction.performAction(requireContext(), action.deeplink)
            }
        }
    }
}