package com.doubtnut.core.utils

import android.animation.Animator
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.google.firebase.crashlytics.FirebaseCrashlytics

object LottieAnimationViewUtils {

    private fun LottieAnimationView.onAnimationEndListener(onAnimationEnd: (Animator) -> Unit) {
        addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                animation?.let { (onAnimationEnd(animation)) }
            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationRepeat(animation: Animator?) {}
        })
    }

    fun LottieAnimationView.applyAnimationFromUrl(
        animationUrl: String?,
        repeatCount: Int = LottieDrawable.INFINITE,
        throwable: ((Throwable) -> Unit)? = null,
        onAnimationEnd: ((Animator) -> Unit)? = null
    ) {
        if (animationUrl.isNotNullAndNotEmpty2()) {
            try {
                setRepeatCount(repeatCount)
                setAnimationFromUrl(animationUrl)
                playAnimation()
                setFailureListener {
                    throwable?.invoke(it)
                    FirebaseCrashlytics.getInstance()
                        .recordException(Throwable("Invalid Lottie Url - " + it.message))
                }
                onAnimationEndListener { animator ->
                    onAnimationEnd?.invoke(animator)
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance()
                    .recordException(Throwable("Invalid Lottie Url - " + e.message))
            }
        }
    }

    fun LottieAnimationView.applyAnimationFromAsset(
        animationFile: String?,
        repeatCount: Int = LottieDrawable.INFINITE,
        throwable: ((Throwable) -> Unit)? = null,
        onAnimationEnd: ((Animator) -> Unit)? = null
    ) {
        if (animationFile.isNotNullAndNotEmpty2()) {
            try {
                setRepeatCount(repeatCount)
                setAnimation(animationFile)
                playAnimation()
                setFailureListener {
                    throwable?.invoke(it)
                    FirebaseCrashlytics.getInstance()
                        .recordException(Throwable("Invalid Lottie File - " + it.message))
                }
                onAnimationEndListener { animator ->
                    onAnimationEnd?.invoke(animator)
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance()
                    .recordException(Throwable("Invalid Lottie File - " + e.message))
            }
        }
    }
}
