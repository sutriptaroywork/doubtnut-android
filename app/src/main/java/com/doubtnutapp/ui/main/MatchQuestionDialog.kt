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
import com.doubtnutapp.workmanager.MatchQuesNotificationManager
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


class MatchQuestionDialog :
    BaseBindingBottomSheetDialogFragment<DummyViewModel, FragmentMatchPageDialogBinding>(),
    View.OnClickListener {

    companion object {

        const val TAG = "MatchQuestionDialog"
        const val QUESTION_ID = "QUESTION_ID"
        const val QUESTION_IMAGE = "QUESTION_IMAGE"
        const val NOTIFICATION_ID = "NOTIFICATION_ID"

        fun newInstance(
            questionId: String,
            questionImage: String?,
            notificationId: Int
        ): MatchQuestionDialog =
            MatchQuestionDialog().apply {
                val bundle = Bundle()
                bundle.putString(QUESTION_ID, questionId)
                bundle.putString(QUESTION_IMAGE, questionImage)
                bundle.putInt(NOTIFICATION_ID, notificationId)
                arguments = bundle
            }
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var questionId: String? = null
    private var questionImage: String? = null
    private var notificationId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        arguments?.let {
            questionId = it.getString(QUESTION_ID)
            questionImage = it.getString(QUESTION_IMAGE)
            notificationId = it.getInt(NOTIFICATION_ID)
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
        if (activity == null || questionId == null) return

        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_IN_APP_MATCH_DIALOG_CLICKED))

        Intent(context, MatchQuestionActivity::class.java).also {
            it.putExtra(Constants.QUESTION_ID, questionId)
            it.putExtra(MatchQuestionActivity.INTENT_EXTRA_FROM_IN_APP, true)
            it.putExtra(MatchQuestionActivity.INTENT_EXTRA_ASKED_QUE_URI, questionImage)
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
        MatchQuesNotificationManager.dismissNotification(notificationManager, notificationId)
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_IN_APP_MATCH_DIALOG_VISIBLE, ignoreSnowplow = true))
    }
}