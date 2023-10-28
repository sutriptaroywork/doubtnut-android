package com.doubtnutapp.notificationmanager

/**
 * Created by Akshat Jindal on 01/06/22.
 */

import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import com.doubtnutapp.R
import java.util.concurrent.TimeUnit

enum class TimerState { STOPPED, PAUSED, RUNNING, TERMINATED }

class TimerService : Service() {

    companion object {
        var state = TimerState.TERMINATED
    }

    private lateinit var timer: CountDownTimer

    private val foreGroundId = NotificationTimer.notificationId
    private var secondsRemaining: Long = 0
    private var setTime: Long = 0
    private lateinit var showTime: String
    private val TAG = "generic_sticky_timer"

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                "PLAY" -> {
                    playTimer(
                        intent.getLongExtra("setTime", 0L),
                        intent.getBooleanExtra("forReplay", false)
                    )
                }
                "TERMINATE" -> terminateTimer()
            }
        }
        return START_NOT_STICKY
    }

    private fun playTimer(setTime: Long, isReplay: Boolean) {

        if (!isReplay) {
            this.setTime = setTime
            secondsRemaining = setTime
            startForeground(foreGroundId, NotificationTimer.createNotification(this, setTime))
        }

        timer = object : CountDownTimer(secondsRemaining, 1000) {
            override fun onFinish() {
                state = TimerState.STOPPED
                terminateTimer()
            }

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished
                updateCountdownUI()
            }
        }.start()

        state = TimerState.RUNNING
    }

    private fun terminateTimer() {
        if (::timer.isInitialized) {
            timer.cancel()
            state = TimerState.TERMINATED
            NotificationTimer.removeNotification()
            stopSelf()
        }
    }

    private fun updateCountdownUI() {
        showTime = getTimeInHHMMSS(secondsRemaining)
        NotificationTimer.updateTimeLeft(this, showTime)
    }

    private fun getTimeInHHMMSS(millisUntilFinished: Long) = getString(
        R.string.string_quiz_question_timer,
        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(
                        millisUntilFinished
                    )
                )),
        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(
                        millisUntilFinished
                    )
                ))
    )

}