package com.doubtnutapp.videoPage.ui

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.navigation.ui.AppBarConfiguration
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ActivityYoutubeTypeVideoBinding
import com.doubtnutapp.video.VideoFragment
import com.doubtnutapp.video.VideoFragmentListener

class YoutubeTypeVideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityYoutubeTypeVideoBinding

    val youtubeId: String? by lazy {
        intent.getStringExtra(YOUTUBE_VIDEO_ID)
    }

    companion object {

        const val YOUTUBE_VIDEO_ID = "youtubeId"

        fun getStartIntent(context: Context, youtubeId: String): Intent {
            val intent = Intent(context, YoutubeTypeVideoActivity::class.java)
            intent.putExtra(YOUTUBE_VIDEO_ID, youtubeId)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityYoutubeTypeVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoData = VideoFragment.Companion.YoutubeVideoData(
            youtubeId!!,
            showFullScreen = true,
            showEngagementTime = false
        )
        val videoFragment =
            VideoFragment.newYoutubeInstance(videoData, object : VideoFragmentListener {
            })
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.placeholder, videoFragment)
            .commit()

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation.equals(Configuration.ORIENTATION_PORTRAIT)) {
            // set top margin to show full screen like view for portrait mode
            binding.placeholder.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToTop = binding.guideline.id
            }
        } else {
            binding.placeholder.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToTop = binding.rootContainer.id
            }
        }
    }


}