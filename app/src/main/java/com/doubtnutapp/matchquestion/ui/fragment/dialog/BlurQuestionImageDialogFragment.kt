package com.doubtnutapp.matchquestion.ui.fragment.dialog

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Constants
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.databinding.FragmentBlurQuestionImageDialogBinding
import com.doubtnutapp.loadImage
import com.doubtnutapp.matchquestion.viewmodel.BlurQuestionImageDialogFragmentViewModel
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.ui.main.demoanimation.DemoAnimationActivity
import com.doubtnut.core.utils.viewModelProvider

/**
 * Created by devansh on 2020-07-02.
 */

class BlurQuestionImageDialogFragment : BaseBindingDialogFragment<
        BlurQuestionImageDialogFragmentViewModel, FragmentBlurQuestionImageDialogBinding>() {

    private var imageUri: String = ""
    private var questionId: String = ""

    companion object {
        const val TAG = "BlurQuestionImageDialogFragment"
        private const val IMAGE_URI = "image_uri"
        private const val QUESTION_ID = "question_id"

        fun newInstance(imageUri: String?, questionId: String) =
                BlurQuestionImageDialogFragment().apply {
                    arguments = Bundle().apply {
                        putString(IMAGE_URI, imageUri)
                        putString(QUESTION_ID, questionId)
                    }
                }
    }

    override fun provideViewBinding(
            inflater: LayoutInflater,
            container: ViewGroup?
    ): FragmentBlurQuestionImageDialogBinding {
        return FragmentBlurQuestionImageDialogBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): BlurQuestionImageDialogFragmentViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar)
        arguments?.let {
            imageUri = it.getString(IMAGE_URI) ?: ""
            questionId = it.getString(QUESTION_ID) ?: ""
        }
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)

        sendEvent(EventConstants.EVENT_NAME_BLUR_DIALOG_SHOWN)

        mBinding?.apply {
            imageViewQuestion.loadImage(imageUri)
            imageViewQuestion.clipToOutline = true
            textViewHelp.paintFlags = textViewHelp.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            buttonClickClearPicture.setOnClickListener {
                if (activity == null) return@setOnClickListener

                sendEvent(EventConstants.EVENT_NAME_BLUR_TAKE_NEW_IMAGE)
                openCamera()
                dialog?.dismiss()
                activity?.finish()
            }

            textViewHelp.setOnClickListener {
                if (activity == null) return@setOnClickListener

                sendEvent(EventConstants.EVENT_NAME_BLUR_VIEW_DEMO)
                val demoActivityIntent =
                        context?.let { it -> DemoAnimationActivity.getStartIntent(it, 0, TAG) }
                startActivity(demoActivityIntent)
                activity?.finish()

            }
        }

        dialog?.setOnKeyListener { dialogInterface, i, keyEvent ->

            if (i == KeyEvent.KEYCODE_BACK && keyEvent?.action == KeyEvent.ACTION_UP) {

                activity?.finish()
            }
            return@setOnKeyListener false
        }
    }

    private fun openCamera() {
        context?.let {
            CameraActivity.getStartIntent(it, TAG).also { intent ->
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    private fun sendEvent(
            eventName: String,
            params: HashMap<String, Any> = hashMapOf(Constants.ASKED_QUESTION_ID to questionId)
    ) {
        viewModel.sendEvent(eventName, params)
    }

}