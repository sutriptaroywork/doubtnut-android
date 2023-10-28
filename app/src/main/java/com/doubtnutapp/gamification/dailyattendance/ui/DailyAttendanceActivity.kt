package com.doubtnutapp.gamification.dailyattendance.ui

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.ViewUtils
import com.doubtnut.core.utils.ViewUtils.screenWidth
import com.doubtnut.core.utils.toast
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityDailyAttendanceBinding
import com.doubtnutapp.gamification.dailyattendance.model.DailyAttendanceDataModel
import com.doubtnutapp.gamification.dailyattendance.ui.adapter.CurrentStreakListAdapter
import com.doubtnutapp.gamification.dailyattendance.ui.viewmodel.DailyAttendanceViewModel
import com.doubtnutapp.show
import com.doubtnutapp.statusbarColor
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.widgets.SpaceItemDecoration
import dagger.android.AndroidInjection
import javax.inject.Inject


class DailyAttendanceActivity : AppCompatActivity(), ActionPerformer {


    override fun performAction(action: Any) {
        dailyAttendanceViewModel.handleAction(action)
    }

    companion object {
        private const val INTENT_EXTRA_USER_ID = "user_id"

        fun startActivity(context: Context, userId: String): Intent {
            return Intent(context, DailyAttendanceActivity::class.java).apply {
                putExtra(INTENT_EXTRA_USER_ID, userId)

            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var dailyAttendanceViewModel: DailyAttendanceViewModel

    private lateinit var currentStreakListAdapter: CurrentStreakListAdapter

    private lateinit var binding : ActivityDailyAttendanceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityDailyAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        statusbarColor(this, R.color.grey_statusbar_color)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        init()
        setClickListeners()
        setUpObservers()
    }

    private fun setClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setUpObservers() {
        dailyAttendanceViewModel.dailyAttendanceLiveData.observeK(
                this,
                ::onSuccess,
                ::onApiError,
                ::unAuthorizeUserError,
                ::ioExceptionHandler,
                ::updateProgressBarState
        )
    }

    private fun init() {
        dailyAttendanceViewModel = ViewModelProviders.of(this, viewModelFactory).get(DailyAttendanceViewModel::class.java)
        dailyAttendanceViewModel.getDailyAttendanceData(intent.getStringExtra(INTENT_EXTRA_USER_ID).orEmpty())
        setUpCurrentStreakRecyclerView()
    }

    private fun onSuccess(dailyAttendanceDataModel: DailyAttendanceDataModel) {
        currentStreakListAdapter.updateList(dailyAttendanceDataModel.dailyStreak)
        currentStreakListAdapter.notifyDataSetChanged()
        setCurrentStreakTitle(dailyAttendanceDataModel.title)
        setLongestStreakText(dailyAttendanceDataModel.longestStreak)

        Glide.with(this)
                .load(dailyAttendanceDataModel.longestStreakImage)
                .into(object : CustomViewTarget<TextView, Drawable>(binding.longestStreakDays) {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        binding.longestStreakDays.background = resource
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {}

                    override fun onResourceCleared(placeholder: Drawable?) {}
                })

    }

    private fun setLongestStreakText(longestStreak: Int) {
        val longestStreakText: String
        if (longestStreak == 1) longestStreakText = "$longestStreak\n" + getString(R.string.item_topic_video_day_text_title)
        else longestStreakText = "$longestStreak\n" + getString(R.string.activity_daily_attendance_days)

        val startDigitIndex = longestStreakText.indexOfFirst {
            it.isDigit()
        }
        val lastDigitIndex = longestStreakText.indexOfLast {
            it.isDigit()
        }
        val spannableString = SpannableStringBuilder(longestStreakText)

        spannableString.setSpan(
                RelativeSizeSpan(2f),
                startDigitIndex, lastDigitIndex + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannableString.setSpan(
                StyleSpan(Typeface.BOLD),
                startDigitIndex, lastDigitIndex + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.longestStreakDays.setText(spannableString, TextView.BufferType.SPANNABLE)
    }

    private fun setCurrentStreakTitle(title: String) {
        if (!title.isBlank()) {
            binding.headerLayout.show()
            val startDigitIndex = title.indexOfFirst {
                it.isDigit()
            }
            val lastDigitIndex = title.indexOfLast {
                it.isDigit()
            }
            if (startDigitIndex == -1 || lastDigitIndex == -1) {
                binding.pointsMessageText.text = title
            } else {
                val spannableString = SpannableString(title)
                binding.pointsMessageText.setText(spannableString, TextView.BufferType.SPANNABLE)
                val spannableText = binding.pointsMessageText.text as Spannable

                spannableText.setSpan(
                        RelativeSizeSpan(2f),
                        startDigitIndex, lastDigitIndex + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                spannableText.setSpan(
                        StyleSpan(Typeface.BOLD),
                        startDigitIndex, lastDigitIndex + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    private fun setUpCurrentStreakRecyclerView() {
        currentStreakListAdapter = CurrentStreakListAdapter(getDailyStreakItemRequireWidth(), emptyList(), this)
        binding.currentStreakList.adapter = currentStreakListAdapter
        binding.currentStreakList.addItemDecoration(SpaceItemDecoration(ViewUtils.dpToPx(5f, this).toInt()))
    }

    private fun getDailyStreakItemRequireWidth(): Int {
        val screenWidth = screenWidth()
        val dailyStreakCardWidth = screenWidth - (binding.attendenceCard.layoutParams as ConstraintLayout.LayoutParams).marginStart * 2
        return dailyStreakCardWidth / 5
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun updateProgressBarState(state: Boolean) {

    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }
}
