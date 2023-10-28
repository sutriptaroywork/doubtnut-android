package com.doubtnutapp.topicboostergame2.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*

import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.base.TbgChapterClicked
import com.doubtnutapp.base.ViewModelFactory
import com.doubtnutapp.base.extension.getNavigationResult
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.topicboostergame2.ChapterSelectionData
import com.doubtnutapp.data.remote.models.topicboostergame2.ChapterType
import com.doubtnutapp.data.remote.models.topicboostergame2.Subject
import com.doubtnutapp.data.remote.models.topicboostergame2.Topic
import com.doubtnutapp.databinding.FragmentTopicBoosterGameChapterSelectionBinding
import com.doubtnutapp.topicboostergame2.ui.adapter.ChapterAdapter
import com.doubtnutapp.topicboostergame2.viewmodel.ChaptersViewModel
import com.doubtnutapp.utils.UserUtil
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class TbgChapterSelectionFragment :
    Fragment(R.layout.fragment_topic_booster_game_chapter_selection), ActionPerformer2 {

    companion object {
        const val SUBJECT = "subject"
        const val CHAPTER = "chapter"
        const val RECENT_CHAPTER = "recent_chapter"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val binding by viewBinding(FragmentTopicBoosterGameChapterSelectionBinding::bind)
    private val navController by lazy { findNavController() }
    private val args by navArgs<TbgChapterSelectionFragmentArgs>()
    private val viewModel by viewModels<ChaptersViewModel> { viewModelFactory }

    private val chapterAdapter by lazy { ChapterAdapter(RECENT_CHAPTER, this) }

    private lateinit var data: ChapterSelectionData
    private lateinit var randomChapter: Topic
    private var lastSelectedChapterIndex = -1
    private var lastRecentChapterIndex = -1
    private var chapterSource = EventConstants.RANDOM_TOPIC
    private var selectedSubject: String = ""
    private var chapterAlias : String? = null


    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        selectedSubject = args.subjectAlias
        chapterAlias = args.chapterAlias
        setupObservers()
        viewModel.getChapters(args.subjectAlias)
    }

    private fun updateUi() {
        with(binding) {
            val content = data.content
            data.subjects.find { it.subjectAlias == selectedSubject }?.let {
                updateSubject(it)
            }

            if (!chapterAlias.isNullOrEmpty()) {
                data.topicObjectList.find { it.title == chapterAlias }?.isSelected = true
                lastSelectedChapterIndex =
                    data.topicObjectList.indexOfFirst { it.title == chapterAlias }
            }
            viewModel.setChapterText(chapterAlias ?: data.randomTopic)
            tvChapterDropdown.apply {
                text = chapterAlias ?: content.selectChapter
                isSelected = chapterAlias != null
            }

            containerSubject.setOnClickListener {
                mayNavigate {
                    navigate(
                        TbgChapterSelectionFragmentDirections.actionOpenSubjectBottomSheet(
                            subjects = data.subjects.toTypedArray(),
                            title = content.chooseSubject.orEmpty()
                        )
                    )
                }
            }

            tvSelectChapterTitle.text = content.heading
            tvSelectChapterDescription.text = content.description


            randomChapter = Topic(data.randomTopic, null, true)
            tvRandomChapter.apply {
                text = content.randomText
                isSelected = chapterAlias == null

                setOnClickListener {
                    viewModel.setChapterText(data.randomTopic)
                    changeRandomChapterState(ChapterType.RandomChapter(data.content.selectChapter))
                    unselectLastChapter()

                    if (lastRecentChapterIndex != -1) {
                        data.recentTopicObjectList?.get(lastRecentChapterIndex)?.isSelected = false
                        chapterAdapter.updateList(data.recentTopicObjectList.orEmpty())
                        lastSelectedChapterIndex = -1
                    }
                }
            }

            if (data.isRecentAvailable) {
                tvRecentlyStudied.text = data.recentContainerData.title
                rvRecentlyStudied.adapter = chapterAdapter
                chapterAdapter.updateList(data.recentTopicObjectList.orEmpty())
            } else {
                tvRecentlyStudied.hide()
            }

            buttonPlayWithFriend.text = content.primaryCta
            buttonPlayWithFriend.setOnClickListener {
                viewModel.sendEvent(
                    EventConstants.TOPIC_BOOSTER_GAME_PLAY_CLICKED,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.SOURCE, chapterSource)
                    })
                viewModel.sendEvent(EventConstants.TOPIC_BOOSTER_GAME_PLAY_WITH_FRIEND_CLICKED)
                navController.navigate(
                    TbgChapterSelectionFragmentDirections.actionOpenInviteScreen(
                        chapterAlias = viewModel.getChapterText(),
                        source = null
                    )
                )
            }

            buttonFindOpponent.text = content.secondaryCta
            buttonFindOpponent.setOnClickListener {
                viewModel.sendEvent(EventConstants.TOPIC_BOOSTER_GAME_FIND_OPPONENT_CLICKED)
                val action = TbgChapterSelectionFragmentDirections.actionStartGameFlow(
                    chapterAlias = viewModel.getChapterText(),
                    isInviter = true,
                    inviterId = UserUtil.getStudentId(),
                    isOpponentBot = true,
                )
                navController.navigate(action)
            }

            tvChapterDropdown.setOnClickListener {
                mayNavigate {
                    navigate(
                        TbgChapterSelectionFragmentDirections.actionOpenChapterBottomSheet(
                            chapters = data.topicObjectList.toTypedArray(),
                            title = content.selectChapterForGame.orEmpty(),
                            searchPlaceholder = content.searchPlaceholder.orEmpty(),
                        )
                    )
                }
            }
        }
    }

    private fun changeRandomChapterState(chapterType: ChapterType) {
        when (chapterType) {
            is ChapterType.BottomSheetChapter -> {
                viewModel.setChapterText(chapterType.chapterName)
                chapterSource = EventConstants.SPECIFIC_TOPIC
                if (lastRecentChapterIndex != -1) {
                    data.recentTopicObjectList?.get(lastRecentChapterIndex)?.isSelected = false
                    lastRecentChapterIndex = -1
                    chapterAdapter.updateList(data.recentTopicObjectList.orEmpty())
                }
                binding.apply {
                    tvChapterDropdown.apply {
                        isSelected = true
                        text = chapterType.chapterName
                    }
                    tvRandomChapter.isSelected = false
                }
            }

            is ChapterType.RecentChapter -> {
                unselectLastChapter()
                chapterSource = EventConstants.RECENT_TOPIC
                binding.apply {
                    tvChapterDropdown.apply {
                        isSelected = false
                        text = chapterType.chapterName
                    }
                    tvRandomChapter.isSelected = false
                }
            }

            is ChapterType.RandomChapter -> {
                chapterSource = EventConstants.RANDOM_TOPIC
                binding.apply {
                    tvChapterDropdown.apply {
                        isSelected = false
                        text = chapterType.chapterName
                    }
                    tvRandomChapter.isSelected = true
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.chaptersLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = it.loading
                }
                is Outcome.Success -> {
                    data = it.data
                    updateUi()
                }
                is Outcome.Failure -> {
                    apiErrorToast(it.e)
                }
            }
        }

        viewModel.chapterLiveData.observe(viewLifecycleOwner, {
            animateButtons()
        })

        getNavigationResult<Subject>(SUBJECT)?.observe(viewLifecycleOwner) {
            selectedSubject = it.subjectAlias
            chapterAlias = null
            viewModel.getChapters(it.subjectAlias)
            viewModel.sendEvent(
                EventConstants.TOPIC_BOOSTER_GAME_SUBJECT_CLICKED, hashMapOf(
                    EventConstants.TYPE to it.title
                ), ignoreMoengage = false
            )
        }

        getNavigationResult<Pair<String, Int>>(CHAPTER)?.observe(viewLifecycleOwner) {
            changeRandomChapterState(ChapterType.BottomSheetChapter(it.first))
            unselectLastChapter()
            lastSelectedChapterIndex = it.second
            data.topicObjectList[lastSelectedChapterIndex].isSelected = true
        }
    }

    private fun animateButtons() {
        val buttonFindOpponentAnimation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
        val buttonPlayWithFriedAnimation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
        buttonFindOpponentAnimation.addAnimationEndListener {
            binding.buttonPlayWithFriend.startAnimation(buttonPlayWithFriedAnimation)
        }
        binding.buttonFindOpponent.startAnimation(buttonFindOpponentAnimation)
    }

    private fun updateSubject(subject: Subject) {
        with(binding) {
            tvSubject.text = subject.title
            ivSubject.loadImage(subject.icon)
        }
    }

    override fun performAction(action: Any) {
        when (action) {
            is TbgChapterClicked -> {
                val pair = action.pair
                viewModel.setChapterText(pair.first)
                changeRandomChapterState(ChapterType.RecentChapter(data.content.selectChapter))
                if (lastRecentChapterIndex != -1) {
                    data.recentTopicObjectList?.get(lastRecentChapterIndex)?.isSelected = false
                }
                lastRecentChapterIndex = pair.second
                data.recentTopicObjectList?.get(pair.second)?.isSelected = true
                chapterAdapter.updateList(data.recentTopicObjectList.orEmpty())
            }
        }
    }

    private fun unselectLastChapter() {
        if (lastSelectedChapterIndex != -1) {
            data.topicObjectList[lastSelectedChapterIndex].isSelected = false
            lastSelectedChapterIndex = -1
        }
    }
}