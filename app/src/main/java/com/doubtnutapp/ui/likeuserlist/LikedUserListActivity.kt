package com.doubtnutapp.ui.likeuserlist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.ActivityLikedUserListBinding
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.ui.ToolbarActivity

class LikedUserListActivity : ToolbarActivity<LikedUserViewModel, ActivityLikedUserListBinding>() {

    companion object {
        private const val TAG = "LikedUserListActivity"

        const val INTENT_EXTRA_FEED_ID = "feed_id"
        const val INTENT_EXTRA_FEED_TYPE = "feed_type"

        fun startActivity(context: Context, feedId: String, feedType: String) {
            val intent = Intent(context, LikedUserListActivity::class.java)
            intent.putExtra(INTENT_EXTRA_FEED_ID, feedId)
            intent.putExtra(INTENT_EXTRA_FEED_TYPE, feedType)
            context.startActivity(intent)
        }
    }

    override fun provideViewBinding(): ActivityLikedUserListBinding {
        return ActivityLikedUserListBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): LikedUserViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int {
        return R.color.redTomato
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setToolbar()
        init()
        getUsersList()
    }

    private fun init() {
        binding.recyclerViewUsersList.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewUsersList.addItemDecoration(
            DividerItemDecoration(
                this,
                RecyclerView.VERTICAL
            )
        )
    }

    private fun getUsersList() {
        val feedId = getFeedId().orEmpty()
        val feedType = getFeedType().orEmpty()

        viewModel.getUserList(feedId, feedType)

        viewModel.userList.observe(this, {
            when (it) {
                is Outcome.Progress -> {
                    manageProgressState(it.loading)
                }

                is Outcome.Success -> {
                    setListAdapter(it.data)
                }
                else -> {
                }
            }
        })
    }

    private fun manageProgressState(loading: Boolean) {
        if (loading) {
            binding.progressBar.show()
        } else {
            binding.progressBar.hide()
        }
    }

    private fun setListAdapter(usersList: List<LikedUser>) {
        binding.recyclerViewUsersList.adapter = LikedUserListAdapter(usersList)
    }

    private fun getFeedId() = intent.getStringExtra(INTENT_EXTRA_FEED_ID)

    private fun getFeedType() = intent.getStringExtra(INTENT_EXTRA_FEED_TYPE)
}
