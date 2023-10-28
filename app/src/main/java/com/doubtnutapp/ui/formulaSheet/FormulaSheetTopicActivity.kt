package com.doubtnutapp.ui.formulaSheet

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.utils.toast
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.addEventNames
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.ActivityFormulaSheetTopicActivityBinding
import com.doubtnutapp.statusbarColor
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import dagger.android.AndroidInjection
import javax.inject.Inject

class FormulaSheetTopicActivity : BaseActivity() {

    private lateinit var viewModel: FormulaSheetViewModel
    private lateinit var adapter: FormulaSheetTopicAdapter
    private lateinit var eventTracker: Tracker

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: ActivityFormulaSheetTopicActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.statusBarColor)
        binding = ActivityFormulaSheetTopicActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        eventTracker = getTracker()
        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(FormulaSheetViewModel::class.java)

        getFormulaHome()
        adapter = FormulaSheetTopicAdapter()
        binding.rvFormulaSheetTopic.layoutManager = GridLayoutManager(this, 2)
        binding.rvFormulaSheetTopic.adapter = adapter

        setSupportActionBar(binding.toolbarFormulas)
        supportActionBar!!.title = getString(R.string.string_formulaSheet)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        binding.btnSearchGlobal.setOnClickListener {
            val questionIntentObject =
                FormulaSheetGlobalSearchActivity.newIntent(this@FormulaSheetTopicActivity)
            startActivity(questionIntentObject)
            overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out)

        }
    }

    private fun getFormulaHome() {
        viewModel.getFormulaHome().observe(this, Observer { response ->
            when (response) {
                is Outcome.Progress -> {
                    binding.progressBarFormulaSheet.visibility = View.VISIBLE
                }
                is Outcome.Failure -> {
                    binding.progressBarFormulaSheet.visibility = View.GONE
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(supportFragmentManager, "NetworkErrorDialog")
                    sendEvent(EventConstants.EVENT_NAME_GET_FORMULA_HOME_CALL_FAILURE)
                }
                is Outcome.ApiError -> {
                    binding.progressBarFormulaSheet.visibility = View.GONE
                    toast(getString(R.string.api_error))
                    sendEvent(EventConstants.EVENT_NAME_GET_FORMULA_HOME_CALL_API_ERROR)

                }
                is Outcome.Success -> {
                    adapter.updateData(response.data.data)
                    binding.progressBarFormulaSheet.visibility = View.GONE
                    sendEvent(EventConstants.EVENT_NAME_GET_FORMULA_HOME_CALL_SUCCESS)

                }
            }
        })
    }

    private fun sendEvent(eventName: String) {
        this@FormulaSheetTopicActivity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(
                    NetworkUtils.isConnected(this@FormulaSheetTopicActivity).toString()
                )
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_FORMULA_SHEET_HOME_ACTIVITY)
                .track()
        }
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = this@FormulaSheetTopicActivity.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
