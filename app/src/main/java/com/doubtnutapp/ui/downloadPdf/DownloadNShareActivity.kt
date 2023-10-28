package com.doubtnutapp.ui.downloadPdf


import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.ActivityDownloadNShareBinding
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.utils.UserUtil.getStudentId

class DownloadNShareActivity :
    BaseBindingActivity<DownloadNShareViewModel, ActivityDownloadNShareBinding>() {

    companion object {
        private const val TAG = "DownloadNShareActivity"
    }

    private val adapter: DownloadNShareAdapter by lazy {
        DownloadNShareAdapter(this@DownloadNShareActivity, eventTracker, null, null)
    }
    private var downloadPdfPackageName: String = ""
    private var downloadPdfLevelOne: String = ""
    private var downloadPdfLevelTwo: String = ""
    private val eventTracker: Tracker by lazy { getTracker() }

    override fun provideViewBinding(): ActivityDownloadNShareBinding =
        ActivityDownloadNShareBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DownloadNShareViewModel = viewModelProvider(viewModelFactory)

    override fun getStatusBarColor(): Int = R.color.Secondary

    override fun setupView(savedInstanceState: Bundle?) {
        fetchBookList()
        binding.rvBookList.layoutManager = LinearLayoutManager(this)
        binding.rvBookList.adapter = adapter

        binding.rvBookList.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                val intent =
                    Intent(this@DownloadNShareActivity, DownloadNShareLevelOneActivity::class.java)
                intent.putExtra(
                    Constants.FILTER_PACKAGE,
                    adapter.downloadDataList[position].packageNameData
                )
                startActivity(intent)
                sendEventByClick(
                    EventConstants.EVENT_NAME_BOOK_ITEM_CLICK,
                    adapter.downloadDataList[position].packageNameData.toString()
                )

            }
        })

        binding.btnCloseDownloadPdf.setOnClickListener {
            this@DownloadNShareActivity.onBackPressed()
            this.finish()
            sendEvent(EventConstants.EVENT_PRAMA_CLOSE_BUTTON_CLICK)
        }
    }

    private fun fetchBookList() {
        viewModel.getPdfDownloads(downloadPdfPackageName, downloadPdfLevelOne, downloadPdfLevelTwo)
            .observe(this, Observer { response ->
                when (response) {
                    is Outcome.Progress -> {
                        binding.progressBarDownloadNShare.visibility = View.VISIBLE
                    }
                    is Outcome.Failure -> {
                        binding.progressBarDownloadNShare.visibility = View.GONE
                        val dialog = NetworkErrorDialog.newInstance()
                        dialog.show(supportFragmentManager, "NetworkErrorDialog")
                    }
                    is Outcome.ApiError -> {
                        binding.progressBarDownloadNShare.visibility = View.GONE
                        apiErrorToast(response.e)

                    }
                    is Outcome.Success -> {
                        adapter.updateData(
                            response.data.data.dataList,
                            response.data.data.filterType
                        )
                        binding.progressBarDownloadNShare.visibility = View.GONE
                    }
                    else -> {}
                }
            })
    }

    private fun sendEvent(eventName: String) {
        this@DownloadNShareActivity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@DownloadNShareActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_DOWNLOAD_N_SHARE_ACTIVITY)
                .track()
        }
    }

    private fun sendEventByClick(eventName: String, bookName: String) {
        this@DownloadNShareActivity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@DownloadNShareActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_DOWNLOAD_N_SHARE_ACTIVITY)
                .addEventParameter(EventConstants.EVENT_PRAMA_CLICKED_ITEM_NAME, bookName)
                .track()
        }
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = this@DownloadNShareActivity.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }
}