package com.doubtnutapp.widgets.base

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.*
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.Dismiss
import com.doubtnutapp.base.OnMediumSelected
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.DialogBaseWidgetBottomSheetBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.libraryhome.coursev3.ui.CourseFragment
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class BaseWidgetBottomSheetDialogFragment :
    BaseBindingBottomSheetDialogFragment<BaseWidgetBottomSheetDialogFragmentVM, DialogBaseWidgetBottomSheetBinding>(),
    ActionPerformer {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var actionPerformer: ActionPerformer? = null
    private var selectedAssortmentId = ""

    private val title: String? by lazy { requireArguments().getString(KEY_TITLE) }

    private val widgetType: String by lazy { requireArguments().getString(KEY_WIDGET_TYPE)!! }

    private val id: String by lazy { requireArguments().getString(KEY_ID)!! }

    private val userCategory: String? by lazy { requireArguments().getString(KEY_USER_CATEGORY) }

    private val questionCount: String? by lazy { requireArguments().getString(KEY_QUESTION_COUNT) }

    private val openCount: String? by lazy { requireArguments().getString(KEY_OPEN_COUNT) }

    private var backPressDeeplink: String? = null


    private val showCloseBtn: Boolean by lazy {
        requireArguments().getBoolean(
            KEY_SHOW_CLOSE_BTN,
            true
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseBottomSheetDialog)
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
    ): DialogBaseWidgetBottomSheetBinding {
        return DialogBaseWidgetBottomSheetBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): BaseWidgetBottomSheetDialogFragmentVM {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        init()
        initListeners()
        viewModel.getBottomSheetWidgetData(
            widgetType = widgetType,
            assortmentId = id,
            userCategory = userCategory,
            openCount = openCount,
            questionAskCount = questionCount
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
                    binding.progressBar.isVisible = false

                    outcome.data.widgets?.filterNotNull()?.forEach {
                        if (it.extraParams == null) {
                            it.extraParams = HashMap()
                        }
                        it.extraParams?.put(EventConstants.WIDGET_TITLE, title.orEmpty())
                        it.extraParams?.put(EventConstants.ASSORTMENT_ID, id)
                        it.extraParams?.put(EventConstants.TYPE, widgetType)
                    }

                    outcome.data.title
                        ?.takeIf { it.isNotEmpty() }
                        ?.let {
                            binding.tvTitle.show()
                            binding.tvTitle.text = it
                            binding.tvTitle.text = HtmlCompat.fromHtml(
                                outcome.data.title!!,
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            );
                        }

                    outcome.data.subtitle
                        ?.takeIf { it.isNotEmpty() }
                        ?.let {
                            binding.tvSubtitle.show()
                            binding.tvSubtitle.text = it
                            binding.tvSubtitle.text = HtmlCompat.fromHtml(
                                outcome.data.subtitle!!,
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            );
                        }

                    outcome.data.cta?.let { cta ->
                        binding.buttonCta.visibility = View.VISIBLE
                        cta.title?.let {
                            binding.buttonCta.text = it
                        }
                        cta.titleColor?.let {
                            binding.buttonCta.setTextColor(Color.parseColor(it))
                        }
                        cta.backgroundColor?.let { bgColor ->
                            binding.buttonCta.background = Utils.getShape(
                                colorString = bgColor,
                                strokeColor = cta.strokeColor ?: bgColor,
                                cornerRadius = 3.dpToPxFloat(),
                                strokeWidth = 1.dpToPx()
                            )
                        }
                        cta.deepLink?.let {
                            binding.buttonCta.setOnClickListener {
                                deeplinkAction.performAction(requireContext(), cta.deepLink)
                            }
                        }
                    }

                    binding.tvTitle.applyTextColor(outcome.data.titleTextColor)
                    binding.tvTitle.applyTextSize(outcome.data.titleTextSize)

                    binding.ivClose.isVisible = outcome.data.showCloseBtn ?: true
                    binding.viewDragTop.isVisible = outcome.data.showTopDragIcon ?: false

                    val adapter = WidgetLayoutAdapter(
                        context = requireContext(),
                        actionPerformer = this,
                        source = TAG,
                    )
                    binding.rvMain.setPadding(
                        0,
                        0,
                        0,
                        (outcome.data.paddingBottom ?: 30).dpToPx()
                    )

                    if (outcome.data.cta != null) {
                        val topMargin = 20.dpToPx()
                        val bottomMargin = 30.dpToPx()
                        binding.rvMain.updateLayoutParams<ConstraintLayout.LayoutParams> {
                            setMargins(0, topMargin, 0, bottomMargin)
                        }
                    }

                    binding.rvMain.adapter = adapter
                    adapter.addWidgets(outcome.data.widgets.orEmpty())

                    backPressDeeplink = outcome.data.backPressDeeplink;
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (widgetType == "select_medium" && selectedAssortmentId.isNotEmpty() && selectedAssortmentId != id) {
            setFragmentResult(
                CourseFragment.TAG,
                Bundle().apply { putString(KEY_ASSORTMENT_ID, selectedAssortmentId) }
            )
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (widgetType == Constants.TYPE_HOMEPAGE_CONTINUE_WATCHING_WIDGET) {
            backPressDeeplink?.let {
                if (it.isNotEmpty()) {
                    deeplinkAction.performAction(requireContext(), backPressDeeplink)
                }
            }

        }
    }

    private fun init() {
        viewModel = viewModelProvider(viewModelFactory)

        binding.tvTitle.text = title
        binding.tvTitle.isVisible = title.isNullOrEmpty().not()
        binding.ivClose.isVisible = showCloseBtn

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                "${EVENT_TAG}_${EventConstants.VIEWED}",
                hashMapOf(
                    EventConstants.WIDGET to widgetType,
                    EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                )
            )
        )

    }

    private fun initListeners() {
        binding.ivClose.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CROSS_CLICKED}",
                    hashMapOf(
                        EventConstants.WIDGET to widgetType,
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                    )
                )
            )

            dismiss()
        }
    }

    companion object {
        const val TAG = "BaseWidgetBottomSheetDialogFragment"
        const val EVENT_TAG = "base_widget_bottom_sheet_dialog_fragment"

        const val KEY_TITLE = "title"
        const val KEY_ID = "id"
        const val KEY_WIDGET_TYPE = "widget_type"
        const val KEY_SHOW_CLOSE_BTN = "show_close_btn"
        const val KEY_ASSORTMENT_ID = "assortment_id"
        const val KEY_USER_CATEGORY = "user_category"
        const val KEY_OPEN_COUNT = "key_open_count"
        const val KEY_QUESTION_COUNT = "key_question_count"

        const val WIDGET_TYPE_PAID_USER_CHAMPIONSHIP = "paid_user_championship"

        fun newInstance(
            id: String?,
            title: String?,
            widgetType: String,
            showCloseBtn: Boolean = true,
            userCategory: String?,
            openCount: String?,
            questionCount: String?
        ) =
            BaseWidgetBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_ID, id)
                    putString(KEY_TITLE, title)
                    putString(KEY_WIDGET_TYPE, widgetType)
                    putBoolean(KEY_SHOW_CLOSE_BTN, showCloseBtn)
                    putString(KEY_USER_CATEGORY, userCategory)
                    putString(KEY_OPEN_COUNT, openCount)
                    putString(KEY_QUESTION_COUNT, questionCount)
                }
            }
    }

    override fun performAction(action: Any) {
        when (action) {
            is OnMediumSelected -> {
                selectedAssortmentId = action.selectedAssortmentId
            }
            is Dismiss -> {
                dismiss()
            }
            else -> {
            }
        }
    }


}

