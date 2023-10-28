
package com.doubtnutapp.ui.test

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.TestDetails
import com.doubtnutapp.databinding.ItemTestListBinding
import com.instacart.library.truetime.TrueTimeRx
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TestListAdapter(val context: QuizActivity) : RecyclerView.Adapter<TestListAdapter.ViewHolder>() {

    var items: MutableList<TestDetails> = mutableListOf()
    private  var countDownTimerQuiz: CountDownTimer? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestListAdapter.ViewHolder {
        return TestListAdapter.ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_test_list, parent, false), context, countDownTimerQuiz)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: TestListAdapter.ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun updateData(items: MutableList<TestDetails>) {
        this.items = items
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: ItemTestListBinding, var context: QuizActivity, var countDownTimerQuiz: CountDownTimer?) : RecyclerView.ViewHolder(binding.root) {

        private val readFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

        fun bind(testList: TestDetails) {
            if (countDownTimerQuiz !=null){
                countDownTimerQuiz?.cancel()
            }
            binding.testDetailsItem = testList
            binding.executePendingBindings()

            val now = when {
                TrueTimeRx.isInitialized() -> TrueTimeRx.now()
                else -> Calendar.getInstance().time
            }

            val flag = getTrueTimeDecision(testList.publishTime, testList.unpublishTime, now)
            when (flag) {
                Constants.TEST_OVER -> {
                    binding.tvTestStatus.text = context.getString(R.string.string_quiz_past)
                    binding.viewTestStatus.background = context.resources.getDrawable(R.drawable.circle_quiz_inactive)
                    binding.tvTestStatusLable.text = context.getString(R.string.string_test_over_dialog_title)
                }
                Constants.TEST_UPCOMING -> {
                    binding.tvTestStatus.text = context.getString(R.string.string_quiz_upcoming)
                    binding.viewTestStatus.background = context.resources.getDrawable(R.drawable.circle_quiz_upcoming)
                    binding.tvTestStatusLable.text = context.getString(R.string.string_next_test_starts_in)
                    setUpTestTimer((getTestStartBeforeTimeDifferenceLong(testList.publishTime)))
                }
                Constants.TEST_ACTIVE -> {
                    binding.tvTestStatus.text = context.getString(R.string.string_quiz_active)
                    binding.viewTestStatus.background = context.resources.getDrawable(R.drawable.circle_quiz_active)
                    binding.tvTestStatusLable.text = context.getString(R.string.string_test_ends)
                    setUpTestTimer(getTestStartAfterTimeDifferenceLong(
                            testList.unpublishTime ?: ""))
                }
            }

        }

        private fun setUpTestTimer(time: Long) {
             countDownTimerQuiz = object : CountDownTimer(time, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.tvTestTimer.text = context.getString(R.string.string_quiz_question_timer,
                            TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                            (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished))),
                            (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))))
                }

                override fun onFinish() {
                    binding.tvTestTimer.text = context.getString(R.string.string_quiz_time_over)
                }
            }
            countDownTimerQuiz?.start()
        }

        fun getTestStartBeforeTimeDifferenceLong(publishTime: String?): Long {
            val now = if (TrueTimeRx.isInitialized()) {
                TrueTimeRx.now()
            } else {
                Calendar.getInstance().getTime()
            }
            val trueTime = readFormat.parse(readFormat.format(now.time))
            val startTestTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(publishTime)
            return startTestTime.time - trueTime.time
        }

        fun getTestStartAfterTimeDifferenceLong(unpublishTime: String?): Long {
            val now = if (TrueTimeRx.isInitialized()) {
                TrueTimeRx.now()
            } else {
                Calendar.getInstance().getTime()
            }
            return readFormat.parse(unpublishTime).time - readFormat.parse(readFormat.format(now.time)).time
        }

        private fun getTrueTimeDecision(publishTime: String?, unpublishTime: String?, now: Date): String {
            val trueTime = readFormat.parse(readFormat.format(now.time))
            val startTestTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(publishTime)
            val endTestTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(unpublishTime)

            val flag = if (trueTime.after(endTestTime)) {
                Constants.TEST_OVER
            } else if (trueTime.before(startTestTime)) {
                Constants.TEST_UPCOMING
            } else {
                Constants.TEST_ACTIVE
            }
            return flag
        }
    }


}