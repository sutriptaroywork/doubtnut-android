package com.doubtnut.core.ui.base

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.NonNull
import androidx.core.content.edit
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.R
import com.doubtnut.core.actions.ActivityAction
import com.doubtnut.core.analytics.ITracker
import com.doubtnut.core.analytics.addEventNames2
import com.doubtnut.core.constant.CoreConstants
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.data.local.defaultPrefs2
import com.doubtnut.core.databinding.SheetBadRequestBinding
import com.doubtnut.core.utils.CoreUserUtils
import com.doubtnut.core.utils.NetworkUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class CoreBadRequestDialog : BottomSheetDialogFragment() {

    companion object {
        private const val KEY_CANCELLABLE = "cancellable"

        fun newInstance(
            from: String,
            cancellable: Boolean = false
        ): CoreBadRequestDialog {
            val fragment = CoreBadRequestDialog()
            val args = Bundle()
            args.putString(CoreConstants.NAVIGATE_FROM, from)
            args.putBoolean(KEY_CANCELLABLE, cancellable)
            fragment.arguments = args
            fragment.isCancelable = cancellable
            return fragment
        }
    }

    private var mBehavior: BottomSheetBehavior<*>? = null
    private lateinit var eventTracker: ITracker

    private lateinit var v: View

    private val cancellable by lazy { arguments?.getBoolean(KEY_CANCELLABLE) ?: false }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        v = View.inflate(context, R.layout.sheet_bad_request, null)
        eventTracker = getTracker()

        dialog.setContentView(v)

        val binding = SheetBadRequestBinding.bind(v)

        mBehavior = BottomSheetBehavior.from(v.parent as View)
        binding.tvHeading.text = getString(R.string.badRequest_tittle_2)
        binding.badrequestTxt.text = getString(R.string.badRequest_body_2)
        binding.btnBadAccess.text = getString(R.string.login_button)
        sendEvent(CoreEventConstants.EVENT_NAME_BAD_ACCESS_TOKEN)

        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById(R.id.design_bottom_sheet) as? View
            if (null != bottomSheet) {
                val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
                behavior.isHideable = false
            }
        }

        binding.btnBadAccess.setOnClickListener {
            goToLogin()
            sendEvent(CoreEventConstants.EVENT_NAME_REQUEST_TO_LOGIN_CLICK)
        }

        if (mBehavior != null) {
            (mBehavior as BottomSheetBehavior<*>).state = BottomSheetBehavior.STATE_EXPANDED
            (mBehavior as BottomSheetBehavior<*>).setBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
                    (mBehavior as BottomSheetBehavior<*>).state =
                        BottomSheetBehavior.STATE_EXPANDED
                }

                override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {

                }
            })
        }
        return dialog
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.setCancelable(cancellable)
        dialog?.setCanceledOnTouchOutside(cancellable)
        (dialog?.window?.decorView?.findViewById(R.id.touch_outside) as? View)
            ?.setOnClickListener(null)
    }

    override fun onStart() {
        super.onStart()
        mBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun goToLogin() {
        defaultPrefs2().edit {
            putString(CoreConstants.STUDENT_LOGIN, "false")
            putBoolean(CoreConstants.ON_BOARDING_COMPLETED, false)
        }
        val intent = Intent(ActivityAction.SPLASH_MAIN).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        activity?.startActivity(intent)
        activity?.finish()
    }

    private fun sendEvent(eventName: String) {
        activity?.apply {
            eventTracker.addEventNames2(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addStudentId(CoreUserUtils.getStudentId())
                .addScreenName(CoreEventConstants.PAGE_BAD_REQUEST)
                .track()
        }
    }

    private fun getTracker(): ITracker {
        val doubtnutApp = activity?.applicationContext as CoreApplication
        return doubtnutApp.getEventTracker()
    }

}