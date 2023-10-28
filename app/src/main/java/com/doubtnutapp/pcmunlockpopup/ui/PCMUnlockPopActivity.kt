package com.doubtnutapp.pcmunlockpopup.ui

import android.os.Bundle
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityPcmunlockPopBinding
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandler
import com.doubtnutapp.domain.pcmunlockpopup.BadgeRequiredType.PC_BADGE_LOCK_DIALOG_VIEW
import com.doubtnutapp.domain.pcmunlockpopup.entity.PCMUnlockDataEntity
import com.doubtnutapp.pcmunlockpopup.ui.viewmodel.PCMUnlockPopViewModel
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.screennavigator.Screen
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.UserUtil.getStudentId
import javax.inject.Inject

class PCMUnlockPopActivity :
    BaseBindingActivity<PCMUnlockPopViewModel, ActivityPcmunlockPopBinding>() {

    @Inject
    lateinit var networkErrorHandler: NetworkErrorHandler

    @Inject
    lateinit var screenNavigator: Navigator

    override fun provideViewBinding(): ActivityPcmunlockPopBinding {
        return ActivityPcmunlockPopBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): PCMUnlockPopViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int {
        return android.R.color.transparent
    }

    override fun setupView(savedInstanceState: Bundle?) {
        viewModel.getUnlockData()

        binding.viewmodel = viewModel

        setupObserver()

        (this.applicationContext as DoubtnutApp).getEventTracker()
            .addEventNames(PC_BADGE_LOCK_DIALOG_VIEW)
            .addStudentId(getStudentId())
            .track()
    }

    private fun setupObserver() {
        viewModel.unlockDataLiveData.observeK(
            this,
            this::onSuccess,
            networkErrorHandler::onApiError,
            networkErrorHandler::unAuthorizeUserError,
            networkErrorHandler::ioExceptionHandler,
            this::updateProgressState
        )

        viewModel.navigateLiveData.observe(this, {
            it.getContentIfNotHandled()?.let { navigationModel ->
                sendEvent(navigationModel.screen)
                screenNavigator.startActivityFromActivity(
                    this@PCMUnlockPopActivity,
                    navigationModel.screen,
                    navigationModel.hashMap?.toBundle()
                )
            }
        })

        viewModel.finishActivityLiveData.observe(this, {
            it.getContentIfNotHandled()?.let {
                finish()
            }
        })

        viewModel.errorString.observe(this, {
            toast(it)
        })
    }

    private fun sendEvent(screen: Screen) {
        (this.applicationContext as DoubtnutApp).getEventTracker()
            .addEventNames(EventConstants.POP_UP_PC_CLICK + screen)
            .addStudentId(getStudentId())
            .track()
    }

    private fun updateProgressState(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }

    private fun onSuccess(pcmUnlockData: PCMUnlockDataEntity) {
        binding.group.show()
        binding.pcmUnlockData = pcmUnlockData
        binding.executePendingBindings()
    }

    companion object {
        const val TAG = "PCMUnlockPopActivity"
    }
}
