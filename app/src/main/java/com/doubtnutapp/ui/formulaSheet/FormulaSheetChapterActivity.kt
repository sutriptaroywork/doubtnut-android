package com.doubtnutapp.ui.formulaSheet

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.data.remote.ResponseMeta
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.FormulaSheetSuperChapter
import com.doubtnutapp.databinding.ActivityFormulaSheetChapterBinding
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FormulaSheetChapterActivity : BaseActivity() {

    private lateinit var viewModel: FormulaSheetViewModel
    private lateinit var adapter: FormulaSheetVerticalAdapter

    private lateinit var eventTracker: Tracker
    private val compositeDisposable = CompositeDisposable()

    private var subjectId = ""

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: ActivityFormulaSheetChapterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.statusBarColor)
        binding = ActivityFormulaSheetChapterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        eventTracker = getTracker()
        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(FormulaSheetViewModel::class.java)

        subjectId = intent!!.getStringExtra(Constants.FORMULA_SUBJECT_ID).orEmpty()
        val subjectName = intent!!.getStringExtra(Constants.FORMULA_SUBJECT_NAME)
        val subjectIcon = intent!!.getStringExtra(Constants.FORMULA_SUBJECT_ICON)
        val colorIndex = intent!!.getIntExtra(Constants.COLOR_INDEX, 0)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = subjectName
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setBackgroundDrawable(
            ColorDrawable(
                this.resources
                    .getColor(Utils.getColorPair(colorIndex)[1])
            )
        )

        fetchSuperTopics(subjectId)
        adapter = FormulaSheetVerticalAdapter()
        binding.rvFormulaSheetChapter.layoutManager =
            LinearLayoutManager(this) as RecyclerView.LayoutManager?
        binding.rvFormulaSheetChapter.adapter = adapter
        viewModel.publishOnFormulaSubjectSelectedEvent(subjectName ?: "")
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }

    override fun onStart() {
        super.onStart()
        subscribeToTextWatcher()
    }

    private fun subscribeToTextWatcher() {
        compositeDisposable.add(RxSearchObservable.fromView(binding.searchBox)
            .debounce(300, TimeUnit.MILLISECONDS)
            .filter { !it.isEmpty() }
            .distinctUntilChanged()
            .switchMap {
                viewModel.getFormulasTopicsSearch(subjectId, it)
                    .onErrorResumeNext(
                        Observable.just(
                            ApiResponse(
                                ResponseMeta(0, "", null),
                                arrayListOf(
                                    FormulaSheetSuperChapter(
                                        "",
                                        "",
                                        "",
                                        arrayListOf(),
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
                    val context = this@FormulaSheetChapterActivity ?: return@subscribe
                    if (!NetworkUtils.isConnected(context)) {
                        ToastUtils.makeText(
                            context,
                            R.string.string_noInternetConnection,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
//                            ToastUtils.makeText(context, R.string.string_webViewActivity_somethingWentWrong, Toast.LENGTH_SHORT).show()
                    }
                }
                adapter.updateData(it.data)
            })
    }

    private fun fetchSuperTopics(subjectId: String) {
        viewModel.getFormulasTopics(subjectId).observe(this, Observer { response ->
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
                    adapter.updateData(response.data.data)
                    sendEvent(EventConstants.EVENT_NAME_GET_CHAPTER_CALL_SUCCESS)
                }
            }
        })
    }

    private fun sendEvent(eventName: String) {
        this@FormulaSheetChapterActivity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(
                    NetworkUtils.isConnected(this@FormulaSheetChapterActivity).toString()
                )
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_FORMULA_SHEET_CHAPTER_ACTIVITY)
                .track()
        }
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = this@FormulaSheetChapterActivity.applicationContext as DoubtnutApp
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


