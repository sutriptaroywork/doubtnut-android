package com.doubtnutapp.matchquestion.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.data.remote.ResponseMeta
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.AutoCompleteQuestion
import com.doubtnutapp.databinding.ActivityOcrEditBinding
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO
import com.doubtnutapp.textsolution.ui.TextSolutionActivity
import com.doubtnutapp.ui.ask.AskQuestionViewModel
import com.doubtnutapp.ui.ask.AutoCompleteTextAdapter
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.BranchIOUtils
import com.doubtnutapp.utils.KeyboardUtils.hideKeyboard
import com.doubtnutapp.utils.KeyboardUtils.showKeyboard
import com.doubtnutapp.utils.NetworkUtil
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.utils.RxEditTextObservable
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class OcrEditActivity : BaseBindingActivity<AskQuestionViewModel, ActivityOcrEditBinding>() {

    @Inject
    lateinit var networkUtil: NetworkUtil

    @Inject
    lateinit var mCompositeDisposable: CompositeDisposable

    private val mAdapter: AutoCompleteTextAdapter by lazy { AutoCompleteTextAdapter(DoubtnutApp.INSTANCE.getEventTracker()) }

    private val mOcrText: String by lazy {
        intent.getStringExtra(INTENT_EXTRA_OCR_TEXT).orEmpty()
    }
    private val mSource: String by lazy {
        intent.getStringExtra(INTENT_EXTRA_SOURCE).orEmpty()
    }
    private val mUiConfig: UiConfig by lazy {
        intent.getParcelableExtra(INTENT_EXTRA_UI_CONFIG) ?: UiConfig()
    }
    private val mQuestionId: String by lazy {
        intent.getStringExtra(INTENT_EXTRA_QUESTION_ID).orEmpty()
    }

    private var mIsLastSearchFromVoice: Boolean = false

    companion object {
        const val TAG = "OcrEditActivity"
        const val RESULT_CLOSED = 99
        const val RESULT_EDITED_OCR = "editedOcr"
        const val RESULT_SOURCE = "source"
        const val SOURCE = "ocr_edit"

        private const val INTENT_EXTRA_OCR_TEXT = "intent_extra_ocr_text"
        private const val INTENT_EXTRA_UI_CONFIG = "ui_variant"
        private const val INTENT_EXTRA_SOURCE = "source"
        private const val INTENT_EXTRA_QUESTION_ID = "question_id"

        fun getStartIntent(
            context: Context, ocrText: String? = null,
            uiConfig: UiConfig,
            source: String,
            questionId: String
        ) = Intent(context, OcrEditActivity::class.java)
            .apply {
                putExtra(INTENT_EXTRA_OCR_TEXT, ocrText)
                putExtra(INTENT_EXTRA_UI_CONFIG, uiConfig)
                putExtra(INTENT_EXTRA_SOURCE, source)
                putExtra(INTENT_EXTRA_QUESTION_ID, questionId)
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            }
    }

    @Keep
    @Parcelize
    data class UiConfig(
        val showSuggestions: Boolean = true,
        val showHelp: Boolean = false,
        val helpText: CharSequence? = null
    ) : Parcelable

    override fun provideViewBinding(): ActivityOcrEditBinding =
        ActivityOcrEditBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun getStatusBarColor(): Int = R.color.colorTransparent

    override fun provideViewModel(): AskQuestionViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        initUi()
        viewModel.sendEvent(EventConstants.OCR_EDIT_POP_UP_DISPLAYED, mSource)
    }

    override fun onResume() {
        super.onResume()
        subscribeToTextWatcher()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        viewModel.sendMatchPageExitEvent("home_key")
    }

    private fun initUi() {
        binding.etQuestion.apply {
            setText(mOcrText)
            setHorizontallyScrolling(false)
            requestFocus()
        }
        binding.btnFindSolution.disable()
        setUpListeners()

        updateUi(mUiConfig)
    }

    private fun updateUi(uiConfig: UiConfig) {
        if (uiConfig.showSuggestions) {
            binding.rvAutoCompleteText.adapter = mAdapter
            binding.rvLayout.show()
            setUpAutoCompleteTextView()
        } else {
            binding.rvLayout.hide()
        }
        binding.tvInfo.isVisible = uiConfig.showHelp
        binding.tvInfo.text = uiConfig.helpText
        showEditTextWithKeyboard()
        binding.btnFindSolution.show()
    }

    private fun setUpListeners() {
        binding.btnFindSolution.setOnClickListener {
            when {
                binding.etQuestion.text.trim().toString().isEmpty() -> {
                    toast(getString(R.string.pleaseentertext))
                }
                networkUtil.isConnectedWithMessage() -> {
                    hideKeyboard(binding.etQuestion)
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra(RESULT_EDITED_OCR, binding.etQuestion.text.toString())
                        putExtra(RESULT_SOURCE, SOURCE)
                    })
                    viewModel.sendEvent(EventConstants.OCR_EDIT_RESEARCH_DONE, mSource)
                    finish()
                }
            }
        }

        binding.ivClose.setOnClickListener {
            viewModel.sendEvent(EventConstants.OCR_EDIT_CROSS_PRESSED, mSource)
            setResult(RESULT_CLOSED)
            finish()
        }
    }

    private fun subscribeToTextWatcher() {
        mCompositeDisposable.add(RxEditTextObservable.fromView(binding.etQuestion)
            .debounce(300, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .doOnNext {
                runOnUiThread {
                    binding.btnFindSolution.isDisabled = it.isBlank() || it == mOcrText
                }
                viewModel.sendEvent(
                    EventConstants.OCR_EDIT_TEXT_CHANGED,
                    mSource,
                    ignoreSnowplow = true
                )
            }
            .filter { text ->
                (text.isNotEmpty() && networkUtil.isConnectedWithMessage() && mUiConfig.showSuggestions).also {
                    runOnUiThread {
                        if (text.isEmpty()) mAdapter.updateData(emptyList())
                    }
                }
            }
            .switchMap {
                viewModel.getAutoCompleteQuestion(it, mIsLastSearchFromVoice, mQuestionId)
                    .onErrorResumeNext(
                        Observable.just(
                            ApiResponse(
                                ResponseMeta(0, "", null),
                                AutoCompleteQuestion(arrayListOf()), null
                            )
                        )
                    )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                it.data.matches.forEach {
                    it.isVoiceSearch = mIsLastSearchFromVoice
                }
                mAdapter.updateData(it.data.matches)
                mIsLastSearchFromVoice = false
                showKeyboard(binding.etQuestion)
            })
    }

    private fun setUpAutoCompleteTextView() {
        binding.rvAutoCompleteText.addOnItemClick(object :
            RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                hideKeyboard(binding.etQuestion)
                val suggestion = mAdapter.hitsQuestionList?.let { it[position] } ?: return
                viewModel.sendEvent(EventConstants.OCR_EDIT_SUGGESTION_CLICKED, mSource)
                if (suggestion.isVoiceSearch) {
                    viewModel.sendEvent(
                        EventConstants.VOICE_SUGGESTION_CLICKED,
                        ignoreSnowplow = true
                    )
                }
                updateMatch(
                    suggestion._id,
                    binding.etQuestion.text.toString(),
                    suggestion.isVoiceSearch
                )
                if (suggestion._id.isBlank()) {

                    // Send this event to Branch
//                    BranchIOUtils.userCompletedAction(
//                        EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
//                        JSONObject().apply {
//                            put(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, suggestion._id)
//                        })
                }

                if (suggestion.resourceType == SOLUTION_RESOURCE_TYPE_VIDEO) {
                    val intent = VideoPageActivity.startActivity(
                        this@OcrEditActivity,
                        suggestion._id,
                        "",
                        "",
                        Constants.PAGE_SUGGESTIONS,
                        "",
                        false,
                        "",
                        "",
                        false
                    )
                    startActivity(intent)
                } else {
                    TextSolutionActivity.startActivity(
                        this@OcrEditActivity,
                        suggestion._id,
                        "",
                        "",
                        Constants.PAGE_SUGGESTIONS,
                        "",
                        false,
                        "",
                        "",
                        false
                    ).apply {
                        startActivity(this)
                    }
                }
                setResult(RESULT_CANCELED)
                finish()
            }
        })
    }

    private fun updateMatch(qid: String, textQuestion: String, isVoiceSearch: Boolean) {
        viewModel.updateMatch(textQuestion, qid, isVoiceSearch)
    }

    override fun onBackPressed() {
        viewModel.sendEvent(EventConstants.OCR_EDIT_POP_UP_CLOSED, mSource)
        setResult(RESULT_CLOSED)
        finish()
    }

    override fun onStop() {
        super.onStop()
        mCompositeDisposable.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCompositeDisposable.dispose()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    private fun showEditTextWithKeyboard() {
        binding.etQuestion.apply {
            show()
            requestFocus()
            postDelayed(100) {
                showKeyboard(this)
            }
        }
    }
}
