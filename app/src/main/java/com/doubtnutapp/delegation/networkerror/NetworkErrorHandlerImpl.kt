package com.doubtnutapp.delegation.networkerror

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.BuildConfig
import com.doubtnutapp.R
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import javax.inject.Inject

class NetworkErrorHandlerImpl @Inject constructor(private val activity: AppCompatActivity) : NetworkErrorHandler {

    override fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(activity.supportFragmentManager, "BadRequestDialog")
    }

    override fun onApiError(e: Throwable) {
        if (BuildConfig.DEBUG) {
            showToast(e?.message ?: "Api error.. please try later")
        } else {
            showToast(R.string.api_error)
        }
    }

    override fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(activity).not()) {
            showToast(R.string.string_noInternetConnection)
        } else {
            showToast(R.string.somethingWentWrong)
        }
    }

    private fun showToast(msg: Int) {
        ToastUtils.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }

    private fun showToast(msg: String) {
        ToastUtils.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }
}
