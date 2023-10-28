package com.doubtnutapp.dictionary

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnDictionaryLangaugeSelected
import com.doubtnutapp.base.OpenDictionaryLangaugeBottomSheet
import com.doubtnutapp.base.PlayAudio
import com.doubtnutapp.base.SearchWordMeaning
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.base.extension.viewBinding
import com.doubtnutapp.data.dictionary.DictionaryResponse
import com.doubtnutapp.data.dictionary.Language
import com.doubtnutapp.data.dictionary.WordDetail
import com.doubtnutapp.databinding.ActivityDictionaryBinding
import com.doubtnutapp.dictionary.adapter.DictionaryRecentSearchAdapter
import com.doubtnutapp.dictionary.adapter.WordDeatilAdapter
import com.doubtnutapp.dictionary.viewmodel.DictionaryActivityViewModel
import com.doubtnutapp.newglobalsearch.ui.InAppSearchActivity
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.KeyboardUtils
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.widgets.NoGifEditText
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.layout_header_dictionary_activity.*
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

class DictionaryActivity : BaseActivity(), ActionPerformer {

    companion object {
        fun getStartIntent(context: Context, source: String) =
            Intent(context, DictionaryActivity::class.java).apply {
                putExtra(EventConstants.SOURCE, source)
            }
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private var mediaPlayer: MediaPlayer? = null

    private val binding by viewBinding(ActivityDictionaryBinding::inflate)
    private lateinit var viewModel: DictionaryActivityViewModel

    private var selectedLanguage: String? = ""
    private var wordDeatilAdapter: WordDeatilAdapter? = null
    private var recentSearchesAdapter: DictionaryRecentSearchAdapter? = null

    private var recentSearchArray: ArrayList<String> = arrayListOf()
    private var searchedText = ""
    private var source = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        source = intent?.extras?.getString(EventConstants.SOURCE).orEmpty()
        val param: HashMap<String, Any> = hashMapOf()
        param[EventConstants.SOURCE] = source
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.DC_PAGE_VISIT, param, ignoreSnowplow = true))

        viewModel =
            ViewModelProvider(this, viewModelFactory).get(DictionaryActivityViewModel::class.java)

        initRecentSearches()

        with(binding) {
            setUpSuggesstorRecyclerView()
            header.btnCloseScreen.setOnClickListener {
                onBackPressed()
            }

            header.btnClearText.setOnClickListener {
                searchedText = ""
                header.searchKeywordInput.setText("")
                rvWordDeatil.hide()
                tvPoweredBy.hide()
                setupRecentSearches(true)
                emptyView.hide()
                rvSuggestions.hide()
                progressbar.hide()
                header.btnSearch.show()
                header.btnClearText.hide()
            }

            header.searchKeywordInput.findViewById<NoGifEditText>(R.id.searchKeywordInput)
                .addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(
                        text: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {

                        if (!searchedText.orEmpty().equals(text?.toString(), true)) {
                            header.btnSearch.show()
                            header.btnClearText.hide()
                        }
                    }

                    override fun afterTextChanged(p0: Editable?) {
                    }
                })

            header.searchKeywordInput.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    KeyboardUtils.hideKeyboard(searchKeywordInput)
                    var text = header.searchKeywordInput.text.toString()
                    if (!text.isNullOrEmpty() && !text.equals(
                            searchedText,
                            true
                        )
                    ) {
                        searchedText = text
                        fetchWordMeaning()
                    } else {
                        toast(getString(R.string.enter_vaild_english_word))
                    }
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }

            header.btnVoiceSearch.setOnClickListener {
                promptSpeechInput()
            }

            header.btnSearch.setOnClickListener {
                var text = header.searchKeywordInput.text.toString()
                if (!text.isNullOrEmpty() && text.length > 1 && !text.equals(searchedText, true)) {
                    searchedText = text
                    fetchWordMeaning()
                } else {
                    toast(getString(R.string.enter_vaild_english_word))
                }
            }
        }

        setupObservers()
    }

    private fun fetchWordMeaning() {
        if (isValidWord(searchedText)) {
            viewModel.fetchWordMeaning(searchedText, selectedLanguage)
            with(binding) {
                rvRecentSearches.hide()
                tvRecentSearches.hide()
                progressbar.show()
                header.btnSearch.hide()
                header.btnClearText.show()
            }
            analyticsPublisher?.publishEvent(
                AnalyticsEvent(
                    EventConstants.DC_WORD_SEARCHED,
                    hashMapOf<String, Any>().apply {
                        put(
                            EventConstants.SEARCHED_TEXT,
                            searchedText
                        )
                    },
                    ignoreSnowplow = true
                )
            )
        } else {
            toast(getString(R.string.enter_vaild_english_word))
        }
    }

    private fun isValidWord(searchedText: String): Boolean {
        return Pattern.matches("[a-zA-Z'-` `]+", searchedText)
    }

    private fun initRecentSearches() {
        val strRecentDictionarySearch =
            defaultPrefs().getString(Constants.RECENT_DICTIONARY_SEARCHES, "").orDefaultValue()
        if (!strRecentDictionarySearch.isNullOrEmpty()) {
            recentSearchArray.addAll(
                strRecentDictionarySearch.split("|").filter { !it.isNullOrEmpty() }
            )
        }

        setupRecentSearches(true)
    }

    private fun saveRecentSearches() {
        if (!recentSearchArray.isNullOrEmpty()) {
            var recentSearchString = ""
            for (searchItem in recentSearchArray) {
                recentSearchString += "$searchItem|"
            }

            defaultPrefs().edit()
                .putString(Constants.RECENT_DICTIONARY_SEARCHES, recentSearchString).apply()
        }
    }

    private fun setupObservers() {
        viewModel.wordSearchLiveData.observeK(
            this,
            ::onUserSearchSuccessNew,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )
    }

    private fun setupRecentSearches(showRecentSearches: Boolean) {

        with(binding) {
            if (!showRecentSearches) {
                rvRecentSearches.hide()
                tvRecentSearches.hide()
                return
            }
            if (!recentSearchArray.isNullOrEmpty()) {
                rvRecentSearches.show()
                tvPoweredBy.hide()
                tvRecentSearches.show()
                layoutWelcome.hide()
                if (recentSearchesAdapter == null) {
                    recentSearchesAdapter =
                        DictionaryRecentSearchAdapter(this@DictionaryActivity, recentSearchArray)
                    rvRecentSearches.adapter = recentSearchesAdapter
                } else {
                    recentSearchesAdapter?.updateData(recentSearchArray)
                }
            } else {
                rvRecentSearches.hide()
                tvRecentSearches.hide()
                layoutWelcome.show()
            }
        }
    }

    private fun setUpSuggesstorRecyclerView() {
        with(binding) {
            rvSuggestions.hide()
        }
    }

    private fun onUserSearchSuccessNew(dictionaryResponse: DictionaryResponse?) {
        with(binding) {
            progressbar.hide()
            header.btnSearch.hide()
            header.btnClearText.show()
            if (dictionaryResponse != null) {
                rvWordDeatil.show()
                layoutWelcome.hide()
                setupRecentSearches(false)
                emptyView.hide()
                rvSuggestions.hide()

                if (!dictionaryResponse.poweredByText.isNullOrEmpty()) {
                    tvPoweredBy.text = dictionaryResponse.poweredByText.orEmpty()
                    tvPoweredBy.show()
                } else {
                    tvPoweredBy.hide()
                }
                setupWordDetail(dictionaryResponse)

                updateRecentSearchArray()
                saveRecentSearches()
            } else {
                showNoResultView()
            }
        }
    }

    private fun updateRecentSearchArray() {
        if (!searchedText.isNullOrEmpty()) {
            if (!recentSearchArray.contains(searchedText)) {
                recentSearchArray.add(0, searchedText)
            } else {
                val index = recentSearchArray.indexOf(searchedText)
                recentSearchArray.removeAt(index)

                recentSearchArray.add(0, searchedText)
            }

            if (recentSearchArray.size > 5) {
                recentSearchArray.removeAt(5)
            }
        }
    }

    private fun setupWordDetail(dictionaryResponse: DictionaryResponse) {

        with(binding) {
            if (!dictionaryResponse.wordDetails.isNullOrEmpty()) {
                rvWordDeatil.show()
                setupRecentSearches(false)
                emptyView.hide()
                rvSuggestions.hide()
                if (wordDeatilAdapter == null) {
                    wordDeatilAdapter = WordDeatilAdapter(
                        this@DictionaryActivity,
                        dictionaryResponse.wordDetails as ArrayList<WordDetail>,
                        dictionaryResponse.languageArray
                    )
                    rvWordDeatil.adapter = wordDeatilAdapter
                } else {
                    wordDeatilAdapter?.updateData(
                        dictionaryResponse.wordDetails!!,
                        dictionaryResponse.languageArray
                    )
                }
            } else {
                showNoResultView()
            }
        }
    }

    private fun onApiError(e: Throwable) {
        showNoResultView()
        apiErrorToast(e)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
        with(binding) {
            progressbar.hide()
            header.btnSearch.hide()
            header.btnClearText.show()
        }
    }

    private fun ioExceptionHandler() {
        showNoResultView()
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        with(binding) {
            if (state) {
                progressbar.show()
            } else {
                progressbar.hide()
                header.btnSearch.hide()
                header.btnClearText.show()
            }
        }
    }

    private fun showNoResultView() {
        with(binding) {
            emptyView.show()
            rvWordDeatil.hide()
            tvPoweredBy.hide()
            layoutWelcome.hide()
            setupRecentSearches(false)
            rvSuggestions.hide()

            analyticsPublisher?.publishEvent(
                AnalyticsEvent(
                    EventConstants.DC_NO_RESULT_FOUND,
                    hashMapOf<String, Any>().apply {
                        put(
                            EventConstants.SEARCHED_TEXT,
                            searchedText
                        )
                    },
                    ignoreSnowplow = true
                )
            )
        }
    }

    override fun performAction(action: Any) {
        when (action) {
            is PlayAudio -> {
                analyticsPublisher?.publishEvent(
                    AnalyticsEvent(
                        EventConstants.DC_PRONUNCIATION_ICON_CLICK,
                        hashMapOf<String, Any>().apply {
                            put(
                                EventConstants.SEARCHED_TEXT,
                                searchedText
                            )
                        },
                        ignoreSnowplow = true
                    )
                )
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(action.audioUrl)
                    prepareAsync()
                }
                mediaPlayer?.setVolume(1.00f, 1.0f)
                mediaPlayer?.setOnPreparedListener {
                    mediaPlayer?.start()
                }
                mediaPlayer?.setOnCompletionListener {
                    releaseMediaPlayer()
                }
            }

            is OpenDictionaryLangaugeBottomSheet -> {
                openLangaugeBottomSheet(action.languageArray)
                analyticsPublisher?.publishEvent(
                    AnalyticsEvent(EventConstants.DC_LANGUAGE_CHANGE_DROPDOWN_CLICK, ignoreSnowplow = true)
                )
            }

            is OnDictionaryLangaugeSelected -> {
                selectedLanguage = action.langauge.locale
                with(binding) {
                    header.searchKeywordInput.setText(searchedText)
                    header.searchKeywordInput.setSelection(
                        header.searchKeywordInput.text?.length ?: 0
                    )
                }
                fetchWordMeaning()
                analyticsPublisher?.publishEvent(
                    AnalyticsEvent(
                        EventConstants.DC_LANGUAGE_CHANGED,
                        hashMapOf<String, Any>().apply {
                            put(
                                EventConstants.PARAM_LANGUAGE,
                                action.langauge.text.orEmpty()
                            )
                        },
                        ignoreSnowplow = true
                    )
                )
            }

            is SearchWordMeaning -> {
                searchedText = action.word
                with(binding) {
                    header.searchKeywordInput.setText(searchedText)
                    header.searchKeywordInput.setSelection(
                        header.searchKeywordInput.text?.length ?: 0
                    )
                }
                fetchWordMeaning()
                analyticsPublisher?.publishEvent(
                    AnalyticsEvent(
                        EventConstants.DC_RECENT_WORD_CLICKED,
                        hashMapOf<String, Any>().apply {
                            put(
                                EventConstants.SELECTED_WORD,
                                searchedText
                            )
                        },
                        ignoreSnowplow = true
                    )
                )
            }
        }
    }

    private fun openLangaugeBottomSheet(languageArray: java.util.ArrayList<Language>) {
        val bottomSheet = LanguageBottomFragment.newInstance(
            languageArray,
            this
        )
        bottomSheet.show(supportFragmentManager, "LangaugeBottomSheet")
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    /**
     * Showing google speech input dialog
     */
    private fun promptSpeechInput() {
        val singleResult = true
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Speech Prompt"
        )
        try {
            startActivityForResult(intent, InAppSearchActivity.REQ_CODE_SPEECH_INPUT)
        } catch (a: ActivityNotFoundException) {
            ToastUtils.makeText(
                applicationContext,
                "Speech not supported",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            InAppSearchActivity.REQ_CODE_SPEECH_INPUT -> {
                if (resultCode == RESULT_OK && null != data) {
                    val result =
                        data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).orEmpty()
                    searchedText = result[0].toString()
                    with(binding) {
                        header.searchKeywordInput.setText(searchedText)
                        header.searchKeywordInput.setSelection(
                            header.searchKeywordInput.text?.length ?: 0
                        )
                    }
                    fetchWordMeaning()
                }
            }
        }
    }

    override fun onBackPressed() {
        var searchViewVisible: Boolean
        with(binding) {
            searchViewVisible = rvWordDeatil.visibility == View.VISIBLE
            if (searchViewVisible) {
                rvWordDeatil.hide()
                setupRecentSearches(true)
                rvSuggestions.hide()
                return
            }
        }

        if (!searchViewVisible) {
            super.onBackPressed()
        }
    }
}
