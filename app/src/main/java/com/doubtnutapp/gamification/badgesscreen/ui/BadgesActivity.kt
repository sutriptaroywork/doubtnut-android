package com.doubtnutapp.gamification.badgesscreen.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*

import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandler
import com.doubtnutapp.gamification.badgesscreen.model.BaseBadgeViewType
import com.doubtnutapp.gamification.badgesscreen.ui.adapter.BadgesViewAdapter
import com.doubtnutapp.gamification.badgesscreen.ui.viewmodel.BadgesViewModel
import com.doubtnutapp.screennavigator.BadgeProgressDialogScreen
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.widgets.SpaceItemDecoration.Companion.HORIZONTAL
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.activity_badges.*
import kotlinx.android.synthetic.main.layout_whatsappprogress.*
import javax.inject.Inject

/**
 * Screen displayed from Profile > Points > My Badges
 */
class BadgesActivity : BaseActivity(), ActionPerformer, HasAndroidInjector {

    companion object {
        const val REQUEST_CODE = 1213
        private const val INTENT_EXTRA_USER_ID = "user_id"

        fun startActivity(context: Context, userId: String): Intent {
            return Intent(context, BadgesActivity::class.java).apply {
                putExtra(INTENT_EXTRA_USER_ID, userId)

            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var networkErrorHandler: NetworkErrorHandler

    private lateinit var badgesViewModel: BadgesViewModel

    @Inject
    lateinit var screenNavigator: Navigator

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badges)
        statusbarColor(this, R.color.grey_statusbar_color)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        badgesViewModel = viewModelProvider(viewModelFactory)
        badgesViewModel.getUserBadges(intent.getStringExtra(INTENT_EXTRA_USER_ID) ?: "")
        setupObserver()
        setUpListener()
        sendEvent(EventConstants.EVENT_NAME_BADGE_SCREEN)
    }

    private fun setUpListener() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun performAction(action: Any) {
        badgesViewModel.handleAction(action)
    }

    private fun setupObserver() {
        badgesViewModel.userBadgesLiveData.observeK(
            this,
            this::onSuccess,
            networkErrorHandler::onApiError,
            networkErrorHandler::unAuthorizeUserError,
            networkErrorHandler::ioExceptionHandler,
            this::updateProgressState
        )

        badgesViewModel.whatsAppShareableData.observe(this, {
            it?.getContentIfNotHandled()?.let {
                val (deepLink, imagePath, sharingMessage) = it

                deepLink?.let { shareOnWhatsApp(deepLink, imagePath, sharingMessage) }
                    ?: showBranchLinkError()
            }
        })

        badgesViewModel.showWhatsAppProgressLiveData.observe(this, {
            progress.setVisibleState(it)
        })

        badgesViewModel.navigateLiveData.observe(this, {
            val data = it?.getContentIfNotHandled()
            val screen = data?.screen
            val args = data?.hashMap
            if (screen == BadgeProgressDialogScreen) {
                screenNavigator.openDialogFromFragment(
                    this@BadgesActivity,
                    screen,
                    args?.toBundle(),
                    supportFragmentManager
                )
            } else if (screen != null) {
                screenNavigator.startActivityForResultFromActivity(
                    this@BadgesActivity,
                    screen,
                    args?.toBundle(),
                    REQUEST_CODE
                )

            }
        })
    }

    private fun onSuccess(badgeList: List<BaseBadgeViewType>) {
        setupBadgesRecyclerView(badgeList)
    }

    private fun updateProgressState(boolean: Boolean) {

    }

    private fun showBranchLinkError() {
        toast(getString(R.string.error_branchLinkNotFound))
        progress.hide()
    }

    private fun shareOnWhatsApp(imageUrl: String, imageFilePath: String?, sharingMessage: String?) {

        Intent(Intent.ACTION_SEND).apply {

            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            `package` = "com.whatsapp"
            putExtra(Intent.EXTRA_TEXT, "$sharingMessage $imageUrl")

            if (imageFilePath == null) {
                type = "text/plain"
            } else {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, Uri.parse(imageFilePath))
            }

        }.also {
            if (AppUtils.isCallable(this, it)) {
                startActivity(it)
            } else {
                ToastUtils.makeText(this, R.string.string_install_whatsApp, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setupBadgesRecyclerView(badgeList: List<BaseBadgeViewType>) {
        badgesList.adapter = BadgesViewAdapter(this)
        badgesList.addItemDecoration(DividerItemDecoration(this, HORIZONTAL))
        (badgesList.adapter as BadgesViewAdapter).updateBadges(badgeList)
    }

    private fun sendEvent(eventName: String) {
        val context = this@BadgesActivity
        context.apply {
            (context.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(context).toString())
                .addStudentId(getStudentId())
                .track()
        }
    }
}
