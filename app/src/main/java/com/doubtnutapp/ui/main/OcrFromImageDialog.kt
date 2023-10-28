package com.doubtnutapp.ui.main

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.FragmentMatchPageDialogBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.matchquestion.ui.activity.MatchQuestionActivity
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.workmanager.OcrFromImageNotificationManager
import javax.inject.Inject


class OcrFromImageDialog :
    BaseBindingBottomSheetDialogFragment<DummyViewModel, FragmentMatchPageDialogBinding>(),
    View.OnClickListener {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    companion object {

        const val TAG = "OcrFromImageDialog"
        const val QUESTION_IMAGE = "QUESTION_IMAGE"
        const val QUESTION_TEXT = "QUESTION_TEXT"
        const val NOTIFICATION_ID = "NOTIFICATION_ID"

        fun newInstance(
            questionText: String,
            questionImage: String?,
            notificationId: Long
        ): OcrFromImageDialog =
            OcrFromImageDialog().apply {
                val bundle = Bundle()
                bundle.putString(QUESTION_TEXT, questionText)
                bundle.putString(QUESTION_IMAGE, questionImage)
                bundle.putLong(NOTIFICATION_ID, notificationId)
                arguments = bundle
            }
    }

    private var questionText: String? = null
    private var questionImage: String? = null
    private var notificationId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            questionText = it.getString(QUESTION_TEXT)
            questionImage = it.getString(QUESTION_IMAGE)
            notificationId = it.getLong(NOTIFICATION_ID)
        }

    }

    private fun updateUi() {
        questionImage?.let { mBinding?.ivQuestionImage?.loadImage(it) }
    }

    private fun setClickListeners() {

        mBinding?.btnViewSolution?.setOnClickListener(this)
        mBinding?.ivQuestionImage?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnViewSolution, R.id.ivQuestionImage -> openMatchPage()
        }
    }

    private fun openMatchPage() {

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.EVENT_IN_APP_MATCH_OCR_DIALOG_CLICKED,
                hashMapOf(),
                ignoreSnowplow = true
            )
        )

        Intent(context, MatchQuestionActivity::class.java).also {
            it.putExtra(Constants.ASK_IMAGE_OCR, questionText)
            it.putExtra(MatchQuestionActivity.INTENT_EXTRA_FROM_IN_APP, true)
            it.putExtra(MatchQuestionActivity.INTENT_EXTRA_ASKED_QUE_URI, questionImage)
            it.putExtra(
                MatchQuestionActivity.INTENT_EXTRA_OCR_NOTIFICATION_ID,
                notificationId.toString()
            )
        }.apply {
            startActivity(this)
        }

        dismiss()
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMatchPageDialogBinding {
        return FragmentMatchPageDialogBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        updateUi()
        setClickListeners()

        // Dismiss notification of this question
        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        OcrFromImageNotificationManager.dismissNotification(notificationManager, notificationId)

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.EVENT_IN_APP_MATCH_OCR_DIALOG_VISIBLE,
                hashMapOf(),
                ignoreSnowplow = true
            )
        )
    }
}