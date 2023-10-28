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
import com.doubtnutapp.databinding.FragmentSingleChoiceBinding
import com.doubtnutapp.survey.ui.adapter.ChoiceAdapter
import com.doubtnutapp.survey.viewmodel.UserSurveyViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment

class SingleChoiceFragment :
    BaseBindingFragment<UserSurveyViewModel, FragmentSingleChoiceBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "SingleChoiceFragment"

        @JvmStatic
        fun newInstance(position: Int) =
                SingleChoiceFragment().apply {
                    val bundle = Bundle()
                    bundle.putInt(UserSurveyBottomSheetFragment.ITEM_POSITION, position)
                    arguments = bundle
                }
    }

    private val questionPosition: Int? by lazy {
        arguments?.getInt(UserSurveyBottomSheetFragment.ITEM_POSITION)
    }

    private val choiceAdapter: ChoiceAdapter by lazy {
        ChoiceAdapter(this@SingleChoiceFragment, R.layout.item_single_choice)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.questionList.observe(viewLifecycleOwner, androidx.lifecycle.Observer { questionList ->
            questionPosition?.let { position ->
                val singleChoiceQuestion = questionList[position]
                    mBinding?.tvTitle?.text = singleChoiceQuestion.questionText
                mBinding?.btNext?.text = singleChoiceQuestion.nextText
                choiceAdapter.updateOptions(singleChoiceQuestion.options.toMutableList())
                mBinding?.btNext?.isEnabled = singleChoiceQuestion.feedback != null
            }
        })
    }

    private fun setUpClickListeners() {
        mBinding?.btNext?.setOnClickListener {

            if (viewModel.questionList.value == null || questionPosition == null) return@setOnClickListener

            when {
                viewModel.questionList.value!![questionPosition!!].options.find { it.isChecked } != null -> {
                    viewModel.storeSurveyFeedback("single", questionPosition)
                }
                else -> viewModel.loadNextQuestion()
            }
        }
    }

    private fun setUpSingleChoiceList() {
        mBinding?.rvSingleChoice?.adapter = choiceAdapter
    }

    override fun performAction(action: Any) {
        when (action) {
            is OnChoiceSelected -> {
                viewModel.questionList.value?.let { questionList ->
                    questionList[questionPosition!!].options.forEachIndexed { index, choiceViewItem ->
                        if (action.position == index) {
                            choiceViewItem.isChecked = action.isChecked
                        } else {
                            choiceViewItem.isChecked = false
                        }
                    }
                    mBinding?.rvSingleChoice?.post {
                        choiceAdapter.updateOptions(questionList[questionPosition!!].options)
                    }
                    questionList[questionPosition!!].feedback = action.title

                    mBinding?.btNext?.isEnabled =
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
    ): FragmentSingleChoiceBinding {
        return FragmentSingleChoiceBinding.inflate(inflater, container, false)
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
        setUpSingleChoiceList()
        setUpClickListeners()
    }
}