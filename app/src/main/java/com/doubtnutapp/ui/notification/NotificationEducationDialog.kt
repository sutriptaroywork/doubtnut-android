package com.doubtnutapp.ui.notification

import android.app.Dialog
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.base.extension.getDatabase
import com.doubtnutapp.databinding.DialogNotificationEducationBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.model.AppEvent
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnut.core.utils.viewModelProvider

class NotificationEducationDialog :
    BaseBindingDialogFragment<DummyViewModel, DialogNotificationEducationBinding>() {

    companion object {
        private const val ARGUMENT_EXTRA_EVENT = "appEvent"
        private const val TAG = "BaseBindingDialogFragment"

        fun newInstance(event: AppEvent) = NotificationEducationDialog().apply {
            arguments = bundleOf(
                ARGUMENT_EXTRA_EVENT to event
            )
        }
    }

    private var appEvent: AppEvent? = null

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_notification_education, null)
        builder.setView(view)
        configureDialogActionsButtons(view)

        appEvent = null

        deleteEvent()
        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        configureNotificationEducationView()
    }

    private fun deleteEvent() {
        appEvent?.let {
            DoubtnutApp.INSTANCE.runOnDifferentThread {
                getDatabase(context)?.eventsDao()?.delete(it)
            }
        }
    }

    private fun configureDialogActionsButtons(view: View) {
        view.findViewById<TextView>(R.id.textView_notificationEducation_allow).setOnClickListener {
            try {
                val settingsIntent = getIntent()
                settingsIntent?.also { intent ->
                    if (intent.resolveActivity(requireActivity().packageManager) != null) {
                        startActivity(intent)
                    }

                }
                if (settingsIntent == null) {
                    startActivityForResult(Intent(Settings.ACTION_SETTINGS), 0);
                }
            } catch (e: Exception) {
                startActivityForResult(Intent(Settings.ACTION_SETTINGS), 0);
            }
        }

        view.findViewById<TextView>(R.id.textView_notificationEducation_cancel).setOnClickListener {
            dialog?.dismiss()
        }

    }

    private fun getIntent() = when (Build.MANUFACTURER) {

        "OnePlus" -> {
            val intent = Intent()
            intent.apply {
                component = ComponentName(
                    "com.android.settings",
                    "com.android.settings.Settings\$BgOptimizeAppListActivity"
                )
            }
        }

        "vivo" -> {
            val intent = Intent()
            intent.apply {
                component = ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )
            }
        }
        "Xiaomi" -> {
            val intent = Intent()
            intent.apply {
                component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            }
        }
        "Oppo" -> {
            val intent = Intent()
            intent.apply {
                component = ComponentName(
                    "com.oppo.safe",
                    "com.oppo.safe.permission.startup.StartupAppListActivity"
                )
            }
        }

        else -> null
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

        val btnString = appEvent?.button_text?.split(":")

        mBinding?.textViewNotificationEducationCancel?.text = btnString?.get(0)
        mBinding?.textViewNotificationEducationAllow?.text = btnString?.get(1)

        mBinding?.textViewNotificationEducationDontMiss?.text = appEvent?.title
        mBinding?.textViewNotificationEducationNotificationTurnOn?.text = appEvent?.sub_title
        mBinding?.textViewNotificationEducationNotificationTurnOn?.text = appEvent?.sub_title
        mBinding?.textViewNotificationEducationEnableNotification?.text = appEvent?.message

    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogNotificationEducationBinding {
        return DialogNotificationEducationBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

    }

}
