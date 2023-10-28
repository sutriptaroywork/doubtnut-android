package com.doubtnutapp.survey.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.doubtnut.core.utils.hideKeyboard
import com.doubtnut.core.utils.immediateParentViewModelStoreOwner
import com.doubtnutapp.databinding.FragmentEdittextBinding
import com.doubtnutapp.survey.viewmodel.UserSurveyViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment


class EditTextFragment : BaseBindingFragment<UserSurveyViewModel, FragmentEdittextBinding>() {

    companion object {
        const val TAG = "EditTextFragment"
        fun newInstance(position: Int) =
            EditTextFragment().apply {
                val bundle = Bundle()
                bundle.putInt(UserSurveyBottomSheetFragment.ITEM_POSITION, position)
                arguments = bundle
            }
    }

    private val questionPosition: Int? by lazy {
        arguments?.getInt(UserSurveyBottomSheetFragment.ITEM_POSITION)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentEdittextBinding =
        FragmentEdittextBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): UserSurveyViewModel {
        val vm: UserSurveyViewModel by viewModels(
            ownerProducer = { immediateParentViewModelStoreOwner }
        ) { viewModelFactory }
        return vm
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpListeners()
    }

    private fun setUpListeners() {
        binding.etInfo.addTextChangedListener { text ->
            if (viewModel.questionList.value == null || questionPosition == null) return@addTextChangedListener
            viewModel.questionList.value!![questionPosition!!].feedback = text?.trim().toString()
            binding.btNext.isEnabled = text?.trim()?.isNotEmpty() == true
        }

        binding.btNext.setOnClickListener {

            if (viewModel.questionList.value == null || questionPosition == null) return@setOnClickListener

            hideKeyboard()

            when {
                binding.etInfo.text.toString().isNotEmpty() -> {
                    viewModel.storeSurveyFeedback("description", questionPosition)
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
                    val editTextQuestion = questionList[position]
                    binding.tvTitle.text = editTextQuestion.questionText

                    editTextQuestion.feedback?.let { binding.etInfo.setText(it) }
                    binding.etInfo.hint = editTextQuestion.options[0].title
                    binding.btNext.text = editTextQuestion.nextText

                    binding.tvInfo.text = editTextQuestion.alertText.orEmpty()
                    binding.tvInfo.isVisible = !editTextQuestion.alertText.isNullOrEmpty()

                    binding.btNext.isEnabled = !editTextQuestion.feedback.isNullOrEmpty()
                }
            })
    }

}