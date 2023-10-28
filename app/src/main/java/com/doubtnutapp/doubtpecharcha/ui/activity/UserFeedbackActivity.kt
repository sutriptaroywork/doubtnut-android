package com.doubtnutapp.doubtpecharcha.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.core.data.remote.Outcome
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.DoubtP2PMember
import com.doubtnutapp.databinding.ActivityUserFeedbackBinding
import com.doubtnutapp.doubtpecharcha.model.FeedbackUserListResponse
import com.doubtnutapp.doubtpecharcha.ui.adapter.UserFeedbackRatingAdapter
import com.doubtnutapp.doubtpecharcha.ui.fragment.UserSelectForFeedbackFragment
import com.doubtnutapp.doubtpecharcha.ui.fragment.UserSelectForFeedbackFragmentDirections
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.showToast

class UserFeedbackActivity :
    BaseBindingActivity<UserFeedbackActivityViewModel, ActivityUserFeedbackBinding>() {

    companion object {
        const val TAG = "UserFeedbackActivity"
        const val MEMBERS = "Members"
        const val SHOW_RATING_PAGE = "show_rating_page"
        const val ROOM_ID = "room_id"
        const val STUDENT_ID = "student_id"
        fun getStartIntent(
            context: Context,
            listMembers: ArrayList<DoubtP2PMember>,
            roomId: String,
            studentId: String,
            showFeedbackRatingPage: Boolean
        ): Intent {
            val intent = Intent(context, UserFeedbackActivity::class.java)
            intent.putParcelableArrayListExtra(MEMBERS, listMembers)
            intent.putExtra(SHOW_RATING_PAGE, showFeedbackRatingPage)
            intent.putExtra(STUDENT_ID, studentId)
            intent.putExtra(ROOM_ID, roomId)
            return intent
        }
    }

    lateinit var navController: NavController

    var moveToFeedbackRatingPage: Boolean? = false

    val membersList: java.util.ArrayList<DoubtP2PMember>? by lazy {
        intent.getParcelableArrayListExtra(MEMBERS)
    }

    val showFeedbackRatingPage: Boolean? by lazy {
        intent.getBooleanExtra(SHOW_RATING_PAGE, false)
    }

    val roomId: String? by lazy {
        intent?.getStringExtra(ROOM_ID)
    }

    val studentId: String? by lazy {
        intent?.getStringExtra(STUDENT_ID)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        removeHostItem()

        membersList?.let {
            val bundle = bundleOf(
                "Members" to membersList,
                ROOM_ID to roomId
            )
            val navController =
                (supportFragmentManager.findFragmentById(R.id.frameLayoutFragment) as NavHostFragment).findNavController()
            navController.setGraph(R.navigation.nav_graph_doubt_pe_charcha_feedback, bundle)
        }
    }

    private fun removeHostItem() {
        var hostItemToBeRemoved: DoubtP2PMember? = null
        membersList?.forEach {
            if (it.isHost == 1) {
                hostItemToBeRemoved = it
            }
        }
        hostItemToBeRemoved?.let {
            membersList?.remove(it)
        }

    }

    override fun provideViewBinding(): ActivityUserFeedbackBinding {
        return ActivityUserFeedbackBinding.inflate(layoutInflater, null, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): UserFeedbackActivityViewModel {
        return viewModelProvider<UserFeedbackActivityViewModel>(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {

        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.frameLayoutFragment.id) as NavHostFragment
        navController = navHostFragment.navController
        navController.setGraph(R.navigation.nav_graph_doubt_pe_charcha_feedback)


        binding.tvSkip.setOnClickListener {
            finish()
        }

        binding.ivBack.setOnClickListener {
            finish()
        }

        showFeedbackRatingPage?.let {
            viewModel.moveDirectlyToRatingFragment = it
        }

        binding.tvSkip.post {
            if (viewModel.moveDirectlyToRatingFragment) {
                val bundle = bundleOf(
                    ROOM_ID to roomId,
                    STUDENT_ID to studentId
                )
                navController.navigate(
                    R.id.action_userSelectForFeedbackFragment_to_doubtPeCharchaUserFeedbackRatingFragment,
                    bundle
                )
                viewModel.moveDirectlyToRatingFragment = false
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
    }


}