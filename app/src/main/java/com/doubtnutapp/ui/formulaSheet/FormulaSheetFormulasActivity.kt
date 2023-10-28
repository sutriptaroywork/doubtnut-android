package com.doubtnutapp.ui.formulaSheet

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.data.remote.ResponseMeta
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.FormulaSheetFormulas
import com.doubtnutapp.databinding.ActivityFormulaSheetFormulasActivityBinding
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.UserUtil.getStudentId
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FormulaSheetFormulasActivity : BaseActivity() {

    private lateinit var viewModel: FormulaSheetViewModel
    private lateinit var adapter: FormulaSheetFormulasAdapter
    private lateinit var eventTracker: Tracker
    private val compositeDisposable = CompositeDisposable()
    private var chapterId = ""
    private var subjectId = ""

    private var totalEngagementTime: Int = 0
    private var engamentTimeToSend: Number = 0
    private var engagementTimerTask: TimerTask? = null
    private var engageTimer: Timer? = null
    private var engagementHandler: Handler? = null
    private var timeFormatter = SimpleDateFormat("m:ss", Locale.getDefault())

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding : ActivityFormulaSheetFormulasActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.Secondary)
        binding = ActivityFormulaSheetFormulasActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        eventTracker = getTracker()
        engagementHandler = Handler(Looper.getMainLooper())
        binding.searchBoxFormulas.maxWidth = Integer.MAX_VALUE

        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(FormulaSheetViewModel::class.java)
        chapterId = intent!!.getStringExtra(Constants.CHAPTER_ID).orEmpty()
        subjectId = intent!!.getStringExtra(Constants.FORMULA_SUBJECT_ID) ?: ""
        val title = intent!!.getStringExtra(Constants.CLICKED_ITEM_NAME)
        setSupportActionBar(binding.toolbarFormulas)
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        fetchBookList(chapterId)

        adapter = FormulaSheetFormulasAdapter()
        binding.rvFormulaSheetFormulas.layoutManager = LinearLayoutManager(this)
        binding.rvFormulaSheetFormulas.adapter = adapter
        viewModel.publishOnFormulaTopicSelectedEvent(title ?: "")
    }

    private fun fetchBookList(chapterId: String) {
        viewModel.getFormulas(chapterId).observe(this, Observer { response ->
            when (response) {
                is Outcome.Progress -> {
                    binding.progressBarFormulaSheetFormulas.visibility = View.VISIBLE
                }
                is Outcome.Failure -> {
                    binding.progressBarFormulaSheetFormulas.visibility = View.GONE
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(supportFragmentManager, "NetworkErrorDialog")
                    sendEvent(EventConstants.EVENT_NAME_GET_FORMULAS_CALL_FAILURE)
                }
                is Outcome.ApiError -> {
                    binding.progressBarFormulaSheetFormulas.visibility = View.GONE
                    toast(getString(R.string.api_error))
                    sendEvent(EventConstants.EVENT_NAME_GET_FORMULAS_CALL_API_ERROR)
                }
                is Outcome.Success -> {
                    adapter.updateData(response.data.data)
                    binding.progressBarFormulaSheetFormulas.visibility = View.GONE
                    sendEvent(EventConstants.EVENT_NAME_GET_FORMULAS_CALL_SUCCESS)
                }
            }
        })
    }

    private fun sendEvent(eventName: String) {
        this@FormulaSheetFormulasActivity.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(
                    NetworkUtils.isConnected(context = this@FormulaSheetFormulasActivity).toString()
                )
                .addStudentId(value = UserUtil.getStudentId())
                .addScreenName(EventConstants.PAGE_FORMULA_SHEET_FORMULAS_ACTIVITY)
                .track()
        }
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = this@FormulaSheetFormulasActivity.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

    override fun onStop() {
        super.onStop()
        engagementTimerTask?.let { engagementHandler?.removeCallbacks(it) }
        sendEventEngagement(
            EventConstants.FORMULA_PAGE_ENGAGEMENT_TOTAL_TIME + subjectId,
            engamentTimeToSend
        )
        compositeDisposable.clear()
    }

    override fun onStart() {
        super.onStart()
        subscribeToTextWatcher()
        startEngagementTimer()
    }

    private fun subscribeToTextWatcher() {
        compositeDisposable.add(RxSearchObservable.fromView(binding.searchBoxFormulas)
            .debounce(300, TimeUnit.MILLISECONDS)
            .filter { !it.isEmpty() }
            .distinctUntilChanged()
            .switchMap {
                viewModel.getFormulasSearch(chapterId, it)
                    .onErrorResumeNext(
                        Observable.just(
                            ApiResponse(
                                ResponseMeta(0, "", null),
                                arrayListOf(
                                    FormulaSheetFormulas(
                                        "",
                                        "",
                                        "",
                                        "",
                                        arrayListOf(),
                                        "",
                                        ""
                                    )
                                ),
                                null
                            )
                        )
                    )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { it ->
                if (it.data.isEmpty()) {
                    val context = this@FormulaSheetFormulasActivity
                    if (!NetworkUtils.isConnected(context)) {
                        ToastUtils.makeText(
                            context,
                            R.string.string_noInternetConnection,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                adapter.updateData(it.data)
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startEngagementTimer() {
        if (engageTimer == null) {
            engageTimer = Timer()
            timeFormatter.timeZone = TimeZone.getTimeZone("UTC")
        }
        engagementTimerTask = object : TimerTask() {
            override fun run() {
                engagementHandler?.post {
                    if (isAppInForeground) {
                        engamentTimeToSend = totalEngagementTime
                        totalEngagementTime++
                    }
                }
            }
        }
        totalEngagementTime = 0
        engageTimer!!.schedule(engagementTimerTask, 0, 1000)
    }

    private fun sendEventEngagement(eventName: String, engagementTime: Number) {
        this@FormulaSheetFormulasActivity.apply {
            (this@FormulaSheetFormulasActivity.applicationContext as DoubtnutApp).getEventTracker()
                .addEventNames(eventName)
                .addNetworkState(
                    NetworkUtils.isConnected(this@FormulaSheetFormulasActivity).toString()
                )
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_FORMULA_SHEET_FORMULAS_ACTIVITY)
                .addEventParameter(
                    EventConstants.FORMULA_PAGE_ENGAGEMENT_TOTAL_TIME_AS_PARAMS,
                    engagementTime
                )
                .track()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        engageTimer?.cancel()
        engageTimer = null
        engagementTimerTask?.cancel()
        engagementTimerTask = null
    }

}
