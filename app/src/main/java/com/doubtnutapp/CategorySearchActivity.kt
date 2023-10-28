package com.doubtnutapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityCategorypageSearchBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.liveclass.viewmodel.CategorySearchViewModel
import com.doubtnutapp.newglobalsearch.model.SearchSessionModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.mockTest.MockTestSubscriptionActivity
import com.doubtnutapp.utils.EndlessNestedScrollListener
import com.doubtnutapp.utils.KeyboardUtils
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.wrapperinterface.SimpleTextWatcherListener
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.google.gson.annotations.SerializedName
import dagger.android.AndroidInjector
import kotlinx.android.synthetic.main.suggestion_list_item.view.*
import java.util.*
import javax.inject.Inject

class CategorySearchActivity :
    BaseBindingActivity<CategorySearchViewModel, ActivityCategorypageSearchBinding>(),
    SimpleTextWatcherListener,
    ActionPerformer {

    companion object {
        private const val REQ_CODE_SPEECH_INPUT: Int = 1010
        private const val SEARCH_TEXT: String = "text"
        private const val PAGE_TEXT: String = "page"
        private const val CATEGORY_ID = "categoryId"

        fun getStartIntent(context: Context, categoryId: String): Intent =
            Intent(context, CategorySearchActivity::class.java).apply {
                putExtra(CATEGORY_ID, categoryId)
            }
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private lateinit var adapter: WidgetLayoutAdapter

    private lateinit var suggestionListAdapter: SuggestionListAdapter

    private lateinit var infiniteScrollListener: EndlessNestedScrollListener

    private lateinit var recyclerViewListing: RecyclerView

    private var query: String = ""

    private var isVoiceEnabled: Boolean = true

    private var showSuggestions: Boolean = true

    private var studentClass: String = ""

    private var isSearchPerformed: Boolean = false

    private var categoryId: String? = null

    override fun provideViewBinding(): ActivityCategorypageSearchBinding =
        ActivityCategorypageSearchBinding.inflate(layoutInflater)

    private val TAG = "CategorySearchActivity"
    override fun providePageName(): String = TAG

    override fun provideViewModel(): CategorySearchViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        initUI()

        categoryId = intent.getStringExtra(CATEGORY_ID)
        studentClass = UserUtil.getStudentClass()
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.searchKeywordInput.requestFocus()
        KeyboardUtils.showKeyboard(binding.searchKeywordInput)
    }

    private fun initUI() {
        setUpRecyclerViews()
        setUpObservers()
        setUpListeners()

        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.SEARCH_BAR_CLICKED, ignoreSnowplow = true))
    }

    private var maxSearchQuery = ""

    private fun setUpListeners() {

        binding.searchKeywordInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val clearIcon: Int
                if (s?.trim()?.isNotEmpty() == true) {
                    clearIcon = R.drawable.ic_search_toolbar
                    isVoiceEnabled = false
                } else {
                    clearIcon = R.drawable.ic_voice_search_tomato
                    isVoiceEnabled = true
                }
                binding.searchActionButton.setImageResource(clearIcon)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length ?: 0 >= maxSearchQuery.length)
                    maxSearchQuery = s?.toString() ?: ""

                query = s.toString().trim()
                binding.layoutNoResults.visibility = View.GONE
                binding.tvSuggestionHeader.visibility = View.GONE
                binding.rvSuggestions.setVisibleState(showSuggestions)
                if (showSuggestions)
                    fetchSuggestionList()
                else
                    showSuggestions = true

                if (s?.isEmpty() == true) {
                    viewModel.handleLastSearchSession(maxSearchQuery)
                    maxSearchQuery = ""
                }
            }
        })

        /**
         * Enter pressed on keyboard
         **/
        binding.searchKeywordInput.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                if (query.isEmpty())
                    return@setOnEditorActionListener false

                binding.tvSuggestionHeader.visibility = View.GONE
                setTextAndHideSuggestionsAndFetchData()
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.KEYBOARD_SEARCH_CLICKED,
                        hashMapOf(
                            EventConstants.SEARCHED_TEXT to query,
                            EventConstants.STUDENT_CLASS to studentClass,
                            EventConstants.SOURCE_PAGE to categoryId.orEmpty()
                        ),
                        ignoreSnowplow = true
                    )
                )
                viewModel.addEventsToLastSession(EventConstants.KEYBOARD_SEARCH_CLICKED)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        binding.ivBack.setOnClickListener {
            if (!isSearchPerformed) {
                analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.LCS_EMPTY_BACK, ignoreSnowplow = true))
                viewModel.addEventsToLastSession(EventConstants.LCS_EMPTY_BACK)
            }
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.BACK_CLICKED,
                    hashMapOf(
                        EventConstants.SEARCHED_TEXT to query,
                        EventConstants.STUDENT_CLASS to studentClass,
                        EventConstants.SOURCE_PAGE to categoryId.orEmpty()
                    ),
                    ignoreSnowplow = true
                )
            )
            viewModel.addEventsToLastSession(EventConstants.BACK_CLICKED)
            onBackPressed()
        }

        binding.ivCross.setOnClickListener {
            if (query.isEmpty()) {
                return@setOnClickListener
            }
            query = ""

            showSuggestions = true
            binding.searchKeywordInput.setText(query)
            initiateRecyclerListenerAndFetchInitialData()
            maxSearchQuery = ""
            viewModel.addEventsToLastSession(EventConstants.LCS_CROSS_CLICKED)
            viewModel.postPreviousSessions()
        }

        binding.searchActionButton.setOnClickListener {
            binding.tvSuggestionHeader.visibility = View.GONE
            if (isVoiceEnabled) {
                promptSpeechInput()
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.VOICE_SEARCH_CLICKED,
                        hashMapOf(
                            EventConstants.SEARCHED_TEXT to query,
                            EventConstants.STUDENT_CLASS to studentClass,
                            EventConstants.SOURCE_PAGE to categoryId.orEmpty()
                        ),
                        ignoreSnowplow = true
                    )
                )
                viewModel.addEventsToLastSession(EventConstants.VOICE_SEARCH_CLICKED)
            } else if (query.isNotEmpty()) {
                setTextAndHideSuggestionsAndFetchData()
            }
        }
    }

    private fun setUpRecyclerViews() {
        recyclerViewListing = binding.rvWidgets
        adapter = WidgetLayoutAdapter(this, this)
        recyclerViewListing.layoutManager = LinearLayoutManager(this)
        recyclerViewListing.adapter = adapter
        initiateRecyclerListenerAndFetchInitialData()

        suggestionListAdapter = SuggestionListAdapter(
            arrayListOf(),
            categoryId.orEmpty(),
            deeplinkAction,
            this,
            analyticsPublisher
        )
        binding.rvSuggestions.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvSuggestions.adapter = suggestionListAdapter
    }

    private fun setTextAndHideSuggestionsAndFetchData() {
        showSuggestions = false
        binding.searchKeywordInput.setText(query)
        binding.searchKeywordInput.setSelection(query.length)
        fetchList(1)
    }

    private fun initiateRecyclerListenerAndFetchInitialData() {
        val startPage = 1
        recyclerViewListing.clearOnScrollListeners()
        infiniteScrollListener = object : EndlessNestedScrollListener() {
            override fun onLoadMore(current_page: Int) {
                this.run {
                    fetchList(current_page)
                }
            }
        }

//        recyclerViewListing.addOnScrollListener(infiniteScrollListener)
        binding.nestedScrollView.setOnScrollChangeListener(infiniteScrollListener)
        infiniteScrollListener.setStartPage(1)

        binding.layoutNoResults.visibility = View.GONE
        binding.rvSuggestions.visibility = View.VISIBLE
        showSuggestions = true

        binding.tvSuggestionHeader.visibility = View.VISIBLE
        fetchList(startPage)
        fetchSuggestionList()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.postPreviousSessions()
    }

    private fun fetchList(pageNumber: Int) {
        if (!query.isNullOrEmpty()) {
            isSearchPerformed = true
        }
        viewModel.getCategorySearchData(
            hashMapOf<String, Any>().apply {
                put(PAGE_TEXT, pageNumber)
                put(SEARCH_TEXT, query)
            }.toRequestBody()
        )

        if (pageNumber == 1) {
            infiniteScrollListener.refresh()
            infiniteScrollListener.setStartPage(1)
        }
    }

    private fun fetchSuggestionList() {
        if (!query.isNullOrEmpty()) {
            isSearchPerformed = true
        }
        viewModel.getSuggestionData(query)
    }

    private fun setUpObservers() {

        viewModel.searchLiveData.observeK(
            this,
            this::onWidgetDataFetched,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )

        viewModel.suggestionLiveData.observeK(
            this,
            this::onSuggestionDataFetched,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateSuggestionProgress
        )
    }

    private fun onWidgetDataFetched(data: WidgetsResponseData) {

        if (data.widgetsList.isEmpty()) {
            infiniteScrollListener.setLastPageReached(true)
//            infiniteScrollListener.refresh()
        }

        recyclerViewListing.setVisibleState(!data.widgetsList.isNullOrEmpty())

        if (infiniteScrollListener.currentPage == 1) {
            adapter.setWidgets(data.widgetsList)
        } else {
            adapter.addWidgets(data.widgetsList)
        }

        binding.ivNoResults.loadImageEtx(data.imageUrl.orEmpty())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.tvTitleNoResults.text =
                Html.fromHtml(data.noResultText.orEmpty(), Html.FROM_HTML_MODE_LEGACY)
            binding.tvSubtitleNoResults.text =
                Html.fromHtml(data.suggestionText.orEmpty(), Html.FROM_HTML_MODE_LEGACY)
            binding.tvWidgetsHeader.text =
                Html.fromHtml(data.title.orEmpty(), Html.FROM_HTML_MODE_LEGACY)
        } else {
            binding.tvTitleNoResults.text = Html.fromHtml(data.noResultText.orEmpty())
            binding.tvSubtitleNoResults.text = Html.fromHtml(data.suggestionText.orEmpty())
            binding.tvWidgetsHeader.text = Html.fromHtml(data.title)
        }

        if (query.isNotEmpty())
            binding.layoutNoResults.setVisibleState(!data.noResultText.isNullOrEmpty())
    }

    private fun onSuggestionDataFetched(data: SuggestionData) {

        binding.rvSuggestions.visibility = View.VISIBLE
        suggestionListAdapter.list = data.suggestionList.orEmpty()
        suggestionListAdapter.notifyDataSetChanged()
        binding.tvSuggestionHeader.text = data.suggestionTitle
    }

    private fun unAuthorizeUserError() {
        supportFragmentManager.beginTransaction()
            .add(BadRequestDialog.newInstance("unauthorized"), "BadRequestDialog").commit()
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        this.let { currentContext ->
            if (NetworkUtils.isConnected(currentContext)) {
                toast(getString(R.string.somethingWentWrong))
            } else {
                toast(getString(R.string.string_noInternetConnection))
            }
        }
    }

    private fun updateProgress(state: Boolean) {
        infiniteScrollListener.setDataLoading(state)
        binding.progressBarWidgets.setVisibleState(state)
    }

    private fun updateSuggestionProgress(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }

    /**
     * Showing google speech input dialog
     */
    private fun promptSpeechInput() {
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
                if (resultCode == RESULT_OK && data != null) {
                    val result =
                        data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).orEmpty()
                    query = result[0]
                    setTextAndHideSuggestionsAndFetchData()
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.VOICE_SEARCH_EXECUTED,
                            hashMapOf(
                                EventConstants.SEARCHED_TEXT to query,
                                EventConstants.STUDENT_CLASS to studentClass,
                                EventConstants.SOURCE_PAGE to categoryId.orEmpty()
                            ),
                            ignoreSnowplow = true
                        )
                    )
                    viewModel.addEventsToLastSession(EventConstants.VOICE_SEARCH_EXECUTED)
                }
            }
            else -> {
            }
        }
    }

    override fun performAction(action: Any) {

        if (action is OnInitialSuggestionClicked) {
            query = action.query
            setTextAndHideSuggestionsAndFetchData()
            val timeStamp = System.currentTimeMillis()
            viewModel.addSession(
                SearchSessionModel(
                    uscId = timeStamp,
                    searchedText = query,
                    size = 0,
                    timeStamp = timeStamp,
                    isSearched = null,
                    isMatched = false,
                    eventTypes = mutableListOf(EventConstants.POPULAR_SUGGESTION_CLICKED)
                )
            )
        }

        if (action is OnLCSSuggestionClicked) {
            val data = action.data
            viewModel.addEventsToLastSession(EventConstants.SUGGESTION_RESULTS_CLICKED)
            viewModel.updateIsMatched(true)
            viewModel.addClickedItemsToLastSession(
                data.titleText.orEmpty(),
                data.resourceId.orEmpty(),
                action.position,
                "",
                -1,
                false
            )
        }

        if (action is OnCourseCarouselChildWidgetItmeClicked) {
            viewModel.addEventsToLastSession(EventConstants.LCS_COURSE_RESULTS_CLICKED)
            viewModel.updateIsMatched(true)
            viewModel.addClickedItemsToLastSession(
                action.title,
                action.id,
                action.position,
                "",
                -1,
                false
            )
        }
    }
}

class SuggestionListAdapter(
    var list: List<SuggestionListItem>,
    val categoryId: String,
    val deeplinkAction: DeeplinkAction,
    val actionPerformer: ActionPerformer,
    val analyticsPublisher: AnalyticsPublisher
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.suggestion_list_item, parent, false)
        return SuggestionListViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = list[position]
        (holder as SuggestionListViewHolder).bindData(
            position,
            data,
            categoryId,
            deeplinkAction,
            actionPerformer,
            analyticsPublisher
        )
    }

    class SuggestionListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(
            position: Int,
            data: SuggestionListItem,
            categoryId: String,
            deeplinkAction: DeeplinkAction,
            actionPerformer: ActionPerformer,
            analyticsPublisher: AnalyticsPublisher
        ) {
            itemView.tvTitle.text = data.titleText
            itemView.tvType.text = data.typeText

            itemView.setOnClickListener {

                when (data.type) {

                    "test" -> {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.SUGGESTION_RESULTS_CLICKED,
                                hashMapOf(
                                    EventConstants.SEARCHED_TEXT to data.titleText.toString(),
                                    EventConstants.STUDENT_CLASS to UserUtil.getStudentClass(),
                                    EventConstants.SOURCE_PAGE to categoryId.orEmpty()
                                ),
                                ignoreSnowplow = true
                            )
                        )
                        if (data.isPremium == true && data.isVip != true) {
                            deeplinkAction.performAction(itemView.context, data.paymentDeeplink)
                        } else {
                            itemView.context.startActivity(
                                MockTestSubscriptionActivity.getStartIntent(
                                    itemView.context,
                                    data.resourceId.orDefaultValue().toInt(), false
                                )
                            )
                        }
                    }

                    "" -> {
                        /**
                         * popular searches clicked
                         * **/
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.POPULAR_SUGGESTION_CLICKED,
                                hashMapOf(
                                    EventConstants.SEARCHED_TEXT to data.titleText.toString(),
                                    EventConstants.STUDENT_CLASS to UserUtil.getStudentClass(),
                                    EventConstants.SOURCE_PAGE to categoryId.orEmpty()
                                ),
                                ignoreSnowplow = true
                            )
                        )
                        actionPerformer.performAction(OnInitialSuggestionClicked(data.titleText.orEmpty()))
                    }

                    else -> {
                        /**
                         * searched item clicked
                         * **/
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.SUGGESTION_RESULTS_CLICKED,
                                hashMapOf(
                                    EventConstants.SEARCHED_TEXT to data.titleText.toString(),
                                    EventConstants.STUDENT_CLASS to UserUtil.getStudentClass(),
                                    EventConstants.SOURCE_PAGE to categoryId.orEmpty()
                                ),
                                ignoreSnowplow = true
                            )
                        )
                        if (data.isPremium == true && data.isVip != true) {
                            deeplinkAction.performAction(itemView.context, data.paymentDeeplink)
                        } else {
                            deeplinkAction.performAction(itemView.context, data.deeplink)
                        }
                        actionPerformer.performAction(OnLCSSuggestionClicked(data, position))
                    }
                }
            }
        }
    }
}

@Keep
data class SuggestionData(
    @SerializedName("title") val suggestionTitle: String?,
    @SerializedName("items") val suggestionList: List<SuggestionListItem>?
)

@Keep
data class SuggestionListItem(
    @SerializedName("display_name") val titleText: String?,
    @SerializedName("display_type") val type: String?,
    @SerializedName("display_type_title") val typeText: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("is_premium") val isPremium: Boolean?,
    @SerializedName("is_vip") val isVip: Boolean?,
    @SerializedName("payment_deeplink") val paymentDeeplink: String?,
    @SerializedName("resource_reference") val resourceId: String?
)

@Keep
data class WidgetsResponseData(
    @SerializedName("widgets") val widgetsList: List<WidgetEntityModel<*, *>>,
    @SerializedName("title") val title: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("no_result_text") val noResultText: String?,
    @SerializedName("suggestion_text") val suggestionText: String?
)
