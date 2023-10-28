package com.doubtnutapp.examcorner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ActivityExamCornerBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.store.ui.adapter.StoreResultPagerAdapter
import com.doubtnutapp.ui.base.BaseBindingActivity

class ExamCornerActivity :
    BaseBindingActivity<DummyViewModel, ActivityExamCornerBinding>() {

    companion object {
        private const val TAG = "ExamCornerActivity"
        private const val EXAM_CORNER_TYPE_NEWS = "news"
        private const val EXAM_CORNER_TYPE_CAREER = "careers"
        fun getStartIntent(context: Context) =
            Intent(context, ExamCornerActivity::class.java)
    }

    override fun provideViewBinding(): ActivityExamCornerBinding =
        ActivityExamCornerBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        setSupportActionBar(binding.toolbar)
        binding.buttonBack.setOnClickListener {
            onBackPressed()
        }
        binding.viewPager.adapter = StoreResultPagerAdapter(
            supportFragmentManager,
            listOf(
                ExamCornerFragment.newInstance(EXAM_CORNER_TYPE_NEWS),
                ExamCornerFragment.newInstance(EXAM_CORNER_TYPE_CAREER)
            ),
            listOf(getString(R.string.news), getString(R.string.career))
        )
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        binding.ivBookmark.setOnClickListener {
            startActivity(ExamCornerBookmarkActivity.getStartIntent(this))
        }
    }
}
