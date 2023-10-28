package com.doubtnutapp.paymentv2.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.events.Dismiss
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.Constants.POST_PURCHASE_SESSION_COUNT
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.ActivityPaymentV2Binding
import com.doubtnutapp.domain.payment.entities.*
import com.doubtnutapp.packageInstallerCheck.CheckForPackageInstall
import com.doubtnutapp.paymentv2.PaymentViewModel
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.vipplan.ui.CheckoutFragment.Companion.CARD
import com.doubtnutapp.vipplan.ui.CheckoutFragment.Companion.NETBANKING
import com.doubtnutapp.vipplan.ui.CheckoutFragment.Companion.UPI
import com.doubtnutapp.vipplan.ui.CheckoutFragment.Companion.UPI_COLLECT
import com.doubtnutapp.vipplan.ui.CheckoutFragment.Companion.UPI_SELECT
import com.doubtnutapp.vipplan.ui.CheckoutFragment.Companion.WALLET
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import com.razorpay.Razorpay
import com.razorpay.ValidationListener
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_payment_v2.*
import kotlinx.android.synthetic.main.dialog_payment_discount_timer.*
import kotlinx.android.synthetic.main.widget_coupon_banner.view.*
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/*
 Activity to initiate and complete payment transactions through multiple providers
 Use the helper companion methods to get intent for different providers
 Can also add extra data that needs to be passed to our server in extraParams (e.g. payment_for: 'bounty', payment_for_id: 'bounty_id')

 Returns one of the following result code PaymentActivity.PAYMENT_SUCCESS_RETURN_CODE or PaymentActivity.PAYMENT_FAILURE_RETURN_CODE

 The following data is returned back:
    - All the initial data used to start this activity (source, amount, extraParams)
    - All the data returned by payment provider
    - Data returned from our server by verifying the transaction (order_id)
    - [SOURCE, AMOUNT, EXTRA_PARAMS, ORDER_ID, PROVIDER_DATA]
 */
class PaymentActivity : AppCompatActivity(),

/* razorpay callback */ PaymentResultWithDataListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var checkForPackageInstall: CheckForPackageInstall

    private lateinit var viewModel: PaymentViewModel

    private var source: String? = ""
    private var amount: String? = ""
    private var paymentSource: String? = ""

    private var finalOrderId = ""

    private val PAYTM_REQUEST_CODE = 101

    private var providerData: Bundle? = null

    //complete
    private var serverResponsePayment: ServerResponsePayment? = null

    //start
    private var taxationData: Taxation? = null

    private var paymentActivityBody: PaymentActivityBody? = null

    private var razorpay: Razorpay? = null

    private var isPaymentSuccessBottomSheetShown: Boolean = false
    private var paymentSuccessBottomSheetReturnIntent: Intent? = null

    private var alertDialog: AlertDialog? = null
    private var timer: CountDownTimer? = null

    companion object {
        const val TAG = "PaymentActivity"

        const val SOURCE = "source"
        const val AMOUNT = "amount"
        const val REASON = "reason"
        const val EXTRA_PARAMS = "params"

        const val SOURCE_PAYTM = "PAYTM"
        const val SOURCE_RAZORPAY = "RAZORPAY"

        const val STATUS_PAYMENT_SUCCESS = "SUCCESS"

        const val PAYMENT_SUCCESS_RETURN_CODE = 1
        const val PAYMENT_FAILURE_RETURN_CODE = 2
        const val PROVIDER_DATA = "provider_data"
        const val ORDER_ID = "order_id"

        const val PAYMENT_DATA = "payment_data"

        var payload = JSONObject("{currency: 'INR'}")

        fun getPaymentIntent(context: Context, paymentActivityData: PaymentActivityBody) =
            Intent(context, PaymentActivity::class.java).apply {
                putExtra(PAYMENT_DATA, paymentActivityData)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.grey_statusbar_color)
        DoubtnutApp.INSTANCE.bus()?.send(Dismiss(CourseBottomSheetDialogFragment.TAG))

        val binding = DataBindingUtil.setContentView<ActivityPaymentV2Binding>(
            this,
            R.layout.activity_payment_v2
        )
        viewModel = viewModelProvider(viewModelFactory)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        toolbarTitle.text = "Complete payment"
        setUpObserver()


        paymentActivityBody = intent.getParcelableExtra(PAYMENT_DATA)
        viewModel.fetchPaymentInitialInfo(paymentActivityBody?.paymentStartBody!!)

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.PAYMENT_CHECKOUT_CUSTOM_START,
                hashMapOf(
                    "method" to paymentActivityBody?.method.orEmpty(),
                    "method_type" to paymentActivityBody?.type.orEmpty(),
                    "variant_id" to paymentActivityBody?.paymentStartBody?.paymentStartInfo?.variantId.orEmpty(),
                    "payment_for" to paymentActivityBody?.paymentStartBody?.paymentFor.orEmpty()
                ), ignoreSnowplow = true
            )
        )
        setUpClickListeners()

        razorpay = Razorpay(this, getString(R.string.RAZORPAY_API_KEY))
        razorpay?.setWebView(webView)
    }

    override fun onBackPressed() {
        viewModel.fetchPaymentDiscountInfo(finalOrderId)
    }

    fun onUserBackHandling() {
        razorpay?.onBackPressed()
        webView?.visibility = View.GONE
        completeTransactionWithResult(false)
        if (isPaymentSuccessBottomSheetShown) {
            setResult(PAYMENT_SUCCESS_RETURN_CODE, paymentSuccessBottomSheetReturnIntent)
        }
        finish()
    }

    private fun showPaymentDiscountTimer(paymentDiscount: PaymentDiscount) {
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater: LayoutInflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_payment_discount_timer, null)
        dialogBuilder.setView(dialogView)

        val tvTimer: TextView = dialogView.findViewById<View>(R.id.tvtime) as TextView
        val title: TextView = dialogView.findViewById<View>(R.id.tvTitleDiscount) as TextView
        val tvSubTitleDiscount: TextView =
            dialogView.findViewById<View>(R.id.tvSubTitleDiscount) as TextView
        val tvDiscountPayment: TextView =
            dialogView.findViewById<View>(R.id.tvDiscountPayment) as TextView
        val tvNewPrice: TextView = dialogView.findViewById<View>(R.id.tvNewPrice) as TextView
        val tvNewAmount: TextView = dialogView.findViewById<View>(R.id.tvNewAmount) as TextView
        val tvOldAmount: TextView = dialogView.findViewById<View>(R.id.tvOldAmount) as TextView
        val tvValidOnly: TextView = dialogView.findViewById<View>(R.id.tvValidOnly) as TextView
        val btnContinueWithDiscount: Button =
            dialogView.findViewById<View>(R.id.btnContinueWithDiscount) as Button
        val btnCloseDiscount: ImageView =
            dialogView.findViewById<View>(R.id.btnCloseDiscount) as ImageView

        title.text = paymentDiscount.title.orEmpty()
        tvSubTitleDiscount.text = paymentDiscount.subtitle.orEmpty()
        tvDiscountPayment.text = paymentDiscount.discount.orEmpty()
        tvNewPrice.text = paymentDiscount.label.orEmpty()
        tvNewAmount.text = paymentDiscount.newAmount.orEmpty()
        tvOldAmount.text = paymentDiscount.oldAmount.orEmpty()
        tvOldAmount.paintFlags = (tvOldAmount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG)
        tvValidOnly.text = paymentDiscount.validityText.orEmpty()
        btnContinueWithDiscount.text = paymentDiscount.buttonText.orEmpty()

        alertDialog = dialogBuilder.create()
        alertDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.setCancelable(false)
        alertDialog?.show()
        btnCloseDiscount.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.PAYMENT_CHECKOUT_DISCOUNT_WIDGET_CLOSED,
                    hashMapOf(
                        EventConstants.ASSORTMENT_ID to taxationData?.assortmentId.orEmpty()
                    )
                )
            )
            timer?.cancel()
            alertDialog?.dismiss()
            onUserBackHandling()
        }

        btnContinueWithDiscount.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.PAYMENT_CHECKOUT_DISCOUNT_WIDGET_CONTINUE_BUYING_CLICKED,
                    hashMapOf(
                        EventConstants.ASSORTMENT_ID to taxationData?.assortmentId.orEmpty()
                    )
                )
            )
            paymentSource = EventConstants.SOURCE_RZP_BACKPRESS_WIDGET
            viewModel.fetchContinuePaymentInfo(paymentDiscount.paymentId!!)
            timer?.cancel()
        }
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.PAYMENT_CHECKOUT_DISCOUNT_WIDGET_SHOWN,
                hashMapOf(
                    EventConstants.ASSORTMENT_ID to taxationData?.assortmentId.orEmpty()
                )
            )
        )
        timer = getTimer(paymentDiscount.validityTime!!.toLong() * 1000, tvTimer)
        timer?.start()
    }

    private fun getTimer(time: Long, tvTimer: TextView): CountDownTimer {
        val timer = object : CountDownTimer(time, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = millisUntilFinished / 1000
                val minutesLeft = totalSeconds / 60
                val secondsLeft = totalSeconds - (minutesLeft * 60)
                if (minutesLeft < 1) {
                    tvTimer.text = totalSeconds.toString() + "s"
                } else {
                    tvTimer.text = minutesLeft.toString() + "m " + secondsLeft.toString() + "s"
                }

            }

            override fun onFinish() {
                cancel()
                alertDialog?.dismiss()
                onUserBackHandling()
            }
        }

        return timer
    }

    private fun setUpClickListeners() {
        buttonBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setUpObserver() {
        viewModel.messageStringIdLiveData.observe(this, EventObserver {
            showToast(it)
        })

        viewModel.transactionInfo.observe(this, EventObserver {
            if (alertDialog != null && alertDialog?.isShowing!!) {
                alertDialog?.dismiss()
            }
            finalOrderId = it.razorPayInfo.taxationId
            taxationData = it
            if (it.isWalletPayment == true) {
                completeTransactionWithResult(true)
            } else {
                beginRazorpayCheckout()
            }
        })

        viewModel.paymentDiscountInfo.observe(this, EventObserver {
            if (!it.discount.isNullOrEmpty()) {
                showPaymentDiscountTimer(it)
            } else {
                onUserBackHandling()
            }
        })

        viewModel.serverResponseOnPayment.observe(this, EventObserver {
            Log.e(TAG, "setUpObserver: $it")
            serverResponsePayment = it
            if (it.status.equals(STATUS_PAYMENT_SUCCESS, true)) {
                completeTransactionWithResult(true)
            } else {
                completeTransactionWithResult(false)
            }
        })
    }

    /*
   --------------------
    Razorpay checkout flow
   -------------------*/

    private fun beginRazorpayCheckout() {
        amount = taxationData?.razorPayInfo?.amount.orEmpty()
        source = "RAZORPAY"
//        source = paymentActivityBody?.method.orEmpty()

        try {
            val currency = taxationData?.razorPayInfo?.currency ?: "INR"
            payload.put("currency", currency)
            payload.put(
                "amount",
                (taxationData?.razorPayInfo?.amount.orEmpty().toInt() * 100).toString()
            )
            payload.put("contact", taxationData?.razorPayInfo?.phone)
            payload.put("email", taxationData?.razorPayInfo?.email)
            payload.put("order_id", taxationData?.razorPayInfo?.taxationId)
        } catch (e: Exception) {
            e.printStackTrace()
            completeTransactionWithResult(false)
            return
        }

        when (paymentActivityBody?.method) {
            CARD -> {
                beginCardCheckOut(paymentActivityBody?.cardDetails)
            }
            UPI -> {
                beginUPICheckOut()
            }
            NETBANKING -> {
                beginNetBankingCheckOut(paymentActivityBody?.type)
            }
            WALLET -> {
                beginWalletsCheckOut(paymentActivityBody?.type)
            }
            UPI_COLLECT -> {
                if (paymentActivityBody?.upi.isNullOrBlank()) {
                    beginUPICheckOut()
                } else {
                    beginCollectUPICheckOut(paymentActivityBody?.upi!!)
                }
            }
            UPI_SELECT -> {
                beginUPISelectCheckOut(paymentActivityBody?.upiPackage!!)
            }
            else -> {
                completeTransactionWithResult(false)
            }
        }
    }

    private fun beginCardCheckOut(data: CardDetails?) {
        try {
            payload.put("method", "card")
            payload.put("card[name]", data?.name)
            payload.put("card[number]", data?.cardNo)
            payload.put("card[expiry_month]", data?.expiry?.substring(0, 2))
            payload.put("card[expiry_year]", data?.expiry?.substring(3, 5))
            payload.put("card[cvv]", data?.CVV)
            sendRequest()
        } catch (e: Exception) {
            e.printStackTrace()
            completeTransactionWithResult(false)
        }
    }

    private fun beginUPICheckOut() {
        try {
            /**
             * Order of UPI apps preference on razorpay
             * */
            val jArray = JSONArray()
            jArray.put("net.one97.paytm")
            jArray.put("com.phonepe.app")
            jArray.put("com.google.android.apps.nbu.paisa.user")
            jArray.put("in.org.npci.upiapp")
            jArray.put("in.amazon.mShop.android.shopping")

            payload.put("display_logo", true)
            payload.put("description", "Doubtnut Payment")
            payload.put("method", "upi")
            payload.put("_[flow]", "intent")
            payload.put("preferred_apps_order", jArray)
            payload.put("other_apps_order", jArray)
            sendRequest()
        } catch (e: Exception) {
            e.printStackTrace()
            completeTransactionWithResult(false)
        }
    }

    private fun beginUPISelectCheckOut(upiPackageName: String) {
        try {
            /**
             * Order of UPI apps preference on razorpay
             * */
            payload.put("display_logo", true)
            payload.put("description", "Doubtnut Payment")
            payload.put("method", "upi")
            payload.put("_[flow]", "intent")
            if (checkForPackageInstall.appInstalled(upiPackageName)) {
                payload.put("upi_app_package_name", upiPackageName)
            } else {
                val jArray = JSONArray()
                jArray.put("net.one97.paytm")
                jArray.put("com.phonepe.app")
                jArray.put("com.google.android.apps.nbu.paisa.user")
                jArray.put("in.org.npci.upiapp")
                jArray.put("in.amazon.mShop.android.shopping")

                payload.put("preferred_apps_order", jArray)
                payload.put("other_apps_order", jArray)
            }
            sendRequest()
        } catch (e: Exception) {
            e.printStackTrace()
            completeTransactionWithResult(false)
        }
    }

    private fun beginCollectUPICheckOut(upi: String) {
        try {
            payload.put("display_logo", true)
            payload.put("description", "Doubtnut Payment")
            payload.put("method", "upi")
            payload.put("vpa", upi)
            sendRequest()
        } catch (e: Exception) {
            e.printStackTrace()
            completeTransactionWithResult(false)
        }
    }

    private fun beginNetBankingCheckOut(bankName: String?) {

        try {
            payload.put("method", "netbanking")
            payload.put("bank", bankName)
            sendRequest()
        } catch (e: Exception) {
            e.printStackTrace()
            completeTransactionWithResult(false)
        }

    }

    private fun beginWalletsCheckOut(walletName: String?) {
        try {
            payload.put("method", "wallet")
            payload.put("wallet", walletName)
            sendRequest()
        } catch (e: Exception) {
            e.printStackTrace()
            completeTransactionWithResult(false)
        }
    }

    private fun sendRequest() {

        razorpay?.validateFields(payload, object : ValidationListener {

            override fun onValidationError(p0: MutableMap<String, String>?) {
                completeTransactionWithResult(false)
            }

            override fun onValidationSuccess() {
                try {
                    webView?.visibility = View.VISIBLE
                    razorpay?.submit(payload, this@PaymentActivity)
                } catch (e: Exception) {
                    Log.e(TAG, "onValidationSuccess: ", e)
                    e.printStackTrace()
                    completeTransactionWithResult(false)
                }
            }
        })
    }

    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
        webView.visibility = View.GONE
        this.providerData = Bundle().apply {
            putString("razorpay_order_id", taxationData?.razorPayInfo?.taxationId)
        }
        verifyTransactionComplete()
    }

    override fun onPaymentSuccess(p0: String?, data: PaymentData?) {
        webView?.visibility = View.GONE
        if (data != null) {
            this.providerData = Bundle().apply {
                putString("razorpay_order_id", data.orderId)
                putString("razorpay_payment_id", data.paymentId)
                putString("razorpay_signature", data.signature)
            }
            verifyTransactionComplete()
        } else {
            completeTransactionWithResult(false)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PAYTM_REQUEST_CODE) {

            // save all payment provider data to be later returned to the caller
            this.providerData = data?.extras

            if (resultCode == Activity.RESULT_OK && data != null) {
                verifyTransactionComplete()
            } else {
                completeTransactionWithResult(false)
            }
        } else if (razorpay != null) {
            razorpay?.onActivityResult(requestCode, resultCode, data)
        }
    }

    /*
     verify payment with our server, args are the provider payment data
    */
    private fun verifyTransactionComplete() {
        if (providerData != null) {
            viewModel.submitPaymentSuccessToServer(this.source.orEmpty(), this.providerData!!)
        } else {
            completeTransactionWithResult(false)
        }
    }

    /*
      return result to caller by mering 3 data streams - initial intent data,
      payment provider data and transaction data from our server
     */
    private fun completeTransactionWithResult(success: Boolean) {
        // put initial intent data
        val returnIntent = intent

        // add payment provider data
        returnIntent.putExtra(PROVIDER_DATA, this.providerData)

        // add transaction info from our server
        returnIntent.putExtra(ORDER_ID, finalOrderId)
        if (!paymentSource.isNullOrEmpty()) {
            returnIntent.putExtra(SOURCE, paymentSource)
        }

        if (!amount.isNullOrEmpty()) {
            returnIntent.putExtra(AMOUNT, amount)
        }

        returnIntent.putExtra(REASON, serverResponsePayment?.reason.orEmpty())

        if (success) {
            if (taxationData?.assortmentType == "course" || serverResponsePayment?.assortmentType == "course") {
                val sessionCount = defaultPrefs().getInt(POST_PURCHASE_SESSION_COUNT, 0)
                if (sessionCount == 0) {
                    defaultPrefs().edit().putInt(POST_PURCHASE_SESSION_COUNT, 1).apply()
                }
                toolbarTitle.text = "Payment Complete"
                tvProcessing.setVisibleState(false)
                val assortmentType = "course"

                var assortmentId = taxationData?.assortmentId
                serverResponsePayment?.assortmentId?.let {
                    assortmentId = it
                }
                viewModel.joinTeacherStudyGroup(assortmentId.orEmpty(),taxationData?.assortmentType.orEmpty(),taxationData?.batchId.orEmpty())
                showPaymentSuccessBottomSheet(assortmentType, assortmentId, returnIntent)

            } else if (serverResponsePayment?.assortmentType == "resource_pdf" || serverResponsePayment?.assortmentType == "resource_video" ||
                taxationData?.assortmentType == "resource_pdf" || taxationData?.assortmentType == "resource_video"
            ) {

                toolbarTitle.text = "Payment Complete"
                tvProcessing.setVisibleState(false)
                var assortmentType = "resource_pdf"
                if (serverResponsePayment?.assortmentType == "resource_video" || taxationData?.assortmentType == "resource_video")
                    assortmentType = "resource_video"

                var assortmentId = taxationData?.assortmentId
                serverResponsePayment?.assortmentId?.let {
                    assortmentId = it
                }

                showPaymentSuccessBottomSheet(assortmentType, assortmentId, returnIntent)

            } else {
                var assortmentId = taxationData?.assortmentId
                serverResponsePayment?.assortmentId?.let {
                    assortmentId = it
                }
                viewModel.joinTeacherStudyGroup(assortmentId.orEmpty(),taxationData?.assortmentType.orEmpty(),taxationData?.batchId.orEmpty())
                showToastMessage("Payment Successful")
                setResult(PAYMENT_SUCCESS_RETURN_CODE, returnIntent)
                finish()
            }

        } else {
            setResult(PAYMENT_FAILURE_RETURN_CODE, returnIntent)
            finish()
        }
    }

    private fun showPaymentSuccessBottomSheet(
        assortmentType: String?,
        assortmentId: String?,
        returnIntent: Intent
    ) {
        val dialog = PaymentSuccessfulBottomSheet.newInstance(assortmentType, assortmentId)
        dialog.show(supportFragmentManager, PaymentSuccessfulBottomSheet.TAG)
        supportFragmentManager.executePendingTransactions()
        dialog.dialog?.setOnDismissListener {
            lifecycleScope.launchWhenResumed {
                dialog.dismiss()
                setResult(PAYMENT_SUCCESS_RETURN_CODE, returnIntent)
                finish()
            }
        }
        isPaymentSuccessBottomSheetShown = true
        paymentSuccessBottomSheetReturnIntent = returnIntent
    }

    private fun showToast(@StringRes resId: Int) {
        showToastMessage(getString(resId))
    }

    private fun showToastMessage(message: String) {
        toast(message)
    }

    override fun onDestroy() {
        if (alertDialog?.isShowing == true) {
            alertDialog?.dismiss()
        }
        if (timer != null) {
            timer?.cancel()
        }
        super.onDestroy()
    }
}