package com.doubtnutapp.quiztfs.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.quiztfs.QuizTfsData
import com.doubtnutapp.databinding.ActivityQuizTfsSolutionBinding
import com.doubtnutapp.quiztfs.viewmodel.QuizTfsSolutionViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils

class QuizTfsSolutionActivity :
    BaseBindingActivity<QuizTfsSolutionViewModel, ActivityQuizTfsSolutionBinding>() {

    companion object {
        private const val TAG = "QuizTfsSolutionActivity"
        private const val ID = "id"
        private const val DATE = "date"
        fun getStartIntent(
            context: Context,
            id: String,
            date: String
        ) = Intent(context, QuizTfsSolutionActivity::class.java).apply {
            putExtra(ID, id)
            putExtra(DATE, date)
        }
    }

    override fun provideViewBinding(): ActivityQuizTfsSolutionBinding =
        ActivityQuizTfsSolutionBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): QuizTfsSolutionViewModel =
        viewModelProvider(viewModelFactory)

    private val id: String
        get() = intent?.getStringExtra(ID)!!

    private val date: String
        get() = intent?.getStringExtra(DATE)!!

    override fun setupView(savedInstanceState: Bundle?) {
        binding.buttonBack.setOnClickListener {
            onBackPressed()
        }
        getQuizQuestion()
    }

    private fun getQuizQuestion() {
        viewModel.getQuizSolution(id, date)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.quizQnaInfoApiLiveData.observeK(
            this,
            this::onQuizFetched,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )
    }

    private fun onQuizFetched(data: QuizTfsData) {
        showQuizSolution(data)
    }

    private fun showQuizSolution(data: QuizTfsData) {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer,
                QuizTfsSolutionFragment.newInstance(data)
            )
            .commit()
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this)) {
            toast(getString(R.string.somethingWentWrong))
        } else {
            toast(getString(R.string.string_noInternetConnection))
        }
    }

    private fun updateProgress(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }

}

