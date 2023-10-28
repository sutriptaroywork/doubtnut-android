package com.doubtnutapp.survey.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.immediateParentViewModelStoreOwner
import com.doubtnutapp.R
import com.doubtnutapp.base.OnChoiceSelected
import com.doubtnutapp.databinding.FragmentMultipleChoiceBinding
import com.doubtnutapp.survey.ui.adapter.ChoiceAdapter
import com.doubtnutapp.survey.viewmodel.UserSurveyViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import kotlinx.android.synthetic.main.fragment_multiple_choice.*


class MultipleChoiceFragment :
    BaseBindingFragment<UserSurveyViewModel, FragmentMultipleChoiceBinding>(), ActionPerformer {

    companion object {
        const val TAG = "MultipleChoiceFragment"

        @JvmStatic
        fun newInstance(position: Int) =
            MultipleChoiceFragment().apply {
                val bundle = Bundle()
                bundle.putInt(UserSurveyBottomSheetFragment.ITEM_POSITION, position)
                arguments = bundle
            }
    }

    private val questionPosition: Int? by lazy {
        arguments?.getInt(UserSurveyBottomSheetFragment.ITEM_POSITION)
    }

    private val choiceAdapter: ChoiceAdapter by lazy {
        ChoiceAdapter(this@MultipleChoiceFragment, R.layout.item_multiple_choice)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.questionList.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { questionList ->
                questionPosition?.let { position ->
                    val multipleChoiceQuestion = questionList[position]
                    tvTitle.text = multipleChoiceQuestion.questionText
                    btNext.text = multipleChoiceQuestion.nextText
                    choiceAdapter.updateOptions(multipleChoiceQuestion.options.toMutableList())
                    btNext.isEnabled = multipleChoiceQuestion.feedback != null
                }
            })
    }

    private fun setUpClickListeners() {
        btNext.setOnClickListener {

            if (viewModel.questionList.value == null || questionPosition == null) return@setOnClickListener

            when {
                viewModel.questionList.value!![questionPosition!!].options.find { it.isChecked } != null -> {
                    viewModel.storeSurveyFeedback("multiple", questionPosition)
                }
                else -> viewModel.loadNextQuestion()
            }
        }
    }

    private fun setUpMultipleChoiceList() {
        rvMultipleChoice.adapter = choiceAdapter
    }

    override fun performAction(action: Any) {
        when (action) {
            is OnChoiceSelected -> {
                viewModel.questionList.value?.let { questionList ->
                    questionList[questionPosition!!].options[action.position].isChecked =
                        action.isChecked
                    questionList[questionPosition!!].feedback =
                        questionList[questionPosition!!].options.filter {
                            it.isChecked
                        }.joinToString("::,::") { it.title }

                    btNext.isEnabled =
                        questionList[questionPosition!!].options.find { it.isChecked } != null
                }
            }
            else -> {
            }
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMultipleChoiceBinding =
        FragmentMultipleChoiceBinding.inflate(layoutInflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): UserSurveyViewModel {
        val vm: UserSurveyViewModel by viewModels(
            ownerProducer = { immediateParentViewModelStoreOwner }
        ) { viewModelFactory }
        return vm
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpMultipleChoiceList()
        setUpClickListeners()
    }
}