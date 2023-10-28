package com.doubtnutapp.widgets.base

import android.animation.Animator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.observeNonNull
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.Dismiss
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.DialogBaseWidgetBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.getScreenWidth
import com.doubtnutapp.hide
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject

class BaseWidgetDialogFragment :
    BaseBindingDialogFragment<BaseWidgetDialogFragmentVM, DialogBaseWidgetBinding>(),
    ActionPerformer {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var actionPerformer: ActionPerformer? = null
    var restartJob: Job? = null

    private val widgetType: String by lazy { requireArguments().getString(KEY_WIDGET_TYPE)!! }
    private val studentId: String by lazy { requireArguments().getString(KEY_STUDENT_ID).orEmpty() }
    private val assortmentId: String by lazy {
        requireArguments().getString(KEY_ASSORTMENT_ID).orEmpty()
    }
    private val testId: String by lazy {
        requireArguments().getString(KEY_TEST_ID).orEmpty()
    }
    private val tabNumber: String by lazy { requireArguments().getString(KEY_TAB_NUMBER).orEmpty() }

    private val showCloseBtn: Boolean by lazy {
        requireArguments().getBoolean(
            KEY_SHOW_CLOSE_BTN,
            true
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogBaseWidgetBinding {
        return DialogBaseWidgetBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): BaseWidgetDialogFragmentVM {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        init()
        initListeners()

        viewModel.getDialogData(
            widgetType = widgetType,
            studentId = studentId,
            assortmentId = assortmentId,
            testId = testId,
            tabNumber = tabNumber
        )
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.widgetsLiveData.observeNonNull(this) { outcome ->
            when (outcome) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = outcome.loading
                }
                is Outcome.Success -> {
                    val loadUi = {
                        binding.lottieAnimationView.hide()
                        binding.progressBar.isVisible = false

                        outcome.data.dialogWidthPadding?.let {
                            dialog?.window?.setLayout(
                                requireActivity().getScreenWidth() - it.dpToPx(),
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        }

                        restartJob?.cancel()
                        restartJob = lifecycleScope.launchWhenResumed {
                            if (outcome.data.startTimeInMillis != null && outcome.data.startTimeInMillis!! > 0L) {
                                delay(outcome.data.startTimeInMillis!!)
                                viewModel.getDialogData(
                                    widgetType = widgetType,
                                    studentId = studentId,
                                    assortmentId = assortmentId,
                                    testId = testId,
                                    tabNumber = tabNumber
                                )
                            }
                        }

                        binding.ivClose.isVisible = outcome.data.showCloseBtn ?: true

                        outcome.data.widgets?.filterNotNull()?.forEach {
                            if (it.extraParams == null) {
                                it.extraParams = HashMap()
                            }
                            if (studentId.isEmpty().not()) {
                                it.extraParams?.put(EventConstants.STUDENT_ID, studentId)
                            }
                            it.extraParams?.put(EventConstants.ASSORTMENT_ID, assortmentId)
                            it.extraParams?.put(EventConstants.TYPE, widgetType)
                            it.extraParams?.put(
                                EventConstants.SCHOLARSHIP_TEST_ID,
                                outcome.data.scholarshipTestId.orEmpty()
                            )
                            it.extraParams?.put(
                                EventConstants.TEST_ID,
                                outcome.data.testId.orEmpty()
                            )
                        }

                        val adapter = WidgetLayoutAdapter(
                            context = requireContext(),
                            actionPerformer = this,
                            source = TAG
                        )

                        binding.rvMain.adapter = adapter
                        adapter.setWidgets(outcome.data.widgets.orEmpty())

                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                "${EVENT_TAG}_${EventConstants.VIEWED}",
                                hashMapOf<String, Any>(
                                    EventConstants.WIDGET to widgetType,
                                    EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                                    EventConstants.SCHOLARSHIP_TEST_ID to outcome.data.scholarshipTestId.orEmpty(),
                                    EventConstants.TEST_ID to outcome.data.testId.orEmpty(),
                                ).apply {
                                    putAll(outcome.data.extraParams.orEmpty())
                                }
                            )
                        )
                    }

                    if (outcome.data.lottieUrl.isNotNullAndNotEmpty()) {
                        binding.lottieAnimationView.show()
                        binding.lottieAnimationView.setAnimationFromUrl(outcome.data.lottieUrl)
                        binding.lottieAnimationView.addAnimatorListener(object :
                            Animator.AnimatorListener {

                            override fun onAnimationStart(animation: Animator?) {
                                binding.progressBar.isVisible = false
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                mBinding ?: return
                                loadUi()
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                            }

                            override fun onAnimationRepeat(animation: Animator?) {
                            }
                        })
                        binding.lottieAnimationView.playAnimation()
                    } else {
                        loadUi()
                    }
                }
                else -> {
                    dismiss()
                }
            }
        }
    }

    fun setActionListener(actionPerformer: ActionPerformer) {
        this.actionPerformer = actionPerformer
    }

    private fun init() {
        viewModel = viewModelProvider(viewModelFactory)

        binding.ivClose.isVisible = showCloseBtn
    }

    private fun initListeners() {
        binding.ivClose.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CROSS_CLICKED}",
                    hashMapOf(
                        EventConstants.WIDGET to widgetType,
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.ASSORTMENT_ID to assortmentId,
                    )
                )
            )
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        activity ?: return

        dialog?.window?.setLayout(
            requireActivity().getScreenWidth() - 32.dpToPx(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        const val TAG = "BaseWidgetDialogFragment"
        const val EVENT_TAG = "base_widget_dialog_fragment"

        private const val KEY_WIDGET_TYPE = "widget_type"
        private const val KEY_STUDENT_ID = "student_id"
        private const val KEY_ASSORTMENT_ID = "assortment_id"
        private const val KEY_TEST_ID = "test_id"
        private const val KEY_TAB_NUMBER = "tab_number"
        private const val KEY_SHOW_CLOSE_BTN = "show_close_btn"

        fun newInstance(
            widgetType: String,
            studentId: String?,
            assortmentId: String?,
            testId: String?,
            tabNumber: String?,
            showCloseBtn: Boolean = true
        ) =
            BaseWidgetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_WIDGET_TYPE, widgetType)
                    putString(KEY_STUDENT_ID, studentId)
                    putString(KEY_ASSORTMENT_ID, assortmentId)
                    putString(KEY_TEST_ID, testId)
                    putString(KEY_TAB_NUMBER, tabNumber)
                    putBoolean(KEY_SHOW_CLOSE_BTN, showCloseBtn)
                }
            }
    }

    override fun performAction(action: Any) {
        when (action) {
            is Dismiss -> {
                dismiss()
            }
            else -> {
            }
        }
    }
}

