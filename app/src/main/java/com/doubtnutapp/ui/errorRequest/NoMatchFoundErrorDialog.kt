package com.doubtnutapp.ui.errorRequest

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.DialogFragment
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnutapp.*
import com.doubtnutapp.gamification.settings.profilesetting.ui.ProfileSettingActivity
import com.doubtnutapp.ui.cameraGuide.CameraGuideActivity
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.utils.NetworkUtils

class NoMatchFoundErrorDialog : DialogFragment() {
    companion object {
        const val TAG = "NoMatchFoundErrorDialog"

        fun newInstance(): NoMatchFoundErrorDialog {
            return NoMatchFoundErrorDialog()
        }
    }

    private lateinit var eventTracker: Tracker

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        val inflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.dialog_hand_written_error, null)
        eventTracker = getTracker()
        dialog.setContentView(view)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        configureDialogActionsButtons(view)

        dialog.setOnKeyListener { dialogInterface, i, keyEvent ->

            if (i == KeyEvent.KEYCODE_BACK && keyEvent?.action == KeyEvent.ACTION_UP) {
                activity!!.onBackPressed()
            }
            return@setOnKeyListener false
        }

        return dialog
    }

    private fun configureDialogActionsButtons(view: View) {

        view.findViewById<TextView>(R.id.knowMoreTextView).setOnClickListener {
            startActivity(Intent(activity!!, CameraGuideActivity::class.java).apply {
                putExtra(ProfileSettingActivity.INTENT_EXTRA_SOURCE, ProfileSettingActivity.TAG)
            })
            activity!!.finish()
            dialog?.dismiss()
            sendEvent(EventConstants.EVENT_NAME_VIEW_MORE_FROM_HAND_WRITTEN_DIALOG)
        }

        view.findViewById<TextView>(R.id.askQuestionAgainBtn).setOnClickListener {
            dialog?.dismiss()
            activity?.finish()

            context?.let { context ->
                CameraActivity.getStartIntent(context, TAG).also {
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(it)
                }
            }

            sendEvent(EventConstants.EVENT_NAME_ASK_AGAIN_FROM_HAND_WRITTEN_DIALOG)
        }

        view.findViewById<ImageView>(R.id.closeImageView).setOnClickListener {
            dialog?.dismiss()
            activity?.finish()

            context?.let {  context ->
                CameraActivity.getStartIntent(context, TAG).also {
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(it)
                }
            }
            sendEvent(EventConstants.EVENT_NAME_CLOSE_FROM_NO_MATCH)
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


    private fun sendEvent(eventName: String) {
        activity?.apply {
            eventTracker.addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(activity!!).toString())
                    .addStudentId(defaultPrefs(activity!!)
                            .getString(Constants.STUDENT_ID, "").orDefaultValue())
                    .addScreenName(EventConstants.PAGE_HAND_WRITTEN_ERROR_DIALOG)
                    .track()
        }
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = activity?.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }


}
