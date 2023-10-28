package com.doubtnutapp.ui.onboarding

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Parcelable
import android.util.DisplayMetrics
import android.view.View
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.Language
import com.doubtnutapp.ui.onboarding.viewmodel.LanguageViewModel

class NetworkErrorDialogOnBoarding : DialogFragment()  {

    private lateinit var viewModel: LanguageViewModel
    companion object {
        fun newInstanceLanguage(errorMsg: String, failedIn: String): NetworkErrorDialogOnBoarding {

            val fragment = NetworkErrorDialogOnBoarding()
            val args = Bundle()
            args.putString(Constants.ERROR_MSG, errorMsg)
            args.putString(Constants.FAILED_IN, failedIn)
            fragment.arguments = args
            return fragment

        }
        fun newInstanceLanguageUpdate(errorMsg: String, failedIn: String, lngcode: Language): NetworkErrorDialogOnBoarding {

            val fragment = NetworkErrorDialogOnBoarding()
            val args = Bundle()
            args.putString(Constants.ERROR_MSG, errorMsg)
            args.putString(Constants.FAILED_IN, failedIn)
            args.putParcelable(Constants.LANG_CODE, lngcode)

            fragment.arguments = args
            return fragment

        }
    }
    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder= AlertDialog.Builder(activity !!)
        val inflater=activity !!.layoutInflater
        val view=inflater.inflate(R.layout.dialog_network_error_onboarding, null)
        viewModel = ViewModelProviders.of(this).get(LanguageViewModel::class.java)

        builder.setView(view)
        builder.setCancelable(false)
        configureDialogActionsButtons(view)
        return builder.create()
    }

    private fun configureDialogActionsButtons(view: View) {
        view.findViewById<TextView>(R.id.textView_network_error).text = arguments!!.getString(Constants.ERROR_MSG)

        view.findViewById<TextView>(R.id.btn_network_error).setOnClickListener {
             if(arguments!!.getString(Constants.FAILED_IN) == Constants.GET_LANGUAGE){
                 (parentFragment as LanguageFragment).getLanguageData()


            }
            else  if(arguments!!.getString(Constants.FAILED_IN) == Constants.UPDATE_LANGUAGE && arguments!!.getParcelable<Parcelable>(Constants.LANG_CODE)!=null){
                 (parentFragment as LanguageFragment).updateLanguage(arguments!!.getParcelable<Parcelable>(Constants.LANG_CODE) as Language)


             }
            dialog?.dismiss()
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
}
