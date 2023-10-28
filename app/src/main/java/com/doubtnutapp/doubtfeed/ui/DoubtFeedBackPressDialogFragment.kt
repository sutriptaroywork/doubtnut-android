package com.doubtnutapp.doubtfeed.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.databinding.FragmentDoubtFeedBackPressDialogBinding
import com.doubtnutapp.doubtfeed.viewmodel.DoubtFeedViewModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.ui.base.BaseBindingDialogFragment

class DoubtFeedBackPressDialogFragment :
    BaseBindingDialogFragment<DoubtFeedViewModel, FragmentDoubtFeedBackPressDialogBinding>() {

    companion object {
        const val TAG = "DoubtFeedBackPressDialogFragment"
        fun newInstance(): DoubtFeedBackPressDialogFragment =
            DoubtFeedBackPressDialogFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDoubtFeedBackPressDialogBinding =
        FragmentDoubtFeedBackPressDialogBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DoubtFeedViewModel {
        val doubtFeedViewModel: DoubtFeedViewModel by viewModels(
            ownerProducer = { requireParentFragment() }
        )
        return doubtFeedViewModel
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        val data = viewModel.backPressPopupData ?: return

        mBinding?.ivEmoji?.loadImage(data.imageUrl)
        mBinding?.tvPopupMessage?.text = data.description
        mBinding?.buttonAskQuestion?.text = data.mainCta
        mBinding?.tvSecondaryCta?.text = data.secondaryCta

        mBinding?.buttonAskQuestion?.setOnClickListener {
            viewModel.deeplinkAction.performAction(view.context, data.mainDeeplink)
            viewModel.sendEvent(EventConstants.DF_BACK_PRESS_POPUP_ASK_QUESTION_CLICK)
        }
        mBinding?.tvSecondaryCta?.setOnClickListener {
            dismiss()
        }
        mBinding?.ivCancel?.setOnClickListener { dismiss() }
        mBinding?.rootContainer?.setOnClickListener { dismiss() }
        mBinding?.cardContainer?.setOnClickListener { }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        return object : Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                dismiss()
            }
        }
    }
}
