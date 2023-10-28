package com.doubtnutapp.videoPage.ui

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.model.Video
import com.doubtnutapp.sticker.BaseActivity
import com.doubtnutapp.video.VideoFragment
import com.doubtnutapp.video.VideoFragmentListener
import com.doubtnutapp.video.VideoPlayerManager
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject


class FullScreenVideoPageActivity : BaseActivity(), VideoFragmentListener,
        HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    private var videoModel: Video? = null
    private var videoPlayerManager: VideoPlayerManager? = null

    companion object {
        private const val VIDEO_DATA = "video_data"
        private const val USE_PAGE_WITHOUT_APPEND = "use_page_without_append"
        fun startActivity(context: Context, video: Video, usePageWithoutAppend: Boolean = false): Intent {
            return Intent(context, FullScreenVideoPageActivity::class.java).apply {
                putExtra(VIDEO_DATA, video)
                putExtra(USE_PAGE_WITHOUT_APPEND, usePageWithoutAppend)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_video)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN xor View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions
        videoModel = intent.getParcelableExtra(VIDEO_DATA)
        videoPlayerManager = null
        videoModel?.videoResources?.map { it.isPlayed = false }
        videoPlayerManager = VideoPlayerManager(supportFragmentManager,
                object : VideoFragmentListener {
                    override fun onPortraitRequested() {
                        finish()
                    }

                    override fun singleTapOnPlayerView() {
                        if (videoPlayerManager?.isPlayerControllerVisible == true) {
                            videoPlayerManager?.hidePlayerController()
                        } else {
                            videoPlayerManager?.showPlayerController()
                        }
                    }
                },
                R.id.videoContainer, { _, _ -> })
        val page = videoModel?.videoPage ?: ""

        videoPlayerManager?.setAndInitPlayFromResource(
                videoModel?.questionId.orEmpty(),
                videoModel?.videoResources!!,
                videoModel?.viewId.orEmpty(),
                videoModel?.videoPosition ?: 0,
                false,
                if (intent.getBooleanExtra(USE_PAGE_WITHOUT_APPEND, false)) page else page.plus("_FULLSCREEN"),
                VideoFragment.DEFAULT_ASPECT_RATIO,
                null, null, true)
    }

    override fun onDestroy() {
        val lastDuration = videoPlayerManager?.currentPlayerPosition ?: 0
        DoubtnutApp.INSTANCE.bus()?.send(LastFullscreenDuration(lastDuration.toLong()))
        super.onDestroy()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        videoPlayerManager?.enterFullScreen()
    }

    class LastFullscreenDuration(val duration: Long)

}