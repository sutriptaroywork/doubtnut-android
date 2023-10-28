package com.doubtnutapp.gamification.popactivity.ui.viewholder

import android.os.Handler
import android.view.View
import androidx.core.os.postDelayed
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.base.FinishActivity
import com.doubtnutapp.gamification.popactivity.model.GamificationPopup

abstract class PopViewHolder<T : GamificationPopup>(val itemView: View) : LifecycleObserver {

    lateinit var actionPerformer: ActionPerformer
    lateinit var lifecycleOwner: LifecycleOwner

    private val timerHandler = Handler()

    open fun bind(data: T) {
        if (data.duration > 0) {
            startTimer(data)
        }
        addThisAsObserver()
    }

    fun unBind() {
        timerHandler.removeCallbacksAndMessages(null)
        lifecycleOwner.lifecycle.removeObserver(this)
    }

    private fun addThisAsObserver() {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    private fun startTimer(data: T) {
        timerHandler.postDelayed(data.duration) {
            performAction(FinishActivity)
        }
    }

    fun performAction(action: Any) {
        this.actionPerformer.performAction(action)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun removeTimer() {
        timerHandler.removeCallbacksAndMessages(null)
        performAction(FinishActivity)
    }
}