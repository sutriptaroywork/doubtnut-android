package com.doubtnutapp.live.ui

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.feed.FeedPostItem
import com.doubtnutapp.databinding.DialogJoinLivePaymentBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.paymentv2.ui.PaymentActivity
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.utils.showToast
import com.doubtnut.core.utils.viewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class JoinLivePaymentDialog(
    private val livePostItem: FeedPostItem,
    private val paymentSuccessListener: () -> Unit
) : BaseBindingBottomSheetDialogFragment<DummyViewModel, DialogJoinLivePaymentBinding>() {

    companion object {
        const val PAYMENT_REQUEST_CODE = 101
        const val TAG = "JoinLivePaymentDialog"

    }

    private var mBehavior: BottomSheetBehavior<*>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        val v = View.inflate(context, R.layout.dialog_join_live_payment, null)

        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        mBehavior = BottomSheetBehavior.from(v.parent as View)

        mBinding?.btnStartPayment?.text = "Pay ₹ ${livePostItem.streamFee} and join now"
        mBinding?.btnStartPayment?.setOnClickListener {
            startPayment()
        }
        if (livePostItem.isLive) {
            mBinding?.tvViewerCount?.text = "${livePostItem.viewerCount} watching"
        } else {
            mBinding?.tvViewerCount?.text = "${livePostItem.bookedCount} booked"
        }

        mBinding?.tvTopic?.text = livePostItem.message
        mBinding?.ivProfileImage?.loadImage(livePostItem.studentImageUrl)
        mBinding?.tvProfileName?.text = livePostItem.username

        mBinding?.btnClose?.setOnClickListener {
            dismiss()
        }

        return dialog
    }

    private fun startPayment() {
        /*SelectPaymentMethodDialog {
            val paymentIntent: Intent
            val paymentParams = hashMapOf<String, String>().apply {
                put("payment_for", "livepost")
                put("payment_for_id", livePostItem.id)
            }

            if (it == SelectPaymentMethodDialog.PAYMENT_METHOD_PAYTM) {
                paymentIntent = PaymentActivity.intentForPaytm(activity!!,
                        livePostItem.streamFee.toString(),
                        params = paymentParams)

            } else if (it == SelectPaymentMethodDialog.PAYMENT_METHOD_RAZORPAY) {

                paymentIntent = PaymentActivity.intentForRazorpay(activity!!,
                        livePostItem.streamFee.toString(),
                        params = paymentParams)

            } else throw IllegalArgumentException("Invalid payment mode selected")

            startActivityForResult(paymentIntent, PAYMENT_REQUEST_CODE)
        }.show(childFragmentManager, "SelectPaymentMethodDialog")*/
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PAYMENT_REQUEST_CODE) {
            if (resultCode == PaymentActivity.PAYMENT_SUCCESS_RETURN_CODE) {
                handlePaymentSuccess()
            } else {
                showToast(context, "Error completing payment")
            }
        }
    }

    private fun handlePaymentSuccess() {
        paymentSuccessListener()
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        mBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogJoinLivePaymentBinding {
        return DialogJoinLivePaymentBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

    }
}