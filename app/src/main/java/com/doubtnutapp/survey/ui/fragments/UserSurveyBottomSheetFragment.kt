package com.doubtnutapp.survey.ui.fragments

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.doubtnut.core.utils.toast
import com.doubtnutapp.R
import com.doubtnutapp.bottomsheetholder.BottomSheetHolderActivity
import com.doubtnutapp.survey.viewmodel.UserSurveyViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_user_survey.*
import javax.inject.Inject

class UserSurveyBottomSheetFragment : BottomSheetDialogFragment(), DismissSurveyListener,
    HasAndroidInjector {

    companion object {

        const val TAG = "UserSurveyFragment"
        private const val PARAM_SURVEY_ID = "survey_id"
        private const val PARAM_PAGE = "page"
        private const val PARAM_TYPE = "type"
        const val ITEM_POSITION = "item_position"

        fun newInstance(
            surveyId: Long,
            page: String? = null,
            type: String? = null
        ) =
            UserSurveyBottomSheetFragment().apply {
                val bundle = Bundle()
                bundle.putLong(PARAM_SURVEY_ID, surveyId)
                bundle.putString(PARAM_PAGE, page)
                bundle.putString(PARAM_TYPE, type)
                arguments = bundle
            }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    private val viewModel: UserSurveyViewModel by viewModels { viewModelFactory }

    private var surveyId: Long? = null
    private val page: String? by lazy {
        arguments?.getString(PARAM_PAGE)
    }
    private val type: String? by lazy {
        arguments?.getString(PARAM_TYPE)
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getIntentData()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnDismissListener {
            if (activity is BottomSheetHolderActivity) {
                activity?.finish()
            }
        }

        dialog.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP) {
                handleBackPress()
                true
            } else false
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return View.inflate(context, R.layout.fragment_user_survey, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBottomSheetBehavior()
        setUpClickListeners()
        setUpObservers()
        viewModel.getSurveyDetails(surveyId!!, page, type)
    }

    private fun initBottomSheetBehavior() {
        val bottomSheetBehavior = BottomSheetBehavior.from(container)
        // Expanded by default
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.skipCollapsed = true
        bottomSheetBehavior.isFitToContents = false
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
    }

    private fun getIntentData() {
        surveyId = arguments?.getLong(PARAM_SURVEY_ID, -1)
        viewModel.surveyId = surveyId
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (activity is BottomSheetHolderActivity) {
            activity?.finish()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (activity is BottomSheetHolderActivity) {
            activity?.finish()
        }
    }

    private fun setUpClickListeners() {
        ivBack.setOnClickListener {
            handleBackPress()
        }

        ivClose.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                dismiss()
            }
        }

        tvSkip.setOnClickListener {
            viewModel.loadNextQuestion()
        }
    }

    private fun setUpObservers() {

        viewModel.totalNoOfQuestion.observe(viewLifecycleOwner, Observer {
            tvTotalQuestion.text = "of $it"
        })

        viewModel.currentPageNo.observe(viewLifecycleOwner, Observer { pageNo ->
            tvPageNo.text = (pageNo + 1).toString()
            viewModel.totalNoOfQuestion.value?.let { totalQuestion ->
                progress.progress = (((pageNo + 1) * 100) / totalQuestion)
            }
            when (pageNo) {
                -1, viewModel.totalNoOfQuestion.value ->
                    toggleUpperLayout(false)
                else ->
                    toggleUpperLayout(true)
            }
        })

        viewModel.currentQuestionPositionAndType.observe(
            viewLifecycleOwner,
            Observer { questionPositionAndType ->
                when (questionPositionAndType.first) {
                    -1 -> addSurveyStartingFragment()
                    viewModel.totalNoOfQuestion.value -> addSurveyEndingFragment()
                    else -> loadNextQuestion(
                        questionPositionAndType.first,
                        questionPositionAndType.second.orEmpty()
                    )
                }
            })

        viewModel.skipLayout.observe(viewLifecycleOwner, Observer {
            tvSkip.text = it.first
            tvSkip.isVisible = it.second
        })

        viewModel.messageLiveData.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.run {
                toast(this)
            }
        })

        viewModel.messageStringIdLiveData.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.run {
                toast(this)
            }
        })
    }

    private fun toggleUpperLayout(toShow: Boolean) {
        ivBack.isVisible = toShow
        ivClose.isVisible = !toShow
        pageNoLayout.visibility = if (toShow) View.VISIBLE else View.INVISIBLE
        progress.visibility = if (toShow) View.VISIBLE else View.INVISIBLE
    }

    private fun loadNextQuestion(position: Int, type: String) {

        when (type) {
            "date" ->
                addNextFragment(CalendarFragment.newInstance(position), CalendarFragment.TAG)

            "single" ->
                addNextFragment(
                    SingleChoiceFragment.newInstance(position),
                    SingleChoiceFragment.TAG
                )

            "multiple" ->
                addNextFragment(
                    MultipleChoiceFragment.newInstance(position),
                    MultipleChoiceFragment.TAG
                )

            "description" ->
                addNextFragment(EditTextFragment.newInstance(position), EditTextFragment.TAG)

            "rating" ->
                addNextFragment(RatingFragment.newInstance(position), RatingFragment.TAG)
        }
    }

    private fun addSurveyStartingFragment() =
        addNextFragment(StartSurveyFragment.newInstance(), StartSurveyFragment.TAG, false)

    private fun addSurveyEndingFragment() {
        val endSurveyFragment = EndSurveyFragment.newInstance()
        endSurveyFragment.setDismissSurveyListener(this)
        addNextFragment(endSurveyFragment, EndSurveyFragment.TAG)
    }

    private fun addNextFragment(fragment: Fragment, tag: String, addToBackStack: Boolean = true) {
        val fragmentTransaction = childFragmentManager
            .beginTransaction()
            .add(R.id.componentFragmentContainer, fragment, tag)

        if (addToBackStack) fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun handleBackPress() {
        if (childFragmentManager.backStackEntryCount > 0) {
            viewModel.loadPreviousQuestion()
            childFragmentManager.popBackStack()
        } else {
            dismissAllowingStateLoss()
        }
    }

    override fun dismissSurvey() {
        dismissAllowingStateLoss()
    }
}

interface DismissSurveyListener {
    fun dismissSurvey()
}