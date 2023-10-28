package com.doubtnutapp.liveclass.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.JavascriptInterface
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.LiveClassQuizSubmitResponse
import com.doubtnutapp.data.remote.models.LiveQuizQuestionDataOptions
import com.doubtnutapp.databinding.FragmentLiveClassQnaBinding
import com.doubtnutapp.liveclass.viewmodel.LiveClassQnaViewModel
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 01/05/20.
 */
class LiveClassQnaFragment : BaseBindingBottomSheetDialogFragment<LiveClassQnaViewModel, FragmentLiveClassQnaBinding>() {

    companion object {
        const val TAG = "LiveClassQnaFragment"
        const val LIVE_QUIZ_DATA = "live_quiz_data"
        const val LIVE_CLASS_ID = "live_class_id"
        const val DETAIL_ID = "detail_id"
        const val RESOURCE_DETAIL_ID = "resource_detail_id"
        const val HAS_NEXT = "has_next"
        fun newInstance(liveQuizQuestionDataOptions: LiveQuizQuestionDataOptions,
                        liveClassId: String, detailId: Long,
                        resourceDetailId: Long,
                        hasNext: Boolean): LiveClassQnaFragment {
            return LiveClassQnaFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(LIVE_QUIZ_DATA, liveQuizQuestionDataOptions)
                    putString(LIVE_CLASS_ID, liveClassId)
                    putLong(DETAIL_ID, detailId)
                    putLong(RESOURCE_DETAIL_ID, resourceDetailId)
                    putBoolean(HAS_NEXT, hasNext)
                }
            }
        }
    }

    private var mBehavior: BottomSheetBehavior<*>? = null
    var testQuestionData: LiveQuizQuestionDataOptions? = null

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var position: Int = 0
    private var checkBoxOptionList: ArrayList<String> = ArrayList()
    private var radioOptionList: ArrayList<String> = ArrayList()
    var testSubscriptionId: Int = 0
    var checkedOptionList: ArrayList<String> = ArrayList()

    var isAnswerSubmitted = false
    var isUserOptedOption = false

    var submitStatus = "QUIZ_SHOW"

    private var liveClassQnaListener: LiveClassQnaListener? = null
    private var submitResponse: Pair<LiveClassQuizSubmitResponse, List<String>>? = null
    private var shouldShowResult = false

    @Inject
    lateinit var disposable: CompositeDisposable

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLiveClassQnaBinding =
        FragmentLiveClassQnaBinding.inflate(layoutInflater)

    override fun provideViewModel(): LiveClassQnaViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT)

        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setCancelable(true)

        mBehavior = BottomSheetBehavior.from(binding.root.parent as View)
        if (mBehavior != null) {
            (mBehavior as BottomSheetBehavior<*>).state = BottomSheetBehavior.STATE_EXPANDED
            (mBehavior as BottomSheetBehavior<*>).setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(p0: View, p1: Int) {
                    (mBehavior as BottomSheetBehavior<*>).state = BottomSheetBehavior.STATE_EXPANDED
                }

                override fun onSlide(p0: View, p1: Float) {
                    (mBehavior as BottomSheetBehavior<*>).state = BottomSheetBehavior.STATE_EXPANDED
                }
            })
        }

        initMathView()
        setQuestionToMathView()
        setQuestionSingleOrMcq()
        getFlagOptionFromSavedState(savedInstanceState)
        setOptionSelected()

        val viewEvent = AnalyticsEvent(
            EventConstants.LIVE_CLASS_QUIZ_VIEW,
            hashMapOf(
                EventConstants.TEST_ID to (testQuestionData?.testId
                    ?: 0).toString(),
                LIVE_CLASS_ID to arguments?.getString(LIVE_CLASS_ID).orEmpty(),
                DETAIL_ID to (arguments?.getLong(DETAIL_ID) ?: 0),
                RESOURCE_DETAIL_ID to (arguments?.getLong(RESOURCE_DETAIL_ID)
                    ?: 0).toString()
            ), ignoreSnowplow = true
        )
        analyticsPublisher.publishEvent(viewEvent)

        binding.buttonSubmit.setOnClickListener {
            submitAnswer(true)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList("checkBoxOptionList", checkBoxOptionList)
        outState.putStringArrayList("radioOptionList", radioOptionList)
    }

    fun setListener(liveClassQnaListener: LiveClassQnaListener) {
        this.liveClassQnaListener = liveClassQnaListener
    }

    private fun handleTimer(count: Long, nextCount: Long) {
        disposable.add(
                getTimerObservable(count)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Long>() {
                            override fun onComplete() {
                                disposable.clear()
                                shouldShowResult = true
                                binding.buttonSubmit.isClickable = false
                                binding.buttonSubmit.visibility = View.INVISIBLE
                                if (nextCount == 0L) {
                                    liveClassQnaListener?.onSubmitOrTimeComplete()
                                    dismiss()
                                } else {
                                    binding.textViewStatus.text = ""
                                    if (isAnswerSubmitted && submitResponse != null) {
                                        showAnswer()
                                    } else {
                                        submitAnswer(false)
                                    }
                                    handleTimer(nextCount, 0)
                                }
                            }

                            override fun onNext(t: Long) {
                                val timerText = (count - t - 1).toString() + " Sec"
                                binding.textViewTimer.text = timerText
                            }

                            override fun onError(e: Throwable) {
                                disposable.clear()
                                dismiss()
                            }
                        })
        )
    }

    private fun getTimerObservable(count: Long) =
            Observable.interval(0, 1, TimeUnit.SECONDS).take(count)

    private fun submitAnswer(isUserOpted: Boolean) {
        isUserOptedOption = isUserOpted
        submitStatus = "REQUEST_INITIATED"
        if (testQuestionData == null) return
        if (checkedOptionList.isEmpty() && isUserOpted) {
            toast("Select Option")
        } else {
            viewModel.submitLiveClassQuiz(
                    testQuestionData!!.testId,
                    arguments?.getString(LIVE_CLASS_ID).orEmpty(),
                    checkedOptionList,
                    arguments?.getLong(DETAIL_ID) ?: 0,
                    arguments?.getLong(RESOURCE_DETAIL_ID) ?: 0
            )
            val event = AnalyticsEvent(
                    EventConstants.LIVE_CLASS_QUIZ_SUBMITTED,
                    hashMapOf(
                            EventConstants.TEST_ID to (testQuestionData?.testId
                                    ?: 0).toString(),
                            LIVE_CLASS_ID to arguments?.getString(LIVE_CLASS_ID).orEmpty(),
                            DETAIL_ID to (arguments?.getLong(DETAIL_ID) ?: 0),
                            RESOURCE_DETAIL_ID to (arguments?.getLong(RESOURCE_DETAIL_ID)
                                    ?: 0).toString()
                    ), ignoreSnowplow = true
            )
            analyticsPublisher.publishEvent(event)
            analyticsPublisher.publishMoEngageEvent(event)
        }
    }

    override fun setupObservers() {
        viewModel.submitResponseLiveData.observeK(this,
                this::onSubmitQuizResponse,
                this::onApiError,
                this::unAuthorizeUserError,
                this::ioExceptionHandler,
                this::updateProgressBarState
        )
    }

    private fun onSubmitQuizResponse(response: Pair<LiveClassQuizSubmitResponse, List<String>>) {
        submitStatus = "REQUEST_SUCCESS"
        isAnswerSubmitted = true
        if (isUserOptedOption) {
            toast("Submitted Successfully")
        } else {
            toast("Correct Answer is " + response.first.answer)
        }
        submitResponse = response
        if (shouldShowResult) {
            showAnswer()
        } else {
            binding.textViewStatus.text = "Thank You, Answer hum quiz khatam hone pe batayenge"
        }
    }

    private fun showAnswer() {
        if (arguments?.getBoolean(HAS_NEXT, false) == true) {
            binding.textViewStatus.text = "Waiting for next quiz"
        } else {
            binding.textViewStatus.text = ""
        }

        val response = submitResponse ?: return
        val correctAnswerList = response.first.answer.split("::")
        val colorList = mutableListOf<String>()
        val options = testQuestionData?.options.orEmpty()
        options.forEach {
            val isInCorrectAnswerList = correctAnswerList.contains(it.key)
            val isInCheckedAnswerList = response.second.contains(it.key)
            if (isInCorrectAnswerList && isInCheckedAnswerList) {
                //correct answer
                colorList.add("#00c779")
            } else if (isInCorrectAnswerList) {
                //wrong answer
                colorList.add("#00c779")
            } else if (isInCheckedAnswerList) {
                //wrong answer
                colorList.add("#FF0000")
            } else {
                colorList.add("#f6f6f6")
            }
        }
        binding.liveQuizMathView.onQuizSubmit(colorList)
    }

    private fun updateProgressBarState(state: Boolean) {
        submitStatus = "REQUEST_ERROR"
        binding.progressBar.setVisibleState(state)
    }

    private fun unAuthorizeUserError() {
        submitStatus = "REQUEST_ERROR"
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(parentFragmentManager, "BadRequestDialog")
    }


    private fun onApiError(e: Throwable) {
        submitStatus = "REQUEST_ERROR"
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        submitStatus = "REQUEST_ERROR"
        context?.let {
            if (NetworkUtils.isConnected(it).not()) {
                toast(it.getString(R.string.string_noInternetConnection))
            } else {
                toast(it.getString(R.string.somethingWentWrong))
            }
        }
    }


    private fun getFlagOptionFromSavedState(savedInstanceState: Bundle?) {
        checkBoxOptionList = savedInstanceState?.getStringArrayList("checkBoxOptionList")
                ?: ArrayList()
        radioOptionList = savedInstanceState?.getStringArrayList("radioOptionList")
                ?: ArrayList()
    }

    private fun initMathView() {
        binding.liveQuizMathView.javaInterfaceForMockTest = this
    }

    private fun setQuestionToMathView() {
        binding.liveQuizMathView.questionNumber = "Select the correct option"
        val liveQuizQuestionDataOptions =
                arguments?.getParcelable<LiveQuizQuestionDataOptions>(LIVE_QUIZ_DATA) ?: return
        testQuestionData = liveQuizQuestionDataOptions
        binding.liveQuizMathView.question = testQuestionData?.text.orEmpty()
        binding.liveQuizMathView.questionData = testQuestionData
        handleTimer(liveQuizQuestionDataOptions.expiry, liveQuizQuestionDataOptions.responseExpiry
                ?: 0)
    }

    private fun setQuestionSingleOrMcq() {
        if (testQuestionData?.type == "SINGLE") {
            binding.liveQuizMathView.questionType = "Single Choice Question"
        } else if (testQuestionData?.type == "MULTI") {
            binding.liveQuizMathView.questionType = "Multiple Choice Question"
        }
    }

    private fun setOptionSelected() {
        checkBoxOptionList.let {
            binding.liveQuizMathView.setMatrixCheckedOptions(it)
        }
        radioOptionList.let {
            binding.liveQuizMathView.setRadioCheckedOptions(it)
        }
    }

    @JavascriptInterface
    fun option(i: Int) {
        Handler(Looper.getMainLooper()).post {
            checkedOptionList = ArrayList()
            checkedOptionList.add(testQuestionData!!.options[i].key)

            radioOptionList = ArrayList()
            radioOptionList.add(i.toString())
        }
    }

    @JavascriptInterface
    fun optioncheckbox(i: Int) {
        Handler(Looper.getMainLooper()).post {
            if (!checkedOptionList.contains(testQuestionData!!.options[i].key)) {
                checkedOptionList.add(testQuestionData!!.options[i].key)
                checkBoxOptionList.add(i.toString())
            } else {
                checkedOptionList.remove(testQuestionData!!.options[i].key)
                checkBoxOptionList.remove(i.toString())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val event = AnalyticsEvent(
                EventConstants.LIVE_CLASS_QUIZ_SUBMIT_STATUS,
                hashMapOf(
                        EventConstants.TEST_ID to (testQuestionData?.testId
                                ?: 0).toString(),
                        LIVE_CLASS_ID to arguments?.getString(LIVE_CLASS_ID).orEmpty(),
                        DETAIL_ID to (arguments?.getLong(DETAIL_ID) ?: 0),
                        RESOURCE_DETAIL_ID to (arguments?.getLong(RESOURCE_DETAIL_ID)
                                ?: 0).toString(),
                        EventConstants.STATUS to submitStatus
                ), ignoreSnowplow = true
        )
        analyticsPublisher.publishEvent(event)
        disposable.clear()
    }

}
