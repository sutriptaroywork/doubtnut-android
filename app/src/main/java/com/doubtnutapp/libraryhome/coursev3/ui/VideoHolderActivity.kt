package com.doubtnutapp.libraryhome.coursev3.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doubtnutapp.databinding.ActivityVideoHolderBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.model.Video
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnut.core.utils.viewModelProvider

class VideoHolderActivity : BaseBindingActivity<DummyViewModel, ActivityVideoHolderBinding>() {

    companion object {
        private const val TAG = "VideoHolderActivity"
        private const val VIDEO_DATA = "video_data"

        fun getStartIntent(context: Context, video: Video): Intent {
            return Intent(context, VideoHolderActivity::class.java).apply {
                putExtra(VIDEO_DATA, video)
            }
        }
    }

    override fun provideViewBinding(): ActivityVideoHolderBinding {
        return ActivityVideoHolderBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        binding.parentView.setOnClickListener {
            finish()
        }
        val video: Video? = intent?.getParcelableExtra(VIDEO_DATA)
        if (video == null) {
            finish()
            return
        }
        val videoDialog = CourseVideoDialog.newInstance(video)
        videoDialog.show(supportFragmentManager, CourseVideoDialog.TAG)
    }
}