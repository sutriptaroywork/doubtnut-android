package com.doubtnutapp.gamification.gamepoints.ui

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityGamePointsBinding
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandler
import com.doubtnutapp.gamification.gamepoints.model.ActionConfigDataItemDataModel
import com.doubtnutapp.gamification.gamepoints.model.GamePointsDataModel
import com.doubtnutapp.gamification.gamepoints.ui.adapter.GamePointsListAdapter
import com.doubtnutapp.gamification.gamepoints.ui.viewmodel.GamePointsViewModel
import com.doubtnutapp.screennavigator.EarnedPointsHistoryScreen
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.activity_game_points.*
import kotlinx.android.synthetic.main.item_my_earned_points_card_details.*
import javax.inject.Inject


class GamePointsActivity : BaseActivity(), ActionPerformer, HasAndroidInjector {

    companion object{
        const val REQUEST_CODE = 1212
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var networkErrorHandler: NetworkErrorHandler

    @Inject
    lateinit var screenNavigator: Navigator

    lateinit var gamePointViewModel: GamePointsViewModel


    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector() = dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        val binding: ActivityGamePointsBinding = DataBindingUtil.setContentView(this, R.layout.activity_game_points)
        statusbarColor(this, R.color.grey_statusbar_color)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        gamePointViewModel = ViewModelProviders.of(this, viewModelFactory).get(GamePointsViewModel::class.java)
        gamePointViewModel.getUserMilestoneAndGameActionData()
        setupObserver()

        binding.viewmodel = gamePointViewModel
        binding.setLifecycleOwner(this)
        binding.executePendingBindings()
        setUpListener()
        sendEvent(EventConstants.EVENT_NAME_POINTS_SCREEN)

    }

    override fun performAction(action: Any) {
        gamePointViewModel.handleAction(action)
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.filter_sheet) as ViewLevelInformationFragment
        if (fragment.isFragmentExpanded()) {
            fragment.collapseFragmentView()
        } else {
            super.onBackPressed()
        }
    }

    private fun setUpListener() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        viewHistoryTextView.setOnClickListener {
            gamePointViewModel.sendClickEvent(EventConstants.EVENT_NAME_VIEW_POINT_HISTORY, ignoreSnowplow = true)
            screenNavigator.startActivityFromActivity(this, EarnedPointsHistoryScreen, null)
        }
        badgeProgressView.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.filter_sheet) as ViewLevelInformationFragment
            fragment.expandFragmentView()
            gamePointViewModel.sendClickEvent(EventConstants.EVENT_NAME_VIEWLEVEL_INFO_TOP_CLICK, ignoreSnowplow = true)
        }
    }

    private fun setupObserver() {
        gamePointViewModel.gamePointsDataModel.observeK(this,
                this::onSuccess,
                networkErrorHandler::onApiError,
                networkErrorHandler::unAuthorizeUserError,
                networkErrorHandler::ioExceptionHandler,
                this::updateProgressHandler
        )
        gamePointViewModel.navigateLiveData.observe(this, Observer {
            val navigationData = it.getContentIfNotHandled()
            if (navigationData != null) {
                val args: Bundle? = navigationData.hashMap?.toBundle()
                this.let {
                    screenNavigator.startActivityForResultFromActivity(it, navigationData.screen, args, REQUEST_CODE)
                }

            }
        })
    }

    private fun onSuccess(gamePointsDataModel: GamePointsDataModel) {

        when {
            gamePointsDataModel.actionConfigData != null && gamePointsDataModel.actionConfigData.isNotEmpty() -> setupActionConfigDataList(gamePointsDataModel.actionConfigData)
        }

        viewHistoryTextView.text = gamePointsDataModel.historyText.replace("_", " ").capitalize()
        updateProgress(gamePointsDataModel.nextLevelPercentage)
    }

    private fun updateProgressHandler(state: Boolean) {
        progressbar.setVisibleState(state)
    }

    private fun setupActionConfigDataList(milestones: List<ActionConfigDataItemDataModel>) {
        gamepointsList.adapter = GamePointsListAdapter(this, milestones)

    }


    private fun sendEvent(eventName: String) {
        val context = this@GamePointsActivity
        context.apply {
            (context.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(context).toString())
                    .addStudentId(getStudentId())
                    .track()
        }
    }

    fun updateProgress(progressValue: Int) {
        val animation = ObjectAnimator.ofInt(progressBar4, "progress", progressValue)
        animation.duration = 850
        animation.interpolator = DecelerateInterpolator()
        animation.start()
    }
}
