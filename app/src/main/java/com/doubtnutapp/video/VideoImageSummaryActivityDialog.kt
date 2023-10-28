package com.doubtnutapp.video

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doubtnutapp.R
import com.doubtnutapp.loadImage
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.utils.Utils
import kotlinx.android.synthetic.main.activity_video_image_summary.*

class VideoImageSummaryActivityDialog : BaseActivity() {

    companion object {

        private const val INTENT_EXTRA_SOLUTION_IMAGE_URL = "text_solution_link"

        @JvmStatic
        fun getStartIntent(context: Context, textSolutionLink: String) =
                Intent(context, VideoImageSummaryActivityDialog::class.java).apply {
                    putExtra(INTENT_EXTRA_SOLUTION_IMAGE_URL, textSolutionLink)
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Utils.isDeviceOrientationPortrait(this)) {
            requestFullScreen()
        }
        setContentView(R.layout.activity_video_image_summary)
        val textSolutionLink = intent.getStringExtra(INTENT_EXTRA_SOLUTION_IMAGE_URL)
        imageViewSolution.loadImage(textSolutionLink)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}