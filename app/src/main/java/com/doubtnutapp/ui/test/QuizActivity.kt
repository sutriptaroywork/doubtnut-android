package com.doubtnutapp.ui.test

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.ActivityTestBinding
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.utils.TestUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.instacart.library.truetime.TrueTimeRx
import java.util.*

class QuizActivity : BaseBindingActivity<TestViewModel, ActivityTestBinding>() {

    private lateinit var adapter: TestListAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun provideViewBinding(): ActivityTestBinding {
        return ActivityTestBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): TestViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int {
        return R.color.redTomato
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setupRecyclerView()
        setUpListener()
    }

    override fun onStart() {
        super.onStart()
        setObservers()
    }

    private fun setupRecyclerView() {
        adapter = TestListAdapter(this)
        layoutManager = LinearLayoutManager(this)
        binding.recyclerViewTest.layoutManager = layoutManager
        binding.recyclerViewTest.adapter = adapter
    }

    private fun setUpListener() {
        binding.recyclerViewTest.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {

                val testSubscriptionId = adapter.items[position].testSubscriptionId?.let {
                    if (it.isEmpty()) {
                        0
                    } else {
                        it.toInt()
                    }
                } ?: 0

                sendEvent(EventConstants.EVENT_NAME_QUIZ_SELECTED)
                startTestQuestionActivity(position, testSubscriptionId)
            }
        })

        binding.imageViewTestActivityBack.setOnClickListener {
            super.onBackPressed()
        }
    }

    private fun setObservers() {
        viewModel.getTestDetails(applicationContext).observe(this, { response ->
            when (response) {
                is Outcome.Progress -> {
                    binding.progressBarTestList.visibility = View.VISIBLE
                }
                is Outcome.Failure -> {
                    binding.progressBarTestList.visibility = View.GONE
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(supportFragmentManager, "NetworkErrorDialog")
                }
                is Outcome.ApiError -> {
                    binding.progressBarTestList.visibility = View.GONE
                    apiErrorToast(response.e)
                }
                is Outcome.BadRequest -> {
                    binding.progressBarTestList.visibility = View.GONE
                    val dialog = BadRequestDialog.newInstance("unauthorized")
                    dialog.show(supportFragmentManager, "BadRequestDialog")
                }
                is Outcome.Success -> {
                    binding.progressBarTestList.visibility = View.GONE

                    if (response.data.data.isNotEmpty()) {
                        adapter.updateData(response.data.data)
                    } else {
                        binding.recyclerViewTest.visibility = View.GONE
                        binding.tvNoTest.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    private fun startTestQuestionActivity(position: Int, testSubscriptionId: Int) {
        val flag = getTimeFlag(position)
        val questionIntentObject = TestQuestionActivity.newIntent(
            this@QuizActivity,
            adapter.items[position], flag, testSubscriptionId, false
        )
        startActivity(questionIntentObject)
    }

    private fun getTimeFlag(position: Int): String {
        return when {
            adapter.items[position].attemptCount != 0 -> Constants.USER_CANNOT_ATTEMPT_TEST
            else -> TestUtils.getTrueTimeDecision(
                adapter.items[position].publishTime,
                adapter.items[position].unpublishTime,
                now = when {
                    TrueTimeRx.isInitialized() -> TrueTimeRx.now()
                    else -> Calendar.getInstance().time
                }
            )
        }
    }

    private fun sendEvent(eventName: String) {
        this@QuizActivity.apply {
            (this@QuizActivity.applicationContext as DoubtnutApp).getEventTracker()
                .addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@QuizActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_QUIZ_LIST)
                .track()
        }
    }

    companion object {
        private const val TAG = "QuizActivity"
    }

}