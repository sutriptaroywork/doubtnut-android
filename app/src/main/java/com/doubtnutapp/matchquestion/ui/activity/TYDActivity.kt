package com.doubtnutapp.matchquestion.ui.activity

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.data.remote.ResponseMeta
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.showKeyboard
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.AutoCompleteQuestion
import com.doubtnutapp.databinding.ActivityTydBinding
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO
import com.doubtnutapp.textsolution.ui.TextSolutionActivity
import com.doubtnutapp.ui.ask.AskQuestionViewModel
import com.doubtnutapp.ui.ask.AutoCompleteTextAdapter
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.KeyboardUtils.hideKeyboard
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.facebook.appevents.AppEventsConstants
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TYDActivity : BaseBindingActivity<AskQuestionViewModel, ActivityTydBinding>() {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var networkUtil: NetworkUtil

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    private var tydSearchFlow = -1
    private var mOpenedAsDialog: Boolean = false

    private var isVoiceSearchEnabled: Boolean = false

    companion object {

        const val TAG = "TYDFragment"

        const val REQ_CODE_SPEECH_INPUT = 100
        private const val TYD_SEARCH_FLOW = "TYD_SEARCH_FLOW"
        private const val OPENED_AS_DIALOG = "opened_as_dialog"

        fun getStartIntent(context: Context, tydSearchFlow: Int? = -1, openAsDialog: Boolean = false) =
                Intent(context, TYDActivity::class.java)
                        .apply {
                            putExtra(TYD_SEARCH_FLOW, tydSearchFlow)
                            putExtra(OPENED_AS_DIALOG, openAsDialog)
                            if (openAsDialog) {
                                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                            }
                        }
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(): ActivityTydBinding =
        ActivityTydBinding.inflate(layoutInflater)

    override fun provideViewModel(): AskQuestionViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        tydSearchFlow = intent.getIntExtra(TYD_SEARCH_FLOW, -1)
        mOpenedAsDialog = intent.getBooleanExtra(OPENED_AS_DIALOG, false)

        initUi()
    }

    private val adapter: AutoCompleteTextAdapter by lazy { AutoCompleteTextAdapter(DoubtnutApp.INSTANCE.getEventTracker()) }

    override fun onResume() {
        super.onResume()
        subscribeToTextWatcher()
    }

    private fun initUi() {
        val backgroundColor = ContextCompat.getColor(this,
                if (mOpenedAsDialog) R.color.black_B3000000 else R.color.colorTransparent)
        binding.rootView.setBackgroundColor(backgroundColor)

        binding.rvAutoCompleteText.adapter = adapter
        binding.etQuestion.setHorizontallyScrolling(false)

        binding.etQuestion.requestFocus()

        setUpAutoCompleteTextView()

        setUpListeners()

        setUpExperiment()
    }

    private fun setUpExperiment() {
        when (tydSearchFlow) {
            Constants.VOICE_SEARCH_FIRST -> { // Start Voice Search first
                promptSpeechInput()
                binding.ivVoiceIcon.isVisible = true
            }
            Constants.TEXT_SEARCH_FIRST -> { // Start Text Search first
                showKeyboard(binding.etQuestion)
                binding.ivVoiceIcon.isVisible = true
            }
            Constants.ONLY_TEXT_SEARCH -> { // Default Flow
                showKeyboard(binding.etQuestion)
                binding.ivVoiceIcon.isVisible = false
            }
        }
    }

    private fun setUpListeners() {
        binding.btnFindSolution.setOnClickListener {

            when {
                binding.etQuestion.text.trim().toString().isEmpty() -> {
                    toast(getString(R.string.pleaseentertext))
                }
                networkUtil.isConnectedWithMessage() -> {
                    hideKeyboard(binding.etQuestion)

                    val countToSendEvent: Int = Utils.getCountToSend(RemoteConfigUtils.getEventInfo(), EventConstants.QUESTION_ASK)
                    repeat((0 until countToSendEvent).count()) {
                        sendEventByClick(EventConstants.QUESTION_ASK)
                        analyticsPublisher.publishEvent(AnalyticsEvent(AppEventsConstants.EVENT_NAME_SUBMIT_APPLICATION, hashMapOf()))
                    }

                    analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_TYD_SUBMIT, hashMapOf(), ignoreSnowplow = true))

                    MatchQuestionActivity.getStartIntent(
                        context = this,
                        questionText = binding.etQuestion.text.toString(),
                        source = TAG,
                        quesImageUri = null
                    ).apply {
                        startActivity(this)
                    }
                }
            }
        }

        binding.ivClose.setOnClickListener {
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_TYD_CROSS_CLICKED, hashMapOf(), ignoreSnowplow = true))
            finish()
            if (mOpenedAsDialog) {
                overridePendingTransition(0, 0)
            }
        }

        binding.ivVoiceIcon.setOnClickListener {
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.VOICE_SEARCH_BUTTON_TAPPED, hashMapOf(), ignoreSnowplow = true))
            promptSpeechInput()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (mOpenedAsDialog) {
            overridePendingTransition(0, 0)
        }
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_TYD_BACK_PRESS, hashMapOf(), ignoreSnowplow = true))
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun setUpAutoCompleteTextView() {
        binding.rvAutoCompleteText.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                hideKeyboard(binding.etQuestion)
                val suggestion = adapter.hitsQuestionList?.let { it[position] } ?: return
                analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_TYD_SUGGESTION_CLICKED, hashMapOf(), ignoreSnowplow = true))
                if (suggestion.isVoiceSearch) {
                    analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.VOICE_SUGGESTION_CLICKED, hashMapOf(), ignoreSnowplow = true))
                }
                updateMatch(suggestion._id, binding.etQuestion.text.toString(), suggestion.isVoiceSearch)
                if (suggestion._id.isBlank()) {

                    sendEventByClick(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, suggestion._id)
//                    // Send this event to Branch
//                    BranchIOUtils.userCompletedAction(
//                        EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
//                        JSONObject().apply {
//                            put(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, suggestion._id)
//                        })
                }

                if (suggestion.resourceType == SOLUTION_RESOURCE_TYPE_VIDEO) {
                    val intent = VideoPageActivity.startActivity(this@TYDActivity, suggestion._id, "", "", Constants.PAGE_SUGGESTIONS, "", false, "", "", false)
                    startActivity(intent)
                } else {
                    TextSolutionActivity.startActivity(this@TYDActivity, suggestion._id
                            , "", "", Constants.PAGE_SUGGESTIONS, "",
                            false, "", "", false).apply {
                                startActivity(this)
                    }
                }
            }
        })
    }

    private fun subscribeToTextWatcher() {
        compositeDisposable.add(RxEditTextObservable.fromView(binding.etQuestion)
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .filter { text ->
                    (text.isNotEmpty() && networkUtil.isConnectedWithMessage()).also {
                        runOnUiThread {
                            if (text.isEmpty()) adapter.updateData(emptyList())
                        }
                    }
                }
                .switchMap {
                    analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_TYD_TYPED, hashMapOf<String, Any>().apply {
                        put(EventConstants.SEARCHED_TEXT, it)
                    }, ignoreSnowplow = true))
                    viewModel.getAutoCompleteQuestion(it, isVoiceSearchEnabled)
                            .onErrorResumeNext(Observable.just(
                                    ApiResponse(
                                            ResponseMeta(0, "", null),
                                            AutoCompleteQuestion(arrayListOf()), null)))
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    it.data.matches.forEach {
                        it.isVoiceSearch = isVoiceSearchEnabled
                    }
                    adapter.updateData(it.data.matches)
                    isVoiceSearchEnabled = false
                    showKeyboard(binding.etQuestion)
                })
    }

    /**
     * Showing google speech input dialog
     */
    private fun promptSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speech Prompt")
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
        } catch (a: ActivityNotFoundException) {
            ToastUtils.makeText(applicationContext,
                    "Speech not supported",
                    Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CODE_SPEECH_INPUT -> {
                if (resultCode == RESULT_OK && null != data) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    result?.let {
                        if (result.size > 0) {
                            val voiceText = result[0]
                            isVoiceSearchEnabled = true
                            binding.etQuestion.setText(voiceText)
                            binding.etQuestion.setSelection(binding.etQuestion.text?.length ?: 0)
                            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.VOICE_INPUT_ENTERED, hashMapOf(), ignoreSnowplow = true))
                        }
                    }
                }
            }
        }
    }

    private fun updateMatch(qid: String, textQuestion: String, isVoiceSearch: Boolean) {
        viewModel.updateMatch(textQuestion, qid, isVoiceSearch)
    }

    private fun sendEventByClick(eventName: String, qid: String? = null) {

        DoubtnutApp.INSTANCE.getEventTracker().addEventNames(eventName)
                .addNetworkState(networkUtil.isConnected().toString())
                .addStudentId(UserUtil.getStudentId())
                .addScreenName(EventConstants.PAGE_TEXT_SEARCH)
                .addEventParameter(Constants.QUESTION_ID, qid.orEmpty())
                .track()
    }
}
