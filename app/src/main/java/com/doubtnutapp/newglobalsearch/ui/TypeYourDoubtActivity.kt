package com.doubtnutapp.newglobalsearch.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.SearchSuggestionClicked
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityTydSearchBinding
import com.doubtnutapp.hide
import com.doubtnutapp.newglobalsearch.model.SearchSuggestionItem
import com.doubtnutapp.newglobalsearch.model.SearchSuggestionsDataItem
import com.doubtnutapp.newglobalsearch.ui.adapter.SearchSuggestionsAdapter
import com.doubtnutapp.newglobalsearch.viewmodel.TypeYourDoubtViewModel
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.KeyboardUtils
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.wrapperinterface.SimpleTextWatcherListener
import dagger.android.HasAndroidInjector
import java.util.*

class TypeYourDoubtActivity :
    BaseBindingActivity<TypeYourDoubtViewModel, ActivityTydSearchBinding>(), View.OnClickListener,
    SimpleTextWatcherListener, HasAndroidInjector, ActionPerformer {

    /**
     * Suggestions recycler view adapter
     */
    private lateinit var adapter: SearchSuggestionsAdapter

    private var searchTextChanged: Boolean = true

    override fun providePageName(): String = TAG

    override fun provideViewBinding(): ActivityTydSearchBinding =
        ActivityTydSearchBinding.inflate(layoutInflater)

    override fun provideViewModel(): TypeYourDoubtViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        init()
        setUpObservers()
    }

    override fun onResume() {
        super.onResume()
        binding.searchKeywordInput.requestFocus()
        KeyboardUtils.showKeyboard(binding.searchKeywordInput)
    }

    private fun init() {
        viewModel =
            ViewModelProvider(this@TypeYourDoubtActivity, viewModelFactory).get(
                TypeYourDoubtViewModel::class.java
            )
        viewModel.source = intent.getStringExtra(SOURCE).orEmpty()
        setUpRecyclerView()
        binding.searchKeywordInput.imeOptions = EditorInfo.IME_ACTION_SEARCH
        binding.searchKeywordInput.setRawInputType(InputType.TYPE_CLASS_TEXT)
    }

    private fun setUpObservers() {

        viewModel.searchSuggestionsLiveData.observeK(
            this@TypeYourDoubtActivity,
            ::onSearchSuggestionSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )


        binding.apply {
            voiceSearch.setOnClickListener(this@TypeYourDoubtActivity)
            btnFindSolution.setOnClickListener(this@TypeYourDoubtActivity)
            closeSearchScreen.setOnClickListener(this@TypeYourDoubtActivity)
            searchKeywordInput.addTextChangedListener(this@TypeYourDoubtActivity)
            searchKeywordInput.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    KeyboardUtils.hideKeyboard(searchKeywordInput)
                    sendKeyboardSearchEvent()
                    fetchResultsForQuery(
                        searchKeywordInput.text.toString(),
                        EventConstants.SEARCH_KEYBOARD_EVENT
                    )
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }
        }
    }

    /**
     * Setting Up Suggestions RecyclerView.
     */
    private fun setUpRecyclerView() {
        adapter = SearchSuggestionsAdapter(this@TypeYourDoubtActivity)
        binding.rvSuggestions.adapter = adapter
        adapter.updateData(emptyList())
    }

    companion object {
        const val TAG = "TypeYourDoubtActivity"
        const val SOURCE = "source"
        const val REQ_CODE_SPEECH_INPUT = 100
        fun startActivity(context: Context, source: String) {
            Intent(context, TypeYourDoubtActivity::class.java).apply {
                putExtra(SOURCE, source)
            }.also {
                context.startActivity(it)
            }
        }
    }

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
        if (text?.trim()?.isNotEmpty() == true) {
            binding.btnFindSolution.background = getDrawable(R.drawable.bg_capsule_tomoto)
            /**
             * checking searchTextChanged value to manage whether user typed some query or
             * user selected/clicked some suggestion to fetch search results directly.
             */
            if (searchTextChanged) {
                getSearchSuggestions(text.trim().toString())
            }

            // re-initializing the value of searchTextChanged to the default value.
            searchTextChanged = true

        } else if (text?.trim()?.isEmpty() == true) {
            binding.btnFindSolution.background = getDrawable(R.drawable.bg_capsule_grey_solid)
            manageSuggestionsListVisibility(false)
            KeyboardUtils.showKeyboard(binding.searchKeywordInput)
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.closeSearchScreen -> {
                sendBackButtonPressEvent()
                finish()
            }
            binding.voiceSearch -> promptSpeechInput()
            binding.btnFindSolution -> {
                sendFindSolnButtonClickEvent()
                fetchResultsForQuery(
                    binding.searchKeywordInput.text.toString(),
                    EventConstants.EVENT_NAME_IN_APP_TYD_BUTTON_CLICK
                )
            }
        }
    }

    private fun unAuthorizeUserError() {
        binding.searchProgressBar.hide()
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        binding.searchProgressBar.hide()
        apiErrorToast(e)
    }

    private fun updateProgressBarState(state: Boolean) {
        binding.searchProgressBar.hide()
    }

    private fun ioExceptionHandler() {
        binding.searchProgressBar.hide()
        if (NetworkUtils.isConnected(this@TypeYourDoubtActivity).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    /**
     * Method to handle the api response for search suggestions.
     */
    private fun onSearchSuggestionSuccess(searchSuggestionsDataItem: SearchSuggestionsDataItem) {

        if (binding.searchKeywordInput.text!!.isNotEmpty()) {
            manageSuggestionsListVisibility(true)
            binding.searchProgressBar.hide()
            searchSuggestionsDataItem.suggestionsList.apply {
                add(
                    0, SearchSuggestionItem(
                        binding.searchKeywordInput.text.toString(),
                        -1,
                        "",
                        "",
                        R.layout.item_tyd_suggestions,
                        ""
                    )
                )
            }
            adapter.updateData(searchSuggestionsDataItem.suggestionsList)
        }
    }

    override fun performAction(action: Any) {
        val searchText = (action as SearchSuggestionClicked).suggestionData
        //ApXor event for suggestion text click
        viewModel.sendEvent(
            EventConstants.EVENT_NAME_IN_APP_SEARCH_SUGGESTION_CLICK,
            hashMapOf<String, Any>().apply {
                put(EventConstants.SOURCE, viewModel.source)
                put(EventConstants.CLICKED_ITEM, action.suggestionData)
                put(EventConstants.ITEM_POSITION, action.itemPosition)
            }, ignoreSnowplow = true)
        fetchResultsForQuery(searchText, EventConstants.EVENT_NAME_IN_APP_SEARCH_SUGGESTION_CLICK)
    }

    private fun fetchResultsForQuery(searchQuery: String, eventType: String) {
        if (searchQuery.trim().isNotEmpty()) {
            InAppSearchActivity.startActivityForSearchResult(
                this@TypeYourDoubtActivity,
                viewModel.source,
                searchQuery,
                false,
                true,
                eventType
            )
            finish()
        }
    }

    private fun getSearchSuggestions(suggestionText: String) {
        binding.searchProgressBar.show()
        viewModel.getSearchSuggestions(suggestionText)
    }

    private fun manageSuggestionsListVisibility(shouldShow: Boolean) {
        binding.rvSuggestions.visibility = if (shouldShow) View.VISIBLE else View.GONE
    }

    /**
     * Showing google speech input dialog
     */
    private fun promptSpeechInput() {
        viewModel.sendEvent(
            EventConstants.EVENT_NAME_IN_APP_SEARCH_VOICE,
            hashMapOf<String, Any>().apply {
                put(EventConstants.SOURCE, viewModel.source)
                put(EventConstants.SEARCHED_ITEM, binding.searchKeywordInput.text.toString())
            })
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
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
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
            REQ_CODE_SPEECH_INPUT -> {
                if (resultCode == RESULT_OK && null != data) {
                    val result =
                        data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).orEmpty()

                    /**
                     * Making searchTextChanged as false because no need to fetch suggestions as
                     * this@TypeYourDoubtActivity is the case to enter search  query through voice speech.
                     */
                    fetchResultsForQuery(
                        result[0].toString(),
                        EventConstants.EVENT_NAME_IN_APP_SEARCH_VOICE
                    )
                    sendVoiceSearchEvent(result[0].toString())
                }
            }
        }
    }

    private fun sendKeyboardSearchEvent() {
        viewModel.sendKeyboardSearchEvent(
            viewModel.source,
            binding.searchKeywordInput.text.toString()
        )
    }

    private fun sendVoiceSearchEvent(voiceSearchQuery: String) {
        viewModel.sendVoiceSearchEvent(viewModel.source, voiceSearchQuery)
    }

    private fun sendFindSolnButtonClickEvent() {
        viewModel.sendEvent(
            EventConstants.EVENT_NAME_IN_APP_TYD_BUTTON_CLICK,
            hashMapOf<String, Any>().apply {
                put(EventConstants.SOURCE, viewModel.source)
                put(EventConstants.SEARCHED_ITEM, binding.searchKeywordInput.text.toString())
            })
    }

    private fun sendBackButtonPressEvent() {
        viewModel.sendEvent(
            EventConstants.EVENT_IN_APP_SEARCH_BACK,
            hashMapOf<String, Any>().apply {
                put(EventConstants.SOURCE, viewModel.source)
                put(EventConstants.SEARCHED_ITEM, binding.searchKeywordInput.text.toString())
            }, ignoreSnowplow = true)
        viewModel.postMongoEvent(hashMapOf<String, Any>().apply {
            put("eventType", "click_back_button_empty")
            put("is_clicked", true)
            put("searched_item", binding.searchKeywordInput.text.toString())
            put("search_text", binding.searchKeywordInput.text.toString())
            put("size", 0)
            put(EventConstants.SOURCE, viewModel.source)
        })
    }

}
