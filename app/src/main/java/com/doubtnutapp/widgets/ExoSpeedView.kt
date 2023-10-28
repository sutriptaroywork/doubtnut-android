package com.doubtnutapp.widgets

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper

class ExoSpeedView(context: Context, attrs: AttributeSet) : AppCompatTextView(context, attrs) {

    init {
        setOnClickListener {
            showSpeedSelectorDialog()
        }
    }

    private var currentPlaybackSpeed: Float = 1.0f

    private var speedChangeListener: SpeedChangeListener? = null

    private fun showSpeedSelectorDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Playback Speed")

        val items = ExoPlayerHelper.SPEED_LIST.map {
            if (it == 1f) "Normal" else "${it}x"
        }.toTypedArray()

        val checkedItem = ExoPlayerHelper.SPEED_LIST.indexOf(currentPlaybackSpeed)
        builder.setSingleChoiceItems(items, checkedItem) { dialog, which ->
            setPlaybackSpeed(ExoPlayerHelper.SPEED_LIST[which])
            speedChangeListener?.onSpeedChanged(this.currentPlaybackSpeed)
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()

    }

    fun setPlaybackSpeed(playbackSpeed: Float) {
        this.currentPlaybackSpeed = playbackSpeed
        text = "${currentPlaybackSpeed}x"
    }

    fun setSpeedChangeListener(listener: SpeedChangeListener) {
        this.speedChangeListener = listener
    }

    interface SpeedChangeListener {
        fun onSpeedChanged(selectedSpeed: Float)
    }

}