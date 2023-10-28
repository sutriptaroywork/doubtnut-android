package com.doubtnutapp.ui.test

import android.app.Dialog
import android.content.Context
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import com.doubtnutapp.R
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.SheetLoginDialogBinding
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnut.core.utils.toast
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.ui.onboarding.viewmodel.MobileVerifyViewModel
import com.doubtnutapp.ui.onboarding.SMSBroadcastReceiver
import com.doubtnutapp.ui.onboarding.viewmodel.VerifyOtpViewModel
import com.doubtnutapp.widgets.GreyOtpView
import com.google.android.gms.auth.api.phone.SmsRetriever
import dagger.android.support.AndroidSupportInjection
import io.reactivex.annotations.NonNull

class LoginDialog : BaseBindingDialogFragment<MobileVerifyViewModel, SheetLoginDialogBinding>() {

    companion object {
        const val TAG = "LoginDialog"
        fun newInstance(): LoginDialog {
            return LoginDialog()
        }
    }

    private lateinit var viewModelVerifyOtp: VerifyOtpViewModel
    private var sessionId: String = ""
    private val smsBroadcastReceiver by lazy { SMSBroadcastReceiver() }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.sheet_login_dialog, null)
        builder.setCancelable(false)
        configureDialogActionsButtons(view)
        viewModelVerifyOtp =
            ViewModelProviders.of(this, viewModelFactory).get(VerifyOtpViewModel::class.java)
        return builder.create()
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    private fun configureDialogActionsButtons(view: View) {
        view.findViewById<Button>(R.id.buttonSendOtp).setOnClickListener {
            if (isValidPhoneNumber(view)) {
                getOtp(view)
            } else {
                toast(getString(R.string.enter_valid_number))
            }
        }

        view.findViewById<Button>(R.id.btnVerifyOtp).setOnClickListener {
            if (mBinding?.otpview?.hasValidOTP() == true) {
                verifyOtpQuiz(it, sessionId, mBinding?.otpview?.otp.toString())
                view.findViewById<ProgressBar>(R.id.progressbar).visibility = View.VISIBLE
                view.findViewById<Button>(R.id.btnVerifyOtp).isClickable = false
            } else {
                toast(getString(R.string.enter_valid_otp))
                view.findViewById<Button>(R.id.btnVerifyOtp).isClickable = true
            }
        }

        view.findViewById<TextView>(R.id.tvResendOtp).setOnClickListener {
            view.findViewById<LinearLayout>(R.id.ll_enter_number).visibility = View.VISIBLE
            view.findViewById<LinearLayout>(R.id.ll_verify_otp).visibility = View.GONE
        }

        view.findViewById<ImageView>(R.id.ivClose).setOnClickListener {
            dialog?.dismiss()
        }
    }

    private fun configureNotificationEducationView() {
        val displayMetrics = DisplayMetrics()

        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels - 100
        val height = (displayMetrics.heightPixels * .60).toInt()
        //changing dialog height
        dialog?.window?.setLayout(width, height)
        //making dialog background transparent
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onStart() {
        super.onStart()
        configureNotificationEducationView()
    }

    private fun isValidPhoneNumber(view: View): Boolean {
        val length = mBinding?.phoneNumber?.text?.length
        return length == 10
    }

    private fun getOtp(view: View) {
        activity?.also {
            viewModel.getotp(mBinding?.phoneNumber?.text.toString(), it).observe(this, { response ->
                when (response) {
                    is Outcome.Progress -> {
                        view.findViewById<ProgressBar>(R.id.progressbar).visibility = View.VISIBLE
                    }
                    is Outcome.Failure -> {
                        view.findViewById<ProgressBar>(R.id.progressbar).visibility = View.GONE
                        val dialog = NetworkErrorDialog.newInstance()
                        dialog.show(requireFragmentManager(), "NetworkErrorDialog")
                    }
                    is Outcome.ApiError -> {
                        view.findViewById<ProgressBar>(R.id.progressbar).visibility = View.GONE
                        toast(getString(R.string.api_error))

                    }
                    is Outcome.BadRequest -> {
                        view.findViewById<ProgressBar>(R.id.progressbar).visibility = View.GONE
                        val dialog = BadRequestDialog.newInstance("unauthorized")
                        dialog.show(requireFragmentManager(), "BadRequestDialog")
                    }
                    is Outcome.Success -> {
                        view.findViewById<ProgressBar>(R.id.progressbar).visibility = View.GONE
                        sessionId = response.data.data.session_id
                        verifyOtp(
                            view,
                            sessionId,
                            view.findViewById<EditText>(R.id.phone_number).text.toString()
                        )
                    }
                }
            })
        }
    }

    private fun verifyOtp(view: View, sessionId: String, mobileNumber: String) {

        view.findViewById<LinearLayout>(R.id.ll_verify_otp)?.show()
        view.findViewById<LinearLayout>(R.id.ll_enter_number)?.hide()
        view.findViewById<TextView>(R.id.tvVerify_otp).text =
            """$mobileNumber पर भेजे गए Verification Code को यहाँ Type करें"""

        val client = SmsRetriever.getClient(requireActivity())
        val retriever = client.startSmsRetriever()
        retriever.addOnSuccessListener {
            val otpListener = object : SMSBroadcastReceiver.OTPListener {
                override fun onOTPReceived(otp: String) {
                    view.findViewById<GreyOtpView>(R.id.otpview).otp = otp
                    verifyOtpQuiz(view, sessionId, view.findViewById<GreyOtpView>(R.id.otpview).otp)

                }

                override fun onOTPTimeOut() {
                }
            }
            smsBroadcastReceiver.injectOTPListener(otpListener)
            requireActivity().applicationContext.registerReceiver(
                smsBroadcastReceiver,
                IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
            )
        }
        retriever.addOnFailureListener {
        }
    }

    private fun verifyOtpQuiz(view: View, sessionId: String, otp: String) {
        viewModelVerifyOtp.publishOnVerifyClickEvent(TAG)
        viewModelVerifyOtp.verifyOTP(sessionId, otp).observe(viewLifecycleOwner, { response ->

            val progressBar = view?.findViewById<ProgressBar>(R.id.progressbar)
            when (response) {
                is Outcome.Progress -> {
                    progressBar?.show()
                }
                is Outcome.Failure -> {
                    progressBar?.hide()
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(requireFragmentManager(), "NetworkErrorDialog")
                }
                is Outcome.ApiError -> {
                    progressBar?.hide()
                    toast(getString(R.string.invalidate_otp))
                }
                is Outcome.Success -> {
                    viewModelVerifyOtp.publishOnOtpSubmitSuccessEvent(TAG)
                    progressBar?.hide()

                    viewModelVerifyOtp.unSetFirebaseRegIdPref()
                    viewModelVerifyOtp.onOtpVerificationSuccess(response.data.data)
                    viewModelVerifyOtp.setUserLoggedIn()
                    viewModelVerifyOtp.updateFCMRegId(true)
                    (activity as TestQuestionActivity).onLoginDone()

                    dialog?.dismiss()
                }
            }
        })
    }

    override fun onDetach() {
        try {
            if (smsBroadcastReceiver != null) requireActivity().applicationContext.unregisterReceiver(
                smsBroadcastReceiver
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDetach()

    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): SheetLoginDialogBinding {
        return SheetLoginDialogBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): MobileVerifyViewModel {
        return ViewModelProviders.of(this, viewModelFactory).get(MobileVerifyViewModel::class.java)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

    }

}
