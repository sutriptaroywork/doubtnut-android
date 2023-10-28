package com.doubtnutapp.doubtfeed2.reward.ui

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.ui.helpers.LinearLayoutManagerWithSmoothScroller
import com.doubtnutapp.*

import com.doubtnutapp.base.RewardClicked
import com.doubtnutapp.base.ShowDayDescription
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.base.extension.getNavigationResult
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.FragmentDfRewardBinding
import com.doubtnutapp.doubtfeed2.reward.data.model.RewardDetails
import com.doubtnutapp.doubtfeed2.reward.receiver.RewardNotificationReceiver
import com.doubtnutapp.doubtfeed2.reward.ui.adapter.KnowMoreAdapter
import com.doubtnutapp.doubtfeed2.reward.ui.adapter.RewardAdapter
import com.doubtnutapp.doubtfeed2.reward.ui.adapter.RewardStreakAdapter
import com.doubtnutapp.doubtfeed2.reward.viewmodel.RewardViewModel
import com.doubtnutapp.doubtfeed2.ui.DoubtFeedFragment2Directions
import com.doubtnutapp.doubtfeed2.viewmodel.DoubtFeedViewModel2
import com.doubtnutapp.utils.extension.observeEvent
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by devansh on 14/7/21.
 */

class RewardFragment : Fragment(R.layout.fragment_df_reward), ActionPerformer {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val binding by viewBinding(FragmentDfRewardBinding::bind)
    private val navController by findNavControllerLazy()
    private val viewModel by viewModels<RewardViewModel> { viewModelFactory }
    private val doubtFeedViewModel by viewModels<DoubtFeedViewModel2>({ requireParentFragment() })

    private val rewardStreakAdapter by lazy { RewardStreakAdapter(this) }
    private val rewardAdapter by lazy { RewardAdapter(this) }
    private val knowMoreAdapter by lazy { KnowMoreAdapter(this) }

    private var incompleteDayText: String? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupUi()
        setupObservers()
        viewModel.getRewardDetails()
    }

    private fun setupObservers() {
        viewModel.rewardDetailsLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = it.loading
                }
                is Outcome.Failure -> {
                    apiErrorToast(it.e)
                }
                is Outcome.Success -> {
                    onRewardDetailsSuccess(it.data)
                }
            }
        }

        getNavigationResult<Boolean>(ScratchCardDialogFragment.CARD_SCRATCHED)
            ?.observe(viewLifecycleOwner) {
                if (it) {
                    viewModel.getRewardDetails()
                }
            }

        doubtFeedViewModel.updateRewardDetailsLiveData.observeEvent(viewLifecycleOwner) {
            if (it) {
                viewModel.getRewardDetails()
            }
        }
    }

    private fun setupUi() {
        setupRecyclerView()

        binding.ivDayDescrptionCancel.setOnClickListener {
            binding.dayDescription.hide()
        }
    }

    private fun setupRecyclerView() {
        with(binding) {
            rvDailyAttendance.layoutManager = LinearLayoutManagerWithSmoothScroller(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            rvDailyAttendance.adapter = rewardStreakAdapter
            rvReward.adapter = rewardAdapter
            rvKnowMore.adapter = knowMoreAdapter
        }
    }

    @SuppressLint("SetTextI18n")
    override fun performAction(action: Any) {
        when (action) {
            is RewardClicked -> {
                openScratchCardDialog(action.level)
            }

            is ShowDayDescription -> {
                with(binding) {
                    dayDescription.show()
                    if (!action.hasGift) {
                        dayDescription.isVisible = incompleteDayText != null
                        tvDayDescription.text = incompleteDayText?.format(action.dayNumber)
                    } else {
                        val reward = viewModel.getRewardByDay(action.dayNumber) ?: return
                        tvDayDescription.text =
                            "${reward.lockedShortDescription}\n\n${reward.lockedLongDescription}"
                    }
                }
            }
        }
    }

    private fun openScratchCardDialog(level: Int) {
        viewModel.getRewardByLevel(level)?.let {
            val action = DoubtFeedFragment2Directions
                .actionShowScratchCardDialog(it, viewModel.shareText)
            mayNavigate {
                navController.navigate(action)
            }
        }
    }

    private fun onRewardDetailsSuccess(data: RewardDetails) {
        with(binding) {
            incompleteDayText = data.incompleteDayText

            tvTitle.text = data.title
            tvSubtitle.text = data.subtitle

            tvRewardInfoTitle.text = data.infoTitle
            tvRewardInfoDescription.text = data.infoDescription

            rewardStreakAdapter.updateList(data.streaks.orEmpty())

            tvRewardTitle.text = data.rewardTitle
            rewardAdapter.updateList(data.rewards)
            val scrollPosition = (data.lastMarkedAttendance - 3).coerceAtLeast(0)
            rvDailyAttendance.scrollToPosition(scrollPosition)

            tvKnowMoreListHeading.text = data.knowMoreData.heading
            knowMoreAdapter.updateList(data.knowMoreData.contentList)

            tvNotesTitle.text = data.knowMoreData.notes.title
            tvNotes.text = data.knowMoreData.notes.description

            sendRewardNotificationThirtyMinutes()
        }
    }

    private fun sendRewardNotificationThirtyMinutes() {
        if (isUnscratchedCardPresent()) {
            if (!defaultPrefs().getBoolean(Constants.IS_FIRST_UNSCRATCHED_SHOWN, false)) {
                val activity = activity ?: return
                val intent = Intent(activity, RewardNotificationReceiver::class.java)
                val pendingIntent = PendingIntent
                    .getBroadcast(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val triggerTimeMillis = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent
                    )
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                }
            }
        }
    }

    private fun isUnscratchedCardPresent(): Boolean {
        return defaultPrefs().getInt(Constants.UNSCRATCHED_CARD_COUNT, 0) > 0
    }
}
