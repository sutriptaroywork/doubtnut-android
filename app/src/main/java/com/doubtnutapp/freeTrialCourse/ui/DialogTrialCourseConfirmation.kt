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
import com.doubtnutapp.databinding.DialogTrialCourseSubscribeConfirmationBinding

class DialogTrialCourseConfirmation() : DialogFragment() {


    var titleText: String? = null
    var confirmationButtonText: String? = null
    var denyButtonText: String? = null

    companion object {
        const val TITLE = "title"
        const val CONFIRMATION_TEXT = "confirmation_label_text"
        const val DENY_TEXT = "deny_label_text"
        fun newInstance(
            title: String?,
            yesText: String?,
            noText: String?
        ): DialogTrialCourseConfirmation {
            return DialogTrialCourseConfirmation().apply {
                arguments = Bundle().apply {
                    putString(TITLE, title)
                    putString(CONFIRMATION_TEXT, yesText)
                    putString(DENY_TEXT, noText)
                }

            }
        }
    }

    lateinit var binding: DialogTrialCourseSubscribeConfirmationBinding
    var listener: DialogConfirmationAndSuccessActionListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogTrialCourseSubscribeConfirmationBinding.inflate(inflater, container, false)
        titleText = arguments?.getString(TITLE)
        confirmationButtonText = arguments?.getString(CONFIRMATION_TEXT)
        denyButtonText = arguments?.getString(DENY_TEXT)
        return binding.root
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as DialogConfirmationAndSuccessActionListener

    }

    override fun onStart() {
        super.onStart()
        dialog?.window
            ?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.dialog?.setCanceledOnTouchOutside(true)

        confirmationButtonText?.let {
            binding.buttonConfirm.text = it
        }

        denyButtonText?.let {
            binding.buttonCancel.text = it
        }

        titleText?.let {
            binding.textViewTitleDialog.text = it
        }


        binding.buttonConfirm.setOnClickListener {
            listener?.confirmActivationOfWidget()
            dismiss()
        }

        binding.buttonCancel.setOnClickListener {
            listener?.onConfirmationDialogNoClicked()
            dismiss()
        }

        binding.imageClose.setOnClickListener {
            listener?.onConfirmationDialogClosed()
            dismiss()
        }


    }


}