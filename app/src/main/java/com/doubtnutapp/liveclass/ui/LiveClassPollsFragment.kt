package com.doubtnutapp.liveclass.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.*
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.SubmitLiveClassPoll
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.LiveClassPollOptionsData
import com.doubtnutapp.data.remote.models.LiveClassPollSubmitResponse
import com.doubtnutapp.databinding.FragmentLiveClassPollsBinding
import com.doubtnutapp.liveclass.adapter.LiveClassPollData
import com.doubtnutapp.liveclass.adapter.LiveClassPollsAdapter
import com.doubtnutapp.liveclass.adapter.LiveClassPollsList
import com.doubtnutapp.liveclass.viewmodel.LiveClassQnaViewModel
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LiveClassPollsFragment :
    BaseBindingBottomSheetDialogFragment<LiveClassQnaViewModel, FragmentLiveClassPollsBinding>(),
    ActionPerformer {

    private lateinit var adapter: LiveClassPollsAdapter

    private var currentPollId: String = ""
    private var currentPublishId: String = ""
    private var currentPosition: Int = 0
    private var isResultShown = false

    @Inject
    lateinit var disposable: CompositeDisposable

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var quizData: LiveClassPollsList? = null
    private var qid: String = ""
    private var course: String = ""
    private var subject: String = ""
    private var isFullScreen: Boolean = false

    companion object {
        const val TAG = "LiveClassPollsFragment"

        private const val LIVE_QUIZ_DATA = "live_quiz_data"
        private const val QUESTION_ID = "question_id"
        private const val COURSE = "course"
        private const val SUBJECT = "subject"
        private const val IS_FULL_SCREEN = "is_full_screen"
        private const val IS_VOD = "is_vod"
        fun newInstance(
            data: LiveClassPollsList, qid: String, courseName: String, subject: String,
            isFullScreen: Boolean, isVod: Boolean
        ): LiveClassPollsFragment {
            return LiveClassPollsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(LIVE_QUIZ_DATA, data)
                    putString(QUESTION_ID, qid)
                    putString(COURSE, courseName)
                    putString(SUBJECT, subject)
                    putBoolean(IS_FULL_SCREEN, isFullScreen)
                    putBoolean(IS_VOD, isVod)
                }
            }
        }
    }

    var originalList: MutableList<LiveClassPollData?>? = null

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLiveClassPollsBinding {
        return FragmentLiveClassPollsBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): LiveClassQnaViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        isFullScreen = arguments?.getBoolean(IS_FULL_SCREEN) ?: false
        if (!isFullScreen) {
            mBinding?.rvWidgets?.setPadding(0, 0, 0, ViewUtils.dpToPx(20f, context).toInt())
        }
        initUI()
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.submitPollResponseLiveData.observeK(
            this,
            this::onSubmitPollResponse,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgressBarState
        )
        viewModel.getPollResultLiveData.observeK(
            this,
            this::onPollResultResponse,
            this::onPollApiError,
            this::pollUnAuthorizeUserError,
            this::pollIoExceptionHandler,
            this::updateProgressBarState
        )
    }

    private fun onSubmitPollResponse(response: LiveClassPollSubmitResponse) {
        toast(response.message.orEmpty(), Toast.LENGTH_SHORT)
        if (arguments?.getBoolean(IS_VOD) == true) {
            val next = originalList?.getOrNull(0)
            if (next != null) {
                handleTimer(
                    0L,
                    next.responseExpiry?.toLong() ?: 5
                )
            }
        }
    }

    private fun onPollResultResponse(resultList: List<LiveClassPollOptionsData>?) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.LIVE_CLASS_POLL_RESULT,
                hashMapOf(
                    EventConstants.QUESTION_ID to qid,
                    EventConstants.COURSE to course,
                    EventConstants.SUBJECT to subject
                ), ignoreSnowplow = true
            )
        )
        isResultShown = true
        adapter.updatePollResult(0, resultList)
    }

    private fun updateProgressBarState(state: Boolean) {
        mBinding?.progressBar?.setVisibleState(state)
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun onPollApiError(e: Throwable) {
        isResultShown = true
        apiErrorToast(e)
    }

    private fun pollIoExceptionHandler() {
        isResultShown = true
        context?.let {
            if (NetworkUtils.isConnected(it).not()) {
                toast(it.getString(R.string.string_noInternetConnection))
            } else {
                toast(it.getString(R.string.somethingWentWrong))
            }
        }
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

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireFragmentManager(), "BadRequestDialog")
    }

    private fun pollUnAuthorizeUserError() {
        isResultShown = true
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireFragmentManager(), "BadRequestDialog")
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    private fun initUI() {
        adapter = LiveClassPollsAdapter(requireContext(), this)
        mBinding?.rvWidgets?.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL, false
        )
        mBinding?.rvWidgets?.adapter = adapter
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(mBinding?.rvWidgets)
        quizData = arguments?.getParcelable(LIVE_QUIZ_DATA)
        qid = arguments?.getString(QUESTION_ID) ?: ""
        course = arguments?.getString(course) ?: ""
        subject = arguments?.getString(SUBJECT) ?: ""
        originalList = quizData?.list

        if (originalList != null) {
            val next = originalList?.getOrNull(0)
            if (next != null) {
                handleTimer(
                    next.expiry?.toLong() ?: 15,
                    next.responseExpiry?.toLong() ?: 3
                )
                currentPollId = next.quizQuestionId.toString()
                currentPublishId = next.publishId.toString()
                mBinding?.layoutPolls?.applyBackgroundColor(next.bgColor)
                mBinding?.textViewTimer?.applyTextColor(next.expiryTextColor)
                mBinding?.textViewTimer?.applyTextSize(next.expiryTextSize)
                mBinding?.closeButton?.isVisible = next.showCloseBtn == true
                adapter.setQuestionList(listOf(next))
            }
        }

        mBinding?.closeButton?.setOnClickListener {
            dismiss()
        }
        dialog?.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun getTimerObservable(count: Long) =
        Observable.interval(0, 1, TimeUnit.SECONDS).take(count)

    private fun handleTimer(count: Long, nextCount: Long) {
        disposable.add(
            getTimerObservable(count)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<Long>() {
                    override fun onComplete() {
                        disposable.clear()
                        doOnTimerComplete(nextCount)
                    }

                    override fun onNext(t: Long) {
                        val timerText = (count - t).toString() + " Sec"
                        mBinding?.textViewTimer?.text = timerText
                    }

                    override fun onError(e: Throwable) {
                        disposable.clear()
                        dismiss()
                    }
                })
        )
    }

    fun doOnTimerComplete(nextCount: Long) {
        if (nextCount == 0L) {
            showNextIfAnyElseDismiss()
        } else {
            viewModel.getPollResult(currentPublishId, currentPollId)
            handleTimer(nextCount, 0)
        }
    }

    override fun performAction(action: Any) {
        if (action is SubmitLiveClassPoll && !isResultShown) {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.LIVE_CLASS_POLL_OPTION_CLICKED,
                    hashMapOf(
                        EventConstants.QUESTION_ID to qid,
                        EventConstants.COURSE to course,
                        EventConstants.SUBJECT to subject
                    ), ignoreSnowplow = true
                )
            )
            viewModel.submitLiveClassPolls(
                originalList?.getOrNull(currentPosition)?.quizQuestionId.toString(),
                originalList?.getOrNull(currentPosition)?.publishId.toString(),
                action.key,
                quizData?.detailId.toString()
            )
            val pollData = originalList?.getOrNull(currentPosition)
            val optionsList = originalList?.getOrNull(currentPosition)?.optionsList
            if (pollData != null && optionsList != null) {
                optionsList.forEach {
                    it.isSelected = it.key == action.key
                }
                pollData.optionsList = optionsList
                adapter.setQuestionList(listOf(pollData))
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun showNextIfAnyElseDismiss() {
        currentPosition++
        val next = originalList?.getOrNull(currentPosition)
        if (next != null) {
            isResultShown = false
            handleTimer(
                next.expiry?.toLong() ?: 15,
                next.responseExpiry?.toLong() ?: 3
            )
            currentPollId = next.quizQuestionId.toString()
            currentPublishId = next.publishId.toString()
            adapter.setQuestionList(listOf(next))
        } else {
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

}