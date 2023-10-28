package com.doubtnutapp.topicboostergame2.ui

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.PagerSnapHelper
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.dpToPx
import com.doubtnutapp.*

import com.doubtnutapp.base.PlayVideo
import com.doubtnutapp.base.ViewModelFactory
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.topicboostergame2.TbgResult
import com.doubtnutapp.databinding.FragmentTbgResultBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.home.recyclerdecorator.HorizontalBannerSpaceItemDecoration
import com.doubtnutapp.resourcelisting.ui.adapter.ResourcePlaylistAdapter
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.topicboostergame2.extensions.loadUserImage
import com.doubtnutapp.topicboostergame2.viewmodel.TbgResultViewModel
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.utils.UserUtil
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * Created by devansh on 30/6/21.
 */

class TbgResultFragment : Fragment(R.layout.fragment_tbg_result), ActionPerformer {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var screenNavigator: Navigator

    private val binding by viewBinding(FragmentTbgResultBinding::bind)
    private val args by navArgs<TbgResultFragmentArgs>()
    private val viewModel by viewModels<TbgResultViewModel> { viewModelFactory }

    private val solutionsAdapter: ResourcePlaylistAdapter by lazy {
        ResourcePlaylistAdapter(childFragmentManager, solutionVideoPage, this, false)
    }
    private val solutionVideoPage = Constants.PAGE_TOPIC_BOOSTER_GAME

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupObservers()
        if (args.submitResult) {
            viewModel.submitResult(args)
        } else {
            viewModel.getPreviousResult(args.gameId)
        }
    }

    private fun setupObservers() {
        viewModel.resultLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = it.loading
                }
                is Outcome.Failure -> {
                    apiErrorToast(it.e)
                }
                is Outcome.Success -> {
                    updateUi(it.data)
                    val params = hashMapOf<String, Any>(
                        EventConstants.HOST_STUDENT_ID to args.inviterId.orEmpty(),
                        EventConstants.OPPONENT_ID to args.inviteeId.orEmpty(),
                        EventConstants.RESULT to it.data.resultText.orEmpty(),
                        EventConstants.CORRECT_ANSWERS to it.data.totalCorrect
                    )
                    viewModel.sendEvent(
                        EventConstants.TOPIC_BOOSTER_GAME_PLAYED_V2,
                        params,
                        ignoreSnowplow = true,
                        ignoreMoengage = false
                    )
                    viewModel.sendEvent(
                        EventConstants.TOPIC_BOOSTER_GAME_PLAYED,
                        params,
                        ignoreSnowplow = true
                    )
                }
            }
        }

        viewModel.quizSolutionsLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Success -> {
                    if (it.data.playlist.isNullOrEmpty().not()) {
                        binding.tvSolutionsTitle.show()
                        solutionsAdapter.updateList(
                            it.data.playlist.orEmpty(),
                            viewModel.solutionsPlaylistId.toString()
                        )
                    }
                }
            }
        }

        viewModel.navigateScreenLiveData.observe(viewLifecycleOwner, EventObserver {
            val args: Bundle? = it.second?.toBundle()
            screenNavigator.startActivityFromActivity(requireActivity(), it.first, args)

            with(viewModel) {
                val playlistId = args?.get(Constants.QUESTION_ID)
                sendEventByQid(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, playlistId.toString())
                sendEventByEventTracker(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK + playlistId)
                sendCleverTapEvent(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK)
            }
        })
    }

    private fun updateUi(data: TbgResult) {
        with(binding) {
            ivUser.loadUserImage(UserUtil.getProfileImage())
            tvTopic.text = data.topic
            tvResult.text = data.resultText
            tvDescription.text = data.description

            tvStatsTitleUser.text = data.name
            tvStatsTitleOpponent.text = data.opponentName

            tvScoreUser.text = getString(R.string.quiz_score_stats, data.score, data.totalScore)
            tvScoreOpponent.text =
                getString(R.string.quiz_score_stats, data.opponentScore, data.totalScore)

            progressBarAccuracyUser.progressMax = data.totalQuestions.toFloat()
            progressBarAccuracyUser.progress = data.totalCorrect.toFloat()
            tvAccuracyUser.text =
                getString(R.string.quiz_accuracy_stats, data.totalCorrect, data.totalQuestions)

            progressBarAccuracyOpponent.progressMax = data.totalQuestions.toFloat()
            progressBarAccuracyOpponent.progress = data.totalOpponentCorrect.toFloat()
            tvAccuracyOpponent.text = getString(
                R.string.quiz_accuracy_stats,
                data.totalOpponentCorrect,
                data.totalQuestions
            )

            if (data.isRankAvailable) {
                tvRank.text = buildSpannedString {
                    append(data.rankText.orEmpty())
                    bold {
                        append(data.rank.orEmpty())
                    }
                }
                tvRank.setOnClickListener {
                    deeplinkAction.performAction(requireContext(), data.rankDeeplink)
                }
            } else {
                tvRank.hide()
            }

            tvSolutionsTitle.text = data.solutionsTitle
            rvSolutions.apply {
                onFlingListener = null
                val snapHelper = PagerSnapHelper()
                snapHelper.attachToRecyclerView(this)

                val outValue = TypedValue()
                resources.getValue(R.dimen.spacing_circle, outValue, true)
                val spacing = outValue.float.dpToPx().toInt()

                addItemDecoration(HorizontalBannerSpaceItemDecoration(spacing, snapHelper))
                adapter = solutionsAdapter
            }


            viewModel.getQuizSolutions(requireContext())

            buttonPlayOnAnotherTopic.text = data.secondaryCta1
            buttonPlayOnAnotherTopic.setOnClickListener {
                deeplinkAction.performAction(requireContext(), data.secondaryCta1Deeplink)
                viewModel.sendEvent(EventConstants.TOPIC_BOOSTER_GAME_PLAY_AGAIN_CLICKED,
                hashMapOf(
                    EventConstants.SOURCE to EventConstants.RESULT_PAGE
                ))
            }

            buttonPlayAgain.text = data.primaryCta
            buttonPlayAgain.setOnClickListener {
                viewModel.sendEvent(EventConstants.TOPIC_BOOSTER_GAME_QUESTION_ASK_CLICKED)
                deeplinkAction.performAction(requireContext(), data.primaryCtaDeeplink)
                activity?.finish()
            }

            buttonHome.text = data.secondaryCta2
            buttonHome.setOnClickListener {
                viewModel.sendEvent(EventConstants.TOPIC_BOOSTER_GAME_GO_HOME_CLICKED)
                deeplinkAction.performAction(requireContext(), data.secondaryCta2Deeplink)
                activity?.finish()
            }

            if (data.isLevelUp && data.levelUpContainer != null) {
                findNavController().navigate(
                    TbgResultFragmentDirections.actionShowLevelUpDialog(data.levelUpContainer)
                )
            }
        }
    }

    override fun performAction(action: Any) {
        when (action) {
            is PlayVideo -> {
                viewModel.openVideoScreen(action, solutionVideoPage)
            }
        }
    }
}