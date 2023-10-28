package com.doubtnutapp.paymentplan.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.base.OnUpiCollectPaymentClick
import com.doubtnutapp.databinding.BottomSheetUpiBinding
import com.doubtnutapp.disable
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.paymentplan.widgets.CheckoutV2DialogData
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior

class UPIBottomSheet :
    BaseBindingBottomSheetDialogFragment<DummyViewModel, BottomSheetUpiBinding>() {

    companion object {
        const val TAG = "UPIBottomSheet"
        const val UPI_DIALOG_DATA = "upi_dialog_data"

        fun newInstance(data: CheckoutV2DialogData?): UPIBottomSheet {
            return UPIBottomSheet().apply {
                arguments = bundleOf(UPI_DIALOG_DATA to data)
            }
        }
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetUpiBinding =
        BottomSheetUpiBinding.inflate(layoutInflater)

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    private var actionPerformer: ActionPerformer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseBottomSheetDialog)
    }

    fun setActionListener(actionPerformer: ActionPerformer) {
        this.actionPerformer = actionPerformer
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        (dialog?.findViewById(R.id.design_bottom_sheet) as? View)?.let {
            BottomSheetBehavior.from(it)
                .setState(BottomSheetBehavior.STATE_EXPANDED)
        }
        val data = arguments?.getParcelable<CheckoutV2DialogData>(UPI_DIALOG_DATA)

        binding.title.text = data?.title
        binding.subtitle.text = data?.subtitle
        binding.button.text = data?.buttonText
        binding.button.disable()
        binding.button.setBackgroundColor(resources.getColor(R.color.grey_cbcbcb))

        binding.upiId.hint = data?.upiData?.title

        binding.upiId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isValidUpi = if (binding.upiId.text?.isBlank() == true) {
                    false
                } else {
                    binding.upiId.text?.matches(Regex("[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}"))
                }

                isValidUpi?.let {
                    binding.button.isEnabled = it
                    if (it)
                        binding.button.setBackgroundColor(resources.getColor(R.color.orange_eb532c))
                    else
                        binding.button.setBackgroundColor(resources.getColor(R.color.grey_cbcbcb))
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.button.setOnClickListener {
            actionPerformer?.performAction(
                OnUpiCollectPaymentClick(
                    "upi_collect",
                    binding.upiId.text.toString()
                )
            )
        }

        binding.close.setOnClickListener {
            dismiss()
        }
    }
}