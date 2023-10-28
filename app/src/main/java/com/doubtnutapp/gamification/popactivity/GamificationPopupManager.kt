package com.doubtnutapp.gamification.popactivity

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.addEventNames

import com.doubtnutapp.eventmanager.CommonEventManager
import com.doubtnutapp.gamification.popactivity.ui.adapter.PopUpViewAdapter
import com.doubtnutapp.gamification.popactivity.ui.viewmodel.GamificationPopupViewModel
import com.doubtnutapp.liveclass.ui.LiveClassActivity
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.toBundle
import dagger.Lazy
import java.lang.ref.WeakReference
import javax.inject.Inject

class GamificationPopupManager @Inject constructor(
    private val gamificationPopupViewModel: Lazy<GamificationPopupViewModel>,
    private val screenNavigator: Lazy<Navigator>,
    private val eventTracker: Lazy<Tracker>,
    private val commonEventManager: Lazy<CommonEventManager>
) : Application.ActivityLifecycleCallbacks, ActionPerformer {

    private var activityWeakReference: WeakReference<Activity?>? = null

    private val popUpViewAdapter: PopUpViewAdapter by lazy {
        PopUpViewAdapter(
            this,
            commonEventManager.get()
        )
    }

    fun showPopup(extras: Bundle) {
        (activityWeakReference?.get() as? AppCompatActivity)?.let {
            it.runOnUiThread {
                finish()
                val gamificationPopupData: Map<String, String>? = getGamificationPopupData(extras)
                if (gamificationPopupData != null) {
                    gamificationPopupViewModel.get().getPopUpData(gamificationPopupData)
                }
            }
        }

    }

    fun onLiveClassDoubtAnswered(qid: String, commentId: String): Boolean {
        var isConsumed = false
        (activityWeakReference?.get() as? AppCompatActivity)?.let {
            if (it is LiveClassActivity) {
                isConsumed = it.isLiveClassOngoingForQid(qid)
                it.runOnUiThread {
                    finish()
                    it.onLiveClassDoubtAnswered(commentId)
                }
            }
        }
        return isConsumed
    }

    private fun setupObserver(activity: AppCompatActivity) {

        val context: Context = activity
        val lifecycleOwner: LifecycleOwner = activity

        gamificationPopupViewModel.get().popViewTypeLiveData.observe(lifecycleOwner, {
            val badge = it?.getContentIfNotHandled()
            if (badge != null) {
                popUpViewAdapter.updateGamificationPopup(badge)
                val viewHolder = popUpViewAdapter.onCreateViewHolder(context, lifecycleOwner)
                if (viewHolder != null) {
                    configureWindow(
                        activity.window,
                        badge.gravity,
                        badge.width,
                        badge.height,
                        viewHolder.itemView
                    )
                    popUpViewAdapter.onBindViewHolder(viewHolder)
                }
            }
        })

        gamificationPopupViewModel.get().closeEventLiveData.observe(lifecycleOwner, {
            if (it?.getContentIfNotHandled() != null) {
                finish()
            }
        })

        gamificationPopupViewModel.get().navigateLiveData.observe(lifecycleOwner, {
            it?.getContentIfNotHandled()?.let { navigationModel ->
                finish()
                screenNavigator.get().startActivityFromActivity(
                    context,
                    navigationModel.screen,
                    navigationModel.hashMap?.toBundle()
                )
                sendEvent(EventConstants.EVENT_NAME_GAMIFICATION_POP_CLICK_VIEW_NOW)
                commonEventManager.get()
                    .eventWith(EventConstants.EVENT_NAME_GAMIFICATION_POP_CLICK_VIEW_NOW)
            }
        })
    }

    private fun finish() {
        val badgeView =
            (activityWeakReference?.get()?.window?.decorView as? ViewGroup)?.findViewWithTag<View>(
                TAG_BADGE_VIEW
            )
        (badgeView?.parent as? ViewGroup)?.removeView(badgeView)
    }

    private fun removePreviousObserver(activity: AppCompatActivity) {
        gamificationPopupViewModel.get().popViewTypeLiveData.removeObservers(activity)
        gamificationPopupViewModel.get().closeEventLiveData.removeObservers(activity)
        gamificationPopupViewModel.get().navigateLiveData.removeObservers(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        (activity as? AppCompatActivity)?.let {
            removePreviousObserver(it)
            finish()
        }
    }

    override fun onActivityResumed(activity: Activity) {
        (activity as? AppCompatActivity)?.let {
            setupObserver(it)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        activityWeakReference = WeakReference(activity)
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun performAction(action: Any) {
        gamificationPopupViewModel.get().handleAction(action)

    }

    private fun getGamificationPopupData(extras: Bundle?): Map<String, String>? {

        if (extras == null) return null

        return hashMapOf(
            POP_DIRECTION_KEY to (extras.getString(POP_DIRECTION_KEY) ?: ""),
            POP_DESCRIPTION_KEY to (extras.getString(POP_DESCRIPTION_KEY) ?: ""),
            POP_MESSAGE_KEY to (extras.getString(POP_MESSAGE_KEY) ?: ""),
            POP_IMAGE_URL_KEY to (extras.getString(POP_IMAGE_URL_KEY) ?: ""),
            POP_DURATION_KEY to (extras.getString(POP_DURATION_KEY) ?: ""),
            POP_TYPE_KEY to (extras.getString(POP_TYPE_KEY) ?: ""),
            POP_BUTTON_TEXT_KEY to (extras.getString(POP_BUTTON_TEXT_KEY) ?: ""),
            POP_UNLOCK_ACTION_DATA_KEY to (extras.getString(POP_UNLOCK_ACTION_DATA_KEY) ?: "")
        )
    }

    private fun configureWindow(
        window: Window,
        gravity: Int,
        width: Int,
        height: Int,
        viewToAdd: View
    ) {
        val viewGroupLayoutManager: FrameLayout.LayoutParams =
            FrameLayout.LayoutParams(width, height)
        viewToAdd.tag = TAG_BADGE_VIEW
        viewGroupLayoutManager.gravity = gravity
        window.addContentView(viewToAdd, viewGroupLayoutManager)
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
    }

    fun sendEvent(eventName: String) {
        eventTracker.get().addEventNames(eventName)
            .track()
    }

    companion object {
        private const val TAG_BADGE_VIEW = "Badge"
    }
}