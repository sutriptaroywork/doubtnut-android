package com.doubtnutapp.ui.formulaSheet


import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.ActivityGlobalSearchFormulaSheetChapterBinding
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId


class CheatSheetFormulaListActivity :
    BaseBindingActivity<FormulaSheetViewModel, ActivityGlobalSearchFormulaSheetChapterBinding>() {

    private lateinit var adapter: FormulaSheetFormulaListAdapter

    private lateinit var eventTracker: Tracker
    private val TAG = "CheatSheetFormulaListActivity"

    override fun provideViewBinding(): ActivityGlobalSearchFormulaSheetChapterBinding =
        ActivityGlobalSearchFormulaSheetChapterBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): FormulaSheetViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        eventTracker = getTracker()

        val cheatSheetId = intent!!.getStringExtra(Constants.FORMULA_ITEM_ID).orEmpty()
        val cheatSheetName = intent!!.getStringExtra(Constants.CLICKED_ITEM_NAME).orEmpty()

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = cheatSheetName
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        fetchFormulas(cheatSheetId)
        adapter = FormulaSheetFormulaListAdapter()
        binding.rvGlobalFormula.layoutManager =
            LinearLayoutManager(this) as RecyclerView.LayoutManager?
        binding.rvGlobalFormula.adapter = adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.statusBarColor)

    }


    private fun fetchFormulas(cheatSheetId: String) {
        viewModel.getFormulasByCheatSheet(cheatSheetId).observe(this, Observer { response ->
            when (response) {
                is Outcome.Progress -> {
                    binding.progressBarFormulsChapter.show()
                }
                is Outcome.Failure -> {
                    binding.progressBarFormulsChapter.hide()
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(supportFragmentManager, "NetworkErrorDialog")
                    sendEvent(EventConstants.EVENT_NAME_GET_CHAPTER_CALL_FAILURE)
                }
                is Outcome.ApiError -> {
                    binding.progressBarFormulsChapter.hide()
                    toast(getString(R.string.api_error))
                    sendEvent(EventConstants.EVENT_NAME_GET_CHAPTER_CALL_API_ERROR)
                }
                is Outcome.Success -> {
                    binding.progressBarFormulsChapter.hide()
                    adapter.updateFormulaData(response.data.data)
                    sendEvent(EventConstants.EVENT_NAME_GET_CHAPTER_CALL_SUCCESS)
                }
            }
        })
    }

    private fun sendEvent(eventName: String) {
        this@CheatSheetFormulaListActivity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(
                    NetworkUtils.isConnected(this@CheatSheetFormulaListActivity).toString()
                )
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_GLOBAL_SEARCH_FORMULAS_ACTIVITY)
                .track()
        }
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = this@CheatSheetFormulaListActivity.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return run {
            onBackPressed()
            super.onOptionsItemSelected(item)
        }
    }
}


