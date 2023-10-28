package com.doubtnutapp.ui.answer

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.DislikeFeedbackDialogBinding
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.videoPage.model.VideoDislikeFeedbackOption
import com.doubtnutapp.videoPage.ui.VideoDislikeFeedbackAdapter
import com.doubtnutapp.videoPage.viewmodel.VideoDislikeFeedbackViewModel

class VideoDislikeFeedbackDialog : BaseBindingBottomSheetDialogFragment<VideoDislikeFeedbackViewModel, DislikeFeedbackDialogBinding>() {

    companion object {

        const val TAG = "VideoDislikeFeedbackDialog"

        private const val PARAM_KEY_VIDEO_NAME = "video_name"
        private const val PARAM_KEY_QUESTION_ID = "question_id"
        private const val PARAM_KEY_ANSWER_ID = "answer_id"
        private const val PARAM_KEY_VIEW_TIME = "view_time"
        private const val PARAM_KEY_IS_LIKED = "is_liked"
        private const val PARAM_KEY_SOURCE = "source"
        private const val PARAM_KEY_SCREEN = "screen"
        private const val PARAM_KEY_VIEW_ID = "view_id"

        fun newInstance(video_name: String, question_id: String, answer_id: String, view_time: Long, is_liked: Boolean, source: String, screen: String, view_id: String): VideoDislikeFeedbackDialog = VideoDislikeFeedbackDialog().also {
            it.arguments = bundleOf(
                    PARAM_KEY_VIDEO_NAME to video_name,
                    PARAM_KEY_QUESTION_ID to question_id,
                    PARAM_KEY_ANSWER_ID to answer_id,
                    PARAM_KEY_VIEW_TIME to view_time,
                    PARAM_KEY_IS_LIKED to is_liked,
                    PARAM_KEY_SOURCE to source,
                    PARAM_KEY_SCREEN to screen,
                    PARAM_KEY_VIEW_ID to view_id
            )
        }
    }

    private var video_name: String = ""
    private var question_id: String = ""
    private var answer_id: String = ""
    private var view_time: Long = 0L
    private var is_liked: Boolean = false
    private var source: String = ""
    private var screen: String = ""
    private var view_id: String = ""

    private val adapter = VideoDislikeFeedbackAdapter()

    private var feedbackOptionList = mutableListOf<VideoDislikeFeedbackOption>()

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DislikeFeedbackDialogBinding =
        DislikeFeedbackDialogBinding.inflate(layoutInflater)

    override fun provideViewModel(): VideoDislikeFeedbackViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setUpRecyclerView()
        setClickListeners()
        viewModel.getFeedbackOptions(screen)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            video_name = it.getString(PARAM_KEY_VIDEO_NAME) ?: ""
            question_id = it.getString(PARAM_KEY_QUESTION_ID) ?: ""
            answer_id = it.getString(PARAM_KEY_ANSWER_ID) ?: ""
            view_time = it.getLong(PARAM_KEY_VIEW_TIME)
            is_liked = it.getBoolean(PARAM_KEY_IS_LIKED)
            source = it.getString(PARAM_KEY_SOURCE) ?: ""
            screen = it.getString(PARAM_KEY_SCREEN) ?: ""
            view_id = it.getString(PARAM_KEY_VIEW_ID) ?: ""
        }
    }

    private fun setClickListeners() {
        binding.closeDialogImage.setOnClickListener {
            dialog?.dismiss()
        }

        binding.otherText.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    viewModel.setOtherText(it.trim().toString())
                    binding.submitButton.isEnabled = it.trim().isNotEmpty() || viewModel._feedbackOptionList.isNotEmpty()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })

        binding.submitButton.setOnClickListener {
            viewModel.submitVideoDislikeFeedback(video_name, question_id, answer_id, view_time, source, is_liked, view_id)
        }
    }

    private fun setUpRecyclerView() {
        binding.feedbackList.adapter = adapter
        binding.feedbackList.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                if (position != feedbackOptionList.size - 1) {
                    feedbackOptionList[position].isChecked = !feedbackOptionList[position].isChecked
                    adapter.notifyItemChanged(position)
                    viewModel.updateFeedbackOption(feedbackOptionList[position].content, !feedbackOptionList[position].isChecked)
                    toggleSubmitButton()
                } else {
                    binding.feedbackList.hide()
                    binding.otherText.show()
                    binding.otherText.requestFocus()
                    dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
                }
            }
        })
    }

    override fun setupObservers() {
        viewModel.feedbackOptionListVideo.observeK(
                viewLifecycleOwner,
                this::onFeedbackOptionSuccess,
                this::onApiError,
                this::unAuthorizeUserError,
                this::ioExceptionHandler,
                this::updateProgressBarState
        )

        viewModel.onVideoFeedbackSubmit.observeK(
                viewLifecycleOwner,
                this::onVideoFeedbackSubmit,
                this::onApiError,
                this::unAuthorizeUserError,
                this::ioExceptionHandler,
                this::updateProgressBarState
        )
    }

    private fun onFeedbackOptionSuccess(optionList: List<VideoDislikeFeedbackOption>) {
        feedbackOptionList = optionList.toMutableList()
        feedbackOptionList.add(
                VideoDislikeFeedbackOption(
                        resources.getString(R.string.other_text),
                        isChecked = false,
                        isCheckboxShown = false
                )
        )
        binding.otherText.hide()
        adapter.updateData(feedbackOptionList)
    }

    private fun onVideoFeedbackSubmit(isSubmitted: Boolean) {
        if (isSubmitted) {
            context?.let {
                showToast(it, resources.getString(R.string.string_thanks_for_feedback))
            }

            dialog?.dismiss()
        }
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(parentFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        context?.let {
            if (NetworkUtils.isConnected(it).not()) {
                toast(it.getString(R.string.string_noInternetConnection))
            } else {
                toast(it.getString(R.string.somethingWentWrong))
            }
        }
    }

    private fun updateProgressBarState(state: Boolean) {

    }

    private fun toggleSubmitButton() {
        val isAnySelectedOption = feedbackOptionList.find {
            it.isChecked
        }
        binding.submitButton.isEnabled = isAnySelectedOption != null
    }
}