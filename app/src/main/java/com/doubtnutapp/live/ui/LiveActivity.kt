package com.doubtnutapp.live.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.statusbarColor
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class LiveActivity : AppCompatActivity(), HasAndroidInjector {

    companion object {

        val TYPE_SCHEDULE_LIVE = "schedule_live"
        val TYPE_LIVE_STREAM_PUBLISH = "live_stream_publish"
        val TYPE_LIVE_STREAM = "live_stream"

        fun getStartIntent(context: Context, type: String, extraData: Bundle? = null): Intent {
            return Intent(context, LiveActivity::class.java).apply {
                action = type
                extraData?.let { putExtras(it) }
            }
        }
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.colorSecondaryDark)
        setContentView(R.layout.activity_live)

        when (intent.action) {
            TYPE_SCHEDULE_LIVE -> replaceFragment(ScheduleLiveFragment())
            TYPE_LIVE_STREAM_PUBLISH -> replaceFragment(LiveStreamPublishFragment.newInstance(
                    intent.getStringExtra(Constants.POST_ID).orEmpty())
            )
            TYPE_LIVE_STREAM -> replaceFragment(LiveStreamFragment.newInstance(
                    intent.getStringExtra(Constants.POST_ID).orEmpty(),
                    intent.getStringExtra(Constants.URL).orEmpty(),
                    intent.getBooleanExtra(Constants.IS_VOD, false))
            )
        }
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
    }

    fun addFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().add(R.id.container, fragment).commit()
    }

    override fun onBackPressed() {
        if (intent.action == TYPE_LIVE_STREAM_PUBLISH) {
            supportFragmentManager.fragments.forEach {
                if (it is LiveStreamPublishFragment) {
                    if (it.confirmStreamEnd()) super.onBackPressed() else return
                }
            }
        }
        super.onBackPressed()
    }
}