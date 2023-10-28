package com.doubtnutapp.ui.groupChat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.ApplicationStateEvent
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.ActivityGroupChatBinding
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.utils.UserUtil.getStudentId
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class GroupChatActivity : BaseActivity(), HasAndroidInjector {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: GroupChatViewModel
    internal lateinit var adapter: GroupChatAdapter
    private var appStateObserver: Disposable? = null

//    @Inject
//    lateinit var eventManager: LiveVoiceEventManager

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    private lateinit var binding: ActivityGroupChatBinding

    override fun androidInjector() = dispatchingAndroidInjector

    companion object {
        const val REQUEST_CODE = 1001

        fun startForResult(activity: Activity) {
            val intent = Intent(activity.applicationContext, GroupChatActivity::class.java)
            ActivityCompat.startActivityForResult(activity, intent, REQUEST_CODE, null)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = viewModelProvider(viewModelFactory)
        setToolbar()
        setObservers()
        setRecyclerView()
        appStateObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is ApplicationStateEvent) {
                viewModel.isApplicationBackground = !event.state
            }
        }
        viewModel.setupEngagementTracking()
    }

    private fun setToolbar() {
        setSupportActionBar(binding.toolbarGroupChat)
        supportActionBar!!.title = getString(R.string.group_chat)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }

    private fun setObservers() {

        viewModel.getGroupList().observe(this, { response ->
            when (response) {
                is Outcome.Progress -> {
                    binding.progressBarGroupChat.visibility = View.VISIBLE
                }
                is Outcome.Failure -> {
                    binding.progressBarGroupChat.visibility = View.GONE
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(supportFragmentManager, "NetworkErrorDialog")
                    binding.rvGroupChatList.visibility = View.INVISIBLE
                }
                is Outcome.ApiError -> {
                    binding.progressBarGroupChat.visibility = View.GONE
                    toast(getString(R.string.api_error))
                    binding.rvGroupChatList.visibility = View.INVISIBLE
                }
                is Outcome.BadRequest -> {
                    binding.progressBarGroupChat.visibility = View.GONE
                    val dialog = BadRequestDialog.newInstance("unauthorized")
                    dialog.show(supportFragmentManager, "BadRequestDialog")
                    binding.rvGroupChatList.visibility = View.INVISIBLE
                }
                is Outcome.Success -> {
                    binding.progressBarGroupChat.visibility = View.GONE
                    adapter.updateData(response.data.data)
                }
            }
        })
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStop()
    }

    private fun setRecyclerView() {

        adapter = GroupChatAdapter(this)

        val layoutManager = LinearLayoutManager(this)
        binding.rvGroupChatList.layoutManager = layoutManager
        binding.rvGroupChatList.adapter = adapter

        binding.rvGroupChatList.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                val intent = Intent(this@GroupChatActivity, LiveChatActivity::class.java)
                intent.putExtra(
                    Constants.INTENT_EXTRA_ENTITY_ID,
                    adapter.groupList[position].groupId
                )
                intent.putExtra(
                    Constants.INTENT_EXTRA_ENTITY_TYPE,
                    adapter.groupList[position].groupType
                )
                intent.putExtra(
                    Constants.INTENT_EXTRA_GROUP_NAME,
                    adapter.groupList[position].groupName
                )
                startActivity(intent)
                sendEvent(EventConstants.EVENT_NAME_GROUP_CLICK_TO_CHAT + adapter.groupList[position].groupName)
                viewModel.eventWith(
                    EventConstants.EVENT_NAME_GROUP_CLICK_TO_CHAT, hashMapOf(
                        Constants.INTENT_EXTRA_GROUP_NAME to adapter.groupList[position].groupName,
                        Constants.GROUP_ID to adapter.groupList[position].groupId,
                        EventConstants.TIME_STAMP to System.currentTimeMillis()
                    )
                )

            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                sendEvent(EventConstants.EVENT_NAME_BACK_FROM_GROUP_CHAT)

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sendEvent(eventName: String) {
        this@GroupChatActivity.apply {
            (this@GroupChatActivity.applicationContext as DoubtnutApp).getEventTracker()
                .addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@GroupChatActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_GROUP_CHAT_ACTIVITY)
                .track()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        appStateObserver?.dispose()
    }
}