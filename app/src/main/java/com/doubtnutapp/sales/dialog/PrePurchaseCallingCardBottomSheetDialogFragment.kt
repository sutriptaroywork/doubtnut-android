package com.doubtnutapp.sales.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentPrePurchaseCallingCardBottomSheetDialogBinding
import com.doubtnutapp.sales.PrePurchaseCallingCardModel2
import com.doubtnutapp.sales.event.PrePurchaseCallingCardDismiss
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.disposables.Disposable

class PrePurchaseCallingCardBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentPrePurchaseCallingCardBottomSheetDialogBinding

    private val prePurchaseCallingCardModel: PrePurchaseCallingCardModel2
        get() = requireArguments().getParcelable(KEY_PRE_PURCHASE_CALLING_CARD_MODEL)!!

    private val isCalledFromOnBackPressed: Boolean
        get() = requireArguments().getBoolean(KEY_IS_CALLED_FROM_ON_BACK_PRESSED)

    private var appStateObserver: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseBottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPrePurchaseCallingCardBottomSheetDialogBinding.inflate(
            inflater,
            container,
            false
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (prePurchaseCallingCardModel._data == null && prePurchaseCallingCardModel._widgetData == null) {
            dismissAllowingStateLoss()
            return
        }

        binding.prePurchaseCallingCard.bindWidget(
            binding.prePurchaseCallingCard.widgetViewHolder,
            prePurchaseCallingCardModel
        )

        appStateObserver = DoubtnutApp.INSTANCE
            .bus()?.toObservable()?.subscribe {
                when (it) {
                    is PrePurchaseCallingCardDismiss -> {
                        dismiss()
                        if (isCalledFromOnBackPressed) {
                            activity?.finish()
                        }
                    }
                }
            }
    }

    companion object {
        const val TAG = "PrePurchaseCallingCardBottomSheetDialogFragment"

        const val KEY_PRE_PURCHASE_CALLING_CARD_MODEL = "key_pre_purchase_calling_card_model"
        const val KEY_IS_CALLED_FROM_ON_BACK_PRESSED = "is_called_from_on_back_pressed"

        fun newInstance(
            prePurchaseCallingCardModel: PrePurchaseCallingCardModel2,
            isCalledFromOnBackPressed: Boolean
        ) =
            PrePurchaseCallingCardBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_PRE_PURCHASE_CALLING_CARD_MODEL, prePurchaseCallingCardModel)
                    putBoolean(KEY_IS_CALLED_FROM_ON_BACK_PRESSED, isCalledFromOnBackPressed)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appStateObserver?.dispose()
    }

}
