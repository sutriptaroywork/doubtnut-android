package com.doubtnutapp.studygroup.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.databinding.FragmentSgSettingBinding
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.loadImage
import com.doubtnutapp.studygroup.model.SgSetting
import com.doubtnutapp.studygroup.ui.activity.StudyGroupActivity
import com.doubtnutapp.studygroup.viewmodel.SgHomeViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import javax.inject.Inject

class SgSettingFragment : BaseBindingFragment<SgHomeViewModel, FragmentSgSettingBinding>(),
    View.OnClickListener {

    companion object {
        const val TAG = "SgSettingFragment"
    }

    @Inject
    lateinit var userPreference: UserPreference

    private val navController by findNavControllerLazy()

    private var blockListDeeplink: String = ""

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSgSettingBinding = FragmentSgSettingBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): SgHomeViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        mBinding?.ivUserImage?.loadImage(
            userPreference.getUserImageUrl(),
            R.drawable.ic_default_one_to_one_chat
        )
        mBinding?.tvUsername?.text = userPreference.getStudentName()
        viewModel.getSgSetting()
        viewModel.sendEvent(EventConstants.SG_SETTING_SCREEN_OPEN, ignoreSnowplow = true)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.settingLiveData.observe(viewLifecycleOwner) { sgSetting ->
            if (sgSetting != null) {
                setupUi(sgSetting)
            }
        }

        viewModel.messageLiveData.observe(this) { message ->
            toast(message, duration = Toast.LENGTH_SHORT)
        }
    }

    private fun setupUi(sgSetting: SgSetting) {
        mBinding?.apply {
            val notificationData = sgSetting.notificationContainer
            switchNotification.apply {
                isChecked = notificationData.toggle
                setOnCheckedChangeListener { _, isChecked ->
                    viewModel.muteNotification(
                        type = if (isChecked) 1 else 0,
                        action = StudyGroupActivity.ActionSource.GROUP_CHAT
                    )
                    viewModel.sendEvent(
                        EventConstants.SG_SETTING_NOTIFICATION_TOGGLE,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.TOGGLE, isChecked)
                        },
                        ignoreSnowplow = true
                    )
                    tvNotificationText.text = getNotificationText(isChecked)
                }
            }
            tvNotificationText.text = getNotificationText(notificationData.toggle)
            tvTitle.text = sgSetting.title
            val blockedData = sgSetting.blockListContainer
            blockListDeeplink = blockedData.deeplink

            blockedWrapperView.setOnClickListener(this@SgSettingFragment)
            tvBlockedText.apply {
                text = blockedData.title
            }
            tvBlockedCount.apply {
                text = blockedData.count
            }

            ivBack.setOnClickListener {
                navController.navigateUp()
            }

            val isImageAutoDownloadEnabled =
                defaultPrefs().getBoolean(Constants.SG_IMAGE_AUTO_DOWNLOAD, false);
            switchAutoDownload.isChecked = isImageAutoDownloadEnabled
            switchAutoDownload.setOnCheckedChangeListener { _, isChecked ->
                viewModel.sendEvent(
                    EventConstants.SG_IMAGE_AUTO_DOWNLOAD,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.IS_ENABLED, isChecked)
                    })
                defaultPrefs().edit {
                    putBoolean(Constants.SG_IMAGE_AUTO_DOWNLOAD, isChecked)
                }
            }
        }
    }

    private fun getNotificationText(status: Boolean): String {
        val text =
            if (status)
                resources.getString(R.string.sg_notification_on)
            else
                resources.getString(R.string.sg_notification_off)
        return String.format(resources.getString(R.string.sg_notification_with_bracket_text), text)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.blockedWrapperView -> {
                val deeplinkUri = Uri.parse(blockListDeeplink)
                if (navController.graph.hasDeepLink(deeplinkUri)) {
                    mayNavigate {
                        navigate(deeplinkUri)
                    }
                }
            }
        }
    }
}