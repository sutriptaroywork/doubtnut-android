package com.doubtnutapp.quicksearch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import com.apxor.androidsdk.core.Attributes
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.databinding.ActivityQuickSearchSettingBinding
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.doubtnutapp.quicksearch.viewmodel.NotificationSettingViewModel
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.utils.ApxorUtils
import dagger.android.AndroidInjection
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2020-01-31.
 */
class QuickSearchSettingActivity : BaseActivity() {

    companion object {
        fun getStartIntent(context: Context) = Intent(context, QuickSearchSettingActivity::class.java)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: NotificationSettingViewModel by viewModels { viewModelFactory }

    private lateinit var binding: ActivityQuickSearchSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityQuickSearchSettingBinding.inflate(layoutInflater)
        setContentView( binding.root)
        statusbarColor(this, R.color.grey)

        setUpObservers()

        binding.buttonBack.setOnClickListener {
            onBackPressed()
        }
        binding.switchQuickSearch.isChecked = !defaultPrefs(this).getBoolean(NotificationConstants.NOTIFICATION_USER_DISMISS_QUICK_SEARCH, false)
        binding.switchQuickSearch.setOnCheckedChangeListener { _, state ->
            if (state) {
                defaultPrefs(this).edit {
                    putBoolean(NotificationConstants.NOTIFICATION_USER_DISMISS_QUICK_SEARCH, false)
                }
                ApxorUtils.logAppEvent("quick_search_selected", Attributes())
            } else {
                defaultPrefs(this).edit {
                    putBoolean(NotificationConstants.NOTIFICATION_USER_DISMISS_QUICK_SEARCH, true)
                }
                ApxorUtils.logAppEvent("quick_search_deselected", Attributes())
            }
            DoubtnutApp.INSTANCE.handleStickyNotification()
        }

        binding.switchVideoStickyNotification.show()
        binding.textViewVideoStickyNotification.show()

        binding.switchVideoStickyNotification.isChecked = !defaultPrefs(this).getBoolean(NotificationConstants.NOTIFICATION_USER_DISMISS_VIDEO_STICKY, false)
        binding.switchVideoStickyNotification.setOnCheckedChangeListener { _, state ->
            if (state) {
                defaultPrefs(this).edit {
                    putBoolean((NotificationConstants.NOTIFICATION_USER_DISMISS_VIDEO_STICKY), false)
                }
                ApxorUtils.logAppEvent(EventConstants.VIDEO_STICKY_SELECTED, Attributes())
            } else {
                defaultPrefs(this).edit {
                    putBoolean(NotificationConstants.NOTIFICATION_USER_DISMISS_VIDEO_STICKY, true)
                }
                ApxorUtils.logAppEvent(EventConstants.VIDEO_STICKY_DESELECTED)
            }
        }

        // will use in next version
//        setUpDoubtP2pSetting()

        // Study Group Notification
        setUpStudyGroupNotification()
    }

    private fun setUpObservers() {
        viewModel.messageLiveData.observe(this, { message ->
            toast(message)
        })
    }

    private fun setUpStudyGroupNotification() {
        binding.switchStudyGroupNotification.isChecked = viewModel.isStudyGroupNotificationMuted().not()
        binding.switchStudyGroupNotification.setOnCheckedChangeListener { _, isChecked ->
            viewModel.muteNotification(type = if (isChecked) 1 else 0)
            viewModel.publishEvent(EventConstants.SG_NOTIFICATION, hashMapOf<String, Any>().apply {
                put(EventConstants.IS_MUTE, isChecked.not())
                put(EventConstants.SOURCE, "notification_setting")
            }, ignoreSnowplow = true)
        }
    }

    private fun setUpDoubtP2pSetting() {
        val isDoubtP2pEnabled = viewModel.isDoubtP2pNotificationEnabled()
        if (isDoubtP2pEnabled) {
            binding.switchDoubtP2pNotification.show()
            binding.textViewDoubtP2pNotification.show()
            binding.switchDoubtP2pNotification.isChecked = viewModel.isDoubtP2pNotificationEnabled()

            binding.switchDoubtP2pNotification.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setDoubtP2pNotificationEnabled(isChecked)
                viewModel.publishEvent(EventConstants.P2P_NOTIFICATION, hashMapOf<String, Any>().apply {
                    put(EventConstants.ENABLE, isChecked)
                })
            }

        } else {
            binding.switchDoubtP2pNotification.hide()
            binding.textViewDoubtP2pNotification.hide()
        }
    }
}
