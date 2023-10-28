package com.doubtnutapp.ui.errorRequest

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnutapp.*
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil

class NetworkErrorDialog : DialogFragment() {
    companion object {
        fun newInstance(): NetworkErrorDialog {
            return NetworkErrorDialog()
        }
    }

    private  lateinit var eventTracker :Tracker

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder=AlertDialog.Builder(activity !!)
        val inflater=activity !!.layoutInflater
        val view=inflater.inflate(R.layout.dialog_network_error, null)
        eventTracker = getTracker()
        builder.setView(view)
        builder.setCancelable(false)
        configureDialogActionsButtons(view)
        return builder.create()
    }

    private fun configureDialogActionsButtons(view: View) {

        view.findViewById<TextView>(R.id.btn_network_error).setOnClickListener {
            startActivity(Intent(activity !!, MainActivity::class.java))
            dialog?.dismiss()
            sendEvent(EventConstants.EVENT_NAME_NETWORK_ERROR_BTN)
        }

    }

    private fun configureNotificationEducationView() {

        val displayMetrics=DisplayMetrics()

        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val width=displayMetrics.widthPixels - 100
        val height=(displayMetrics.heightPixels * .60).toInt()
        //changing dialog height
        dialog?.window?.setLayout(width, height)
        //making dialog background transparent
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


    }

    override fun onStart() {
        super.onStart()
        configureNotificationEducationView()
    }


    private fun sendEvent(eventName : String){
        activity?.apply {
            eventTracker.addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(activity!!).toString())
                    .addStudentId(UserUtil.getStudentId())
                    .addScreenName(EventConstants.PAGE_NETWORK_ERROR_DIALOG)
                    .track()
        }
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = activity?.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }


}
