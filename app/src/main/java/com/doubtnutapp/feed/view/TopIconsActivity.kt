package com.doubtnutapp.feed.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.ViewUtils.screenWidth
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.ActivityTopIconsBinding
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.feed.viewmodel.TopIconsViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.showApiErrorToast
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter

class TopIconsActivity : BaseBindingActivity<TopIconsViewModel, ActivityTopIconsBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "TopIconsActivity"
        const val EXTRA_PARAM_SCREEN = "screen"
        fun getStartIntent(context: Context, screen: String?) =
            Intent(context, TopIconsActivity::class.java)
                .apply {
                    putExtra(EXTRA_PARAM_SCREEN, screen)
                }
    }

    private val screen: String? by lazy {
        intent.getStringExtra(EXTRA_PARAM_SCREEN)
    }

    private val widgetLayoutAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(
            context = this@TopIconsActivity,
            actionPerformer = this,
            source = TAG
        )
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(): ActivityTopIconsBinding =
        ActivityTopIconsBinding.inflate(layoutInflater)

    override fun provideViewModel(): TopIconsViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        binding.rvTopOptions.adapter = widgetLayoutAdapter
        binding.ivBack.setOnClickListener {
            finish()
        }
        getAllTopIcons()
    }

    private fun getAllTopIcons() {
        val assortmentId = defaultPrefs().getString(Constants.SELECTED_ASSORTMENT_ID, "") ?: ""
        viewModel.fetchAllHomeTopIconsData(screen.orEmpty(), assortmentId, screenWidth())
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.topIconsData.observe(this) { outcome ->
            when (outcome) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = outcome.loading
                }
                is Outcome.Success -> {
                    val optionList = outcome.data.widgets.orEmpty()
                    binding.apply {
                        progressBar.isVisible = false
                        tvTitle.text = outcome.data.title
                        widgetLayoutAdapter.addWidgets(optionList)
                    }
                }
                is Outcome.ApiError -> {
                    binding.progressBar.isVisible = false
                    apiErrorToast(outcome.e)
                }
                else -> {
                    showApiErrorToast(this@TopIconsActivity)
                }
            }
        }
    }

    override fun performAction(action: Any) {}
}