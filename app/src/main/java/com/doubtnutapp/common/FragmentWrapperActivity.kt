package com.doubtnutapp.common

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.gamification.userProfileData.ui.ProfileFragment
import com.doubtnutapp.profile.userprofile.UserProfileFragment
import com.doubtnutapp.shorts.ShortsFragment
import com.doubtnutapp.sticker.BaseActivity
import com.doubtnutapp.video.SimpleVideoFragment
import com.doubtnutapp.video.VideoFragmentListener
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class FragmentWrapperActivity : BaseActivity(), HasAndroidInjector {

    companion object {
        const val FRAGMENT_ID = "fragment_id"

        const val FRAGMENT_USER_PROFILE = "user_profile"
        const val FRAGMENT_OLD_PROFILE = "old_profile"
        const val FRAGMENT_SIMPLE_VIDEO = "simple_video"
        const val FRAGMENT_SHORTS = "shorts"

        fun userProfile(context: Context, studentId: String, source: String? = null) {
            Intent(context, FragmentWrapperActivity::class.java).apply {
                putExtra(FRAGMENT_ID, FRAGMENT_USER_PROFILE)
                putExtra(Constants.STUDENT_ID, studentId)
                putExtra(Constants.SOURCE, source)
            }.also {
                context.startActivity(it)
            }
        }

        fun oldProfile(context: Context) {
            Intent(context, FragmentWrapperActivity::class.java).putExtra(
                FRAGMENT_ID,
                FRAGMENT_OLD_PROFILE
            ).also {
                context.startActivity(it)
            }
        }

        fun simpleVideo(
            context: Context,
            videoData: SimpleVideoFragment.Companion.VideoData,
            source: String?,
            isFromFeed: Boolean
        ) {
            Intent(context, FragmentWrapperActivity::class.java).putExtra(
                FRAGMENT_ID,
                FRAGMENT_SIMPLE_VIDEO
            ).putExtra("video_data", videoData).putExtra(Constants.SOURCE, source)
                .putExtra("isFromFeed", isFromFeed).also {
                    context.startActivity(it)
                }
        }

        fun getShortsIntent(context: Context, qid: String?, type: String, navSource: String?) =
            Intent(context, FragmentWrapperActivity::class.java).putExtra(
                FRAGMENT_ID,
                FRAGMENT_SHORTS
            ).putExtra("qid", qid)
                .putExtra("type", type)
                .putExtra("nav_source", navSource)
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    private var source: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_wrapper)

        when (intent.getStringExtra(FRAGMENT_ID)) {
            FRAGMENT_USER_PROFILE -> {
                replaceFragment(
                    UserProfileFragment.newInstance(
                        intent.getStringExtra(Constants.STUDENT_ID).orEmpty(),
                        intent.getStringExtra(Constants.SOURCE)
                    )
                )
            }
            FRAGMENT_OLD_PROFILE -> {
                replaceFragment(ProfileFragment.newInstance())
            }
            FRAGMENT_SIMPLE_VIDEO -> {
                replaceFragment(
                    SimpleVideoFragment.newInstance(
                        intent.getParcelableExtra("video_data")!!,
                        videoFragmentListener = object : VideoFragmentListener {},
                        source = intent.getStringExtra(Constants.SOURCE),
                        isFromFeed = intent.getBooleanExtra("isFromFeed", false)
                    )
                )
            }
            FRAGMENT_SHORTS -> {
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
                replaceFragment(
                    ShortsFragment.newInstance(
                        intent.getStringExtra("qid"),
                        intent.getStringExtra("type") ?: "DEFAULT",
                        intent.getStringExtra("nav_source") ?: "DEFAULT"
                    )
                )
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (intent.getStringExtra(FRAGMENT_ID) == FRAGMENT_SHORTS) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

}
