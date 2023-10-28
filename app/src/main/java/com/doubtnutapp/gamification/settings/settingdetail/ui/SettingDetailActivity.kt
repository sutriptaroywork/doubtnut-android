package com.doubtnutapp.gamification.settings.settingdetail.ui

import android.os.Bundle
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityWebViewNewBinding
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandler
import com.doubtnutapp.gamification.settings.settingdetail.model.SettingDetails
import com.doubtnutapp.gamification.settings.settingdetail.viewmodel.SettingDetailViewModel
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnut.core.utils.viewModelProvider
import javax.inject.Inject

/**
 * Created by shrreya on 2/7/19.
 */
class SettingDetailActivity :
    BaseBindingActivity<SettingDetailViewModel, ActivityWebViewNewBinding>() {

    @Inject
    lateinit var screenNavigator: Navigator

    @Inject
    lateinit var networkErrorHandler: NetworkErrorHandler

    override fun provideViewBinding(): ActivityWebViewNewBinding {
        return ActivityWebViewNewBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): SettingDetailViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int {
        return R.color.grey_statusbar_color
    }

    override fun setupView(savedInstanceState: Bundle?) {
        checkScreenClicked()
        setUpObserver()
    }

    private fun checkScreenClicked() {
        val screen = intent.getStringExtra(Constants.PAGE_NAME).orEmpty()
        viewModel.checkScreen(screen)
    }

    private fun setUpObserver() {
        viewModel.settingsLiveData.observeK(
            this,
            this::onSettingDetail,
            networkErrorHandler::onApiError,
            networkErrorHandler::ioExceptionHandler,
            networkErrorHandler::unAuthorizeUserError,
            this::updateProgressBarState
        )
    }

    private fun updateProgressBarState(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }

    private fun onSettingDetail(settingList: SettingDetails) {
        binding.webviewSetting.loadDataWithBaseURL(
            null,
            settingList.dataValue,
            "text/HTML", "UTF-8", null
        )
    }

    companion object {
        const val TAG = "SettingDetailActivity"
    }
}