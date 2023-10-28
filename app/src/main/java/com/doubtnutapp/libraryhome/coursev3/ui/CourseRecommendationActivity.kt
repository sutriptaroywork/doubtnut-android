package com.doubtnutapp.libraryhome.coursev3.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ActivityCourseRecommendationBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity

class CourseRecommendationActivity :
    BaseBindingActivity<DummyViewModel, ActivityCourseRecommendationBinding>() {

    companion object {
        private const val TAG = "CourseRecommendationActivity"
        private const val IS_BACK = "is_back"
        private const val PAGE = "page"
        fun getStartIntent(context: Context, isBack: Boolean, page: String = "") =
                Intent(context, CourseRecommendationActivity::class.java)
                        .apply {
                            putExtra(IS_BACK, isBack)
                            putExtra(PAGE, page)
                        }
    }

    override fun provideViewBinding(): ActivityCourseRecommendationBinding =
        ActivityCourseRecommendationBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DummyViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        supportFragmentManager.beginTransaction()
            .apply {
                add(
                    R.id.fragmentContainer,
                    CourseRecommendationFragment.newInstance(intent.getBooleanExtra(IS_BACK, false),
                            intent.getStringExtra(PAGE))
                )
                commit()
            }
    }

}