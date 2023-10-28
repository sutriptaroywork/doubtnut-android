package com.doubtnutapp.gamification.mybio.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doubtnutapp.R
import com.doubtnutapp.ui.base.BaseActivity
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class MyBioActivity : BaseActivity(), HasAndroidInjector {

    companion object {
        const val PARAM_KEY_REFRESH_HOME_FEED = "refresh_home_feed"

        fun getStartIntent(
            context: Context,
            refreshHomeFeed: Boolean? = false
        ): Intent {
            val intent = Intent(context, MyBioActivity::class.java)
            intent.putExtra(PARAM_KEY_REFRESH_HOME_FEED, refreshHomeFeed)
            return intent
        }
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bio)
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer, MyBioFragment.newInstance(
                    intent.getBooleanExtra(
                        PARAM_KEY_REFRESH_HOME_FEED, false
                    )
                )
            )
            .commit()
    }

    // for testing Gamification pop up manager.
//    override fun onResume() {
//        super.onResume()
//
//        val extras = Bundle().apply {
//            putString(POP_DIRECTION_KEY, "TOP_RIGHT")
//            putString(POP_DESCRIPTION_KEY, "Welcome to Doubtnut")
//            putString(POP_MESSAGE_KEY, "Hello Shubham!")
//            putString(
//                POP_IMAGE_URL_KEY,
//                "https://https://pbs.twimg.com/profile_images/1292833247362080775/jwax2RtY_400x400.jpg"
//            )
//            putString(POP_TYPE_KEY, POPUP_TYPE_BADGE)
//        }
//        (DoubtnutApp.INSTANCE).showPopup(extras)
//    }
}
