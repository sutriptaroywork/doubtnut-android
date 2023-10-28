package com.doubtnutapp.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.BranchDeeplinkHandler
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.utils.BranchIOUtils
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

open class TransparentActivity : BaseActivity() {

    private val deepLinkDisposable: CompositeDisposable = CompositeDisposable()

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun onStart() {
        super.onStart()
        BranchIOUtils.reInitBranchSession(this)
        BranchIOUtils.setIdentity(
            Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ANDROID_ID
            )
        )
        handleDeepLink()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transparent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        BranchIOUtils.reInitBranchSession(this)
    }

    override fun onStop() {
        super.onStop()
        deepLinkDisposable.dispose()
    }

    private fun handleDeepLink() {
        val startValue: Long = 0
        val endValue: Long = 15
        val initialDelay: Long = 0
        val pauseTime: Long = 1

        deepLinkDisposable.add(
            Observable.intervalRange(
                startValue,
                endValue,
                initialDelay,
                pauseTime,
                TimeUnit.SECONDS,
                AndroidSchedulers.mainThread()
            )
                .subscribe({ value ->
                    if (checkForDeepLink()) {
                        deepLinkDisposable.clear()
                    } else if (value == endValue) {
                        //Add toast let the user something went wrong
                        finish()
                    }
                }, {
                    //Add toast let the user something went wrong
                    finish()
                })
        )
    }

    private fun checkForDeepLink(): Boolean {
        val referringParams = BranchIOUtils.getReferringParam(applicationContext)
        if (!referringParams.isNullOrBlank()) {
            val deeplink = BranchDeeplinkHandler.getParsedDeeplink(
                applicationContext,
                referringParams,
                (applicationContext as DoubtnutApp).getEventTracker(),
                analyticsPublisher
            )
            if (deeplink != null) {
                deeplinkAction.performAction(this, deeplink,
                    Bundle().apply {
                        putBoolean(Constants.CLEAR_TASK, false)
                        val source = intent?.getStringExtra(Constants.SOURCE)
                            ?: EventConstants.PAGE_DEEPLINK_CLICK
                        putString(Constants.SOURCE, source)
                    })

            }
            finish()
            BranchIOUtils.clearReferringParam(applicationContext)
            return true
        }
        return false
    }
}

