package com.doubtnutapp.ui.games

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.CoreActions
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.addEventNames
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.repository.GamesRepository
import com.doubtnutapp.data.remote.repository.UserActivityRepository
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class GamesViewModel @Inject constructor(
    private val gamesEvent: GamesEventManager,
    private val gamesRepository: GamesRepository,
    private val userActivityRepository: UserActivityRepository,
    compositeDisposable: CompositeDisposable,
) : BaseViewModel(compositeDisposable) {

    private var totalEngagementTime: Int = 0
    private var engamentTimeToSend: Number = 0
    private var timerTask: TimerTask? = null
    private var engageTimer: Timer? = null
    private var handler: Handler? = null
    private var game: String = ""
    var isApplicationBackground = false

    fun fetchGames() = gamesRepository.fetchGamesList(null)

    fun onStop() {
        timerTask?.let { handler?.removeCallbacks(it) }
        this.sendEventForEngagement(EventConstants.EVENT_GAME_ENGAGEMENT, engamentTimeToSend)

        gamesEvent.eventWith(
            StructuredEvent(
                action = EventConstants.EVENT_GAME_ENGAGEMENT,
                value = engamentTimeToSend.toDouble(),
                category = EventConstants.EVENT_GAME,
                eventParams = hashMapOf(
                    EventConstants.EVENT_GAME to game,
                    EventConstants.PARAM_TIMESTAMP to System.currentTimeMillis()
                )
            )
        )

        totalEngagementTime = 0
    }

    fun setupEngagementTracking(gameName: String) {
        handler = Handler(Looper.getMainLooper())
        game = gameName
        startEngagementTimer()
    }

    fun sendEvent(eventName: String, map: HashMap<String, Any> = hashMapOf()) {
        gamesEvent.eventWith(eventName, map)
    }

    private fun startEngagementTimer() {
        if (engageTimer == null) {
            engageTimer = Timer()
        }
        timerTask = object : TimerTask() {
            override fun run() {
                handler?.post {
                    if (!isApplicationBackground) {
                        engamentTimeToSend = totalEngagementTime
                        totalEngagementTime++
                    }
                }
            }
        }
        totalEngagementTime = 0
        engageTimer!!.schedule(timerTask, 0, 1000)
    }

    private fun sendEventForEngagement(
        @Suppress("SameParameterValue") eventName: String,
        engagementTimeToSend: Number
    ) {

        val app = DoubtnutApp.INSTANCE
        app.getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(app.applicationContext).toString())
            .addStudentId(getStudentId())
            .addScreenName(EventConstants.EVENT_GAME_SCREEN)
            .addEventParameter(
                mapOf(
                    EventConstants.EVENT_GAME_ENGAGEMENT_TOTAL_TIME to engagementTimeToSend,
                    EventConstants.EVENT_GAME to game,
                    game to engagementTimeToSend
                )
            )
            .track()
    }

    fun postDNActivityGame(gameId: String) {
        DataHandler.INSTANCE.teslaRepository.postDNActivityGame(gameId)
            .applyIoToMainSchedulerOnCompletable()
            .subscribeToCompletable({})
    }

    fun storeGameOpenedCoreAction() {
        viewModelScope.launch {
            userActivityRepository.storeCoreActionDone(CoreActions.GAME_OPEN).catch { }.collect()
        }
    }

    override fun onCleared() {
        super.onCleared()
        engageTimer?.cancel()
        engageTimer = null

        timerTask?.cancel()
        timerTask = null
    }
}