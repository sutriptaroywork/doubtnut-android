package com.doubtnutapp.survey.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.doubtnut.core.utils.immediateParentViewModelStoreOwner
import com.doubtnutapp.databinding.FragmentCalendarBinding
import com.doubtnutapp.survey.viewmodel.UserSurveyViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import java.util.*


class CalendarFragment : BaseBindingFragment<UserSurveyViewModel, FragmentCalendarBinding>() {

    companion object {

        const val TAG = "CalendarFragment"

        @JvmStatic
        fun newInstance(position: Int) =
            CalendarFragment().apply {
                val bundle = Bundle()
                bundle.putInt(UserSurveyBottomSheetFragment.ITEM_POSITION, position)
                arguments = bundle
            }
    }


    private val questionPosition: Int? by lazy {
        arguments?.getInt(UserSurveyBottomSheetFragment.ITEM_POSITION)
    }

    private fun setUpClickListeners() {
        binding.calendarLayout.setOnClickListener {
            showCalendar()
        }

        binding.btNext.setOnClickListener {

            if (viewModel.questionList.value == null || questionPosition == null) return@setOnClickListener

            when {
                viewModel.questionList.value!![questionPosition!!].feedback != null -> {
                    viewModel.storeSurveyFeedback("date", questionPosition)
                }

                else -> viewModel.loadNextQuestion()
            }
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.questionList.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { questionList ->
                questionPosition?.let { position ->
                    val dateQuestion = questionList[position]
                    binding.btNext.isEnabled = dateQuestion.feedback != null
                    binding.tvTitle.text = dateQuestion.questionText
                    binding.tvMessage.text = dateQuestion.feedback ?: dateQuestion.options[0].title
                    binding.btNext.text = dateQuestion.nextText
                }
            })
    }

    private fun showCalendar() {
        val calendar = Calendar.getInstance()
        val mYear = calendar.get(Calendar.YEAR)
        val mMonth = calendar.get(Calendar.MONTH)
        val mDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { view, year, monthOfYear, dayOfMonth ->
                val selectedDate = (dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year)
                binding.tvMessage.text = selectedDate
                if (questionPosition == null || viewModel.questionList.value == null) {
                    return@DatePickerDialog
                }
                viewModel.questionList.value!![questionPosition!!].feedback = selectedDate
                binding.btNext.isEnabled = true

            }, mYear, mMonth, mDay
        )
        datePickerDialog.show()
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCalendarBinding {
        return FragmentCalendarBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): UserSurveyViewModel {
        val vm: UserSurveyViewModel by viewModels(
            ownerProducer = { immediateParentViewModelStoreOwner }
        ) { viewModelFactory }
        return vm
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpClickListeners()
    }
}