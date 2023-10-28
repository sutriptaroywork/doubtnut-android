package com.doubtnutapp.freeTrialCourse.ui

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.doubtnutapp.MainActivity
import com.doubtnutapp.databinding.DialogFreeCourseActivatedBinding
import kotlinx.android.synthetic.main.dialog_free_course_activated.*
import android.content.Intent
import com.doubtnutapp.R


class DialogTrialFreeCourseActivatedSuccess : DialogFragment() {

    private lateinit var binding: DialogFreeCourseActivatedBinding

    private var buttonText: String? = null
    private var deeplink: String? = null

    var listener: DialogConfirmationAndSuccessActionListener? = null

    companion object {
        const val DIALOG_FREE_TRIAL_COURSE = "dialog_activation_success"
        const val TITLE: String = "title"
        const val DEEPLINK: String = "deeplink"

        fun newInstance(
            buttonText: String?,
            deeplink: String?
        ): DialogTrialFreeCourseActivatedSuccess {


            return DialogTrialFreeCourseActivatedSuccess().apply {
                arguments = Bundle().apply {
                    putString(TITLE, buttonText)
                    putString(DEEPLINK, deeplink)

                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = parentFragment as DialogConfirmationAndSuccessActionListener
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFreeCourseActivatedBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCanceledOnTouchOutside(true)

        buttonText = arguments?.getString(TITLE)
        deeplink = arguments?.getString(DEEPLINK)

        buttonText?.let {
            binding.buttonContinue.text = buttonText
        }

        binding.buttonContinue.setOnClickListener {
            listener?.onViewCourseClickedAfterPurchaseSuccess()
            dismiss()
        }

        successLottieAnimationView.resumeAnimation()

        binding.imageClose.setOnClickListener {
            dismiss()
            startMainActivity()
        }

    }

    // when user presses outside open MainActivity
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        startMainActivity()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window
            ?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
    }

    private fun startMainActivity() {
        startActivity(
            MainActivity.getStartIntent(
                requireContext(),
                doOpenHomePage = true
            )
        )
    }
}