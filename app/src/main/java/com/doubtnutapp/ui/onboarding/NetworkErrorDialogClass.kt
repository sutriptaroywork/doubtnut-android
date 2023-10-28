package com.doubtnutapp.ui.onboarding

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.doubtnutapp.Constants
import com.doubtnutapp.R

class NetworkErrorDialogClass : DialogFragment() {

    companion object {
        fun newInstanceClass(errorMsg: String, failedIn: String): NetworkErrorDialogClass {

            val fragment = NetworkErrorDialogClass()
            val args = Bundle()
            args.putString(Constants.ERROR_MSG, errorMsg)
            args.putString(Constants.FAILED_IN, failedIn)

            fragment.arguments = args
            return fragment

        }
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
        val inflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.dialog_network_error_onboarding_class, null)

        builder.setView(view)
        builder.setCancelable(false)
        configureDialogActionsButtons(view)
        return builder.create()
    }

    private fun configureDialogActionsButtons(view: View) {
        view.findViewById<TextView>(R.id.textView_network_error_class).text =
            arguments!!.getString(Constants.ERROR_MSG)

        view.findViewById<TextView>(R.id.btn_network_error_class).setOnClickListener {
            if (arguments!!.getString(Constants.FAILED_IN) == Constants.CLASS_SSC) {

                (parentFragment as SelectClassFragment).getClassesList()

            } else if (arguments!!.getString(Constants.FAILED_IN) == Constants.ADD_PUBLIC_USER) {
                (parentFragment as SelectClassFragment).reloadAfterNetworkError()
            }
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
}