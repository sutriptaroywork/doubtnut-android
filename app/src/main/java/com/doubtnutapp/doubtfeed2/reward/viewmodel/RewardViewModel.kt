package com.doubtnutapp.doubtfeed2.reward.viewmodel

import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.Constants
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.doubtfeed2.reward.data.model.*
import com.doubtnutapp.doubtfeed2.reward.data.repository.RewardRepository
import com.doubtnutapp.utils.DateUtils.isToday
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class RewardViewModel @Inject constructor(
    private val rewardRepository: RewardRepository,
) : ViewModel() {

    private var rewardsLevelMap: Map<Int, Reward>? = null

    private var rewardsDayMap: Map<Int, Reward>? = null

    var shareText: String? = null
        private set

    //region LiveData
    private val _rewardDetailsLiveData = MutableLiveData<Outcome<RewardDetails>>()
    val rewardDetailsLiveData: LiveData<Outcome<RewardDetails>>
        get() = _rewardDetailsLiveData
    //endregion

    fun getRewardDetails() {
        _rewardDetailsLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            rewardRepository.getRewardDetails()
                .catch {
                    _rewardDetailsLiveData.value = Outcome.loading(false)
                    _rewardDetailsLiveData.value = Outcome.failure(it)
                }
                .collect {
                    val maxStreakDay = it.data.rewards.lastOrNull()?.day ?: return@collect

                    val attendanceItems = List(maxStreakDay) { Streak(it + 1) }
                    for (i in 0 until it.data.lastMarkedAttendance) {
                        attendanceItems[i].state = Streak.MARKED
                    }

                    it.data.rewards.forEach {
                        val attendanceItem = attendanceItems[it.day - 1]
                        if (it.isUnlocked) {
                            attendanceItem.state = if (it.isScratched) {
                                Streak.SCRATCHED
                            } else {
                                Streak.UNSCRATCHED
                            }
                        } else {
                            attendanceItem.showGift = true
                        }
                    }
                    runCatching {
                        if (it.data.lastMarkedAttendanceTimestamp != null) {
                            val dateFormat = SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                Locale.getDefault()
                            )
                            val lastMarkedAttendanceDate =
                                dateFormat.parse(it.data.lastMarkedAttendanceTimestamp)!!
                            if (lastMarkedAttendanceDate.isToday().not()) {
                                attendanceItems[it.data.lastMarkedAttendance].state =
                                    Streak.UNMARKED
                                attendanceItems[it.data.lastMarkedAttendance].isCurrentDay = true
                            } else {
                                attendanceItems[it.data.lastMarkedAttendance - 1].isCurrentDay =
                                    true
                            }
                        } else {
                            attendanceItems[it.data.lastMarkedAttendance].state = Streak.UNMARKED
                            attendanceItems[it.data.lastMarkedAttendance].isCurrentDay = true
                        }
                    }
                    it.data.streaks = attendanceItems

                    defaultPrefs().edit {
                        putInt(
                            Constants.UNSCRATCHED_CARD_COUNT,
                            it.data.rewards.count { it.isUnlocked && it.isScratched.not() }
                        )
                    }

                    rewardsLevelMap = it.data.rewards.associateBy { it.level }
                    rewardsDayMap = it.data.rewards.associateBy { it.day }
                    shareText = it.data.shareText

                    _rewardDetailsLiveData.value = Outcome.loading(false)
                    _rewardDetailsLiveData.value = Outcome.success(it.data)
                }
        }
    }

    fun getRewardByLevel(level: Int): Reward? = rewardsLevelMap?.get(level)

    fun getRewardByDay(day: Int): Reward? = rewardsDayMap?.get(day)
}
