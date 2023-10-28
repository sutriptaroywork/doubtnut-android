package com.doubtnutapp.ui.questionAskedHistory

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.ActivityQuestionAskedHistoryBinding
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils

class QuestionAskedHistoryActivity :
    BaseBindingActivity<QuestionAskedHistoryViewModel, ActivityQuestionAskedHistoryBinding>() {

    companion object {
        const val TAG = "QuestionAskedHistoryActivity"

        fun getStartIntent(context: Context) =
            Intent(context, QuestionAskedHistoryActivity::class.java)
    }

    private var nextUrl = "/v1/question/watch-history?page=1"

    private lateinit var questionAskedHistoryListAdapter: QuestionAskedHistoryListAdapter

    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener

    override fun provideViewBinding(): ActivityQuestionAskedHistoryBinding {
        return ActivityQuestionAskedHistoryBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): QuestionAskedHistoryViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setAppbar()
        init()
        getQuestionAskedHistoryFromURLResult()
    }

    override fun getStatusBarColor(): Int {
        return R.color.grey_statusbar_color
    }

    private fun init() {
        setupRecyclerView()
        binding.textViewAskQuestion.setOnClickListener {
            openCameraPage()
        }
    }

    private fun openCameraPage() {
        startActivity(CameraActivity.getStartIntent(baseContext, "QuestionAskActivity").apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    private fun setAppbar() {
        setSupportActionBar(binding.toolbar)
        binding.textViewTitle.text = getString(R.string.question_asked_history)
        binding.buttonBack.setOnClickListener {
            finish()
        }
        binding.toolbar.setContentInsetsAbsolute(0, 0)
    }

    private fun getQuestionAskedHistoryFromURLResult() {
        viewModel.getQuestionAskedHistoryListFromURL(nextUrl).observe(this, {
            updateProgressBarState(it is Outcome.Progress)
            when (it) {
                is Outcome.Success -> onSuccess(it.data.data)
                is Outcome.Failure -> ioExceptionHandler()
                is Outcome.BadRequest -> unAuthorizeUserError()
                is Outcome.ApiError -> onApiError()
                else -> {
                }
            }
        })
    }

    private fun onSuccess(questionAskedHistoryDetails: QuestionAskedHistoryDetails) {
        if (questionAskedHistoryDetails.questionAskedList.isNotEmpty()) {
            questionAskedHistoryListAdapter.updateList(questionAskedHistoryDetails.questionAskedList)
            nextUrl = questionAskedHistoryDetails.nextUrl
        } else {
            if (questionAskedHistoryListAdapter.itemCount == 0) {
                binding.noQuestionAskedLayout.visibility = View.VISIBLE
                binding.questionAskedHistoryRv.visibility = View.GONE
                binding.textViewAskQuestion.isClickable = true
                binding.textViewAskQuestion.setOnClickListener {
                    openCameraPage()
                }
                updateProgressBarState(false)
            }
            infiniteScrollListener.isLastPageReached = true
        }
    }

    private fun setupRecyclerView() {
        questionAskedHistoryListAdapter = QuestionAskedHistoryListAdapter()
        binding.questionAskedHistoryRv.adapter = questionAskedHistoryListAdapter
        val linearLayoutManager = LinearLayoutManager(this)
        binding.questionAskedHistoryRv.layoutManager = linearLayoutManager
        infiniteScrollListener = object : TagsEndlessRecyclerOnScrollListener(linearLayoutManager) {
            override fun onLoadMore(current_page: Int) {
                if (nextUrl.isNotEmpty()) {
                    getQuestionAskedHistoryFromURLResult()
                } else {
                    infiniteScrollListener.isLastPageReached = true
                }
            }
        }
        binding.questionAskedHistoryRv.addOnScrollListener(infiniteScrollListener)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError() {
        toast(getString(R.string.api_error))
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }

    }

    private fun updateProgressBarState(state: Boolean) {
        infiniteScrollListener.setDataLoading(state)
        binding.progressBar.setVisibleState(state)
    }

}