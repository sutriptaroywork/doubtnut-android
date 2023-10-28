package com.doubtnutapp.quiztfs.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.quiztfs.QuizQnaInfoApi
import com.doubtnutapp.data.remote.models.quiztfs.QuizTfsData
import com.doubtnutapp.data.remote.models.quiztfs.QuizTfsWaitData
import com.doubtnutapp.databinding.ActivityQuizTfsBinding
import com.doubtnutapp.liveclass.ui.LiveClassChatActivity
import com.doubtnutapp.liveclass.viewmodel.LiveClassChatViewModel
import com.doubtnutapp.quiztfs.viewmodel.QuizTfsViewModel
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tourguide.tourguide.util.locationOnScreen
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class QuizTfsActivity :
    BaseBindingActivity<QuizTfsViewModel, ActivityQuizTfsBinding>(), QuizTfsListener {

    companion object {
        private const val TAG = "QuizTfsActivity"
        private const val SELECTED_CLASS = "selected_class"
        private const val SUBJECT = "subject"
        private const val LANGUAGE = "language"
        fun getStartIntent(
            context: Context,
            selectedClass: String,
            subject: String,
            language: String
        ) = Intent(context, QuizTfsActivity::class.java).apply {
            putExtra(SELECTED_CLASS, selectedClass)
            putExtra(SUBJECT, subject)
            putExtra(LANGUAGE, language)
        }
    }

    override fun provideViewBinding(): ActivityQuizTfsBinding =
        ActivityQuizTfsBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): QuizTfsViewModel =
        viewModelProvider(viewModelFactory)

    private val selectedClass: String
        get() = intent?.getStringExtra(SELECTED_CLASS)!!

    private val subject: String
        get() = intent?.getStringExtra(SUBJECT)!!

    private val language: String
        get() = intent?.getStringExtra(LANGUAGE)!!

    private var isFirst = true

    private val outRect = Rect()
    private val location = IntArray(2)
    private var mIsScrolling = false

    private var initalX = 0F
    private var initalY = 0F
    private var sessionId: String? = null

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun setupView(savedInstanceState: Bundle?) {
        binding.buttonBack.setOnClickListener {
            onBackPressed()
        }
        binding.tvToolbarTitle.setOnClickListener {
            confirmExit()
        }

        binding.chatView.doOnPreDraw {
            initalX = it.translationX
            initalY = it.translationY
        }
        initTouchListener()
        getQuizQuestion()

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.QUIZ_TFS_PAGE_VIEW,
                hashMapOf(
                    SELECTED_CLASS to selectedClass,
                    LANGUAGE to language,
                    SUBJECT to subject,
                ), ignoreSnowplow = true
            )
        )

    }

    override fun onBackPressed() {
        confirmExit()
    }

    private fun confirmExit() {
        val builder = AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Are you sure you want to end this quiz?")
            .setPositiveButton("Yes, End Quiz") { _, _ ->
                val intent = DailyPracticeActivity.getStartIntent(this, TAG,"")
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No") { _, _ -> }

        val dialog = builder.create()

        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isAllCaps = false
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).isAllCaps = false
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initTouchListener() {
        binding.chatView.setOnTouchListener(
            MoveViewTouchListener(
                binding.chatView,
                binding.parentView
            )
        )
    }

    private fun getQuizQuestion() {
        viewModel.getQuizQuestion(isFirst, selectedClass, language, subject)
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

    private fun onQuizFetched(data: QuizQnaInfoApi) {
        binding.tvPageTitle.text = data.pageTitle.orEmpty()
        if (!data.sessionId.isNullOrBlank()) {
            binding.chatView.isVisible = true
            sessionId = data.sessionId
        }
        isFirst = false
        if (data.timeToWait != null && data.waitData != null) {
            showQuizTfsWaitFragment(data.waitData, data.timeToWait)
            lifecycleScope.launch {
                delay(TimeUnit.SECONDS.toMillis(data.timeToWait))
                getQuizQuestion()
            }
        } else if (data.data != null) {
            showQuiz(data.data, data.timeToRespond, data.timeToNext - data.timeToRespond)
            lifecycleScope.launch {
                delay(TimeUnit.SECONDS.toMillis(data.timeToNext))
                getQuizQuestion()
            }
        }
    }

    private fun showQuizTfsWaitFragment(data: QuizTfsWaitData, timeToWait: Long) {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer,
                QuizTfsWaitFragment.newInstance(data, timeToWait)
            )
            .commit()
    }

    private fun showQuiz(data: QuizTfsData, timeToNext: Long, waitForNext: Long) {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer,
                QuizTfsFragment.newInstance(data, timeToNext, waitForNext)
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

    inner class MoveViewTouchListener(var view: View, var boundView: View) : View.OnTouchListener {
        private val mGestureDetector: GestureDetector
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (mGestureDetector.onTouchEvent(event)) {
                return true
            }
            if (event.action == MotionEvent.ACTION_UP) {
                if (mIsScrolling) {
                    mIsScrolling = false
                    if (!isViewInBounds(
                            boundView,
                            event.rawX.toInt(), event.rawY.toInt()
                        )
                    ) {
                        view.translationX = initalX
                        view.translationY = initalY
                    }
                }
            }
            return false
        }

        private val mGestureListener: GestureDetector.OnGestureListener =
            object : GestureDetector.SimpleOnGestureListener() {
                private var mMotionDownX = 0f
                private var mMotionDownY = 0f
                override fun onDown(e: MotionEvent): Boolean {
                    mMotionDownX = e.rawX - view.translationX
                    mMotionDownY = e.rawY - view.translationY
                    return true
                }

                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    view.translationX = initalX
                    view.translationY = initalY
                    showChat()
                    return true
                }

                override fun onScroll(
                    e1: MotionEvent,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    mIsScrolling = true
                    view.translationX = e2.rawX - mMotionDownX
                    view.translationY = e2.rawY - mMotionDownY
                    return true
                }
            }

        init {
            mGestureDetector = GestureDetector(view.context, mGestureListener)
        }
    }

    private fun isViewInBounds(view: View, x: Int, y: Int): Boolean {
        view.getDrawingRect(outRect)
        view.getLocationOnScreen(location)
        outRect.offset(location[0], location[1])
        return outRect.contains(x, y)
    }

    override fun showChat() {
        val locationOnScreen = Point()
        binding.chatView.locationOnScreen.let { point ->
            locationOnScreen.x = point.x
            locationOnScreen.y = point.y + 42.dpToPx()
        }
        val sessionID = sessionId ?: return
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.QUIZ_TFS_CHAT_OPEN,
                hashMapOf(
                    SELECTED_CLASS to selectedClass,
                    LANGUAGE to language,
                    SUBJECT to subject,
                ), ignoreSnowplow = true
            )
        )
        val intent =
            LiveClassChatActivity.getStartIntent(
                this,
                sessionID,
                LiveClassChatViewModel.PATH
            )
        startActivity(intent)
        updateRedDotVisibility(false)
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        if (fragment is QuizTfsFragment) {
            fragment.setListener(this)
        }
    }

    override fun updateRedDotVisibility(state: Boolean) {
        binding.textViewDot.isVisible = state
    }

}