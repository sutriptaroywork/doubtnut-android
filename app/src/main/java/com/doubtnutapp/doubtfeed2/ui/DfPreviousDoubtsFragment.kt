package com.doubtnutapp.doubtfeed2.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.FragmentDfPreviousDoubtsBinding
import com.doubtnutapp.doubtfeed2.ui.adapter.TopicAdapter
import com.doubtnutapp.doubtfeed2.viewmodel.DfPreviousDoubtsViewModel
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.DoubtFeedWidget
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import javax.inject.Inject

/**
 * Created by devansh on 13/7/21.
 */

class DfPreviousDoubtsFragment : Fragment(R.layout.fragment_df_previous_doubts), ActionPerformer2 {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val binding by viewBinding(FragmentDfPreviousDoubtsBinding::bind)
    private val args by navArgs<DfPreviousDoubtsFragmentArgs>()
    private val viewModel by viewModels<DfPreviousDoubtsViewModel> { viewModelFactory }
    private val navController by findNavControllerLazy()

    private val topicAdapter by lazy { TopicAdapter(this) }
    private val widgetAdapter by lazy { WidgetLayoutAdapter(requireContext()) }

    private var busDisposable: Disposable? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupUi()
        setupObservers()
    }

    private fun setupUi() {
        with(binding) {
            tvDoubtFeedTitle.text = args.title

            viewModel.initTopicsData(args.topics)
            rvTopics.adapter = topicAdapter
            topicAdapter.updateList(viewModel.topicsList)

            rvCarousels.adapter = widgetAdapter
            widgetAdapter.setWidgets(viewModel.getCarouselsFromJson(args.carouselsJson))

            ivBack.setOnClickListener {
                navController.navigateUp()
            }
        }
    }

    private fun setupObservers() {
        viewModel.previousDoubtFeedLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = it.loading
                }
                is Outcome.Failure -> {
                    apiErrorToast(it.e)
                }
                is Outcome.Success -> {
                    onPreviousDoubtFeedSuccess(it.data)
                }
            }
        }
    }

    private fun onPreviousDoubtFeedSuccess(data: DoubtFeedWidget.Data) {
        with(binding) {
            tvDoubtFeedTitle.text = data.heading

            rvCarousels.adapter = widgetAdapter
            widgetAdapter.setWidgets(data.carousels.orEmpty())
        }
    }

    override fun onStart() {
        super.onStart()
        busDisposable = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe {
            when (it) {
                is WidgetClickedEvent -> {
                    it.extraParams ?: return@subscribe
                    val widgetType = it.extraParams[Constants.WIDGET_TYPE] as? String ?: ""
                    val topicName = viewModel.currentTopic?.title.orEmpty()

                    if ((it.extraParams[Constants.IS_DONE] as? Boolean) != true) {
                        viewModel.sendEvent(
                            EventConstants.DG_PREVIOUS_DOUBT_TASK_DONE,
                            hashMapOf(
                                Constants.GOAL_NUMBER to (
                                    (it.extraParams[Constants.GOAL_NUMBER] as? Int)
                                        ?: -1
                                    ),
                                Constants.WIDGET_TYPE to widgetType,
                                Constants.TOPIC to topicName,
                            )
                        )
                        (it.extraParams[Constants.GOAL_ID] as? Int)?.let { goalId ->
                            viewModel.submitDoubtCompletion(goalId)
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        busDisposable?.dispose()
    }

    override fun performAction(action: Any) {
        when (action) {
            is TopicClicked -> {
                if (viewModel.lastTopicPosition != action.position && action.position != RecyclerView.NO_POSITION) {
                    // Order is important
                    viewModel.getPreviousDoubtFeedForTopic(action)
                    topicAdapter.apply {
                        notifyItemChanged(action.position)
                        notifyItemChanged(viewModel.lastTopicPosition)
                    }
                    viewModel.lastTopicPosition = action.position
                    viewModel.sendEvent(
                        EventConstants.DG_TOPIC_CLICK,
                        hashMapOf(
                            Constants.TOPIC to action.topicName,
                            Constants.IS_PREVIOUS to true,
                        )
                    )
                }
            }
        }
    }
}
