package com.doubtnutapp.notification

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.NotificationClickAction
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.base.extension.viewBinding
import com.doubtnutapp.data.remote.models.NotificationCenterData
import com.doubtnutapp.databinding.ActivityNotificationsBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.utils.showApiErrorToast
import com.moengage.inbox.core.MoEInboxHelper
import com.moengage.inbox.core.listener.OnMessagesAvailableListener
import com.moengage.inbox.core.model.InboxMessage
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class NotificationCenterActivity : AppCompatActivity(), HasAndroidInjector,
    ActionPerformer {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private lateinit var viewModel: NotificationViewModel
    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener
    private lateinit var adapter: NotificationAdapter

    private val startPage = 1
    private var currentPage = 1
    private var source: String = HOME

    private val binding by viewBinding(ActivityNotificationsBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        viewModel = viewModelProvider(viewModelFactory)
        setupRecyclerView()
        setUpObserver()
        setupListeners()
        getMoeNotificationData(startPage)
        binding.rvNotifiCation.addOnScrollListener(infiniteScrollListener)
        source = intent?.extras?.getString(SOURCE) ?: HOME
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.NOTIFICATION_ICON_CLICKED,
                hashMapOf(
                    EventConstants.SOURCE to source
                ), ignoreSnowplow = true
            )
        )
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(null, this, deeplinkAction, this)
        binding.rvNotifiCation.adapter = adapter
    }

    @SuppressLint("CheckResult")
    fun getMoeNotificationData(pageNo: Int) {
        MoEInboxHelper.getInstance()
            .fetchAllMessagesAsync(this, object : OnMessagesAvailableListener {
                override fun onMessagesAvailable(messageList: List<InboxMessage>) {
                    viewModel.getNotifications(messageList, pageNo)
                }
            })
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
        infiniteScrollListener =
            object : TagsEndlessRecyclerOnScrollListener(binding.rvNotifiCation.layoutManager) {

                override fun onLoadMore(currentPage: Int) {
                    getMoeNotificationData(currentPage)
                    this@NotificationCenterActivity.currentPage++
                }
            }.apply {
                setStartPage(startPage)
                this@NotificationCenterActivity.currentPage = 1
            }
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        finish()
    }

    private fun setUpObserver() {
        viewModel.notificationLiveData.observeK(
            this@NotificationCenterActivity,
            this::onNotificationFetched,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        showApiErrorToast(this)
    }

    private fun unAuthorizeUserError() {
        showApiErrorToast(this)
    }

    private fun updateProgress(state: Boolean) {
        infiniteScrollListener.setDataLoading(state)
        binding.progressBar.setVisibleState(state)
    }

    companion object {
        const val REQUEST_CODE_NOTIFICATION = 108
        const val SOURCE = "source"
        const val HOME = "home"
        const val LIBRARY = "library"
        const val FEED = "feed"
        const val USER_PROFILE = "profile"
        fun getStartIntent(context: Context, source: String): Intent {
            val intent = Intent(context, NotificationCenterActivity::class.java)
            intent.putExtra(SOURCE, source)
            return intent
        }
    }

    fun setNotificationData(notificationList: List<NotificationCenterData>?) {
        if (notificationList.isNullOrEmpty() && currentPage == 1) {
            binding.tvNoData.visibility = View.VISIBLE
        } else {
            binding.tvNoData.visibility = View.GONE
            adapter.updateList(notificationList)
        }
    }

    private fun onNotificationFetched(list: List<NotificationCenterData>) {
        setNotificationData(list)
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    override fun performAction(action: Any) {
        val params: HashMap<String, Any> = hashMapOf(
            EventConstants.SOURCE to source
        )
        if (action is NotificationClickAction) {
            if (action.id != null && action.isClicked == 0) {
                viewModel.updateClickedNotifications(action.id)
            }
            action.type?.let {
                params.put(EventConstants.TYPE, action.type)
            }
        }
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.NOTIFICATION_ITEM_CLICKED,
                params,
                ignoreSnowplow = true
            )
        )
    }
}