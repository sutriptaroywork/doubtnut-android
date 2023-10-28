package com.doubtnutapp.paymentplan.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.DialogFragment
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.base.OnCardPaymentClick
import com.doubtnutapp.databinding.BottomSheetCardBinding
import com.doubtnutapp.disable
import com.doubtnutapp.domain.payment.entities.CardDetails
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.enable
import com.doubtnutapp.paymentplan.widgets.CheckoutV2DialogData
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.razorpay.Razorpay
import java.util.*

/**
 * Created by Mehul Bisht on 14-10-2021
 */

class CardBottomSheet :
    BaseBindingBottomSheetDialogFragment<DummyViewModel, BottomSheetCardBinding>() {

    companion object {
        const val TAG = "CardBottomSheet"
        const val CARD_DIALOG_DATA = "card_dialog_data"

        fun newInstance(data: CheckoutV2DialogData?): CardBottomSheet {
            return CardBottomSheet().apply {
                arguments = bundleOf(CARD_DIALOG_DATA to data)
            }
        }
    }

    private var actionPerformer: ActionPerformer? = null

    override fun providePageName(): String = TAG

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseBottomSheetDialog)
    }

    fun setActionListener(actionPerformer: ActionPerformer) {
        this.actionPerformer = actionPerformer
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetCardBinding =
        BottomSheetCardBinding.inflate(layoutInflater)

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        (dialog?.findViewById(R.id.design_bottom_sheet) as? View)?.let {
            BottomSheetBehavior.from(it)
                .setState(BottomSheetBehavior.STATE_EXPANDED)
        }
        val data = arguments?.getParcelable<CheckoutV2DialogData>(CARD_DIALOG_DATA)
        val razorpay = Razorpay(
            requireActivity(),
            binding.root.context.getString(R.string.RAZORPAY_API_KEY)
        )

        binding.title.text = data?.title
        binding.subtitle.text = data?.subtitle
        binding.button.text = data?.buttonText

        binding.etCardNumber.hint = data?.cardData?.info?.cardNoHint
        binding.etNameOnCard.hint = data?.cardData?.info?.nameHint
        binding.etExpiry.hint = data?.cardData?.info?.expiryHint
        binding.etCVV.hint = data?.cardData?.info?.cvvHint
        binding.cvvTooltip.setOnClickListener {
            CardTooltipBottomSheet.newInstance(
                data?.cardData?.help ?: return@setOnClickListener
            ).show(parentFragmentManager, CardTooltipBottomSheet.TAG)
        }

        binding.button.disable()
        binding.button.setBackgroundColor(resources.getColor(R.color.grey_cbcbcb))

        val cardDetails = CardDetails()

        binding.etCardNumber.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(editable: Editable) {

                if (editable.isNotEmpty() && editable.length % 5 == 0) {
                    val c = editable[editable.length - 1]
                    if (c == ' ') {
                        editable.delete(editable.length - 1, editable.length)
                    }
                }
                if (editable.isNotEmpty() && editable.length % 5 == 0) {
                    val c = editable[editable.length - 1]
                    if (Character.isDigit(c) && TextUtils.split(
                            editable.toString(),
                            " "
                        ).size <= 4
                    ) {
                        editable.insert(editable.length - 1, " ")
                    }
                }


                if (editable.toString()
                        .isNotEmpty() && editable.toString().length >= 19
                ) {
                    var cardNo = ""
                    editable.toString().forEach {
                        if (it.isDigit())
                            cardNo += it
                    }
                    val status = razorpay.isValidCardNumber(cardNo)
                    if (status) {
                        cardDetails.cardNo = cardNo
                        if (cardDetails.cardNo.isNotEmpty() && cardDetails.name.isNotEmpty() && cardDetails.CVV.isNotEmpty() && cardDetails.expiry.isNotEmpty()) {
                            binding.button.enable()
                            binding.button.setBackgroundColor(resources.getColor(R.color.orange_eb532c))
                        } else {
                            binding.button.disable()
                            binding.button.setBackgroundColor(resources.getColor(R.color.grey_cbcbcb))
                        }
                    } else {
                        cardDetails.cardNo = ""
                        binding.etCardNumber.error = "Invalid"
                        binding.button.disable()
                        binding.button.setBackgroundColor(resources.getColor(R.color.grey_cbcbcb))
                    }
                } else {
                    cardDetails.cardNo = ""
                    binding.button.disable()
                    binding.button.setBackgroundColor(resources.getColor(R.color.grey_cbcbcb))
                }
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

        })

        binding.etExpiry.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun afterTextChanged(editable: Editable) {

                if (editable.isNotEmpty() && editable.length % 3 == 0) {
                    val c = editable[editable.length - 1]
                    if (c == '/') {
                        editable.delete(editable.length - 1, editable.length)
                    }
                }
                if (editable.isNotEmpty() && editable.length % 3 == 0) {
                    val c = editable[editable.length - 1]
                    if (Character.isDigit(c) && TextUtils.split(
                            editable.toString(),
                            "/"
                        ).size <= 2
                    ) {
                        editable.insert(editable.length - 1, "/")
                    }
                }

                if (editable.length == 5) {
                    val text = editable.toString()
                    if (!text.substring(0, 2).isDigitsOnly() || !text.substring(
                            3,
                            5
                        ).isDigitsOnly()
                    ) {
                        binding.etExpiry.error = "Invalid"
                        return
                    }
                    val mon = text.substring(0, 2).toInt()
                    val year = text.substring(3, 5).toInt()
                    val c = Calendar.getInstance()
                    if (mon in 1..12 && year >= c.get(Calendar.YEAR).rem(100)) {
                        if (year == 21 && mon < 11) {
                            binding.etExpiry.error = "Invalid"
                        } else {
                            cardDetails.expiry = text
                            if (cardDetails.cardNo.isNotEmpty() && cardDetails.name.isNotEmpty() && cardDetails.CVV.isNotEmpty() && cardDetails.expiry.isNotEmpty()) {
                                binding.button.enable()
                                binding.button.setBackgroundColor(resources.getColor(R.color.orange_eb532c))
                            } else {
                                binding.button.disable()
                                binding.button.setBackgroundColor(resources.getColor(R.color.grey_cbcbcb))
                                if (cardDetails.cardNo.isEmpty()) {
                                    binding.etCardNumber.requestFocus()
                                } else if (cardDetails.name.isEmpty()) {
                                    binding.etNameOnCard.requestFocus()
                                } else if (cardDetails.CVV.isEmpty()) {
                                    binding.etCVV.requestFocus()
                                }
                            }
                        }
                    } else {
                        binding.etExpiry.error = "Invalid"
                    }
                } else {
                    binding.button.disable()
                    binding.button.setBackgroundColor(resources.getColor(R.color.grey_cbcbcb))
                    cardDetails.expiry = ""
                }
            }
        })

        binding.etCVV.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                val cardNetwork =
                    razorpay.getCardNetwork(cardDetails.cardNo).orEmpty()
                val cvvLength = if (cardNetwork == "amex") 4 else 3

                if (s.toString().isNotEmpty() && s.toString().length >= cvvLength) {
                    cardDetails.CVV = s.toString()
                    if (cardDetails.cardNo.isNotEmpty() && cardDetails.name.isNotEmpty() && cardDetails.CVV.isNotEmpty() && cardDetails.expiry.isNotEmpty()) {
                        binding.button.enable()
                        binding.button.setBackgroundColor(resources.getColor(R.color.orange_eb532c))
                    } else {
                        if (cardDetails.cardNo.isEmpty()) {
                            binding.etCardNumber.requestFocus()
                        } else if (cardDetails.name.isEmpty()) {
                            binding.etNameOnCard.requestFocus()
                        } else if (cardDetails.expiry.isEmpty()) {
                            binding.etExpiry.requestFocus()
                        }
                        binding.button.disable()
                        binding.button.setBackgroundColor(resources.getColor(R.color.grey_cbcbcb))
                    }
                } else {
                    cardDetails.CVV = ""
                    binding.button.disable()
                    binding.button.setBackgroundColor(resources.getColor(R.color.grey_cbcbcb))
                }
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

        })

        binding.etNameOnCard.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                if (s.toString().isNotEmpty()) {
                    cardDetails.name = s.toString()
                    if (cardDetails.cardNo.isNotEmpty() && cardDetails.name.isNotEmpty() && cardDetails.CVV.isNotEmpty() && cardDetails.expiry.isNotEmpty()) {
                        binding.button.enable()
                        binding.button.setBackgroundColor(resources.getColor(R.color.orange_eb532c))
                    }
                } else {
                    binding.button.disable()
                    binding.button.setBackgroundColor(resources.getColor(R.color.grey_cbcbcb))
                    cardDetails.name = ""
                }
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

        })

        binding.button.setOnClickListener {
            actionPerformer?.performAction(OnCardPaymentClick(cardDetails))
        }

        binding.close.setOnClickListener {
            dismiss()
        }
    }
}