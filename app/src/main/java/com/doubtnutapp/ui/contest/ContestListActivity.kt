package com.doubtnutapp.ui.contest

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.ui.dailyPrize.DailyPrizeActivity
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.utils.UserUtil.getStudentId
import kotlinx.android.synthetic.main.activity_contests.*

class ContestListActivity : BaseActivity() {

    private lateinit var viewModel: ContestViewModel
    private lateinit var adapter: ContestAdapter
    private lateinit var eventTracker: Tracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.blueDark)
        setContentView(R.layout.activity_contests)
        eventTracker = getTracker()
        viewModel = ViewModelProviders.of(this).get(ContestViewModel::class.java)
        fetchContestList()
        adapter = ContestAdapter(this@ContestListActivity, eventTracker)
        rv_contestList.layoutManager = LinearLayoutManager(this)
        rv_contestList.adapter = adapter

        rv_contestList.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                val intent = Intent(this@ContestListActivity!!, DailyPrizeActivity::class.java)
                intent.putExtra(Constants.CONTEST_ID, adapter.contests!![position].contestId)
                startActivity(intent)
                sendEvent(EventConstants.EVENT_NAME_CONTEST_ITEM_CLICK)
            }
        })
        imageView_contestActivity_back.setOnClickListener {
            onBackPressed()
            sendEvent(EventConstants.EVENT_NAME_BACK_FROM_CONTEST)
        }

    }

    private fun fetchContestList() {
        viewModel.getContestList().observe(this, Observer { response ->
            when (response) {
                is Outcome.Progress -> {
                    progressBarContest.visibility = View.VISIBLE
                }
                is Outcome.Failure -> {
                    progressBarContest.visibility = View.GONE
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(supportFragmentManager, "NetworkErrorDialog")
                }
                is Outcome.ApiError -> {
                    progressBarContest.visibility = View.GONE
                    apiErrorToast(response.e)

                }
                is Outcome.Success -> {
                    adapter.updateData(response.data.data)
                    progressBarContest.visibility = View.GONE
                }
            }
        })
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = this@ContestListActivity.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

    private fun sendEvent(eventName: String) {
        this@ContestListActivity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@ContestListActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_CONTEST_LIST)
                .track()
        }
    }

}
