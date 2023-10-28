package com.doubtnutapp.liveclass.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.databinding.ActivityHomeworkBinding

import com.doubtnutapp.liveclass.viewmodel.HomeworkViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity


class HomeWorkActivity : BaseBindingActivity<HomeworkViewModel, ActivityHomeworkBinding>() {

    companion object {
        const val TAG = "HomeWorkActivity"
        const val QUESTION_ID = "question_id"
        const val TYPE_SHARE = "SHARE"
        const val TYPE_DOWNLOAD = "DOWNLOAD"

        fun startActivity(context: Context, start: Boolean = true, questionId: String): Intent {
            return Intent(context, HomeWorkActivity::class.java).apply {
                putExtra(QUESTION_ID, questionId)
            }.also {
                if (start) context.startActivity(it)
            }
        }

        fun getIntent(context: Context, questionId: String): Intent {
            return Intent(context, HomeWorkActivity::class.java).apply {
                putExtra(QUESTION_ID, questionId)
            }
        }
    }

    override fun provideViewBinding(): ActivityHomeworkBinding {
        return ActivityHomeworkBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): HomeworkViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        supportFragmentManager.beginTransaction()
            .replace(
                binding.container.id,
                HomeWorkFragment.newInstance(intent.getStringExtra(QUESTION_ID))
            ).commitAllowingStateLoss()
    }
}