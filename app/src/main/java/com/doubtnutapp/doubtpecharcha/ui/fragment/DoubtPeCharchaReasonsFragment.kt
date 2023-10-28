package com.doubtnutapp.doubtpecharcha.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.doubtnut.core.utils.toast
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentDoubtPeCharchaUserExperienceBinding
import com.doubtnutapp.doubtpecharcha.viewmodel.DoubtP2pViewModel
import com.doubtnutapp.getScreenWidth
import com.doubtnutapp.ui.base.BaseBindingDialogFragment

class DoubtPeCharchaReasonsFragment :
    BaseBindingDialogFragment<DoubtP2pViewModel, FragmentDoubtPeCharchaUserExperienceBinding>() {

    companion object {
        const val TAG = "DoubtPeCharchaReasonsFragment"
        private const val ROOM_ID = "room_id"
        fun newInstance(roomId: String) = DoubtPeCharchaReasonsFragment().apply {
            val bundle = Bundle()
            bundle.putString(ROOM_ID, roomId)
            arguments = bundle
        }
    }

    private var selectedOptionText: String? = null
    private var otherText: String? = null

    private val roomId: String? by lazy {
        arguments?.getString(ROOM_ID)
    }

    private var submitFeedbackListenerListener: SubmitNegativeFeedbackListener? = null

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDoubtPeCharchaUserExperienceBinding =
        FragmentDoubtPeCharchaUserExperienceBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DoubtP2pViewModel {
        val vm: DoubtP2pViewModel by activityViewModels { viewModelFactory }
        return vm
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpClickListeners()
    }

    override fun onStart() {
        super.onStart()
        val params = view?.layoutParams
        params?.width = activity?.getScreenWidth()
        view?.layoutParams = params
    }

    fun setSubmitFeedbackListener(submitNegativeFeedbackListener: SubmitNegativeFeedbackListener) {
        this.submitFeedbackListenerListener = submitNegativeFeedbackListener
    }

    private fun setUpClickListeners() {
        binding.tvSubmit.setOnClickListener {
            if (selectedOptionText.isNullOrEmpty() && otherText.isNullOrEmpty()) {
                toast(R.string.error_doubt_pe_charcha_feedback)
                return@setOnClickListener
            }
            val reason = if (otherText.isNullOrEmpty()) selectedOptionText else otherText
            viewModel.submitFeedback(
                studentId = null,
                rating = null,
                reason = reason,
                roomId = roomId!!
            )
        }

        binding.tvOther.addTextChangedListener {
            otherText = it.toString().trim()
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.reasons.observe(viewLifecycleOwner) {
            setUpRadioGroupList(it)
        }

        viewModel.isFeedbackSubmitted.observe(viewLifecycleOwner) { isSubmitted ->
            isSubmitted.getContentIfNotHandled()?.let {
                if (it.first) {
                    dismiss()
                    submitFeedbackListenerListener?.onSubmitFeedback()
                }
            }
        }
    }

    private fun setUpRadioGroupList(reasons: List<String>) {
        binding.radioButtonContainer.removeAllViews()
        val radioGroup = RadioGroup(requireContext())
        reasons.forEachIndexed { index, reason ->
            val radioButton = RadioButton(requireContext())
            radioButton.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            radioButton.text = reason
            radioButton.id = index

            radioGroup.addView(radioButton)
        }

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        radioGroup.layoutParams = params

        binding.radioButtonContainer.addView(radioGroup)

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedId: Int = group.checkedRadioButtonId
            val radioButton = group.findViewById(selectedId) as RadioButton
            selectedOptionText = radioButton.text.toString()
        }
    }

    interface SubmitNegativeFeedbackListener {
        fun onSubmitFeedback()
    }
}
