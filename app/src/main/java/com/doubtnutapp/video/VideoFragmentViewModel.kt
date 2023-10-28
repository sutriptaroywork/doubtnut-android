package com.doubtnutapp.video

import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.RemoteConfigUtils
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.db.DoubtnutDatabase
import com.doubtnutapp.db.entity.VideoViewStats
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.domain.videoPage.entities.ViewOnboardingEntity
import com.doubtnutapp.domain.videoPage.interactor.PublishViewOnboarding
import com.doubtnutapp.domain.videoPage.interactor.UpdateAdVideoViewInteractor
import com.doubtnutapp.domain.videoPage.interactor.UpdateVideoViewInteractor
import com.doubtnutapp.ui.mediahelper.ImaAdData
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.videoPage.event.VideoPageEventManager
import com.doubtnutapp.workmanager.WorkManagerHelper
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.audio.AudioAttributes
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
 Created by Naman on 04/03/2020
*/

class VideoFragmentViewModel @Inject constructor(
    private val videoPageEventManager: VideoPageEventManager,
    private val updateVideoViewInteractor: UpdateVideoViewInteractor,
    private val updateAdVideoViewInteractor: UpdateAdVideoViewInteractor,
    private val publishViewOnboardingInteractor: PublishViewOnboarding,
    private val workManagerHelper: WorkManagerHelper,
    private val database: DoubtnutDatabase,
    private val analyticsPublisher: AnalyticsPublisher,
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _getViewIdLiveData: MutableLiveData<String> = MutableLiveData()

    val getViewIdLiveData: LiveData<String>
        get() = _getViewIdLiveData

    fun getAudioAttributes(): AudioAttributes =
        AudioAttributes.Builder().apply {
            setUsage(C.USAGE_MEDIA)
            setContentType(C.CONTENT_TYPE_SPEECH)
        }.build()

    fun updateVideoView(
        viewId: String,
        isBack: String,
        maxSeekTime: String,
        engagementTime: String,
        lockUnlockLogs: String?,
        networkBytes: String = ""
    ) {
        Single.fromCallable {
            workManagerHelper.assignUpdateVideoStatsWork(
                viewId,
                isBack,
                maxSeekTime,
                engagementTime,
                lockUnlockLogs
            )
        }.flatMap {
            return@flatMap updateVideoViewInteractor.execute(
                UpdateVideoViewInteractor.Param(
                    viewId = viewId,
                    isBack = isBack,
                    maxSeekTime = maxSeekTime,
                    engagementTime = engagementTime,
                    lockUnlockLogs = lockUnlockLogs,
                    networkBytes = networkBytes
                )
            ).toSingleDefault<Boolean?>(true)
        }.doAfterSuccess {
            database.videoStatusTrackDao().updateVideoTrackStatus(viewId)
            workManagerHelper.cancelUpdateVideoStatsUniqueWork(viewId)
        }.applyIoToMainSchedulerOnSingle().subscribe()
    }

    fun updateAdVideoView(adUuid: String, adId: String, engagementTime: String) {
        updateAdVideoViewInteractor.execute(
            UpdateAdVideoViewInteractor.Param(adUuid, adId, engagementTime)
        ).applyIoToMainSchedulerOnCompletable().subscribe()
    }

    /*
    If we don't have a viewid, we need to call this to create a new viewid and use that in subsequent view tracking
     */
    fun publishVideoViewOnboarding(
        questionId: String,
        videoTime: String,
        engageTime: String,
        page: String,
        studentId: String
    ) {
        if (questionId.isBlank() || page.isBlank()) {
            return
        }
        val videoStatusTrack = VideoStatusTrack(
            questionId = questionId,
            studentId = studentId,
            videoTime = videoTime,
            engageTime = engageTime,
            page = page
        )
        val id = database.videoStatusTrackDao().insertVideoStatusTrack(videoStatusTrack)
        compositeDisposable.add(
            publishViewOnboardingInteractor
                .execute(
                    PublishViewOnboarding.RequestValues(
                        page,
                        videoTime,
                        engageTime,
                        studentId,
                        questionId
                    )
                )
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({
                    database.videoStatusTrackDao().getVideoTrackerById(id)?.copy(viewId = it.viewId)
                        ?.let {
                            database.videoStatusTrackDao().updateEntity(it)
                        }
                    onViewIdPublishedSuccess(it)
                }, {})
        )
    }

    private fun onViewIdPublishedSuccess(viewOnboardingEntity: ViewOnboardingEntity) {
        _getViewIdLiveData.value = viewOnboardingEntity.viewId
    }

    fun publishEventWith(
        eventName: String,
        params: HashMap<String, Any>,
        ignoreSnowplow: Boolean = false,
        ignoreFirebase: Boolean = false
    ) {
        videoPageEventManager.eventWith(eventName, params, ignoreSnowplow, ignoreFirebase)
    }

    fun publishImaAdEvent(
        eventName: String,
        viewId: String?,
        questionId: String?,
        adData: ImaAdData?
    ) {
        val params = hashMapOf<String, Any>(
            EventConstants.AD_ID to adData?.adId.orEmpty(),
            EventConstants.AD_DURATION to adData?.duration.toString(),
            EventConstants.VIDEO_VIEW_ID to viewId.orEmpty(),
            EventConstants.QUESTION_ID to questionId.orEmpty(),
            EventConstants.ERROR_MESSAGE to adData?.errorMessage.orEmpty()
        )
        publishEventWith(eventName, params, ignoreSnowplow = false, ignoreFirebase = false)
    }

    fun insertVideoViewStatsAndSendEvent(
        maxSeekTime: String,
        engagementTime: String,
        type: String
    ) {
        val videoViewStatsDao = database.videoViewStatsDao()
        val engageTime = engagementTime.toLongOrNull() ?: 0
        val timeDiff = Utils.getInstallationDays(DoubtnutApp.INSTANCE)

        if (timeDiff <= 7) {
            val lfVidCount = videoViewStatsDao.getLongFormVideoCount() + 1
            val sfVidCount = videoViewStatsDao.getShortFormVideoCount() + 1
            val lfEt = videoViewStatsDao.getLongFormVideoEngageTime() + engageTime
            val lf2dPrefName = EventConstants.CONS_LF_2d + "_event_sent"
            if (!defaultPrefs().getBoolean(lf2dPrefName, false)) {
                val all = videoViewStatsDao.getAllLongFormVideoStats().orEmpty()
                val isVideoViewedYesterday = all.any {
                    it.isVideoViewedYesterday()
                }
                if (isVideoViewedYesterday) {
                    sendVideoViewStatBranchEvent(EventConstants.CONS_LF_2d)
                }
            }

            val sf3dPrefName = EventConstants.CONS_SF_3d + "_event_sent"
            if (!defaultPrefs().getBoolean(sf3dPrefName, false)) {
                val all = videoViewStatsDao.getAllShortFormVideoStats().orEmpty()
                val isVideoViewedYesterday = all.any {
                    it.isVideoViewedYesterday()
                }

                val isVideoViewedDayBeforeYesterday = all.any {
                    it.isVideoViewedDayBeforeYesterday()
                }

                if (isVideoViewedYesterday && isVideoViewedDayBeforeYesterday) {
                    sendVideoViewStatBranchEvent(EventConstants.CONS_SF_3d)
                }
            }

            if (lfEt >= 1200) {
                //Long form ET greater than 20 mins in 7 day
                sendVideoViewStatBranchEvent(EventConstants.LFET_20M_7d)
            }

            if (lfEt >= 600 && timeDiff <= 3) {
                //Long form ET greater than 10 mins in 3 day
                sendVideoViewStatBranchEvent(EventConstants.LFET_10M_3d)
            }

            if (lfEt >= 300 && timeDiff <= 1) {
                //Long form ET greater than 5 mins in 1 day
                sendVideoViewStatBranchEvent(EventConstants.LFET_5M_1d)
            }


            if (timeDiff in 4..7) {
                if (lfVidCount >= 4) {
                    //4 Long video views in 7 days
                    sendVideoViewStatBranchEvent(EventConstants.LFVV_4_7d)
                }
            } else if (timeDiff in 1..3) {
                if (timeDiff == 1 && sfVidCount >= 3) {
                    //3 Video views in 1 day
                    sendVideoViewStatBranchEvent(EventConstants.SFVV_3_1d)
                }
                if (timeDiff <= 2 && sfVidCount >= 6) {
                    //6 Video views in 2 days
                    sendVideoViewStatBranchEvent(EventConstants.SFVV_6_2d)
                }
                if (lfVidCount >= 4) {
                    //4 Long video views in 3 days
                    sendVideoViewStatBranchEvent(EventConstants.LFVV_4_3d)
                } else if (lfVidCount >= 2) {
                    if (timeDiff == 1) {
                        //2 Long form video views in 1 days
                        sendVideoViewStatBranchEvent(EventConstants.LFVV_2_1d)
                    } else {
                        //	2 Long form video views in 3 days
                        sendVideoViewStatBranchEvent(EventConstants.LFVV_2_3d)
                    }
                }
            }

            if (engageTime != 0L) {
                viewModelScope.launch {
                    videoViewStatsDao.insertWithReplace(
                        VideoViewStats(
                            videoTime = maxSeekTime.toLongOrNull() ?: 0,
                            engageTime = engageTime,
                            type = type
                        )
                    )
                }
            }
        }

    }

    private fun sendVideoViewStatBranchEvent(eventName: String) {
        val prefName = eventName + "_event_sent"
        if (!defaultPrefs().getBoolean(prefName, false)) {
            val countToSendEvent: Int = Utils.getCountToSend(
                RemoteConfigUtils.getEventInfo(),
                eventName
            )
            repeat((0 until countToSendEvent).count()) {
                analyticsPublisher.publishBranchIoEvent(AnalyticsEvent(eventName))
            }
            defaultPrefs().edit {
                putBoolean(prefName, true)
            }
        }
    }

}