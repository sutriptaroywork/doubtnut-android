package com.doubtnutapp.revisioncorner.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*

import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.base.TbgChapterClicked
import com.doubtnutapp.base.ViewModelFactory
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.base.extension.getNavigationResult
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.revisioncorner.ChapterSelectionData
import com.doubtnutapp.data.remote.models.revisioncorner.ChapterType
import com.doubtnutapp.data.remote.models.revisioncorner.Subject
import com.doubtnutapp.data.remote.models.revisioncorner.Topic
import com.doubtnutapp.databinding.FragmentRevisionCornerChapterSelectionBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.revisioncorner.ui.adapter.ChapterAdapter
import com.doubtnutapp.revisioncorner.viewmodel.RcChapterSelectionViewModel
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class RcChapterSelectionFragment : Fragment(R.layout.fragment_revision_corner_chapter_selection),
    ActionPerformer2 {

    companion object {
        const val SUBJECT = "subject"
        const val CHAPTER = "chapter"
        const val RECENT_CHAPTER = "recent_chapter"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private val binding by viewBinding(FragmentRevisionCornerChapterSelectionBinding::bind)
    private val navController by findNavControllerLazy()
    private val args by navArgs<RcChapterSelectionFragmentArgs>()
    private val viewModel by viewModels<RcChapterSelectionViewModel> { viewModelFactory }

    private val chapterAdapter by lazy { ChapterAdapter(RECENT_CHAPTER, this) }

    private lateinit var data: ChapterSelectionData
    private lateinit var randomChapter: Topic
    private var lastSelectedChapterIndex = -1
    private var lastRecentChapterIndex = -1
    private var chapterSource = EventConstants.RANDOM_TOPIC
    private var selectedSubject: String = ""
    private var chapterAlias: String? = null
    private var selectedChapterText: String? = null

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
            val randomTopic = data.randomTopic.orEmpty()

            data.subjects.find { it.subjectAlias == selectedSubject }?.let {
                updateSubject(it)
            }

            if (!chapterAlias.isNullOrEmpty()) {
                data.topicObjectList.find { it.title == chapterAlias }?.isSelected = true
                lastSelectedChapterIndex =
                    data.topicObjectList.indexOfFirst { it.title == chapterAlias }
            }
            viewModel.setChapterText(chapterAlias ?: randomTopic)
            tvChapterDropdown.apply {
                text = chapterAlias ?: content.selectChapter
                isSelected = chapterAlias != null
            }

            containerSubject.setOnClickListener {
                mayNavigate {
                    navigate(
                        RcChapterSelectionFragmentDirections.actionOpenSubjectBottomSheet(
                            subjects = data.subjects.toTypedArray(),
                            title = content.chooseSubject.orEmpty()
                        )
                    )
                }
            }

            tvSelectChapterTitle.text = content.heading
            tvSelectChapterDescription.text = content.description

            randomChapter = Topic(randomTopic, null, true)
            tvRandomChapter.apply {
                text = content.randomText
                isSelected = chapterAlias == null

                setOnClickListener {
                    viewModel.setChapterText(randomTopic)
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

            buttonNext.text = content.nextCta
            buttonNext.setOnClickListener {
                navController.navigate(
                    RcChapterSelectionFragmentDirections.actionOpenRulesDetailFragment(
                        widgetId = content.widgetId,
                        chapterAlias = selectedChapterText,
                        subject = selectedSubject
                    )
                )
            }

            tvChapterDropdown.setOnClickListener {
                mayNavigate {
                    navigate(
                        RcChapterSelectionFragmentDirections.actionOpenChapterBottomSheet(
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
            selectedChapterText = it
        })

        getNavigationResult<Subject>(SUBJECT)?.observe(viewLifecycleOwner) {
            selectedSubject = it.subjectAlias
            chapterAlias = null
            viewModel.getChapters(it.subjectAlias)
            viewModel.sendEvent(
                EventConstants.RC_SHORT_TEST_SUBJECT_CLICK, hashMapOf(
                    EventConstants.SUBJECT to it.title
                ), ignoreSnowplow = true
            )
        }

        getNavigationResult<Pair<String, Int>>(CHAPTER)?.observe(viewLifecycleOwner) {
            changeRandomChapterState(ChapterType.BottomSheetChapter(it.first))
            unselectLastChapter()
            lastSelectedChapterIndex = it.second
            data.topicObjectList[lastSelectedChapterIndex].isSelected = true
            viewModel.sendEvent(
                EventConstants.RC_SHORT_TEST_CHAPTER_SELECTED, hashMapOf(
                    EventConstants.CHAPTER to it.first
                ), ignoreSnowplow = true
            )
        }
    }

    private fun animateButtons() {
        val buttonPlayWithFriedAnimation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
            binding.buttonNext.startAnimation(buttonPlayWithFriedAnimation)
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