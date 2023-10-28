package com.doubtnutapp.matchquestion.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.databinding.FullImageViewActivityBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.ui.base.BaseBindingActivity

class FullImageViewActivity : BaseBindingActivity<DummyViewModel, FullImageViewActivityBinding>() {

    companion object {
        private const val TAG = "FullImageViewActivity"
        const val INTENT_EXTRA_ASKED_QUESTION_URI = "ask_que_uri"
        const val INTENT_EXTRA_TITLE = "title"

        fun getStartIntent(
            context: Context,
            askQuestionImageUri: String,
            title: String? = null
        ): Intent =
            Intent(context, FullImageViewActivity::class.java).apply {
                putExtra(INTENT_EXTRA_ASKED_QUESTION_URI, askQuestionImageUri)
                putExtra(INTENT_EXTRA_TITLE, title)
            }
    }

    override fun provideViewBinding(): FullImageViewActivityBinding =
        FullImageViewActivityBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DummyViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        intent?.getStringExtra(INTENT_EXTRA_ASKED_QUESTION_URI)?.let { askedQuestionImageUri ->
            binding.previewImage.loadImage(askedQuestionImageUri)
        }
        intent?.getStringExtra(INTENT_EXTRA_TITLE)?.let { title ->
            binding.tvTitle.text = title
        }
        binding.backImageView.setOnClickListener {
            finish()
        }
    }
}
