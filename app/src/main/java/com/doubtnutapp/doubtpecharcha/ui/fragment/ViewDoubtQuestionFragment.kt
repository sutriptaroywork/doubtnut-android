package com.doubtnutapp.doubtpecharcha.ui.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.DialogFragment
import com.doubtnut.core.utils.dpToPx
import com.doubtnutapp.*
import com.doubtnutapp.databinding.FragmentViewDoubtPeCharchaQuestionBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ViewDoubtQuestionFragment : DialogFragment() {

    lateinit var binding: FragmentViewDoubtPeCharchaQuestionBinding

    private var viewQuestionButtonClickListener:ViewQuestionButtonListener?= null

    companion object {
        const val HOST_NAME = "HOST_NAME"
        const val QUESTION_IMG_URL = "QUESTION_IMG"
        const val QUESTION_TEXT = "QUESTION_TEXT"
        const val PROFILE_IMAGE = "PROFILE_IMG"
        const val TAG = "ViewDoubtQuestion"
        fun newInstance(
            hostName: String,
            profileImage: String?,
            questionImgUrl: String,
            questionText: String,
        ) = ViewDoubtQuestionFragment().apply {
            val bundle = Bundle()
            bundle.putString(HOST_NAME, hostName)
            bundle.putString(PROFILE_IMAGE, profileImage)
            bundle.putString(QUESTION_IMG_URL, questionImgUrl)
            bundle.putString(QUESTION_TEXT, questionText)
            arguments = bundle
        }
    }

    fun setButtonClickListener(listener: ViewQuestionButtonListener) {
        this.viewQuestionButtonClickListener = listener
    }

    private val hostName: String? by lazy {
        arguments?.getString(HOST_NAME)
    }

    private val hostProfileImage: String? by lazy {
        arguments?.getString(PROFILE_IMAGE)
    }

    private val questionImgUrl: String? by lazy {
        arguments?.getString(QUESTION_IMG_URL)
    }

    private val questionText: String? by lazy {
        arguments?.getString(QUESTION_TEXT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewDoubtPeCharchaQuestionBinding.inflate(inflater, container, false)
        if (questionImgUrl.isNotNullAndNotEmpty()) {
            binding.ivDoubt.show()
            binding.ivDoubt.loadImage(questionImgUrl.orEmpty())
            binding.clTextQuestion.hide()
        } else {
            binding.clTextQuestion.show()
            binding.ivDoubt.hide()
            if (questionText.isNotNullAndNotEmpty()) {
                if (questionText!!.length > 300) {
                    binding.llQuestionView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        val dpHeight = 300.dpToPx()
                        height = dpHeight
                    }
                }

                binding.tvTitle.text = hostName
                binding.tvText.text = questionText.orEmpty()
                binding.ivProfile.loadImage(hostProfileImage.orEmpty())
            }
        }

        binding.close.setOnClickListener {
            dismiss()
        }
        binding.rootLayout.setOnClickListener {
            dismiss()
        }

        binding.actionButton.setOnClickListener {
            dismiss()
            viewQuestionButtonClickListener?.onViewQuestionSolveNowButtonClicked()
        }




        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true);
    }



    override fun onStart() {
        super.onStart()
        dialog?.window
            ?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
    }

    interface ViewQuestionButtonListener{
        fun onViewQuestionSolveNowButtonClicked()
    }
}