package com.doubtnutapp.libraryhome.coursev3.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.ApiTimetableData
import com.doubtnutapp.databinding.ActivityTimetableBinding
import com.doubtnutapp.libraryhome.coursev3.viewmodel.TimetableViewModel
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.showApiErrorToast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import dagger.android.HasAndroidInjector

class TimetableActivity : BaseBindingActivity<TimetableViewModel, ActivityTimetableBinding>(), HasAndroidInjector {

    private lateinit var adapter: WidgetLayoutAdapter
    private var courseId = ""

    companion object {
        const val TAG = "TimetableActivity"
        const val COURSE_ID = "course_id"

        fun getStartIntent(context: Context, courseId: String): Intent {
            val intent = Intent(context, TimetableActivity::class.java)
            intent.putExtra(COURSE_ID, courseId)
            return intent
        }
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(): ActivityTimetableBinding =
        ActivityTimetableBinding.inflate(layoutInflater)

    override fun provideViewModel(): TimetableViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        setupRecyclerView()
        initListeners()
        courseId = intent.getStringExtra(COURSE_ID).orEmpty()
        viewModel.getTimetable(courseId, UserUtil.getStudentClass())
    }

    private fun initListeners() {
        binding.ivBack.setOnClickListener {
            super.onBackPressed()
        }
    }

    override fun setupObservers() {
        viewModel.widgetsLiveData.observeK(
                this@TimetableActivity,
                this::onWidgetListFetched,
                this::onApiError,
                this::unAuthorizeUserError,
                this::ioExceptionHandler,
                this::updateProgress
        )
    }

    private fun setupRecyclerView() {
        adapter = WidgetLayoutAdapter(this)
        binding.rvWidgets.layoutManager = LinearLayoutManager(this)
        binding.rvWidgets.adapter = adapter
    }

    private fun onWidgetListFetched(data: ApiTimetableData) {
        adapter.setWidgets(data.widgets)
        binding.tvToolbarTitle.text = data.title
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        showApiErrorToast(this)
    }

    private fun unAuthorizeUserError() {
        showApiErrorToast(this)
    }

    private fun updateProgress(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }
}