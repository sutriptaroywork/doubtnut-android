package com.doubtnutapp.liveclass.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.VipStateEvent
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ActivityBundleBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import io.reactivex.disposables.Disposable

class NudgeActivity : BaseBindingActivity<DummyViewModel, ActivityBundleBinding>() {

    companion object {
        const val TAG = "NudgeActivity"

        fun getStartIntent(
            context: Context,
            id: String,
            // nudgeTypes is used in event so far.
            nudgeType: String,
            page: String?,
            // type is used in api calls.
            type: String?,
            isTransparent: Boolean
        ) =
            Intent(context, NudgeActivity::class.java).apply {
                putExtra(Constants.NUDGE_ID, id)
                putExtra(Constants.NUDGE_TYPE, nudgeType)
                putExtra(Constants.PAGE, page)
                putExtra(Constants.TYPE, type)
                putExtra(NudgeFragment.IS_TRANSPARENT, isTransparent)
            }
    }

    private var fragment: NudgeFragment? = null
    private var vipObserver: Disposable? = null

    override fun provideViewBinding(): ActivityBundleBinding {
        return ActivityBundleBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int {
        return R.color.grey_light
    }

    override fun setupView(savedInstanceState: Bundle?) {
        val id = intent.getStringExtra(Constants.NUDGE_ID).orEmpty()
        val nudgeType = intent.getStringExtra(Constants.NUDGE_TYPE).orEmpty()
        val page = intent.getStringExtra(Constants.PAGE).orEmpty()
        val type = intent.getStringExtra(Constants.TYPE).orEmpty()

        val isTransparent = intent.getBooleanExtra(NudgeFragment.IS_TRANSPARENT, true)
        fragment = NudgeFragment.newInstance(id, nudgeType, page, type, isTransparent)
        fragment?.show(supportFragmentManager, "")
        supportFragmentManager.executePendingTransactions()
        fragment?.dialog?.setOnCancelListener {
            finish()
        }
        fragment?.dialog?.setOnDismissListener {
            finish()
        }

        vipObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is VipStateEvent) {
                if (event.state) {
                    finish()
                }
            }
        }

        binding.parentContainer.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vipObserver?.dispose()
    }

}