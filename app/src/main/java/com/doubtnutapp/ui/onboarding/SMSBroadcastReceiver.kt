package com.doubtnutapp.ui.onboarding

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.util.regex.Pattern

class SMSBroadcastReceiver : BroadcastReceiver() {

    private var otpReceiver: OTPListener? = null

    fun injectOTPListener(receiver: OTPListener?) {
        this.otpReceiver = receiver
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            val status =
                if (extras?.get(SmsRetriever.EXTRA_STATUS) != null) extras.get(SmsRetriever.EXTRA_STATUS) as Status else Status(
                    13
                )

            when (status.statusCode) {

                CommonStatusCodes.SUCCESS -> {

                    val message = extras?.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String

                    val pattern = Pattern.compile("\\d{4}")
                    val matcher = pattern.matcher(message)

                    if (matcher.find()) {
                        otpReceiver?.onOTPReceived(matcher.group(0))
                        return
                    }
                }
                CommonStatusCodes.TIMEOUT -> {
                    otpReceiver?.onOTPTimeOut()
                }
            }
        }
    }

    interface OTPListener {

        fun onOTPReceived(otp: String)

        fun onOTPTimeOut()
    }
}