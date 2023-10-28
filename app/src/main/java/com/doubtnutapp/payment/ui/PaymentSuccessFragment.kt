package com.doubtnutapp.payment.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.apxor.androidsdk.core.Attributes
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnutapp.Constants
import com.doubtnutapp.MainActivity
import com.doubtnutapp.R
import com.doubtnutapp.show
import com.doubtnutapp.transactionhistory.TransactionHistoryActivityV2
import com.doubtnutapp.utils.ApxorUtils
import kotlinx.android.synthetic.main.fragment_payment_success.*

/**
 * Created by Anand Gaurav on 2019-12-14.
 */
class PaymentSuccessFragment : Fragment() {

    companion object {
        private const val TAG = "PaymentSuccessFragment"
        fun newInstance(): PaymentSuccessFragment {
            return PaymentSuccessFragment()
        }
    }

    var orderId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_payment_success, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let { currentActivity ->
            if (currentActivity.intent.hasExtra("transactionId")) {
                orderId = currentActivity.intent.getStringExtra("transactionId").orEmpty()
                if (orderId.isNullOrBlank().not()) {
                    textViewTransactionId.text = "Order Id : $orderId"
                }
            }
        }

        activity?.let { currentActivity ->
            if (currentActivity.intent.hasExtra("amount")) {
                val currentAmount = currentActivity.intent.getStringExtra("amount")
                var currencySymbol = currentActivity.intent.getStringExtra(PaymentStatusActivity.CURRENCY_SYMBOL).orEmpty()
                if (currencySymbol.isBlank()) {
                    currencySymbol = "₹"
                }
                if (currentAmount.isNullOrBlank().not()) {
                    textViewAmount.text = currencySymbol + currentAmount
                }
            }

            if (currentActivity.intent.hasExtra("paymentFor") && currentActivity.intent.getStringExtra(
                    "paymentFor"
                ) == "bounty"
            ) {
                if (currentActivity.intent.hasExtra("type") && currentActivity.intent.getStringExtra(
                        "type"
                    ) == "DEBIT"
                ) {
                    textViewInfo.text = "Paid for Doubt"
                } else {
                    textViewInfo.text = "Won for accepted answer"
                }
            }
        }

        var isForDoubt = false

        if (activity!!.intent!!.hasExtra("paymentFor")
            && activity!!.intent!!.getStringExtra("paymentFor") == "doubt"
        ) {
            isForDoubt = true
            buttonAskDoubt.show()
        }

        imageViewClose.setOnClickListener {
            if (isForDoubt) {
                startActivity(Intent(activity!!, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            } else {
                activity?.onBackPressed()
            }
        }

        buttonAskDoubt.setOnClickListener {
            activity?.let { currentActivity ->
                val intent = Intent(currentActivity, MainActivity::class.java)
                intent.action = Constants.NAVIGATE_CAMERA_SCREEN
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                this.startActivity(intent)
            }
        }

        activity?.let { currentActivity ->
            if (isForDoubt) {
                view?.isFocusableInTouchMode = true
                view?.requestFocus()
                view?.setOnKeyListener(object : View.OnKeyListener {
                    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.action === KeyEvent.ACTION_UP) {
                            val manager = fragmentManager
                            val backStackEntryCount = manager!!.backStackEntryCount
                            if (backStackEntryCount == 0) {
                                activity?.let { currentActivity ->
                                    startActivity(
                                        Intent(
                                            currentActivity,
                                            MainActivity::class.java
                                        ).apply {
                                            flags =
                                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        })
                                }
                            }
                            return true
                        }
                        return false
                    }
                })
            }
        }

        layoutMail.setOnClickListener {
            try {
                if (isPackageExisted("com.google.android.gm")) {
                    val intent = Intent(android.content.Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    val pm = activity!!.packageManager
                    val matches = pm.queryIntentActivities(intent, 0)
                    var best: ResolveInfo? = null
                    for (info in matches)
                        if (info.activityInfo.packageName.endsWith(".gm") || info.activityInfo.name.toLowerCase()
                                .contains("gmail")
                        )
                            best = info
                    if (best != null)
                        intent.setClassName(best.activityInfo.packageName, best.activityInfo.name)
                            .putExtra(Intent.EXTRA_TEXT, orderId)
                            .data = Uri.parse("payments@doubtnut.com")
                    startActivity(intent)
                } else {
                    showToastMessage("Gmail is not installed on your device")
                }
            } catch (e: Exception) {
                showToastMessage("Gmail is not installed on your device")
            }
        }

        textViewPaymentHistory.setOnClickListener {
            context?.let { currentContext ->
                ApxorUtils.logAppEvent(
                    EventConstants.EVENT_NAME_PAYMENT_HISTORY_BUTTON_CLICK,
                    Attributes().apply {
                        putAttribute(EventConstants.SOURCE, EventConstants.EVENT_NAME_SUCCESS_PAGE)
                    })
                startActivity(TransactionHistoryActivityV2.getStartIntent(currentContext))
            }
        }
    }

    private fun isPackageExisted(targetPackage: String): Boolean {
        val pm = activity?.packageManager
        try {
            val info = pm?.getPackageInfo(targetPackage, PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }

        return true
    }

    private fun showToast(@StringRes resId: Int) {
        showToastMessage(getString(resId))
    }

    private fun showToastMessage(message: String) {
        toast(message)
    }

}