package com.doubtnutapp.widgets.typewriter

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import androidx.annotation.IntRange
import androidx.appcompat.widget.AppCompatTextView
import java.util.concurrent.TimeUnit

class TypeWriterTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var mText: CharSequence = ""
    private var mIndex = 0
    private var mCharDelay: Long = 100 //Default 500ms delay
    private var mChangeStringDelay: Long = 250 //Default 2 sec delay
    private var mRepeatDelay: Long = 1000
    private var currentPosition = 0
    private var animating = false
    private val mHandler: Handler = Handler()
    private var textLists = listOf<String>()
    private var lastText: String? = null

    /**
     * set isRepeat true to loop animations
     */

    var isRepeat = false

    private val mCharacterAdder: Runnable = object : Runnable {
        override fun run() {
            if (animating) {
                text = mText.subSequence(0, mIndex++)
                if (mIndex <= mText.length) {
                    mHandler.postDelayed(this, mCharDelay)
                } else {
                    animating = false
                    nextAnimCheck()
                }
            }
        }
    }

    private fun nextAnimCheck() {
        if (!isRepeat)
            return
        val nextDelay = Runnable {
            startAnimation(currentPosition)
        }
        if (currentPosition >= textLists.size) {
            stopAnim()
        }
        mHandler.postDelayed(
            nextDelay,
            if (currentPosition == 0) mRepeatDelay else mChangeStringDelay
        )
    }

    private fun startAnimation(index: Int = 0) {
        animating = true
        mIndex = 0
        text = ""
        mText = textLists.getOrNull(index) ?: ""
        lastText = textLists.getOrNull(index)
        mHandler.removeCallbacks(mCharacterAdder)
        mHandler.postDelayed(mCharacterAdder, mCharDelay)
        currentPosition++
    }

    /**
     * Call this function to display multiple string animation
     *
     * @param list pass list of string to animate
     */

    fun setTexts(list: List<String>) {
        currentPosition = 0
        textLists = list
        text = list.getOrNull(0)
    }

    /**
     * Call this function to set delay in MILLISECOND [20..200]
     *
     * @param delay
     */

    fun setCharacterDelay(@IntRange(from = 20, to = 200) delay: Int) {
        mCharDelay = delay.toLong()
    }

    /**
     * Call this function to set repeat delay in MILLISECOND min 100ms
     *
     * @param delay
     */

    fun setChangeStringDelay(@IntRange(from = 100) delay: Long) {
        mChangeStringDelay = delay
    }

    /**
     * Call this function to set repeat delay in SECONDS min 1sec
     *
     * @param delay
     */

    fun setRepeatAnimDelay(@IntRange(from = 1) delay: Int) {
        mRepeatDelay = TimeUnit.SECONDS.toMillis(delay.toLong())
    }

    /**
     * Call this to remove animation at any time
     */
    fun stopAnim() {
        if (textLists.isNullOrEmpty()) {
            return
        }
        mHandler.removeCallbacks(mCharacterAdder)
        animating = false
        currentPosition = 0
        text = lastText
    }

    /**
     * Call this to start playing animations
     * @param initialDelay set delay in animation by default no delay
     */
    fun playAnim(initialDelay: Long = 1000) {
        if (textLists.isNullOrEmpty()) {
            return
        }
        if (animating) {
            stopAnim()
        }
        mHandler.postDelayed({
            startAnimation()
        }, initialDelay)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mHandler.removeCallbacksAndMessages(null)
    }
}