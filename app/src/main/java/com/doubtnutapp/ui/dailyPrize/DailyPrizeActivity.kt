package com.doubtnutapp.ui.dailyPrize


import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.ActivityDailyPrizeBinding
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId


class DailyPrizeActivity : BaseActivity() {

    private lateinit var dailyPrizeViewModel: DailyPrizeViewModel
    internal lateinit var adapter: TodaysUsersAdapter
    internal lateinit var adapterLastDayUsers: LastDayUsersAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var layoutManagerLastUser: RecyclerView.LayoutManager
    private lateinit var contestId: String
    private lateinit var eventTracker: Tracker

    private lateinit var binding : ActivityDailyPrizeBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyPrizeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        eventTracker = getTracker()

        init()
        setValues()
        setUpListener()
        setObservers()
        setupRecyclerView()

    }

    private fun setUpListener() {
        binding.itemLeftScrollToday.setOnClickListener {
            if ((layoutManager as LinearLayoutManager).findLastVisibleItemPosition() > 0) {
                binding.recyclerViewWinningNow.smoothScrollToPosition((layoutManager as LinearLayoutManager).findLastVisibleItemPosition() - 1)
            } else {
                binding.recyclerViewWinningNow.smoothScrollToPosition(0)
            }

            sendEventByClick(EventConstants.EVENT_NAME_LEFT_SCROLL_TODAY)
        }

        binding.itemLeftScrollToday.setOnClickListener {
            binding.recyclerViewWinningNow.smoothScrollToPosition((layoutManager as LinearLayoutManager).findLastVisibleItemPosition() + 1)
            sendEventByClick(EventConstants.EVENT_NAME_RIGHT_SCROLL_TODAY)

        }

        binding.itemLeftScrollLast.setOnClickListener {
            when {
                (layoutManager as LinearLayoutManager).findLastVisibleItemPosition() > 0 -> binding.recyclerViewLastDay.smoothScrollToPosition((layoutManager as LinearLayoutManager).findLastVisibleItemPosition() - 1)
                (layoutManagerLastUser as LinearLayoutManager).findLastVisibleItemPosition() > 0 -> binding.recyclerViewLastDay.smoothScrollToPosition((layoutManagerLastUser as LinearLayoutManager).findLastVisibleItemPosition() - 1)
//                else -> recyclerViewLastDay.smoothScrollToPosition(0)

            }
            sendEventByClick(EventConstants.EVENT_NAME_LEFT_SCROLL_LASTDAY)

        }

        binding.itemRightScrollLast.setOnClickListener {
            when {
                (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()!=null -> binding.recyclerViewLastDay.smoothScrollToPosition((layoutManager as LinearLayoutManager).findLastVisibleItemPosition() + 1)
                (layoutManagerLastUser as LinearLayoutManager).findLastVisibleItemPosition()!=null -> binding.recyclerViewLastDay.smoothScrollToPosition((layoutManagerLastUser as LinearLayoutManager).findLastVisibleItemPosition() + 1)
            }
            sendEventByClick(EventConstants.EVENT_NAME_RIGHT_SCROLL_LASTDAY)

        }

        binding.imageViewDailyActivityBack.setOnClickListener {
            this.onBackPressed()
            sendEventByClick(EventConstants.EVENT_NAME_BACK_FROM_DAILY_PRIZE)

        }
    }

    private fun setupRecyclerView() {
        adapter=TodaysUsersAdapter(this, eventTracker)
        adapterLastDayUsers=LastDayUsersAdapter(this, eventTracker)

        layoutManager=LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        layoutManagerLastUser=LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewWinningNow.layoutManager=layoutManager
        binding.recyclerViewWinningNow.adapter=adapter
        binding.recyclerViewLastDay.layoutManager=layoutManagerLastUser
        binding.recyclerViewLastDay.adapter=adapterLastDayUsers
    }

    private fun setObservers() {
        dailyPrizeViewModel.getDailyPrize(contestId).observe(this, Observer { response ->
            when (response) {
                is Outcome.Progress -> {
                    binding.progressDailyActivityProfileImage.visibility=View.VISIBLE
                }
                is Outcome.Failure -> {
                    binding.progressDailyActivityProfileImage.visibility=View.GONE
                    val dialog=NetworkErrorDialog.newInstance()
                    dialog.show(supportFragmentManager, "NetworkErrorDialog")
                }
                is Outcome.ApiError -> {
                    binding.progressDailyActivityProfileImage.visibility=View.GONE
                    apiErrorToast(response.e)
                }
                is Outcome.BadRequest -> {
                    binding.progressDailyActivityProfileImage.visibility=View.GONE
                    val dialog=BadRequestDialog.newInstance("unauthorized")
                    dialog.show(supportFragmentManager, "BadRequestDialog")
                }
                is Outcome.Success -> {
                    binding.progressDailyActivityProfileImage.visibility=View.GONE
                    Glide.with(this@DailyPrizeActivity).load(response.data.data.headerDetails.contestLogo).apply(RequestOptions().placeholder(R.drawable.item_contest_icon)).into(binding.imageViewDailyActivityProfileImage)


                    val data=response.data.data.userDetails
                    binding.tvWatchedVideos.text = data.contextTextString
                    binding.tvCount.text = getString(R.string.string_video_count, data.count)
                    binding.tvDailyPrizeTitle.text=getString(R.string.string_daily_prize_title, response.data.data.headerDetails.amount)
                    when {
                        data.eligible == 0 -> {
                            binding.tvToElegible.text=this.getString(R.string.you_are_not_eligible)
                            binding.tvToElegible.setBackgroundResource(R.color.redDark)

                        }
                        data.eligible == 1 -> {
                            binding.tvToElegible.text=this.getString(R.string.you_are_eligible)
                            binding.tvToElegible.setBackgroundResource(R.color.greendark)


                        }
                        data.eligible == 2 -> binding.tvToElegible.visibility=View.GONE
                    }

                    if(response.data.data.todayUsers.isNotEmpty()){
                        adapter.updateData(response.data.data.todayUsers)
                        binding.tvWinningNow.visibility = View.VISIBLE
                    } else binding.llTodayList.visibility=View.GONE
                    if(response.data.data.lastDayUser.isNotEmpty()){
                        adapterLastDayUsers.updateData(response.data.data.lastDayUser)
                        binding.tvYesterdayWinner.visibility = View.VISIBLE
                    } else binding.llYesterdayList.visibility=View.GONE

                    val builder=StringBuilder()
                    for(details in response.data.data.contestRules) {
                        builder.append("\u25A0\t\t ${details.rules}.\n\n")
                    }
                    binding.tvRules.text=builder.toString()
                }
            }
        })
    }

    private fun init() {
        dailyPrizeViewModel=ViewModelProviders.of(this)[DailyPrizeViewModel::class.java]
    }
    private fun setValues() {
        contestId = intent.getStringExtra(Constants.CONTEST_ID).orEmpty()
    }

    private fun sendEventByClick(eventName : String){
        this@DailyPrizeActivity?.apply {
            eventTracker.addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(this@DailyPrizeActivity).toString())
                    .addStudentId(getStudentId())
                    .addScreenName(EventConstants.PAGE_DAILY_PRIZE_ACTIVITY)
                    .track()
        }
    }
    private fun getTracker(): Tracker {
        val doubtnutApp = this@DailyPrizeActivity.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }
}