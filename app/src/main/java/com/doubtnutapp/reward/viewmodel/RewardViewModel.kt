package com.doubtnutapp.reward.viewmodel

import android.graphics.Bitmap
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.view.drawToBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.BuildConfig
import com.doubtnutapp.Constants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.base.di.qualifier.ApplicationCachePath
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.models.reward.*
import com.doubtnutapp.data.remote.repository.RewardRepository
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.domain.base.manager.FileManager
import com.doubtnutapp.utils.DateUtils.isToday
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class RewardViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val rewardRepository: RewardRepository,
    @ApplicationCachePath private val appCachePath: String,
    private val fileManager: FileManager,
    private val analyticsPublisher: AnalyticsPublisher,
    private val userPreference: UserPreference,
) : BaseViewModel(compositeDisposable) {

    private var rewardsLevelMap: Map<Int, Reward>? = null

    private var rewardsDayMap: Map<Int, Reward>? = null

    val currentLevel: Int
        get() = userPreference.getRewardSystemCurrentLevel()

    val currentDay: Int
        get() = userPreference.getRewardSystemCurrentDay()

    var isAttendanceUnmarked: Boolean = false
        private set

    var shareText: String? = null
        private set

    //region LiveData
    private val _popupLiveData = MutableLiveData<RewardPopupModel?>()
    val popupLiveData: LiveData<RewardPopupModel?>
        get() = _popupLiveData

    private val _rewardDetailsLiveData = MutableLiveData<Outcome<RewardDetails>>()
    val rewardDetailsLiveData: LiveData<Outcome<RewardDetails>>
        get() = _rewardDetailsLiveData

    private val _notifSubMessageLiveData = MutableLiveData<String>()
    val notifSubMessageLiveData: LiveData<String>
        get() = _notifSubMessageLiveData

    private val _rewardNotificationLiveData = MutableLiveData<RewardNotificationData>()
    val rewardNotificationLiveData: LiveData<RewardNotificationData>
        get() = _rewardNotificationLiveData

    private val _attendanceLiveData = MutableLiveData<MarkAttendanceModel?>()
    val attendanceLiveData: LiveData<MarkAttendanceModel?>
        get() = _attendanceLiveData

    val playVideo = MutableLiveData<Boolean>()
    //endregion

    var backpressPopupdata: BackPressPopupData? = null
        private set

    fun markDailyAttendance() {
        viewModelScope.launch {
            rewardRepository.markAutoDailyAttendance()
                .catch {
                    _attendanceLiveData.value = null
                }
                .collect {
                    //Store API call timestamp

                    defaultPrefs().edit {
                        putLong(Constants.LAST_MARKED_DAY, System.currentTimeMillis())
                    }
                    //Reset flag for shown unscratched notification
                    defaultPrefs().edit {
                        putBoolean(Constants.IS_FIRST_UNSCRATCHED_SHOWN, false)
                    }
                    _attendanceLiveData.value = it.data
                    if (it.data.isRewardPresent.not()) {
                        _popupLiveData.value = it.data.popupData?.apply {
                            isRewardPresent = it.data.isRewardPresent == true
                            isStreakBreak = it.data.isStreakBreak == true
                        }
                    }
                    //Save notification data
                    val notificationData: RewardNotificationData? = it.data.notificationData
                    if (notificationData?.notificationHeading != null && notificationData.notificationDescription != null) {
                        defaultPrefs().edit {
                            putString(
                                Constants.REWARD_NOTIFICATION_TITLE,
                                notificationData.notificationHeading
                            )
                            putString(
                                Constants.REWARD_NOTIFICATION_DESCRIPTION,
                                notificationData.notificationDescription
                            )
                            putBoolean(Constants.ATTENDANCE_MARKED_FROM_REWARD_PAGE, true)
                            putBoolean(Constants.ATTENDANCE_WIDGET_USER_INTERACTION_DONE, true)
                        }
                        _rewardNotificationLiveData.value = notificationData
                    }
                }
        }
    }

    fun getRewardDetails() {
        _rewardDetailsLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            rewardRepository.getRewardDetails()
                .catch {
                    _rewardDetailsLiveData.value = Outcome.loading(false)
                }
                .collect { response ->
                    val maxStreakDay = response.data.rewards.lastOrNull()?.day ?: return@collect

                    val attendanceItems = List(maxStreakDay) { AttendanceItem(it + 1) }
                    for (i in 0 until response.data.lastMarkedAttendance) {
                        attendanceItems[i].state = AttendanceItem.MARKED
                    }

                    response.data.rewards.forEach {
                        val attendanceItem = attendanceItems[it.day - 1]
                        if (it.isUnlocked) {
                            attendanceItem.state = if (it.isScratched) {
                                AttendanceItem.SCRATCHED
                            } else {
                                AttendanceItem.UNSCRATCHED
                            }
                        } else {
                            attendanceItem.showGift = true
                        }
                    }
                    runCatching {
                        isAttendanceUnmarked = false
                        if (response.data.lastMarkedAttendanceTimestamp != null) {
                            val dateFormat = SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                Locale.getDefault()
                            )
                            val lastMarkedAttendanceDate =
                                dateFormat.parse(response.data.lastMarkedAttendanceTimestamp)!!
                            if (lastMarkedAttendanceDate.isToday().not()) {
                                attendanceItems[response.data.lastMarkedAttendance].state =
                                    AttendanceItem.UNMARKED
                                attendanceItems[response.data.lastMarkedAttendance].isCurrentDay = true
                                isAttendanceUnmarked = true
                            } else {
                                attendanceItems[response.data.lastMarkedAttendance - 1].isCurrentDay =
                                    true
                            }
                        } else {
                            attendanceItems[response.data.lastMarkedAttendance].state =
                                AttendanceItem.UNMARKED
                            attendanceItems[response.data.lastMarkedAttendance].isCurrentDay = true
                            isAttendanceUnmarked = true
                        }
                    }
                    response.data.attendanceItems = attendanceItems

                    defaultPrefs().edit {
                        putInt(
                            Constants.UNSCRATCHED_CARD_COUNT,
                            response.data.rewards.count { it.isUnlocked && it.isScratched.not() })
                    }

                    rewardsLevelMap = response.data.rewards.associateBy { it.level }
                    rewardsDayMap = response.data.rewards.associateBy { it.day }
                    shareText = response.data.shareText

                    backpressPopupdata = response.data.backPressPopupData

                    _rewardDetailsLiveData.value = Outcome.loading(false)
                    _rewardDetailsLiveData.value = Outcome.success(response.data)
                }
        }
    }

    fun getRewardByLevel(level: Int): Reward? = rewardsLevelMap?.get(level)

    fun getRewardByDay(day: Int): Reward? = rewardsDayMap?.get(day)

    fun getShareableViewImageUri(view: View): LiveData<String> {
        val imageUriLiveData: MutableLiveData<String> = MutableLiveData()

        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = view.drawToBitmap()

            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val transformedInputStream = ByteArrayInputStream(stream.toByteArray())
            bitmap.recycle()

            val shareableImageFilePath = "$appCachePath${File.separator}ThumbnailTempImage.jpg"
            fileManager.saveFileToDirectory(transformedInputStream, shareableImageFilePath)
            imageUriLiveData.postValue(
                FileProvider.getUriForFile(
                    view.context,
                    BuildConfig.AUTHORITY,
                    File(shareableImageFilePath)
                ).toString()
            )
        }
        return imageUriLiveData
    }

    fun subscribeRewardNotification(isSubscribed: Boolean) {
        viewModelScope.launch {
            rewardRepository.subscribeRewardNotification(isSubscribed)
                .catch { }
                .collect {
                    _notifSubMessageLiveData.value = it.meta.message
                }
        }
    }

    fun markRewardScratched(level: Int) {
        viewModelScope.launch {
            rewardRepository.markRewardScratched(level)
                .catch { }
                .collect {
                    if (it.meta.success.toBoolean()) {
                        getRewardDetails()
                    }
                }
        }
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf(), ignoreSnowplow: Boolean = false) {
        params[Constants.CURRENT_LEVEL] = userPreference.getRewardSystemCurrentLevel()
        params[Constants.CURRENT_DAY] = userPreference.getRewardSystemCurrentDay()
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }
}