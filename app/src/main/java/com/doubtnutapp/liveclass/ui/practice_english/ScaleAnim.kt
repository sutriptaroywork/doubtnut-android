package com.doubtnutapp.liveclass.ui.practice_english

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

class ScaleAnim(view: View) {
    private val view: View
    fun start() {
        val set = AnimatorSet()
        val scaleY: ObjectAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1.3f)
        val scaleX: ObjectAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1.3f)
        set.duration = 150
        set.interpolator = AccelerateDecelerateInterpolator()
        set.playTogether(scaleY, scaleX)
        set.start()
    }

    fun stop() {
        val set = AnimatorSet()
        val scaleY: ObjectAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1.0f)
        //        scaleY.setDuration(250);
        //        scaleY.setInterpolator(new DecelerateInterpolator());
        val scaleX: ObjectAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1.0f)
        //        scaleX.setDuration(250);
        //        scaleX.setInterpolator(new DecelerateInterpolator());
        set.duration = 150
        set.interpolator = AccelerateDecelerateInterpolator()
        set.playTogether(scaleY, scaleX)
        set.start()
    }

    init {
        this.view = view
    }
}