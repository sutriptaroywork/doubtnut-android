package com.doubtnutapp.libraryhome.dailyquiz.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher

import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.FragmentDailyQuizLibraryBinding
import com.doubtnutapp.libraryhome.dailyquiz.model.QuizDetailsDataModel
import com.doubtnutapp.libraryhome.dailyquiz.ui.adapter.DailyQuizListAdapter
import com.doubtnutapp.libraryhome.dailyquiz.viewmodel.DailyQuizViewModel
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.TestUtils
import com.google.android.exoplayer2.util.Log
import com.instacart.library.truetime.TrueTimeRx
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import java.util.*
import javax.inject.Inject

class DailyQuizFragment : Fragment(R.layout.fragment_daily_quiz_library),
    ActionPerformer {

    companion object {
        const val TAG = "DailyQuizFragment"
        private const val REQUEST_CODE_ATTEMPT_QUIZ = 1010

        fun newInstance() = DailyQuizFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var screenNavigator: Navigator

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private lateinit var viewModel: DailyQuizViewModel
    private lateinit var adapter: DailyQuizListAdapter
    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener
    private var position: Int = 0

    private var startPage = 1

    private val binding by viewBinding(FragmentDailyQuizLibraryBinding::bind)

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = viewModelProvider(viewModelFactory)
        startShimmer()
        setUpObserver()
        setUpRecyclerView()
        getLibraryPlaylist(startPage)
    }

    private fun startShimmer() {
        binding.shimmerFrameLayout.startShimmer()
        binding.shimmerFrameLayout.show()
    }

    private fun stopShimmer() {
        binding.shimmerFrameLayout.stopShimmer()
        binding.shimmerFrameLayout.hide()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            if (::viewModel.isInitialized) {
                viewModel.publishLibraryTabSelectedEvent(TAG)
            }


        }
    }

    override fun performAction(action: Any) {
        viewModel.handleAction(action)
    }

    private fun setUpObserver() {
        viewModel.quizDetailsLiveData.observeK(
            this,
            this::onSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )

        viewModel.navigateQuizLiveData.observe(viewLifecycleOwner, {
            val (screen, position, subscriptionId) = it
            if (screen != null && adapter.items.isNotEmpty()) {
                val flag = getTimeFlag(position)
                this.position = position
                val quizDetails = viewModel.mapDetailsData(adapter.items[position])
                val arg = hashMapOf(
                    Constants.TEST_DETAILS_OBJECT to quizDetails,
                    Constants.TEST_TRUE_TIME_FLAG to flag,
                    Constants.TEST_SUBSCRIPTION_ID to subscriptionId,
                    Constants.FROM_LIBRARY to true
                )

                screenNavigator.startActivityForResultFromFragment(
                    this,
                    screen,
                    arg.toBundle(),
                    REQUEST_CODE_ATTEMPT_QUIZ
                )
            }
        })

    }

    private fun setUpRecyclerView() {
        binding.rvDailyQuiz.clearOnScrollListeners()
        adapter = DailyQuizListAdapter(this)
        binding.rvDailyQuiz.adapter = adapter
        infiniteScrollListener = getTagsEndlessRecyclerOnScrollListener()
        binding.rvDailyQuiz.addOnScrollListener(infiniteScrollListener)
    }

    private fun getLibraryPlaylist(pageNumber: Int) {
        viewModel.getQuizData(pageNumber)
    }

    private fun onSuccess(quizDetailsDataModel: List<QuizDetailsDataModel>) {

        adapter.updateData(quizDetailsDataModel)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireFragmentManager(), "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        context?.let {
            if (NetworkUtils.isConnected(it).not()) {
                toast(it.getString(R.string.string_noInternetConnection))
            } else {
                toast(it.getString(R.string.somethingWentWrong))
            }
        }
    }

    private fun updateProgress(state: Boolean) {
        infiniteScrollListener.setDataLoading(state)
        binding.pbPagination.setVisibleState(state)
        if (state.not()) {
            stopShimmer()
        }
    }

    private fun getTagsEndlessRecyclerOnScrollListener(): TagsEndlessRecyclerOnScrollListener {
        return object : TagsEndlessRecyclerOnScrollListener(binding.rvDailyQuiz.layoutManager) {
            override fun onLoadMore(current_page: Int) {
                Log.d("current_page", current_page.toString())
                binding.pbPagination.visibility = View.VISIBLE
                getLibraryPlaylist(++startPage)
            }
        }
    }

    private fun getTimeFlag(position: Int): String {
        return when {
            adapter.items[position].attemptCount != 0 -> Constants.USER_CANNOT_ATTEMPT_TEST
            else -> TestUtils.getTrueTimeDecision(
                adapter.items[position].publishTime,
                adapter.items[position].unpublishTime,
                now = when {
                    TrueTimeRx.isInitialized() -> TrueTimeRx.now()
                    else -> Calendar.getInstance().time
                }
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_ATTEMPT_QUIZ) {
            refreshQuizList()
        }
    }

    private fun refreshQuizList() {
        startPage = 1
        getLibraryPlaylist(startPage)
        setUpRecyclerView()
    }

    override fun onDestroyView() {
        adapter.countDownTimerQuiz?.cancel()
        adapter.countDownTimerQuiz = null

        super.onDestroyView()
    }

}