package com.doubtnutapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.doubtnutapp.Constants
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.data.remote.models.Chapter
import com.doubtnutapp.feed.view.FeedFragment
import com.doubtnutapp.gamification.userProfileData.ui.ProfileFragment
import com.doubtnutapp.inappupdate.InAppUpdateManager
import com.doubtnutapp.resourcelisting.ui.ResourceListingFragment
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.ui.course.ChaptersFragment
import com.doubtnutapp.ui.course.CourseDetailFragment
import com.doubtnutapp.ui.onboarding.LanguageFragment
import com.doubtnutapp.ui.onboarding.SelectClassFragment
import com.doubtnutapp.utils.Utils
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class FragmentHolderActivity : BaseActivity(), HasAndroidInjector {

    companion object {
        const val ACTION_NAVIGATE_PROFILE_SCREEN = "action_navigation_profile_screen"
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var inAppUpdateManager: InAppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        if (Utils.is21()) postponeEnterTransition()

        intent?.let {

            when (it.action) {

                Constants.NAVIGATE_COURSE_DETAIL -> addFragment(
                    CourseDetailFragment.newInstance(
                        it.getIntExtra("index", 0),
                        it.getParcelableArrayListExtra<Chapter>(Constants.CHAPTER_LIST)
                            .orEmpty() as ArrayList<Chapter>,
                        it.getStringExtra(Constants.CDN_PATH).orEmpty()
                    )
                )

                Constants.NAVIGATE_COURSE_DETAIL_NOTIFICATION -> addFragment(
                    CourseDetailFragment.newInstanceNotification(
                        it.getStringExtra(Constants.CLASS).orEmpty(),
                        it.getStringExtra(Constants.COURSE).orEmpty(),
                        it.getStringExtra(Constants.CHAPTER).orEmpty()
                    )
                )

                Constants.NAVIGATE_VIEW_PLAYLIST -> addFragment(
                    ResourceListingFragment.newInstance(
                        it.getStringExtra(Constants.PLAYLIST_ID).orEmpty(),
                        it.getStringExtra(Constants.PLAYLIST_TITLE).orEmpty(),
                        it.getBooleanExtra(Constants.NAVIGATE_FROM_DEEPLINK, false),
                        false,
                        it.getBooleanExtra(Constants.IS_FROM_VIDEO_TAG, false),
                        it.getStringExtra(Constants.VIDEO_TAG_NAME),
                        it.getStringExtra(Constants.QUESTION_ID),
                        it.getStringExtra(Constants.PAGE),
                        it.getStringExtra(Constants.PACKAGE_DETAIL_ID),
                        it.getBooleanExtra(Constants.IS_AUTO_PLAY, false),
                        it.getStringExtra(Constants.QUESTION_IDS)
                    )
                )

                Constants.NAVIGATE_COURSE -> addFragment(ChaptersFragment.newInstance())

                Constants.NAVIGATE_FORUM_FEED -> addFragment(FeedFragment.newInstance(false))

                Constants.NAVIGATE_LANGUAGE_FRAGMENT_FROM_EDITPROFILE -> {
                    addFragment(LanguageFragment.newInstance(true))
                }
                Constants.NAVIGATE_LANGUAGE_FRAGMENT_FROM_PROFILE -> {
                    addFragment(LanguageFragment.newInstanceForProfile(true))
                }
                Constants.NAVIGATE_TO_SELECT_CLASS -> {
                    addFragment(SelectClassFragment.newInstanceFromLibrary(true))
                }

                Constants.NAVIGATE_LANGUAGE_FRAGMENT_FROM_NAV -> {
                    addFragment(LanguageFragment.newInstanceForNav(true))
                }

                Constants.NAVIGATE_CLASS_FRAGMENT_FROM_NAV -> {
                    addFragment(
                        SelectClassFragment.newInstanceForNav(
                            true,
                            it.getBooleanExtra(SelectClassFragment.INTENT_SOURCE_LIBRARY, false)
                        )
                    )
                }

                Constants.NAVIGATE_CAMERA_SCREEN -> CameraActivity.getStartIntent(
                    this,
                    if (it.hasExtra(Constants.SOURCE)) {
                        it.getStringExtra(Constants.SOURCE).orEmpty()
                    } else {
                        ""
                    },
                    it.getStringExtra(Constants.CROP_IMAGE_URL)
                ).also { cameraActivityIntent ->
                    startActivity(cameraActivityIntent)
                    finish()
                }

                ACTION_NAVIGATE_PROFILE_SCREEN -> addFragment(ProfileFragment.newInstance())

                else -> Log.e("FragmentHolder", "no action")
            }

        }

    }

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        inAppUpdateManager.onActivityResult(requestCode, resultCode)
    }

    fun addFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().add(android.R.id.content, fragment).commit()
    }
}