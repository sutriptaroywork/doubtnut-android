package com.doubtnutapp.liveclass.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.Status
import com.doubtnutapp.databinding.ActivityImageSelectionBinding
import com.doubtnutapp.liveclass.viewmodel.LiveClassChatViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.KeyboardUtils.hideKeyboard
import com.doubtnutapp.utils.showToast

class ImageCaptionActivity : BaseBindingActivity<LiveClassChatViewModel,ActivityImageSelectionBinding>() {

    private var imagePath: String = ""
    private var type: Int = IMAGE_SHOW
    private var imageFileName: String = ""

    companion object {
        private const val IMAGE_PATH = "imagePath"
        private const val TYPE = "type"
        const val IMAGE_FILE_NAME = "fileName"
        const val MESSAGE = "message"
        const val IMAGE_SHOW = 0
        const val IMAGE_CAPTION = 1
        const val TAG = "ImageCaptionActivity"

        fun getStartIntent(context: Context, imagePath: String, type: Int): Intent {
            val intent = Intent(context, ImageCaptionActivity::class.java)
            intent.putExtra(IMAGE_PATH, imagePath)
            intent.putExtra(TYPE, type)
            return intent
        }
    }

    private fun setupObserver() {
        viewModel.imageFileNameLiveData.observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                imageFileName = it
                binding.progressBar.hide()
                val intent = Intent()
                intent.putExtra(MESSAGE, binding.etMsg.text.toString())
                intent.putExtra(IMAGE_FILE_NAME, imageFileName)
                setResult(RESULT_OK, intent)
                finish()
            }
        })

        viewModel.stateLiveData.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> {
                    binding.progressBar.show()
                }
                Status.ERROR -> {
                    binding.progressBar.hide()
                    showToast(this, it.message!!)
                }
            }
        })
    }

    private fun setupListeners() {
        if (type == IMAGE_CAPTION) {
            binding.btnSend.setOnClickListener {
                hideKeyboard(binding.etMsg)
                viewModel.getSignedUrlsForAttachments(imagePath)
            }
        } else {
            binding.layoutSend.visibility = View.GONE
        }
        binding.ivBack.setOnClickListener {
            super.onBackPressed()
        }
    }

    private fun init() {
        viewModel = viewModelProvider(viewModelFactory)
        imagePath = intent.getStringExtra(IMAGE_PATH).orDefaultValue()
        type = intent.getIntExtra(TYPE, IMAGE_SHOW)
        binding.ivAttachment.loadImageEtx(imagePath)
        if (type == IMAGE_CAPTION)
            binding.tvToolbarTitle.text = getString(R.string.image_caption)
    }

    override fun provideViewBinding(): ActivityImageSelectionBinding {
        return ActivityImageSelectionBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): LiveClassChatViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        init()
        setupListeners()
        if (type == IMAGE_CAPTION) {
            setupObserver()
        }
    }
}