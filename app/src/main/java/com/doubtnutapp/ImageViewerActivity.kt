package com.doubtnutapp

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import com.bumptech.glide.Glide
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.databinding.ActivityImageViewerBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity

/**
 * Created by Anand Gaurav on 25/03/20.
 */
class ImageViewerActivity : BaseBindingActivity<DummyViewModel, ActivityImageViewerBinding>() {

    companion object {
        const val IMAGE_URL = "IMAGE_URL"
        private const val TAG = "ImageViewerActivity"
        fun getStartIntent(context: Context, imageUrl: String) =
            Intent(context, ImageViewerActivity::class.java).apply {
                putExtra(IMAGE_URL, imageUrl)
            }
    }

    override fun provideViewBinding(): ActivityImageViewerBinding {
        return ActivityImageViewerBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        statusbarColor(this, R.color.white_20)
        val imageUrl = intent.getStringExtra(IMAGE_URL)
        Glide.with(this).load(imageUrl).into(binding.imageView)
        binding.imageViewClose.setOnClickListener {
            finish()
        }
        binding.viewRotate.setOnClickListener {
            requestedOrientation =
                if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
        }
    }
}
