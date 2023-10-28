package com.doubtnutapp.liveclass.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.ui.mediahelper.MEDIA_TYPE_BLOB
import com.google.android.exoplayer2.ExoPlaybackException
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_audio.*
import kotlinx.android.synthetic.main.exo_audio_player_control_view.view.*

class AudioPlayerActivity : AppCompatActivity(),
        ExoPlayerHelper.ExoPlayerStateListener,
        ExoPlayerHelper.MediaSourceStatusListener {

    companion object {
        fun getStartIntent(context: Context, url: String) =
                Intent(context, AudioPlayerActivity::class.java).apply {
                    putExtra(Constants.URL, url)
                }
    }

    private var exoPlayerHelper: ExoPlayerHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)
        setupPlayer(intent.getStringExtra(Constants.URL))
        parentView.setOnClickListener {
            finish()
        }
    }

    private fun setupPlayer(streamUrl: String?) {
        if (streamUrl == null) return
        startPlayer(streamUrl)
    }

    private fun startPlayer(streamUrl: String) {
        exoPlayerHelper = ExoPlayerHelper(this, playerView)
        lifecycle.addObserver(exoPlayerHelper!!)
        exoPlayerHelper!!.setMediaData(MEDIA_TYPE_BLOB)
        playerView.linearLayout3.show()
        exoPlayerHelper!!.setVideoUrl(streamUrl)
        exoPlayerHelper!!.setFallbackUrl(streamUrl)
        exoPlayerHelper!!.setExoPlayerStateListener(this)
        exoPlayerHelper!!.setMediaSourceStatusListener(this)
        playerView.showController()
        playerView.controllerHideOnTouch = false
        playerView.exo_fullscreen.hide()
    }


    override fun onPlayerStart() {
        bufferingProgressBar.hide()
    }

    override fun onPlayerBuffering() {
        bufferingProgressBar.show()
    }

    override fun onPlayerEnd() {

    }

    override fun onPlayerPause() {

    }


    override fun onMediaSourceSelected(mediaSourceType: ExoPlayerHelper.MediaSourceType) {

    }

    override fun onMediaSourceFailed(mediaSourceType: ExoPlayerHelper.MediaSourceType,
                                     error: ExoPlaybackException?, fromFallbackHandler: Boolean,
                                     hlsTimeoutTime: Long, videoUrl: String) {

    }

    override fun hasToShowDateDialog() {

    }
}