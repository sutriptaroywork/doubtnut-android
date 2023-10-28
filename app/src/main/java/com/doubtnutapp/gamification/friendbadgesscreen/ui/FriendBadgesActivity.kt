package com.doubtnutapp.gamification.friendbadgesscreen.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityFriendBadgesBinding
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandler
import com.doubtnutapp.gamification.friendbadgesscreen.model.FriendBadge
import com.doubtnutapp.gamification.friendbadgesscreen.ui.adapter.FriendBadgesListAdapter
import com.doubtnutapp.gamification.friendbadgesscreen.ui.viewmodel.FriendBadgesViewModel
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import dagger.android.AndroidInjection
import javax.inject.Inject

class FriendBadgesActivity : AppCompatActivity()  {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var networkErrorHandler: NetworkErrorHandler

    private lateinit var friendBadgesViewModel: FriendBadgesViewModel
    private var userId: String = ""

    private lateinit var binding : ActivityFriendBadgesBinding


    companion object {

        private const val INTENT_EXTRA_USER_ID = "user_id"

        fun startActivity(context: Context, userId: String): Intent {
            return Intent(context, FriendBadgesActivity::class.java).apply {
                putExtra(INTENT_EXTRA_USER_ID, userId)

            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityFriendBadgesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        statusbarColor(this, R.color.grey_statusbar_color)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        friendBadgesViewModel = ViewModelProviders.of(this, viewModelFactory).get(FriendBadgesViewModel::class.java)
        init()
        setupObserver()
        setUpListener()
        sendEvent(EventConstants.EVENT_NAME_OTHERS_BADGE_SCREEN)

    }

    fun init() {
        this.userId = intent.getStringExtra(INTENT_EXTRA_USER_ID).orEmpty()
        friendBadgesViewModel.getUserBadges(this.userId)

    }

    private fun setUpListener() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }


    private fun setupObserver() {
        friendBadgesViewModel.friendBadgesLiveData.observeK(this,
                this::onSuccess,
                networkErrorHandler::onApiError,
                networkErrorHandler::unAuthorizeUserError,
                networkErrorHandler ::ioExceptionHandler,
                this::updateProgressState)
    }

    private fun onSuccess(badgeList: List<FriendBadge>) {
        setupBadgesRecyclerView(badgeList)
    }

    private fun updateProgressState(state: Boolean) {
        binding.progressbar.setVisibleState(state)
    }

    private fun setupBadgesRecyclerView(friendBadgeList: List<FriendBadge>) {
        binding.friendBadgesList.adapter = FriendBadgesListAdapter(friendBadgeList)
        binding.friendBadgesList.addItemDecoration(DividerItemDecoration(this, RecyclerView.VERTICAL))

    }

    private fun sendEvent(eventName: String) {
        val context = this@FriendBadgesActivity
        context?.apply {
            (context?.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(context!!).toString())
                    .addStudentId(getStudentId())
                    .track()
        }
    }
}
