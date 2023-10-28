package com.doubtnutapp.doubtpecharcha.ui

import android.content.Context
import android.graphics.Color
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.doubtnut.referral.data.entity.DoubtP2pPageMetaData
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.base.*
import com.doubtnutapp.data.remote.models.ChatPageMetaData
import com.doubtnutapp.databinding.LayoutAnswerAcceptBinding
import com.doubtnutapp.databinding.WidgetStudyGroupParentBinding
import com.doubtnutapp.doubtpecharcha.model.P2PAnswerAcceptModel
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.widgetmanager.widgets.StudyGroupParentWidget
import kotlinx.android.synthetic.main.widget_study_group_parent.view.*
import java.util.concurrent.TimeUnit

class LayoutAnswerAcceptView : ConstraintLayout {

    var layoutAnswerAcceptButtonClickListener:LayoutAnswerAcceptButtonClickListener?=null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

    }

    fun initialize(): LayoutAnswerAcceptBinding {
        return LayoutAnswerAcceptBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun render(data: StudyGroupParentWidget.Data,
               studentId: String,
               layoutAnswerAcceptButtonClickListener: LayoutAnswerAcceptButtonClickListener) {
        this.layoutAnswerAcceptButtonClickListener = layoutAnswerAcceptButtonClickListener
        val hostStudentId = data.hostStudentID
        data.answerAcceptModel?.let {
            if (hostStudentId == studentId && it.answerAcceptState == StudyGroupParentWidget.STATE_ACCEPTANCE_PENDING) {
                this.show()
                hostAnswerPending(data)
            } else if (hostStudentId == studentId && it.answerAcceptState == StudyGroupParentWidget.STATE_ACCEPTANCE_ACCEPTED) {
                this.show()
                hostAnswerAccepted(data)
            } else if (hostStudentId == studentId && it.answerAcceptState == StudyGroupParentWidget.STATE_ACCEPTANCE_REJECTED) {
                this.show()
                hostAnswerRejected(data)
            } else if (hostStudentId == studentId && it.answerAcceptState == StudyGroupParentWidget.STATE_ACCEPTANCE_REJECTED_AND_SOLVED) {
                this.show()
                hostAnswerRejected(data)
            } else if (data.studentId == studentId && it.answerAcceptState == StudyGroupParentWidget.STATE_ACCEPTANCE_PENDING) {
                this.show()
                solverAnswerPending(data)
            } else if (data.studentId == studentId && it.answerAcceptState == StudyGroupParentWidget.STATE_ACCEPTANCE_ACCEPTED) {
                this.show()
                solverAnswerAccepted(data)
            } else if (data.studentId == studentId && it.answerAcceptState == StudyGroupParentWidget.STATE_ACCEPTANCE_REJECTED) {
                this.show()
                solverAnswerRejected(data)

            } else if (data.studentId == studentId && it.answerAcceptState == StudyGroupParentWidget.STATE_ACCEPTANCE_REJECTED_AND_SOLVED) {
                this.show()
                solverAnswerRejectedAndSolved(data)
            } else if (data.studentId == studentId && it.answerAcceptState == StudyGroupParentWidget.STATE_UNMARKED) {
                this.show()
                solverAnswerUnmarked(data)
            } else {
                this.hide()
            }

        } ?: run {
            this.hide()

        }

    }

    private fun hostAnswerPending(
        data: StudyGroupParentWidget.Data,
    ) {
        this.removeAllViews()
        val binding = initialize()
        val titleText = DoubtP2pPageMetaData.hostResponseText
            ?: context.getString(R.string.did_you_get_right_answer)
        binding.tvTitleActionLayout.text = titleText
        binding.tvSubtitleActionLayout.text = ""
        binding.tvSubtitleActionLayout.setTextColor(ContextCompat.getColor(context, R.color.black))
        binding.imageViewTick.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.tick))
        binding.ivTickOuter.setBackgroundColor(
            ContextCompat.getColor(
                context,
                R.color.green
            )
        )
        binding.tvYes.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
        binding.tvNo.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
        binding.tvYes.show()
        binding.tvNo.show()
        binding.tvYes.setOnClickListener {
            layoutAnswerAcceptButtonClickListener?.
            onYesClicked(StudyGroupParentWidget.STATE_ACCEPTANCE_PENDING,true)
            binding.tvYes.setTextColor(Color.BLACK)
            binding.tvNo.setTextColor(Color.GRAY)
            binding.tvYes.isEnabled = false
            binding.tvNo.isEnabled = false

        }
        binding.tvNo.setOnClickListener {
            layoutAnswerAcceptButtonClickListener?.
            onNoClicked(StudyGroupParentWidget.STATE_ACCEPTANCE_PENDING,true)
            binding.tvYes.setTextColor(Color.GRAY)
            binding.tvNo.setTextColor(Color.BLACK)
            binding.tvYes.isEnabled = false
            binding.tvNo.isEnabled = false
        }
        setTime(data, binding)
    }

    private fun hostAnswerAccepted(
        data: StudyGroupParentWidget.Data) {
        this.removeAllViews()
        val binding = initialize()
        val titleText = DoubtP2pPageMetaData.hostResponseText
            ?: context.getString(R.string.did_you_get_right_answer)
        binding.tvTitleActionLayout.text = titleText
        binding.tvSubtitleActionLayout.text = ""
        binding.tvSubtitleActionLayout.setTextColor(ContextCompat.getColor(context, R.color.black))
        binding.imageViewTick.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.tick))
        binding.ivTickOuter.setBackgroundColor(
            ContextCompat.getColor(
                context,
                R.color.green
            )
        )
        binding.tvYes.show()
        binding.tvYes.setTextColor(Color.BLACK)
        binding.tvNo.setTextColor(Color.GRAY)

        binding.tvYes.isEnabled = false
        binding.tvNo.isEnabled = false
        binding.tvNo.show()
        setTime(data, binding)
    }

    private fun hostAnswerRejected(
        data: StudyGroupParentWidget.Data
    ) {
        this.removeAllViews()
        val binding = initialize()
        val titleText = DoubtP2pPageMetaData.hostResponseText
            ?: context.getString(R.string.did_you_get_right_answer)
        binding.tvTitleActionLayout.text = titleText
        binding.tvSubtitleActionLayout.text = ""
        binding.tvSubtitleActionLayout.setTextColor(ContextCompat.getColor(context, R.color.black))
        binding.imageViewTick.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.tick))
        binding.ivTickOuter.setBackgroundColor(
            ContextCompat.getColor(
                context,
                R.color.green
            )
        )
        binding.tvYes.show()

        binding.tvYes.setTextColor(Color.GRAY)
        binding.tvNo.setTextColor(Color.BLACK)
        binding.tvYes.isEnabled = false
        binding.tvNo.isEnabled = false
        binding.tvNo.show()
        setTime(data, binding)
    }

    private fun solverAnswerUnmarked(
        data: StudyGroupParentWidget.Data
    ) {
        this.removeAllViews()
        val binding = initialize()
        val titleText = DoubtP2pPageMetaData.answerMarkAsSolvedTitle
            ?: context.getString(R.string.is_this_your_answer)
        val subtitle = DoubtP2pPageMetaData.answerMarkAsSolvedSubtitle ?: ""
        binding.tvTitleActionLayout.text = titleText
        binding.tvSubtitleActionLayout.text = subtitle
        binding.tvSubtitleActionLayout.setTextColor(ContextCompat.getColor(context, R.color.black))
        binding.imageViewTick.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.tick))
        binding.ivTickOuter.setBackgroundColor(
            ContextCompat.getColor(
                context,
                R.color.grey_e5e5e5
            )
        )
        binding.tvYes.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
        binding.tvNo.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
        binding.tvYes.show()
        binding.tvNo.show()
        binding.tvYes.setOnClickListener {
            layoutAnswerAcceptButtonClickListener?.onYesClicked(StudyGroupParentWidget.STATE_UNMARKED,false)
            binding.tvYes.setTextColor(Color.BLACK)
            binding.tvNo.setTextColor(Color.GRAY)
            binding.tvYes.isEnabled = false
            binding.tvNo.isEnabled = false

        }
        binding.tvNo.setOnClickListener {
            layoutAnswerAcceptButtonClickListener?.onNoClicked(StudyGroupParentWidget.STATE_UNMARKED,false)
            binding.tvYes.setTextColor(Color.GRAY)
            binding.tvNo.setTextColor(Color.BLACK)
            binding.tvYes.isEnabled = false
            binding.tvNo.isEnabled = false
        }
        setTime(data, binding)
    }

    private fun solverAnswerPending(
        data: StudyGroupParentWidget.Data
    ) {
        this.removeAllViews()
        val binding = initialize()
        val titleText =
            DoubtP2pPageMetaData.answerPendingDataTitle ?: context.getString(R.string.doubt_solved)
        val subtitleText =
            DoubtP2pPageMetaData.answerPendingDataSubtitle
                ?: context.getString(R.string.answer_not_accepted_yet)
        binding.tvTitleActionLayout.text = titleText
        binding.tvSubtitleActionLayout.text = subtitleText
        binding.tvSubtitleActionLayout.setTextColor(ContextCompat.getColor(context, R.color.black))
        binding.imageViewTick.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.tick))
        binding.ivTickOuter.setBackgroundColor(
            ContextCompat.getColor(
                context,
                R.color.grey_e5e5e5
            )
        )
        binding.tvYes.visibility = View.GONE
        binding.tvNo.visibility = View.GONE
        setTime(data, binding)
    }

    private fun solverAnswerAccepted(
        data: StudyGroupParentWidget.Data
    ) {
        this.removeAllViews()
        val binding = initialize()
        val titleText =
            DoubtP2pPageMetaData.answerAcceptedDataTitle
                ?: context.getString(R.string.your_answer_has_been_accepted)
        val subtitleText = DoubtP2pPageMetaData.answerAcceptedDataSubtitle
            ?: context.getString(R.string.brilliant_solve_more_and_help)
        binding.tvSubtitleActionLayout.setTextColor(ContextCompat.getColor(context, R.color.black))
        binding.tvTitleActionLayout.text = titleText
        binding.tvSubtitleActionLayout.text = subtitleText
        binding.imageViewTick.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.tick))
        binding.ivTickOuter.setBackgroundColor(
            ContextCompat.getColor(
                context,
                R.color.green
            )
        )
        binding.tvYes.hide()
        binding.tvNo.hide()
        setTime(data, binding)
    }

    private fun solverAnswerRejected(
        data: StudyGroupParentWidget.Data
    ) {
        this.removeAllViews()
        val binding = initialize()
        val titleText =
            DoubtP2pPageMetaData.answerRejectedDataTitle
                ?: context.getString(R.string.your_answer_has_been_rejected)
        val subtitleText = DoubtP2pPageMetaData.answerRejectedDataSubtitle
            ?: "Solve again"
        binding.tvTitleActionLayout.text = titleText
        binding.tvSubtitleActionLayout.text = subtitleText
        binding.imageViewTick.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_multiply
            )
        )
        binding.ivTickOuter.setBackgroundColor(
            ContextCompat.getColor(
                context,
                R.color.red
            )
        )


        binding.tvSubtitleActionLayout.setTextColor(Color.RED)
        binding.tvSubtitleActionLayout.setOnClickListener {
            layoutAnswerAcceptButtonClickListener?.
            onSubtitleClicked(StudyGroupParentWidget.STATE_ACCEPTANCE_REJECTED,false)
            binding.tvSubtitleActionLayout.text = "Solving.."
        }
        binding.tvYes.hide()
        binding.tvNo.hide()
        setTime(data, binding)
    }

    private fun solverAnswerRejectedAndSolved(
        data: StudyGroupParentWidget.Data
    ) {
        this.removeAllViews()
        val binding = initialize()
        val titleText =
            DoubtP2pPageMetaData.answerRejectedDataTitle
                ?: context.getString(R.string.your_answer_has_been_rejected)
        binding.tvTitleActionLayout.text = titleText
        binding.tvSubtitleActionLayout.text = ""
        binding.imageViewTick.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_multiply
            )
        )
        binding.ivTickOuter.setBackgroundColor(
            ContextCompat.getColor(
                context,
                R.color.red
            )
        )


        binding.tvSubtitleActionLayout.setTextColor(Color.RED)
        binding.tvSubtitleActionLayout.setOnClickListener {
        }
        binding.tvYes.hide()
        binding.tvNo.hide()
        setTime(data, binding)
    }

    fun setTime(
        data: StudyGroupParentWidget.Data,
        binding: LayoutAnswerAcceptBinding
    ) {
        val diff = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - data.createdAt)
        binding.tvTimeLayout.text = if (diff <= 0) {
            context.getString(R.string.seconds_ago)
        } else {
            DateUtils.getRelativeDateTimeString(
                context, data.createdAt, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS,
                DateUtils.FORMAT_SHOW_TIME
            )
        }
    }

    interface LayoutAnswerAcceptButtonClickListener {
        fun onYesClicked(state:String,isHost:Boolean)
        fun onNoClicked(state: String,isHost: Boolean)
        fun onSubtitleClicked(state: String,isHost: Boolean)
    }


}