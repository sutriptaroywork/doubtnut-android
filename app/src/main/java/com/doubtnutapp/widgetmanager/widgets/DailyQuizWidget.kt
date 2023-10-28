package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.CountDownTimer
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher

import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.remote.models.TestDetails
import com.doubtnutapp.databinding.WidgetDailyQuizBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.domain.common.interactor.CheckQuizAttempted
import com.doubtnutapp.domain.mockTestLibrary.entities.BottomWidgetEntity
import com.doubtnutapp.ui.test.TestQuestionActivity
import com.doubtnutapp.utils.TestUtils
import com.google.gson.annotations.SerializedName
import com.instacart.library.truetime.TrueTimeRx
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DailyQuizWidget(
        context: Context)
    : BaseBindingWidget<DailyQuizWidget.DailyQuizWidgetHolder,
        DailyQuizWidget.DailyQuizWidgetModel, WidgetDailyQuizBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var checkQuizAttempted: CheckQuizAttempted

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    override fun getViewBinding(): WidgetDailyQuizBinding {
        return WidgetDailyQuizBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = DailyQuizWidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: DailyQuizWidgetHolder, model: DailyQuizWidgetModel): DailyQuizWidgetHolder {
        super.bindWidget(holder, model)

        val data = model.data
        val binding = holder.binding

        binding.ivQuizTimer.loadImage(data.imageUrl)
        setQuizData(binding, data)

        data.buttonBgColor?.let {
            binding.tvQuizTimerCapsule.backgroundTintList = ColorStateList.valueOf(Color.parseColor(it))
            binding.btnQuizTimer.background.setColorFilter(Color.parseColor(it), PorterDuff.Mode.MULTIPLY)
        }

        data.buttonTextColor?.let {
            binding.btnQuizTimer.setTextColor(Color.parseColor(it))
        }

        binding.btnQuizTimer.text = data.buttonText

        binding.tvQuizTimerTitle.text = data.title
        binding.tvQuizDes.text = data.description

        binding.quizContainer.setOnClickListener {
            handleQuizDetail(data)
        }

        return holder
    }

    private fun handleQuizDetail(quizData: DailyQuizWidgetData) {
        checkQuizAttempted.execute(CheckQuizAttempted.Param(quizData.testId))
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({ attemptedQuiz ->

                    val attemptedCount = 1
                    val testSubcriptionId = attemptedQuiz.subscriptionId

                    val updatedData = quizData.copy(
                            attemptCount = attemptedCount,
                            testSubscriptionId = testSubcriptionId.toString()
                    )

                    openQuizDetail(updatedData)
                }, {
                    openQuizDetail(quizData)
                })
    }

    private fun openQuizDetail(quizData: DailyQuizWidgetData) {
        val testSubscriptionId = if (quizData.testSubscriptionId.isNullOrEmpty()) 0 else quizData.testSubscriptionId.toInt()
        val testDetail = getTestDetail(quizData)

        val intent = Intent(context, TestQuestionActivity::class.java).also {
            it.putExtra(Constants.TEST_DETAILS_OBJECT, testDetail)
            it.putExtra(Constants.TEST_TRUE_TIME_FLAG, getTimeFlag(testDetail))
            it.putExtra(Constants.TEST_SUBSCRIPTION_ID, testSubscriptionId)
        }

        context.startActivity(intent)
    }

    private fun setQuizData(binding: WidgetDailyQuizBinding, data: DailyQuizWidgetData) {
        if (!data.unpublishTime.isNullOrBlank()) {
            if (isQuizOver(data.unpublishTime)) {
                binding.tvQuizTimerCapsule.text = binding.root.context.resources.getString(R.string.string_test_over_dialog_title)
            } else {
                val now = when {
                    TrueTimeRx.isInitialized() -> TrueTimeRx.now()
                    else -> Calendar.getInstance().time
                }
                val flag = getTrueTimeDecision(data.unpublishTime, data.unpublishTime, now)
                when (flag) {
                    Constants.TEST_UPCOMING -> {
                        setUpTestTimer(getTestStartBeforeTimeDifferenceLong(data.publishTime), binding)
                    }
                    Constants.TEST_ACTIVE -> {
                        setUpTestTimer(getTestStartAfterTimeDifferenceLong(
                                data.unpublishTime ?: ""), binding)
                    }
                }
            }

        }
    }

    private fun setUpTestTimer(time: Long, binding: WidgetDailyQuizBinding) {
        val countDownTimer = object : CountDownTimer(time, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvQuizTimerCapsule.text = binding.root.context.getString(R.string.string_quiz_question_timer,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished))),
                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))))
            }

            override fun onFinish() {
                binding.tvQuizTimerCapsule.text = binding.root.context.getString(R.string.string_quiz_time_over)
            }
        }
        countDownTimer.start()
    }

    private fun getTestStartBeforeTimeDifferenceLong(publishTime: String?): Long {
        val now = if (TrueTimeRx.isInitialized()) {
            TrueTimeRx.now()
        } else {
            Calendar.getInstance().time
        }
        val trueTime = readFormat.parse(readFormat.format(now.time))
        val startTestTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(publishTime)
        return startTestTime.time - trueTime.time
    }

    private fun getTestStartAfterTimeDifferenceLong(unpublishTime: String?): Long {
        val now = if (TrueTimeRx.isInitialized()) {
            TrueTimeRx.now()
        } else {
            Calendar.getInstance().time
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

    private fun getTimeFlag(testDetail: TestDetails?) = when {
        testDetail?.attemptCount != 0 -> Constants.USER_CANNOT_ATTEMPT_TEST
        else -> TestUtils.getTrueTimeDecision(
                testDetail.publishTime,
                testDetail.unpublishTime,
                now = when {
                    TrueTimeRx.isInitialized() -> TrueTimeRx.now()
                    else -> Calendar.getInstance().time
                })
    }

    private fun getTestDetail(quizData: DailyQuizWidgetData): TestDetails = with(quizData) {
        TestDetails(
                testId,
                classCode,
                subjectCode,
                chapterCode,
                title,
                description,
                durationInMin,
                solutionPdf,
                totalQuestions,
                publishTime,
                unpublishTime,
                isActive,
                null,
                type,
                ruleId,
                attemptCount = attemptCount,
                canAttempt = canAttempt,
                bottomWidgetEntity = bottomWidgetEntity
        )
    }


    class DailyQuizWidgetHolder(binding: WidgetDailyQuizBinding, widget: BaseWidget<*, *>) :
            WidgetBindingVH<WidgetDailyQuizBinding>(binding, widget)

    class DailyQuizWidgetModel : WidgetEntityModel<DailyQuizWidgetData, WidgetAction>()

    @Keep
    data class DailyQuizWidgetData(
            @SerializedName("_id") val id: String,
            @SerializedName("button_bg_color") val buttonBgColor: String?,
            @SerializedName("button_text") val buttonText: String?,
            @SerializedName("button_text_color") val buttonTextColor: String?,
            @SerializedName("title") val title: String?,
            @SerializedName("image_url") val imageUrl: String?,
            @SerializedName("type") val type: String?,
            @SerializedName("description") val description: String?,
            @SerializedName("chapter_code") val chapterCode: String?,
            @SerializedName("duration_in_min") val durationInMin: Int?,
            @SerializedName("solution_pdf") val solutionPdf: String?,
            @SerializedName("no_of_questions") val totalQuestions: Int?,
            @SerializedName("publish_time") val publishTime: String?,
            @SerializedName("unpublish_time") val unpublishTime: String?,
            @SerializedName("class_code") val classCode: String?,
            @SerializedName("subject_code") val subjectCode: String?,
            @SerializedName("is_active") val isActive: Int?,
            @SerializedName("rule_id") val ruleId: Int?,
            @SerializedName("can_attempt") val canAttempt: Boolean?,
            @SerializedName("test_subscription_id") val testSubscriptionId: String?,
            @SerializedName("attempt_count") val attemptCount: Int?,
            @SerializedName("test_id") val testId: Int,
            @SerializedName("bottom_widget") val bottomWidgetEntity: BottomWidgetEntity?) : WidgetData()


}