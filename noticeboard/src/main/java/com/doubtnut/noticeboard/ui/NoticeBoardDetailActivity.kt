package com.doubtnut.noticeboard.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.ui.base.CoreBadRequestDialog
import com.doubtnut.core.ui.base.CoreBindingActivity
import com.doubtnut.core.utils.*
import com.doubtnut.core.utils.ViewUtils.screenWidth
import com.doubtnut.noticeboard.NoticeBoardConstants
import com.doubtnut.noticeboard.R
import com.doubtnut.noticeboard.data.entity.NoticeBoardData
import com.doubtnut.noticeboard.data.entity.NoticeBoardItem
import com.doubtnut.noticeboard.data.entity.UnreadNoticeCountUpdate
import com.doubtnut.noticeboard.data.remote.NoticeBoardRepository
import com.doubtnut.noticeboard.databinding.ActivityNoticeDetailBinding
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NoticeBoardDetailActivity :
    CoreBindingActivity<NoticeBoardDetailActivityVM, ActivityNoticeDetailBinding>() {

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    private var mDisposable: Disposable? = null
    private var mCurrentProgress: Long = 0
    private var mCurrentIndex: Int = 0

    private var notices: List<NoticeBoardItem> = emptyList()

    override fun provideViewBinding(): ActivityNoticeDetailBinding {
        return ActivityNoticeDetailBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): NoticeBoardDetailActivityVM {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        init()
        initListener()
        initObserver()

        viewModel.getNotices()
    }

    private fun init() {
        binding.viewPager.isUserInputEnabled = false
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        binding.viewPager.setOnTouchListener(onTouchListener)

        binding.ivShare.setOnClickListener {
            notices.getOrNull(binding.viewPager.currentItem)?.shareText
                ?.takeIf { it.isNotEmpty() }
                ?.let { shareText ->
                    ShareUtils.shareText(context = this, text = shareText)
                }
        }
    }

    var startTime: Long = System.currentTimeMillis()

    private val onTouchListener = View.OnTouchListener { v, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startTime = System.currentTimeMillis()
                pauseStatus()
                return@OnTouchListener true
            }
            MotionEvent.ACTION_UP -> {
                if (System.currentTimeMillis() - startTime > 500) {
                    resumeStatus()
                } else {
                    if (event.x < getScreenWidth2() / 2) {
                        moveToPreviousStatus()
                    } else {
                        moveToNextStatus()
                    }
                }
                startTime = 0
                return@OnTouchListener v.performClick()
            }
            MotionEvent.ACTION_BUTTON_RELEASE -> {
                resumeStatus()
                return@OnTouchListener v.performClick()
            }
        }
        false
    }

    private fun initObserver() {
        viewModel.noticesLiveData.observeK2(
            this,
            ::onNoticeFetched,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )
    }

    private fun onNoticeFetched(data: NoticeBoardData) {
        notices = data.items ?: return
        updateProgressBarState(false)
        binding.viewPager.adapter = NoticeBoardAdapter(
            this,
            notices
        )

        // Setting progress bars
        binding.llProgress.removeAllViews()
        binding.llProgress.weightSum = notices.size.toFloat()
        repeat(notices.size) {
            val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
            val params = LinearLayout.LayoutParams(
                (screenWidth() - 20.dpToPx()) / notices.size,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            progressBar.layoutParams = params
            // max progress i am using is 100 for
            // each progress bar you can modify it
            progressBar.max = 100
            progressBar.progressDrawable.setTint(
                Color.parseColor("#eb552b")
            )
            progressBar.indeterminateDrawable.setTint(
                Color.parseColor("#c4c4c4")
            )
            progressBar.progress = 0
            binding.llProgress.addView(progressBar)
            progressBar.updateMargins2(
                end = 6.dpToPx()
            )
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tvLabel.text = notices.getOrNull(position)?.name
                binding.ivShare.isVisible =
                    !notices.getOrNull(position)?.shareText.isNullOrEmpty()
                resumeStatus()

                viewModel.markRead(notices.getOrNull(position)?.id.orEmpty())
                NoticeBoardRepository.unreadNoticeIds.remove(notices.getOrNull(position)?.id.orEmpty())
                CoreApplication.INSTANCE.bus()?.send(UnreadNoticeCountUpdate())

                val eventName = when (notices.getOrNull(position)?.type) {
                    NoticeBoardProfileAdapter.TYPE_NO_INFO -> EventConstants.DN_NB_NO_STORY_VISIBLE
                    else -> EventConstants.DN_NB_STORY_VIEWED
                }
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        name = eventName,
                        params = hashMapOf(
                            EventConstants.TYPE to notices.getOrNull(position)?.type.orEmpty(),
                            NoticeBoardConstants.NB_ID to notices.getOrNull(position)?.id.orEmpty(),
                            EventConstants.CTA_TEXT to notices.getOrNull(position)?.ctaText.orEmpty(),
                            EventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                            EventConstants.STUDENT_CLASS to CoreUserUtils.getStudentClass(),
                            EventConstants.BOARD to CoreUserUtils.getUserBoard(),
                        ),
                        ignoreFacebook = true
                    )
                )
            }
        }
        )
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast2(e)
    }

    private fun unAuthorizeUserError() {
        val dialog = CoreBadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun updateProgressBarState(visible: Boolean) {
        binding.pbMain.setVisibleState2(visible)
    }

    private fun emitStatusProgress() {
        mDisposable?.dispose()
        mDisposable = null
        mDisposable = Observable.intervalRange(
            mCurrentProgress,
            100 - mCurrentProgress,
            0,
            100,
            TimeUnit.MILLISECONDS
        )
            .observeOn(Schedulers.computation())
            .subscribeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                moveToNextStatus()
            }
            .subscribe({
                updateProgress(it)
            }, {
                it.printStackTrace()
            })
    }

    private fun updateProgress(progress: Long) {
        if (NoticeBoardDetailFragment.isImageLoading) {
            return
        }
        runOnUiThread {
            mCurrentProgress = progress
            (binding.llProgress.getChildAt(mCurrentIndex) as? ProgressBar)?.progress =
                progress.toInt()
        }
    }

    private fun moveToPreviousStatus() {
        runOnUiThread {
            if (mCurrentIndex == 0) {
                if (mCurrentProgress < 25) {
                    pauseStatus()
                    val position = binding.viewPager.currentItem
                    if (position - 1 >= 0) {
                        binding.viewPager.currentItem = position - 1
                    } else {
                        finish()
                    }
                } else {
                    mCurrentIndex = 0
                    mCurrentProgress = 0
                    (binding.llProgress.getChildAt(mCurrentIndex) as? ProgressBar)?.progress = 0
                    binding.viewPager.currentItem = mCurrentIndex
                    resumeStatus()
                }
            } else {
                mCurrentProgress = 0
                (binding.llProgress.getChildAt(mCurrentIndex) as? ProgressBar)?.progress = 0
                mCurrentIndex--
                binding.viewPager.currentItem = mCurrentIndex
                if (mCurrentIndex != notices.lastIndex) {
                    resumeStatus()
                }
            }
        }
    }

    private fun moveToNextStatus() {
        runOnUiThread {
            mCurrentProgress = 0
            if (mCurrentIndex < notices.lastIndex) {
                try {
                    (binding.llProgress.getChildAt(mCurrentIndex) as? ProgressBar)?.progress = 100
                    mCurrentIndex++
                    binding.viewPager.currentItem = mCurrentIndex
                    if (mCurrentIndex <= notices.lastIndex) {
                        emitStatusProgress()
                    }
                } catch (e: Exception) {
                }
            } else {
                pauseStatus()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startViewing()
    }

    override fun onPause() {
        super.onPause()
        pauseStatus()
    }

    override fun onDestroy() {
        pauseStatus()
        super.onDestroy()
    }

    fun pauseStatus() {
        mDisposable?.dispose()
        mDisposable = null
    }

    fun resumeStatus() {
        emitStatusProgress()
    }

    private fun startViewing() {
        emitStatusProgress()
    }

    companion object {
        fun getStartIntent(
            context: Context,
        ) = Intent(context, NoticeBoardDetailActivity::class.java)

        private const val TAG = "NoticeBoardDetailActivity"
    }

}

internal class NoticeBoardAdapter(
    activity: AppCompatActivity,
    val items: List<NoticeBoardItem>
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun createFragment(position: Int): Fragment {
        return NoticeBoardDetailFragment.newInstance(items[position])
    }
}