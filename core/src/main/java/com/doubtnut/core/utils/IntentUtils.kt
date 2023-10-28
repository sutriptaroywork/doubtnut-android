package com.doubtnut.core.utils

import android.content.Context
import android.widget.Toast

object IntentUtils {
    fun showCallActionNotPerformToast(context: Context, phoneNumber: String) {
        ToastUtils.makeText(
            context,
            "Abhi call initiate karne mein issue aa rha hai, Kripya Doubtnut helpline <$phoneNumber> pe call karen",
            Toast.LENGTH_LONG
        ).show()
    }
}
