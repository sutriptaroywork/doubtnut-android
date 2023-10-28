package com.doubtnutapp.ui.splash

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import com.doubtnutapp.R
import com.doubtnutapp.ui.editProfile.CameraGalleryDialog
import com.doubtnutapp.camera.ui.CameraActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class RequiredPermissionDialog : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "RequiredPermissionDialog"
        const val TITLE = "TITLE"
        fun newInstance(title: String = ""): RequiredPermissionDialog {
            return RequiredPermissionDialog().apply {
                arguments = Bundle().apply {
                    putString(TITLE, title)
                }
            }
        }
    }

    private lateinit var v: View
    private var mBehavior: BottomSheetBehavior<*>? = null


    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        v = View.inflate(context, R.layout.dialog_required_permission, null)
        dialog.setContentView(v)
        dialog.setCanceledOnTouchOutside(false)
        configureDialogActionsButtons(v)
        mBehavior = BottomSheetBehavior.from(v.parent as View)
        return dialog
    }

    private fun configureDialogActionsButtons(view: View) {

        view.findViewById<Button>(R.id.exit_permission).setOnClickListener {
            if (activity != null && activity is CameraActivity) {
                activity?.onBackPressed()
            } else if (parentFragment is CameraGalleryDialog) {
                dialog?.dismiss()
            }
        }

        view.findViewById<Button>(R.id.grant_permission).setOnClickListener {
            if (activity != null && activity is CameraActivity) {
                (activity as CameraActivity).requestAgain()
                dialog?.dismiss()
            }  else {
                activity?.onBackPressed()
            }
        }
        val title = arguments?.getString(TITLE)
        if (title.isNullOrBlank().not()) {
            view.findViewById<TextView>(R.id.textView_permission_required).text = title
        }

    }


    private fun configureNotificationEducationView() {

        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels - 100
        val height = (displayMetrics.heightPixels * .60).toInt()
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onStart() {
        super.onStart()
        mBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        configureNotificationEducationView()
    }

}