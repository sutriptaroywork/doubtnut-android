package com.doubtnutapp.libraryhome.dailyquiz.ui.adapter

import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.Constants
import com.doubtnutapp.R

import com.doubtnutapp.base.OpenTestQuestionActivity
import com.doubtnutapp.libraryhome.dailyquiz.model.QuizDetailsDataModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instacart.library.truetime.TrueTimeRx
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DailyQuizListAdapter(private val actionPerformer: ActionPerformer) :
    RecyclerView.Adapter<DailyQuizListAdapter.ViewHolder>() {

    var items: MutableList<QuizDetailsDataModel> = mutableListOf()
    var countDownTimerQuiz: CountDownTimer? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_daily_quiz_list, parent, false
            ), countDownTimerQuiz
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position, actionPerformer)
    }

    fun updateData(items: List<QuizDetailsDataModel>) {
        val changeStartIndex = this.items.size
        this.items.addAll(items)
        notifyItemRangeInserted(changeStartIndex, items.size)
    }

    fun updatePosition(position: Int) {

        this.items[position].canAttempt = false

    }

    class ViewHolder(
        var binding: com.doubtnutapp.databinding.ItemDailyQuizListBinding,
        var countDownTimerQuiz: CountDownTimer?
    ) : RecyclerView.ViewHolder(binding.root) {

        private val readFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

        fun bind(
            testList: QuizDetailsDataModel,
            position: Int,
            actionPerformer: ActionPerformer
        ) {
            val context = binding.root.context

            if (countDownTimerQuiz != null) {
                countDownTimerQuiz?.cancel()
            }
            binding.quizDetailsItem = testList
            binding.actionPerformer = actionPerformer
            binding.openTestQuestionActivityAction = OpenTestQuestionActivity(
                position,
                if (!testList.testSubscriptionId.isNullOrBlank()) {
                    testList.testSubscriptionId.toInt()
                } else 0
            )
            binding.executePendingBindings()

            val now = when {
                TrueTimeRx.isInitialized() -> TrueTimeRx.now()
                else -> Calendar.getInstance().time
            }

            when (getTrueTimeDecision(
                testList.testId,
                testList.publishTime,
                testList.unpublishTime,
                now
            )) {
                Constants.TEST_OVER -> {
                    binding.tvTestStatus.text = context.getString(R.string.string_quiz_past)
                    binding.viewTestStatus.background =
                        AppCompatResources.getDrawable(context, R.drawable.circle_quiz_inactive)
                    binding.tvTestStatusLable.text =
                        context.getString(R.string.string_test_over_dialog_title)
                }
                Constants.TEST_UPCOMING -> {
                    binding.tvTestStatus.text = context.getString(R.string.string_quiz_upcoming)
                    binding.viewTestStatus.background =
                        AppCompatResources.getDrawable(context, R.drawable.circle_quiz_upcoming)
                    binding.tvTestStatusLable.text =
                        context.getString(R.string.string_next_test_starts_in)
                    setUpTestTimer(
                        context,
                        getTestStartBeforeTimeDifferenceLong(testList.publishTime)
                    )
                }
                Constants.TEST_ACTIVE -> {
                    binding.tvTestStatus.text = context.getString(R.string.string_quiz_active)
                    binding.viewTestStatus.background =
                        AppCompatResources.getDrawable(context, R.drawable.circle_quiz_active)
                    binding.tvTestStatusLable.text = context.getString(R.string.string_test_ends)
                    setUpTestTimer(
                        context,
                        getTestStartAfterTimeDifferenceLong(
                            testList.unpublishTime ?: ""
                        )
                    )
                }
            }

        }

        private fun setUpTestTimer(context: Context, time: Long) {
            countDownTimerQuiz = object : CountDownTimer(time, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.tvTestTimer.text = context.getString(
                        R.string.string_quiz_question_timer,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                                TimeUnit.HOURS.toMinutes(
                                    TimeUnit.MILLISECONDS.toHours(
                                        millisUntilFinished
                                    )
                                )),
                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(
                                        millisUntilFinished
                                    )
                                ))
                    )
                }

                override fun onFinish() {
                    binding.tvTestTimer.text = context.getString(R.string.string_quiz_time_over)
                }
            }
            countDownTimerQuiz?.start()
        }

        fun getTestStartBeforeTimeDifferenceLong(publishTime: String?): Long {
            publishTime ?: Long.MIN_VALUE
            val now = if (TrueTimeRx.isInitialized()) {
                TrueTimeRx.now()
            } else {
                Calendar.getInstance().time
            }
            val trueTime = readFormat.parse(readFormat.format(now.time)) ?: return 0L
            val startTestTime =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    .parse(
                        publishTime ?: return 0L
                    ) ?: return 0L
            return startTestTime.time - trueTime.time
        }

        fun getTestStartAfterTimeDifferenceLong(unpublishTime: String): Long {
            val now = if (TrueTimeRx.isInitialized()) {
                TrueTimeRx.now()
            } else {
                Calendar.getInstance().time
            }
            return try {
                readFormat.parse(unpublishTime)!!.time - readFormat.parse(
                    readFormat.format(
                        now.time
                    )
                )!!.time
            } catch (e: Exception) {
                0L
            }
        }

        private fun getTrueTimeDecision(
            testId: Int,
            publishTime: String?,
            unpublishTime: String?,
            now: Date
        ): String {
            try {
                val trueTime =
                    readFormat.parse(readFormat.format(now.time)) ?: return Constants.TEST_OVER
                val startTestTime =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(
                        publishTime ?: return Constants.TEST_OVER
                    )
                val endTestTime =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(
                        unpublishTime ?: return Constants.TEST_OVER
                    )

                return when {
                    trueTime.after(endTestTime) -> {
                        Constants.TEST_OVER
                    }
                    trueTime.before(startTestTime) -> {
                        Constants.TEST_UPCOMING
                    }
                    else -> {
                        Constants.TEST_ACTIVE
                    }
                }
            } catch (e: ParseException) {
                FirebaseCrashlytics.getInstance().log("testId: $testId)")
                FirebaseCrashlytics.getInstance().log("publishTime: $publishTime)")
                FirebaseCrashlytics.getInstance().log("unpublishTime: $unpublishTime)")
                FirebaseCrashlytics.getInstance().recordException(e)
                return Constants.TEST_OVER
            }
        }
    }
}
