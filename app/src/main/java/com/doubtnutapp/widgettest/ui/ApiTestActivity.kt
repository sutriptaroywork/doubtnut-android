package com.doubtnutapp.widgettest.ui

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.data.DefaultDataStoreImpl
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.databinding.ActivityApiTestBinding
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.widgettest.data.DummyJson.DUMMY_WIDGET_JSON
import com.doubtnutapp.widgettest.viewmodel.ApiTestViewModel
import com.google.android.material.tabs.TabLayoutMediator
import javax.inject.Inject

class ApiTestActivity :
    BaseBindingActivity<ApiTestViewModel, ActivityApiTestBinding>() {

    companion object {
        private const val TAG = "ApiTestActivity"
    }

    @Inject
    lateinit var defaultDataStore: DefaultDataStore

    override fun provideViewBinding(): ActivityApiTestBinding =
        ActivityApiTestBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): ApiTestViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {

        val adapter = WidgetTestAdapter(this)
        binding.viewpager.adapter = adapter
        binding.viewpager.isUserInputEnabled = false

        lifecycleScope.launchWhenCreated {
            defaultDataStore.set(
                DefaultDataStoreImpl.COURSE_CARD_BALLOON_IDS_SHOWN,
                ""
            )
        }

        TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, position ->
            tab.text = when (position) {
                0 -> {
                    "Test Widgets"
                }
                1 -> {
                    "Preview Json"
                }
                else -> ""
            }
        }.attach()

        viewModel.fetch(DUMMY_WIDGET_JSON)

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }
}