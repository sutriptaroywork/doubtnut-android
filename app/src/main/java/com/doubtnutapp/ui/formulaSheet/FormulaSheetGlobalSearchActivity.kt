package com.doubtnutapp.ui.formulaSheet


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.data.remote.ResponseMeta
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.*
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.FormulaSheetGlobalSearch
import com.doubtnutapp.databinding.ActivityFormulaSheetGlobalSearchActivityBinding
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.RxEditTextObservable
import com.doubtnutapp.utils.UserUtil.getStudentId
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FormulaSheetGlobalSearchActivity : BaseActivity() {

    private lateinit var viewModel: FormulaSheetViewModel
    private lateinit var adapter: FormulaGlobalSearchAdapter
    private lateinit var eventTracker: Tracker
    private val compositeDisposable = CompositeDisposable()

    companion object {
        fun newIntent(formulaSheetGlobalSearchActivity: FormulaSheetTopicActivity): Intent {
            return Intent(formulaSheetGlobalSearchActivity, FormulaSheetGlobalSearchActivity::class.java)
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding : ActivityFormulaSheetGlobalSearchActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.white)
        binding = ActivityFormulaSheetGlobalSearchActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        eventTracker = getTracker()
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FormulaSheetViewModel::class.java)

        adapter = FormulaGlobalSearchAdapter()
        binding.rvGlobalSearch.layoutManager = LinearLayoutManager(this)
        binding.rvGlobalSearch.adapter = adapter

        binding.btnBack.setOnClickListener {
            this@FormulaSheetGlobalSearchActivity.onBackPressed()
            this.finish()
            sendEvent(EventConstants.EVENT_PRAMA_CLOSE_BUTTON_CLICK)

        }

        binding.ivClose.setOnClickListener {
            binding.etSearch.text.clear()
        }
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
        compositeDisposable.add(RxEditTextObservable.fromView(binding.etSearch)
                .debounce(300, TimeUnit.MILLISECONDS)
                .filter { !it.isEmpty() }
                .distinctUntilChanged()
                .switchMap {
                    viewModel.getGlobalSearch(it)
                            .onErrorResumeNext(Observable.just(ApiResponse(
                                ResponseMeta(0, "", null), arrayListOf(FormulaSheetGlobalSearch("", arrayListOf(), arrayListOf(), arrayListOf())), null)))
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { it ->
                    binding.progressBarFormulaSheetFormulas.hide()
                    if (it.data.isNotEmpty()) {
                        val context = this@FormulaSheetGlobalSearchActivity
                        if (!NetworkUtils.isConnected(context)) {
                            ToastUtils.makeText(context, R.string.string_noInternetConnection, Toast.LENGTH_SHORT).show()
                        }
                    }

                    setFormulasData(it.data)
                })
    }

    private fun setFormulasData(data: ArrayList<FormulaSheetGlobalSearch>) {
        var dataList = arrayListOf<FormulaSheetGlobalSearch>()
        for (i in 0 until data.size) {
            if ((data[i].searchType == "formulas" && data[i].formulasList!!.isNotEmpty()) || (data[i].searchType == "topics" && data[i].topicList!!.isNotEmpty()) || (data[i].searchType == "chapters" && data[i].chapterList!!.isNotEmpty())) {
                dataList.add(data[i])
            }
        }
        adapter.updateData(dataList)
    }

    private fun sendEvent(eventName: String) {
        this@FormulaSheetGlobalSearchActivity?.apply {
            eventTracker.addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(this@FormulaSheetGlobalSearchActivity).toString())
                    .addStudentId(getStudentId())
                    .addScreenName(EventConstants.PAGE_FORMULA_SHEET_HOME_ACTIVITY)
                    .track()
        }
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = this@FormulaSheetGlobalSearchActivity.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }


}
