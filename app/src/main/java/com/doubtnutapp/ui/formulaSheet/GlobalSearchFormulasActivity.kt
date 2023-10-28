package com.doubtnutapp.ui.formulaSheet


import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.ActivityGlobalSearchFormulaSheetChapterBinding
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.Utils
import dagger.android.AndroidInjection
import javax.inject.Inject


class GlobalSearchFormulasActivity : BaseActivity() {

    private lateinit var viewModel: FormulaSheetViewModel
    private lateinit var adapter: FormulaSheetFormulaListAdapter
    private lateinit var eventTracker: Tracker
    private var chapterId = ""

    private lateinit var binding : ActivityGlobalSearchFormulaSheetChapterBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.statusBarColor)
        binding = ActivityGlobalSearchFormulaSheetChapterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        eventTracker = getTracker()
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FormulaSheetViewModel::class.java)

        chapterId = intent!!.getStringExtra(Constants.CHAPTER_ID).orEmpty()
        val subjectName = intent!!.getStringExtra(Constants.SUPER_CHAPTER_NAME)
        val colorIndex = intent!!.getIntExtra(Constants.COLOR_INDEX, 0)
        val searchType = intent!!.getStringExtra(Constants.SEARCH_TYPE).orEmpty()

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = subjectName
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(this.resources
                .getColor(Utils.getColorPair(colorIndex)[1])))

        fetchFormulas(chapterId, searchType)
        adapter = FormulaSheetFormulaListAdapter()
        binding.rvGlobalFormula.layoutManager = LinearLayoutManager(this) as RecyclerView.LayoutManager?
        binding.rvGlobalFormula.adapter = adapter
    }


    private fun fetchFormulas(chapterId: String, searchType: String) {
        viewModel.getFormulasBySearch(chapterId, searchType).observe(this, Observer { response ->
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
                    apiErrorToast(response.e)
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
        this@GlobalSearchFormulasActivity?.apply {
            eventTracker.addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(this@GlobalSearchFormulasActivity).toString())
                    .addStudentId(defaultPrefs(this@GlobalSearchFormulasActivity)
                            .getString(Constants.STUDENT_ID, "").orDefaultValue())
                    .addScreenName(EventConstants.PAGE_GLOBAL_SEARCH_FORMULAS_ACTIVITY)
                    .track()
        }
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = this@GlobalSearchFormulasActivity.applicationContext as DoubtnutApp
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


