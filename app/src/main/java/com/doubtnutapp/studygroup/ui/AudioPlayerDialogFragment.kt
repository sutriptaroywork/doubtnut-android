package com.doubtnutapp.studygroup.ui

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentAudioPlayerDialogBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.getHumanTimeText
import com.doubtnutapp.orDefaultValue
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnut.core.utils.viewModelProvider
import java.io.IOException


class AudioPlayerDialogFragment : BaseBindingDialogFragment<DummyViewModel, FragmentAudioPlayerDialogBinding>() {

    companion object {
        const val TAG = "AudioPlayerDialogFragment"

        private const val AUDIO_DURATION = "audio_duration"
        private const val AUDIO_URL = "audio_url"

        fun newInstance(audioDuration: Long?, audioUrl: String?): AudioPlayerDialogFragment =
            AudioPlayerDialogFragment().apply {
                val bundle = Bundle()
                bundle.putLong(AUDIO_DURATION, audioDuration ?: 0L)
                bundle.putString(AUDIO_URL, audioUrl)
                arguments = bundle
            }
    }

    private val mediaPlayer = MediaPlayer()

    enum class PlayerState{
        PLAY,
        PAUSE
    }

    private var mediaPlayerState = PlayerState.PAUSE

    private var finalTime: Long? = null
    private var startTime: Int? = null
    private val myHandler: Handler = Handler()
    private var audioUrl: String = ""

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAudioPlayerDialogBinding =
        FragmentAudioPlayerDialogBinding.inflate(layoutInflater)

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setOnKeyListener { dialog, keyCode, event ->

            if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                myHandler.removeCallbacksAndMessages(null)
                stopPlaying()
                dismiss()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        finalTime = arguments?.getLong(AUDIO_DURATION, 0L)
        audioUrl = arguments?.getString(AUDIO_URL).orDefaultValue()

        binding.seekBar.max = finalTime?.toInt() ?: 0
        binding.tvDuration.text = finalTime?.getHumanTimeText()
        prepareMediaPlayer()
        setUpListeners()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar)
    }

    override fun onDismiss(dialog: DialogInterface) {
        myHandler.removeCallbacksAndMessages(null)
        stopPlaying()
        super.onDismiss(dialog)
    }

    private fun setUpListeners() {
        binding.ivPlayPause.setOnClickListener {
            playPausePlayer()
        }

        binding.audioPlayerDialogRootContainer.setOnClickListener {
            myHandler.removeCallbacksAndMessages(null)
            stopPlaying()
            dialog?.dismiss()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.seekTo(seekBar?.progress ?: 0)
                }
            }
        })

        mediaPlayer.setOnPreparedListener {
            playPausePlayer()
            startTime = if (binding.seekBar.progress == 0) {
                mediaPlayer.currentPosition
            } else {
                mediaPlayer.seekTo(binding.seekBar.progress)
                binding.seekBar.progress
            }
            binding.tvDuration.text = startTime?.toLong()?.getHumanTimeText()
            myHandler.post(updatePlayerTime)
        }

        mediaPlayer.setOnCompletionListener {
            if (it.isPlaying.not()) {
                binding.ivPlayPause.setImageResource(R.drawable.ic_voice_note_play_button)
                mediaPlayer.seekTo(0)
                mediaPlayerState = PlayerState.PAUSE
            }
        }
    }

    private fun playPausePlayer() {
        if (mediaPlayerState == PlayerState.PAUSE) {
            mediaPlayer.start()
            mediaPlayerState = PlayerState.PLAY
            binding.ivPlayPause.setImageResource(R.drawable.ic_pause_tomato_24_dp)
        } else {
            mediaPlayer.pause()
            mediaPlayerState = PlayerState.PAUSE
            binding.ivPlayPause.setImageResource(R.drawable.ic_voice_note_play_button)
        }
    }

    private fun prepareMediaPlayer() {
        mediaPlayer.apply {
            try {
                setDataSource(audioUrl)
                prepareAsync()
                isLooping = false
            } catch (e: IOException) {
                android.util.Log.e("AudioPlayerWidget", "prepare() failed")
            }
        }
    }

    private val updatePlayerTime: Runnable = object : Runnable {
        override fun run() {
            startTime = mediaPlayer.currentPosition

            binding.tvDuration.text = startTime?.toLong()?.getHumanTimeText()
            binding.seekBar.progress = startTime!!
            myHandler.postDelayed(this, 100)
        }
    }

    private fun stopPlaying() {
        mediaPlayer.release()
    }
}