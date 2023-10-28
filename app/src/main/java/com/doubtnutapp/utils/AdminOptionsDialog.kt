package com.doubtnutapp.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.data.DefaultDataStoreImpl
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.getTextFromClipboard
import com.doubtnutapp.*
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.scheduledquiz.ScheduledNotificationWorkManagerHelper
import com.doubtnutapp.scheduledquiz.ui.ScheduledQuizNotificationActivity
import com.doubtnutapp.ui.quiz.EvernoteUtils
import com.doubtnutapp.ui.quiz.FetchQuizJob
import com.doubtnutapp.ui.quiz.FetchQuizJobWorker
import com.doubtnutapp.widgettest.ui.ApiTestActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.dialog_admin_options.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.system.exitProcess

class AdminOptionsDialog : BottomSheetDialogFragment() {

    private var mBehavior: BottomSheetBehavior<*>? = null

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var defaultDataStore: DefaultDataStore

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (!BuildConfig.DEBUG && !BuildConfig.ENABLE_ADMIN_OPTIONS) {
            dismiss()
        }
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        val v = View.inflate(context, R.layout.dialog_admin_options, null)
        dialog.setContentView(v)

        init(v)
        initClickListeners(v)

        return dialog
    }

    override fun onStart() {
        super.onStart()
        mBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun init(view: View) {
        mBehavior = BottomSheetBehavior.from(view.parent as View)

        view.txt_build_type.text = "Build type:  " + BuildConfig.BUILD_TYPE
        view.txt_version_code.text = "Version code:  " + BuildConfig.VERSION_CODE
        view.txt_branch.text = "Branch:  " + BuildConfig.BRANCH_NAME

        lifecycleScope.launch {
            defaultDataStore.adminDeepLink.firstOrNull()
                    ?.takeIf { it.isNotEmpty() }
                    ?.let {
                        view.etDeeplink.setText(it)
                    }
        }

        view.debugLogCheckbox.isChecked =
                defaultPrefs(requireActivity()).getBoolean(Constants.ENABLE_DEBUG_LOG, false)

        if (DoubtnutApp.INSTANCE.getBaseMicroApiUrl() == MICRO_URL) {
            view.rbMicro.isChecked = true
        } else {
            view.rbMicroTest.isChecked = true
        }

        view.etMicroUrl.setText(DoubtnutApp.INSTANCE.getBaseMicroApiUrl())
        view.etCompleteBaseUrl.setText(DoubtnutApp.INSTANCE.getBaseApiUrl())
        view.etCompleteSocketUrl.setText(DoubtnutApp.INSTANCE.getBaseSocketUrl())

        view.etBaseUrl.doOnTextChanged { text, _, _, _ ->
            val textInput = text.toString()
            view.etCompleteBaseUrl.setText("https://$textInput.doubtnut.com")
        }
    }

    fun initClickListeners(view: View) {
        view.rbMicro.setOnClickListener {
            if (view.rbMicro.isChecked) {
                view.etMicroUrl.setText(MICRO_URL)
            }
        }
        view.rbMicroTest.setOnClickListener {
            if (view.rbMicroTest.isChecked) {
                view.etMicroUrl.setText(MICRO_Test_URL)
            }
        }
        view.btnSave.setOnClickListener {
            if (view.etCompleteBaseUrl.text.toString().isNotEmpty()) {
                val url = view.etCompleteBaseUrl.text.toString()
                defaultPrefs(requireActivity()).edit {
                    putString(Constants.ADMIN_API_URL, url)
                }
            }

            if (view.etCompleteSocketUrl.text.toString().isNotEmpty()) {
                val url = view.etCompleteSocketUrl.text.toString()
                defaultPrefs(requireActivity()).edit {
                    putString(Constants.SOCKET_BASE_URL, url)
                }
            }

            defaultPrefs(requireActivity()).edit {
                putString(Constants.ADMIN_API_MICRO_URL, view.etMicroUrl.text.toString().trim())
            }

            defaultPrefs(requireActivity()).edit {
                putBoolean(Constants.ENABLE_DEBUG_LOG, view.debugLogCheckbox.isChecked)
            }

            dialog?.hide()

            lifecycleScope.launch {
                delay(500)
                val packageManager = requireContext().packageManager
                val intent = packageManager.getLaunchIntentForPackage(requireContext().packageName)
                requireActivity().finishAffinity()
                requireActivity().startActivity(intent)
                exitProcess(0)
            }
        }

        view.btnDownloadVideo.setOnClickListener {
            VideoDownloadOptionsDialog().show(childFragmentManager, "VideoDownloadOptionsDialog")
        }

        view.btnTestExo.setOnClickListener {
            startActivity(Intent(context, VideoTestActivity::class.java))
        }

        view.launchTest.setOnClickListener {
            view.context.startActivity(
                    Intent(
                            view.context,
                            ScheduledQuizNotificationActivity::class.java
                    )
            )
        }
        view.testNotif.setOnClickListener {
            context?.let { it1 ->
                ToastUtils.makeText(
                        it1,
                        "Periodic notification set",
                        Toast.LENGTH_SHORT
                ).show()
            }
            setupTestJob(view)
        }
        view.launchCall.setOnClickListener {
            ScheduledNotificationWorkManagerHelper(DoubtnutApp.INSTANCE.applicationContext).assignPeriodicWork(
                    DoubtnutApp.INSTANCE.applicationContext
            )
        }
        view.apiTest.setOnClickListener {
            view.context.startActivity(
                    Intent(
                            view.context,
                            ApiTestActivity::class.java
                    )
            )
        }

        view.btnLaunchDeeplink?.setOnClickListener {
            if (view.etDeeplink?.text.toString().isNotEmpty()) {
                lifecycleScope.launch {
                    defaultDataStore.set(
                            DefaultDataStoreImpl.ADMIN_DEEP_LINK,
                            view.etDeeplink?.text?.toString().orEmpty()
                    )
                }
            }
            deeplinkAction.performAction(requireContext(), view.etDeeplink?.text?.toString())
        }

        view.findViewById<View>(R.id.btnLaunchDeeplinkFromClipboard)?.apply {
            setOnClickListener {
                view.findViewById<EditText>(R.id.etDeeplink)?.apply {
                    this.setText(requireContext().getTextFromClipboard())
                    view.findViewById<View>(R.id.btnLaunchDeeplink)?.performClick()
                }
            }
        }
    }

    private fun setupTestJob(view: View, count: Long = 0) {
        if (authToken(DoubtnutApp.INSTANCE.applicationContext).isNotEmpty()) {
            if (FeaturesManager.isFeatureEnabled(
                            requireContext(),
                            Features.EVERNOTE_ANDROID_JOB,
                            true
                    )
            ) {
                FetchQuizJob.enqueue(
                        requireContext(),
                        TimeUnit.HOURS.toMillis(
                                view.etStartHours.text.toString().toLong()
                        ) + TimeUnit.MINUTES.toMillis(view.etStartMin.text.toString().toLong() + count),
                        TimeUnit.HOURS.toMillis(
                                view.etStartHours.text.toString().toLong()
                        ) + TimeUnit.MINUTES.toMillis(
                                view.etStartMin.text.toString().toLong()
                        ) + count + 2
                )
            } else {
                EvernoteUtils.cancelAll(requireContext())
                FetchQuizJobWorker.enqueue(
                        requireContext(),
                        view.etStartHours.text.toString().toInt(),
                        view.etStartMin.text.toString().toInt()
                )
            }
        }
    }

    companion object {
        const val MICRO_URL = "https://micro.doubtnut.com/"
        const val MICRO_Test_URL = "https://micro.stg.doubtnut.com/"
    }
}