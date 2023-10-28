package com.doubtnutapp.mathview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ActivityMathViewBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.statusbarColor
import com.doubtnutapp.ui.base.BaseBindingActivity

/**
 * Created by Anand Gaurav on 2021-06-02.
 */
class MathViewActivity : BaseBindingActivity<DummyViewModel, ActivityMathViewBinding>() {

    companion object {
        private const val INTENT_EXTRA_RESOURCE_DATA = "resource_data"
        const val TAG = "MathViewActivity"
        fun getStartIntent(context: Context, resourceData: String?): Intent {
            return Intent(context, MathViewActivity::class.java).apply {
                putExtra(INTENT_EXTRA_RESOURCE_DATA, resourceData)
            }
        }
    }

    private fun initMathView(resourceData: String?) {
        resourceData?.apply {
            binding.mathView.question = this
            binding.mathView.javaInterfaceForAppCompatActivity = this@MathViewActivity
        }
    }

    override fun provideViewBinding(): ActivityMathViewBinding {
        return ActivityMathViewBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        statusbarColor(this, R.color.white_50)
        binding.ivClose.setOnClickListener {
            onBackPressed()
        }
        initMathView(intent?.getStringExtra(INTENT_EXTRA_RESOURCE_DATA))
    }

}
