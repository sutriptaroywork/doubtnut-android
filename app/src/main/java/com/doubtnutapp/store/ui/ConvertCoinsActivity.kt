package com.doubtnutapp.store.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityConvertCoinsBinding
import com.doubtnutapp.store.dto.ConvertCoinsResultDTO
import com.doubtnutapp.store.viewmodel.ConvertCoinsViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.showToast
import dagger.android.HasAndroidInjector

class ConvertCoinsActivity :
    BaseBindingActivity<ConvertCoinsViewModel, ActivityConvertCoinsBinding>(), View.OnClickListener,
    HasAndroidInjector {


    override fun onClick(v: View?) {
        when (v) {
            binding.closeButton -> {
                finish()
            }
            binding.convertCoinsButton -> {
                viewModel.convertCoins()
            }
        }
    }

    companion object {
        private const val TAG = "ConvertCoinsActivity"

        private const val UNUSED_POINTS = "unused_points"
        fun startActivity(context: Context, unUsedPoints: Int) {
            Intent(context, ConvertCoinsActivity::class.java).also {
                it.putExtra(UNUSED_POINTS, unUsedPoints)
                context.startActivity(it)
            }
        }
    }

    override fun provideViewBinding(): ActivityConvertCoinsBinding =
        ActivityConvertCoinsBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): ConvertCoinsViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        extractIntentParams()
        updateUi()
        setListeners()
        setUpObservers()
    }

    private var unUsedPoints: Int = 0


    private fun updateUi() {
        binding.totalPoints.text = "$unUsedPoints Points"
    }

    private fun extractIntentParams() {
        intent?.apply {
            unUsedPoints = getIntExtra(UNUSED_POINTS, 0)
        }
    }

    private fun setUpObservers() {
        viewModel.convertCoinsLiveData.observeK(
            this,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )
    }


    private fun setListeners() {
        binding.closeButton.setOnClickListener(this)
        binding.convertCoinsButton.setOnClickListener(this)
    }

    private fun onSuccess(convertCoinsResultDTO: ConvertCoinsResultDTO) {
        if (convertCoinsResultDTO.isConverted) {
            showToast(this, convertCoinsResultDTO.message)
            finish()
        }
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun updateProgressBarState(state: Boolean) {

    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }
}
