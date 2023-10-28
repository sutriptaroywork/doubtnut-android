package com.doubtnutapp.profile.social

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ActivityUserRelationshipsBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.google.android.material.tabs.TabLayout

class UserRelationshipsActivity :
    BaseBindingActivity<DummyViewModel, ActivityUserRelationshipsBinding>() {

    companion object {
        const val TAG = "UserRelationshipsActivity"
        const val TYPE_FOLLOWERS = "followers"
        const val TYPE_FOLLOWING = "following"

        fun getStartIntent(
            context: Context,
            userId: String,
            username: String,
            type: String
        ): Intent {
            return Intent(context, UserRelationshipsActivity::class.java).apply {
                putExtra(Constants.STUDENT_ID, userId)
                putExtra(Constants.STUDENT_USER_NAME, username)
                putExtra(Constants.TYPE, type)
            }
        }
    }

    override fun provideViewBinding(): ActivityUserRelationshipsBinding {
        return ActivityUserRelationshipsBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        binding.toolBar.title = intent.getStringExtra(Constants.STUDENT_USER_NAME)
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupTabs()
    }

    override fun getStatusBarColor(): Int {
        return R.color.colorSecondaryDark
    }

    private fun setupTabs() {
        val userId = intent.getStringExtra(Constants.STUDENT_ID) ?: return

        val type = intent.getStringExtra(Constants.TYPE)
        if (type == TYPE_FOLLOWERS) {
            binding.tabLayout.getTabAt(0)?.select()
            replaceFragment(UserRelationshipFragment.newInstance(TYPE_FOLLOWERS, userId))
        } else if (type == TYPE_FOLLOWING) {
            binding.tabLayout.getTabAt(1)?.select()
            replaceFragment(UserRelationshipFragment.newInstance(TYPE_FOLLOWING, userId))
        }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    replaceFragment(UserRelationshipFragment.newInstance(TYPE_FOLLOWERS, userId))
                } else if (tab.position == 1) {
                    replaceFragment(UserRelationshipFragment.newInstance(TYPE_FOLLOWING, userId))
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }
        })
    }

    private fun replaceFragment(fragment: UserRelationshipFragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            super.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

}