package com.doubtnutapp.feed.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ActivityLiveFeedBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.feed.viewmodel.FeedViewModel
import com.doubtnutapp.statusbarColor
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.google.android.material.tabs.TabLayout
import dagger.android.HasAndroidInjector

class LiveFeedActivity : BaseBindingActivity<DummyViewModel, ActivityLiveFeedBinding>(),
    HasAndroidInjector {

    companion object {

        const val TYPE_LIVE = "live"
        const val TYPE_LIVE_UPCOMING = "upcoming"
        private const val TAG = "LiveFeedActivity"

        fun getStartIntent(context: Context, type: String?, start: Boolean = false): Intent {
            val intent = Intent(context, LiveFeedActivity::class.java).apply {
                if (type != null) {
                    putExtra(Constants.TYPE, type)
                } else {
                    putExtra(Constants.TYPE, TYPE_LIVE)
                }
            }
            return intent.also { if (start) context.startActivity(it) }
        }
    }

    private fun setupTabs() {
        val type = intent.getStringExtra(Constants.TYPE)
        if (type == TYPE_LIVE) {
            binding.tabLayout.getTabAt(0)?.select()
            replaceFragment(FeedFragment.newLiveFeedInstance(FeedViewModel.FEED_LIVE))
        } else if (type == TYPE_LIVE_UPCOMING) {
            binding.tabLayout.getTabAt(1)?.select()
            replaceFragment(FeedFragment.newLiveFeedInstance(FeedViewModel.FEED_LIVE_UPCOMING))
        }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    replaceFragment(FeedFragment.newLiveFeedInstance(FeedViewModel.FEED_LIVE))
                } else if (tab.position == 1) {
                    replaceFragment(FeedFragment.newLiveFeedInstance(FeedViewModel.FEED_LIVE_UPCOMING))
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }
        })
    }

    private fun replaceFragment(fragment: FeedFragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            super.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun provideViewBinding(): ActivityLiveFeedBinding {
        return ActivityLiveFeedBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        statusbarColor(this, R.color.colorSecondaryDark)
        binding.toolBar.title = "Live"
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupTabs()
    }
}