package com.doubtnutapp.inappupdate

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.SharedPreferences
import android.graphics.Rect
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.doubtnutapp.*
import com.doubtnutapp.data.camerascreen.datasource.LocalConfigDataSource.Companion.PREF_FORCE_UPDATE
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.inappupdate.ui.ImmediateUpdateActivity
import com.doubtnutapp.utils.AppUtils.isImmediateUpdate
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import java.util.*
import javax.inject.Inject

private const val MY_REQUEST_CODE = 100

class InAppUpdateManager @Inject constructor(private val activity: AppCompatActivity, private val userPreference: UserPreference) : LifecycleObserver,
        InstallStateUpdatedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private var rootView: ViewGroup? = null
    private var isShownForTheSession = false
    private var isUpdateFlowStarted = false
    private var snackBar: Snackbar? = null
    private var updateType: Int = AppUpdateType.FLEXIBLE
    var isFlexibleUpdateEnabled: Boolean = false

    init {
        activity.lifecycle.addObserver(this)
    }

    private val MILLIS_IN_DAY = 86400000
    private val DEFAULT_UPDATE_CHECK_DAYS = 7

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onActivityCreated() {
        rootView = getRootView()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun startInAppUpdate() {
        defaultPrefs(activity).registerOnSharedPreferenceChangeListener(this)
        appUpdateManager.registerListener(this)
        if (!isUpdateFlowStarted) {
            isUpdateFlowStarted = true
            manageInAppUpdate()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun unregisterListener() {
        appUpdateManager.unregisterListener(this)
        defaultPrefs(activity).unregisterOnSharedPreferenceChangeListener(this)
    }


    fun onActivityResult(requestCode: Int, resultCode: Int) {
        if (resultCode != Activity.RESULT_OK && requestCode == MY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                if (updateType == AppUpdateType.IMMEDIATE) {
                    activity.finishAffinity()
                    System.exit(0)
                } else {
                    showInAppUpdateSnackBar(R.string.msg_installToUseNewFeature, R.string.text_downloadUpdate, Snackbar.LENGTH_SHORT) {
                        manageInAppUpdate()
                    }
                }
            } else if (resultCode == ActivityResult.RESULT_IN_APP_UPDATE_FAILED) {
                showInAppUpdateSnackBar(R.string.msg_inUpdateFailed, R.string.retry, Snackbar.LENGTH_SHORT) {
                    manageInAppUpdate()
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED &&
                requestCode == ImmediateUpdateActivity.IMMEDIATE_UPDATE_REQUEST_CODE) {
            activity.finishAffinity()
            System.exit(0)
        }
    }

    override fun onStateUpdate(installState: InstallState) {
        if (installState?.installStatus() == InstallStatus.FAILED) {
            //show snack bar to trigger the update the app
        } else if (installState?.installStatus() == InstallStatus.CANCELED) {
            //show status like get access to new feature
        } else if (installState?.installStatus() == InstallStatus.DOWNLOADED) {
            showInAppUpdateSnackBar(R.string.msg_updateHasbeenDownloaded, R.string.text_installUpdate, Snackbar.LENGTH_INDEFINITE) {
                appUpdateManager.completeUpdate()
            }
        }
    }

    private fun manageInAppUpdate() {
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            //            toast("OnSuccess ${appUpdateInfo.availableVersionCode()}")

            val installStatus = appUpdateInfo.installStatus()

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) &&
                    isImmediateUpdate()) {
                requestUpdate(appUpdateInfo)
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                    && isFlexibleUpdateEnabled) {
                val lastShown = userPreference.getLastShownInAppPopUp()
                if (!shouldShowAppUpdatePopup(activity, lastShown)) return@addOnSuccessListener
                userPreference.setLastShownInAppPopUp(Calendar.getInstance().timeInMillis)
                requestUpdate(appUpdateInfo)
            } else if (installStatus == InstallStatus.DOWNLOADED) {
                //show snack bar to restart the app
                showInAppUpdateSnackBar(R.string.msg_updateHasbeenDownloaded, R.string.text_installUpdate, Snackbar.LENGTH_INDEFINITE) {
                    appUpdateManager.completeUpdate()
                }
                unregisterListener()
            } else if (installStatus == InstallStatus.CANCELED) {
                //show snack bar to trigger the update the app
                showInAppUpdateSnackBar(R.string.msg_installToUseNewFeature, R.string.text_downloadUpdate, Snackbar.LENGTH_SHORT) {
                    requestUpdate(appUpdateInfo)
                }
            } else if (installStatus == InstallStatus.FAILED) {
                showInAppUpdateSnackBar(R.string.msg_inUpdateFailed, R.string.retry, Snackbar.LENGTH_SHORT) {
                    requestUpdate(appUpdateInfo)
                }
            }
        }

        appUpdateInfoTask.addOnFailureListener {
            if (isImmediateUpdate()) {
                ImmediateUpdateActivity.startActivity(activity, true)
            }
        }
    }

    private fun requestUpdate(appUpdateInfo: AppUpdateInfo) {
        if (isImmediateUpdate()) {
            updateType = AppUpdateType.IMMEDIATE
        }
        // Request the update.
        // Since a PendingIntent can be used only once, it throws this Exception.
        try {
            appUpdateManager.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                    updateType,
                    // The current activity making the update request.
                    activity,
                    // Include a request code to later monitor this update request.
                    MY_REQUEST_CODE)
        } catch (e: IntentSender.SendIntentException) {

        }
    }

    private fun getRootView() =
            (activity.findViewById(android.R.id.content) as ViewGroup)
                    .getChildAt(0) as? ViewGroup

    private fun showInAppUpdateSnackBar(message: Int, actionText: Int, duration: Int, action: (() -> Unit)? = null) {
        rootView?.let {
            val snackbar = Snackbar.make(
                    it,
                    message,
                    duration
            ).apply {
                setAction(actionText) {
                    action?.invoke()
                }
                setActionTextColor(ContextCompat.getColor(it.context, R.color.white))
                show()
            }

            if (snackBar != null && snackBar?.isShown == true) {
                snackBar?.dismiss()
                snackBar = null
            }

            if (duration == Snackbar.LENGTH_INDEFINITE) {
                snackBar = snackbar
            }
        }
    }

    fun dispatchTouchEvent(ev: MotionEvent) {
        if (snackBar != null && snackBar?.isShown == true) {
            val rect = Rect()
            snackBar?.view?.getHitRect(rect)
            if (!rect.contains(ev.x.toInt(), ev.y.toInt())) {
                snackBar?.dismiss()
                snackBar = null
            }
        }
    }

    private fun shouldShowAppUpdatePopup(context: Context, lastShown: Long): Boolean {
        var shouldShow = false
        var updateFrequency = DEFAULT_UPDATE_CHECK_DAYS

        val appFrequencyPayload = FeaturesManager.getFeaturePayload(context, Features.APP_UPDATE_FREQUENCY)
        if (appFrequencyPayload != null) {
            updateFrequency = (appFrequencyPayload["update_check_days"] as? Double)?.toInt()
                    ?: DEFAULT_UPDATE_CHECK_DAYS
        }

        shouldShow = (Calendar.getInstance().timeInMillis - lastShown) > (updateFrequency * MILLIS_IN_DAY)
        return shouldShow
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key.equals(PREF_FORCE_UPDATE) && !isUpdateFlowStarted) {
            isUpdateFlowStarted = true
            manageInAppUpdate()
        }
    }
}