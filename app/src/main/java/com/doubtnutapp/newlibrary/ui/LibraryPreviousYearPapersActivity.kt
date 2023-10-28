package com.doubtnutapp.newlibrary.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityLibraryPreviousYearPapersBinding
import com.doubtnutapp.newlibrary.model.FilterData
import com.doubtnutapp.newlibrary.model.LibraryPreviousYearPapers
import com.doubtnutapp.newlibrary.model.LibraryPreviousYearPapersFilter
import com.doubtnutapp.newlibrary.viewmodel.LibraryPreviousYearPapersViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.google.android.material.tabs.TabLayoutMediator

class LibraryPreviousYearPapersActivity :
    BaseBindingActivity<LibraryPreviousYearPapersViewModel, ActivityLibraryPreviousYearPapersBinding>() {

    companion object {
        const val TAG = "NewLibraryPreviousYearsPapersActivity"
        const val PARAM_EXAM_ID = "exam_id"
        fun getStartIntent(context: Context, examId: String, source: String) =
            Intent(context, LibraryPreviousYearPapersActivity::class.java).apply {
                putExtra(PARAM_EXAM_ID, examId)
                putExtra(Constants.SOURCE, source)
            }
    }

    private val examId: String by lazy { intent.getStringExtra(PARAM_EXAM_ID).orDefaultValue() }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(): ActivityLibraryPreviousYearPapersBinding =
        ActivityLibraryPreviousYearPapersBinding.inflate(layoutInflater)

    override fun provideViewModel(): LibraryPreviousYearPapersViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        viewModel.getPreviousPapersTabsAndFilterData(examId)
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.data.observeK(
            this,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgress
        )
    }

    private fun onSuccess(data: LibraryPreviousYearPapers) {
        binding.apply {
            toolbar.tvTitle.text = data.pageTitle
            toolbar.ivBack.setOnClickListener {
                onBackPressed()
            }

            val isSingleItem = data.tabData.size == 1
            tabLayout.setVisibleState(!isSingleItem)

            val allFilters: MutableList<List<LibraryPreviousYearPapersFilter>> = mutableListOf()
            data.tabData.forEach {
                allFilters.add(it.filterData)
            }

            val adapter = ViewPagerAdapter(
                this@LibraryPreviousYearPapersActivity,
                data.tabData.map { it.tabId },
                FilterData(allFilters),
                examId
            )
            viewpager.adapter = adapter
            TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, position ->
                tab.text = data.tabData[position].tabText
            }.attach()
        }
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        this.let { currentContext ->
            if (NetworkUtils.isConnected(currentContext)) {
                toast(getString(R.string.somethingWentWrong))
            } else {
                toast(getString(R.string.string_noInternetConnection))
            }
        }
    }

    private fun unAuthorizeUserError() {
        supportFragmentManager.beginTransaction()
            .add(BadRequestDialog.newInstance("unauthorized"), "BadRequestDialog").commit()
    }

    private fun updateProgress(state: Boolean) {}
}