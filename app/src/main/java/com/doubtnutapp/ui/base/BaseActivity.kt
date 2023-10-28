package com.doubtnutapp.ui.base

import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.doubtnut.core.entitiy.SingleEvent
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnut.core.sharing.event.Share
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.ApplicationStateEvent
import com.doubtnutapp.LocaleManager
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.ui.mediahelper.MediaPlayerHelper
import dagger.Lazy
import io.reactivex.disposables.Disposable
import javax.inject.Inject

open class BaseActivity : AppCompatActivity() {

    // This is terminated on activity destroy
    private var appStateObserver: Disposable? = null

    protected var isAppInForeground: Boolean = true

    @Inject
    lateinit var baseWhatsAppSharing: Lazy<WhatsAppSharing>

    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleManager.setLocale(this)
        super.onCreate(savedInstanceState)
        startObserver()
    }

    override fun onStop() {
        super.onStop()
        MediaPlayerHelper.stop()
    }

    private fun startObserver() {
        appStateObserver = (application as DoubtnutApp)
            .bus()?.toObservable()?.subscribe {
                when (it) {
                    is ApplicationStateEvent -> {
                        isAppInForeground = it.state
                    }
                    is SingleEvent<*> -> {
                        if (it.peekContent() is Share) {
                            (it.getContentIfNotHandled() as? Share)?.let { share ->
                                baseWhatsAppSharing.get().shareOnWhatsApp(
                                    ShareOnWhatApp(
                                        channel = "",
                                        featureType = "",
                                        campaign = "",
                                        imageUrl = share.imageUrl,
                                        controlParams = hashMapOf(),
                                        bgColor = "#00000000",
                                        sharingMessage = share.message,
                                        questionId = "",
                                        packageName = share.packageName,
                                        appName = share.appName,
                                        skipBranch = share.skipBranch,
                                    )
                                )
                                baseWhatsAppSharing.get().startShare(this)
                            }
                        }
                    }
                }
            }
    }

    /**
     * Use this method before setContentView to enable full screen
     */
    protected fun requestFullScreen() {
        // Full Screen Activity
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    protected fun enforceLtrLayout() {
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
    }

    override fun onDestroy() {
        super.onDestroy()
        appStateObserver?.dispose()
        if (::baseWhatsAppSharing.isInitialized) {
            baseWhatsAppSharing.get().dispose()
        }
    }
}

