package com.doubtnutapp.paymentplan.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.databinding.BottomSheetCardTooltipBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.paymentplan.widgets.CardHelpDialogData
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior

/**
 * Created by Akshat Jindal on 14-01-2022
 */

class CardTooltipBottomSheet :
    BaseBindingBottomSheetDialogFragment<DummyViewModel, BottomSheetCardTooltipBinding>() {

    companion object {
        const val TAG = "CardTooltipBottomSheet"
        const val CARD_TOOL_TIP_DIALOG_DATA = "card_tooltip_dialog_data"

        fun newInstance(data: CardHelpDialogData): CardTooltipBottomSheet {
            return CardTooltipBottomSheet().apply {
                arguments = bundleOf(CARD_TOOL_TIP_DIALOG_DATA to data)
            }
        }
    }

    override fun providePageName(): String = TAG

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseBottomSheetDialog)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetCardTooltipBinding =
        BottomSheetCardTooltipBinding.inflate(layoutInflater)

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        (dialog?.findViewById(R.id.design_bottom_sheet) as? View)?.let {
            BottomSheetBehavior.from(it)
                .setState(BottomSheetBehavior.STATE_EXPANDED)
        }
        val data = arguments?.getParcelable<CardHelpDialogData>(CARD_TOOL_TIP_DIALOG_DATA)

        binding.apply {
            back.setOnClickListener { this@CardTooltipBottomSheet.dismiss() }
            close.setOnClickListener { this@CardTooltipBottomSheet.dismiss() }
            tvTitle.text = data?.title
            tvSubTitle.text = data?.subtitle
            ivCardImage.loadImageEtx(data?.imageUrl.orEmpty())
        }
    }
}