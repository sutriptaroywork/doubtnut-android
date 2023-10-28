package com.doubtnutapp.dnr.ui.fragment

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentDnrTncBottomSheetBinding
import com.doubtnutapp.dnr.widgets.DnrTotalRewardWidget
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnut.core.utils.viewModelProvider

class DnrTncBottomSheet :
    BaseBindingBottomSheetDialogFragment<DummyViewModel, FragmentDnrTncBottomSheetBinding>() {

    companion object {
        const val TAG = "DnrTncBottomSheet"
        const val TNC_DIALOG_DATA = "tnc_dialog_data"

        fun newInstance() =
            DnrTncBottomSheet().apply {
                arguments = bundleOf()
            }
    }

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDnrTncBottomSheetBinding =
        FragmentDnrTncBottomSheetBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        val tncDialogData =
            arguments?.getParcelable<DnrTotalRewardWidget.TncDialogData>(TNC_DIALOG_DATA)
        if (tncDialogData != null) {
            binding.title.text = tncDialogData.title
            binding.content.apply {
                text = tncDialogData.content
                movementMethod = ScrollingMovementMethod()
            }
        }
        binding.close.setOnClickListener {
            dismiss()
        }
    }
}
