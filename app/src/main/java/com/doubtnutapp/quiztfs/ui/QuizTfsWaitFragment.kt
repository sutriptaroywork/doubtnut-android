package com.doubtnutapp.quiztfs.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnutapp.data.remote.models.quiztfs.QuizTfsWaitData
import com.doubtnutapp.databinding.FragmentQuizTfsWaitBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnut.core.utils.viewModelProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class QuizTfsWaitFragment : BaseBindingFragment<DummyViewModel, FragmentQuizTfsWaitBinding>() {

    companion object {
        const val TAG = "QuizTfsFragment"
        const val QUIZ_DATA = "quiz_data"
        private const val TIME_TO_WAIT = "time_to_wait"
        fun newInstance(
            quizTfsData: QuizTfsWaitData,
            timeToWait: Long
        ): QuizTfsWaitFragment {
            return QuizTfsWaitFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(QUIZ_DATA, quizTfsData)
                    putLong(TIME_TO_WAIT, timeToWait)
                }
            }
        }
    }

    private val quizTfsData: QuizTfsWaitData
        get() = arguments?.getParcelable(QUIZ_DATA)!!

    private val timeToWait: Long
        get() = arguments?.getLong(TIME_TO_WAIT)!!

    @Inject
    lateinit var disposable: CompositeDisposable

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentQuizTfsWaitBinding =
        FragmentQuizTfsWaitBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        binding.tvWaitTitle.text = quizTfsData.title.orEmpty()
        binding.tvStatus.text = quizTfsData.subTitle.orEmpty()
        binding.tvStartingIn.text = quizTfsData.startingIn.orEmpty()
        handleTimer(timeToWait)
    }

    private fun handleTimer(count: Long) {
        disposable.add(
            getTimerObservable(count)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<Long>() {
                    override fun onComplete() {
                        disposable.clear()
                        binding.tvTimeRemaining.text = ""
                    }

                    override fun onNext(t: Long) {
                        val timerLeft = (count - t).toString()
                        binding.tvTimeRemaining.text = timerLeft
                    }

                    override fun onError(e: Throwable) {
                        disposable.clear()
                    }
                })
        )
    }

    private fun getTimerObservable(count: Long) =
        Observable.interval(0, 1, TimeUnit.SECONDS).take(count)

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

}
