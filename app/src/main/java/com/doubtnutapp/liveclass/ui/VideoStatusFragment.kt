package com.doubtnutapp.liveclass.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.databinding.FragmentVideoStatusBinding
import com.doubtnutapp.liveclass.viewmodel.VideoStatusViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.EventObserver
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class VideoStatusFragment :
    BaseBindingFragment<VideoStatusViewModel, FragmentVideoStatusBinding>() {

    companion object {
        private const val TAG = "VideoStatusFragment"
        const val ID = "id"
        fun newInstance(id: String)
                : VideoStatusFragment = VideoStatusFragment()
                .apply {
                    arguments = Bundle().apply {
                        putString(ID, id)
                    }
                }
    }

    @Inject
    lateinit var timerDisposable: CompositeDisposable

    var qid = ""

    private var isImageSet = false

    private var isVideoStarted = false

    private var videoStatusListener: VideoStatusListener? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        qid = arguments?.getString(ID).orEmpty()
        setUpObserver()
        viewModel.fetchInitialData(qid)
    }

    fun setListener(videoStatusListener: VideoStatusListener) {
        this.videoStatusListener = videoStatusListener
    }

    private fun setUpObserver() {
        viewModel.videoStatus.observe(viewLifecycleOwner, EventObserver {
            mBinding?.textViewTitleTwo?.text = it.text.orEmpty()
            mBinding?.textViewTitleOne?.text = ""

            if (!isImageSet) {
                isImageSet = true
                mBinding?.layoutBackground?.loadBackgroundImage(it.thumbnail.orEmpty(),
                        R.color.grey_2a4863)
            }

            when (it.state) {
                "0" -> {
                    val timeLeft = it.timeRemaining?.toLongOrNull()
                    if (timeLeft != null && timeLeft > 0) {
                        scheduleTimer(timeLeft)
                    } else if (!viewModel.isLiveClassStatsPollingStarted) {
                        viewModel.getLiveClassStats(qid)
                    }
                }
                else -> {
                    reset()
                }
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, EventObserver {
            mBinding?.progressBar?.setVisibleState(it)
        })

        viewModel.messageLiveData.observe(viewLifecycleOwner, EventObserver {
            toast(it)
        })
    }

    @Synchronized
    private fun reset() {
        if (!isVideoStarted) {
            isVideoStarted = true
            videoStatusListener?.reRequest()
        }
    }

    private fun scheduleTimer(delayInSecond: Long) {
        timerDisposable.add(
            Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(delayInSecond, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<Long>() {
                    override fun onComplete() {
                        timerDisposable.clear()
                        viewModel.getLiveClassStats(qid)
                    }

                    override fun onNext(t: Long) {
                        val timeLeft = delayInSecond - t
                        if (timeLeft < 0) return
                        val timeLeftText =
                            String.format(
                                "%02d:%02d:%02d",
                                TimeUnit.SECONDS.toHours(timeLeft),
                                TimeUnit.SECONDS.toMinutes(timeLeft) % TimeUnit.HOURS.toMinutes(1),
                                TimeUnit.SECONDS.toSeconds(timeLeft) % TimeUnit.MINUTES.toSeconds(1)
                            )
                        mBinding?.textViewTitleOne?.text = timeLeftText
                    }

                    override fun onError(e: Throwable) {
                        timerDisposable.clear()
                    }
                })
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        timerDisposable.dispose()
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentVideoStatusBinding {
        return FragmentVideoStatusBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): VideoStatusViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

    }

}