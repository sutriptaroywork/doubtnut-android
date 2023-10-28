package com.doubtnutapp.newglobalsearch.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.*
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.newglobalsearch.model.TrendingAndRecentFeedViewItem
import com.doubtnutapp.newglobalsearch.model.TrendingSearchDataListViewItem
import com.doubtnutapp.newglobalsearch.ui.adapter.TrendingSearchFeedsAdapter
import com.doubtnutapp.newglobalsearch.viewmodel.InAppSearchViewModel
import com.doubtnutapp.newglobalsearch.viewmodel.SearchFragmentViewModel
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.in_app_search_fragment.*
import kotlinx.android.synthetic.main.in_app_search_trending_data_fragment.*
import javax.inject.Inject

class InAppSearchTrendingDataFragment : DaggerFragment(), ActionPerformer {

    companion object {
        private const val SEARCH_DATA = "search_data"
        private const val IS_TRENDING = "is_trending"

        var isTrending: Boolean? = null

        fun newInstance(dataList: List<TrendingSearchDataListViewItem>) =
                InAppSearchTrendingDataFragment().also {
                    it.arguments = bundleOf(
                            SEARCH_DATA to dataList,
                            IS_TRENDING to true
                    )
                }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var screenNavigator: Navigator

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private lateinit var viewModel: SearchFragmentViewModel

    private lateinit var parentViewModel: InAppSearchViewModel

    private var disposable: Disposable? = null

    private var searchResults: ArrayList<TrendingSearchDataListViewItem>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.in_app_search_trending_data_fragment, container, false)
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SearchFragmentViewModel::class.java)
        parentViewModel = activityViewModelProvider(viewModelFactory)

        setUpObservers()
        setUpListeners()
        init()
    }

    private fun setUpObservers() {

        viewModel.postSearchClicked.observe(viewLifecycleOwner, Observer {
            if (it.type == "video" && it.isVip) {

                parentViewModel.sendEvent(EventConstants.INAPP_SEARCH_CLICK_ETOOS_RESULT, hashMapOf<String, Any>().apply {
                    put(EventConstants.ETOOS_CONTENT_TITLE, it.display)
                }, parentViewModel.searchLandingVersion)
            }
            (activity as InAppSearchActivity).postUserSearchData(it)
        })

        viewModel.postSearchVipItemClicked.observe(viewLifecycleOwner, Observer {
            when (it.type) {
                Constants.ETOOS_VIDEO ->
                    if (parentViewModel.isVipUser) {
                        openCourseVideoPage(it.id, it.page)
                    } else {
//                        startActivity(VipPlanActivity.getStartIntent(activity as InAppSearchActivity, source = "InAppSearchTrendingDataFragment", paymentDetails = null))
                    }
                Constants.ETOOS_COURSE -> startActivity(Intent(activity as InAppSearchActivity, MainActivity::class.java).apply {
                    action = Constants.OPEN_LIBRARY_FROM_BOTTOM
                })
            }
        })

        viewModel.navigateLiveData.observe(viewLifecycleOwner, EventObserver {
            val screen = it.screen
            val args = it.hashMap
            screenNavigator.startActivityFromActivity(context!!, screen, args?.toBundle())
        })

        viewModel.recentSearchClicked.observe(viewLifecycleOwner, Observer {
            parentViewModel.recentSearchClicked(it)
        })

        viewModel.topTagSubjectClicked.observe(viewLifecycleOwner, Observer {
            parentViewModel.topTagSubjectClicked(it)
        })

        viewModel.sendEventLiveData.observe(viewLifecycleOwner, Observer {
            sendEvent(it)
        })
    }

    private fun openCourseVideoPage(id: String, page: String) {
        if (context == null) return
        val intent = VideoPageActivity.startActivity(
                context!!,
                id, "",
                "", page, "",
                false, "", "", false
        )
        startActivity(intent)
    }

    private fun setUpListeners() {
        searchFeedCardsList.setOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0)
                    (activity as InAppSearchActivity).closeKeyboard()
                else if (dy < 0)
                    (activity as InAppSearchActivity).closeKeyboard()
            }
        })
    }

    private fun init() {
        setUpSearchFeed()
        searchResults = arguments?.getParcelableArrayList<TrendingSearchDataListViewItem>(SEARCH_DATA)
        isTrending = arguments?.getBoolean(IS_TRENDING) ?: false

        if (!searchResults.isNullOrEmpty()) {
            val recentSearch = setupLocalRecentSearch()
            if (!recentSearch.isNullOrEmpty()) {
                var recentFound = false
                for (results in searchResults!!) {
                    if (results.dataType == "recent") {
                        (results.playlist as ArrayList).clear()
                        (results.playlist).addAll(getTrendingSearchData(recentSearch))
                        recentFound = true
                        break
                    }
                }
                if (!recentFound) {
                    if (searchResults!!.size >= 2) {
                        searchResults?.add(2, TrendingSearchDataListViewItem("Recent Searches",
                                "recent",
                                "",
                                "list",
                                -1,
                                R.layout.item_trending_vertical_list,
                                getTrendingSearchData(recentSearch)))
                    } else {
                        searchResults?.add(TrendingSearchDataListViewItem("Recent Searches",
                                "recent",
                                "",
                                "list",
                                -1,
                                R.layout.item_trending_vertical_list,
                                getTrendingSearchData(recentSearch)))
                    }

                }
            }
            (searchFeedCardsList.adapter as TrendingSearchFeedsAdapter).updateFeeds(searchResults!!)
        } else {
            val recentSearch = setupLocalRecentSearch()
            if (!recentSearch.isNullOrEmpty()) {
                searchResults = ArrayList()
                searchResults?.add(TrendingSearchDataListViewItem("Recent Searches",
                        "recent",
                        "",
                        "list",
                        -1,
                        R.layout.item_trending_vertical_list,
                        getTrendingSearchData(recentSearch)))
                (searchFeedCardsList.adapter as TrendingSearchFeedsAdapter).updateFeeds(searchResults!!)
            } else {
                msgNoResultFound.show()
                searchFeedCardsList.hide()
            }
        }
    }

    private fun getTrendingSearchData(recentSearch: ArrayList<String>): ArrayList<TrendingAndRecentFeedViewItem> {
        var iconImageUrl = defaultPrefs().getString(Constants.RECENT_SEARCHES_ICON_URL, "https://d10lpgp6xz60nq.cloudfront.net/images/ias_recent.png")
        defaultPrefs().edit().putString(Constants.RECENT_SEARCHES_ICON_URL, iconImageUrl).commit()

        val array = ArrayList<TrendingAndRecentFeedViewItem>()
        for (str in recentSearch) {
            array.add(TrendingAndRecentFeedViewItem("recent", str, iconImageUrl.orEmpty(), true, false, false, str, "", "",R.layout.item_recent_search))
        }
        return array

    }

    private fun setupLocalRecentSearch(): ArrayList<String>? {
        var recentSearchesSet = defaultPrefs().getString(Constants.RECENT_SEARCHES, "")
        val gson = Gson()
        val type = object : TypeToken<List<String?>?>() {}.type
        val list: ArrayList<String>? = gson.fromJson(recentSearchesSet, type)
        return list
    }


    override fun performAction(action: Any) =
            viewModel.handleAction(action)

    private fun sendEvent(action: Any) {
        when (action) {
            is NewRecentSearchClicked -> parentViewModel.sendSearchLandingClickEvent(EventConstants.SEARCH_NEW_LANDING_RECENT_SEARCH_CLICKED, action.text, action.position, parentViewModel.searchLandingVersion)

            is NewTrendingSearchClicked -> parentViewModel.sendSearchLandingClickEvent(EventConstants.SEARCH_NEW_LANDING_TRENDING_SEARCH_CLICKED, action.text, action.position, parentViewModel.searchLandingVersion)

            is NewTrendingRecentDoubtClicked -> {
                parentViewModel.sendSearchLandingClickEvent(EventConstants.SEARCH_NEW_LANDING_MOST_RECENT_DOUBT_CLICKED, action.data.ocrText, action.position, parentViewModel.searchLandingVersion)
                parentViewModel.sendMostRecentDoubtClicked(action)
            }

            is NewTrendingMostWatchedClicked -> parentViewModel.sendSearchLandingClickEvent(EventConstants.SEARCH_NEW_LANDING_MOST_WATCHED_DOUBT_CLICKED, action.text, action.position, parentViewModel.searchLandingVersion)

            is NewTrendingBookClicked -> parentViewModel.sendSearchLandingClickEvent(EventConstants.SEARCH_NEW_LANDING_BOOK_CLICKED, action.text, action.position, parentViewModel.searchLandingVersion)

            is NewTrendingPdfClicked -> parentViewModel.sendSearchLandingClickEvent(EventConstants.SEARCH_NEW_LANDING_PDF_CLICKED, action.text, action.position, parentViewModel.searchLandingVersion)

            is NewTrendingSubjectClicked -> parentViewModel.sendSearchLandingClickEvent(EventConstants.SEARCH_NEW_LANDING_SUBJECT_CLICKED, action.data.searchKey, action.position, parentViewModel.searchLandingVersion)

            is NewTrendingExamPaperClicked -> parentViewModel.sendSearchLandingClickEvent(EventConstants.SEARCH_NEW_LANDING_EXAM_PAPER_CLICKED, action.text, action.position, parentViewModel.searchLandingVersion)

            is TrendingPlaylistClicked -> (activity as InAppSearchActivity).sendTrendingClickEvent(action.type, action.searchedData, action.type, action.itemPosition, action.isRecentSearch)

            is TrendingPlaylistMongoEvent -> {
                (activity as InAppSearchActivity).sendTrendingRecentMongoClickEvent(action)
                parentViewModel.searchedDisplayTrending = action.data.display
            }

            is TrendingBookClicked -> {
                (activity as InAppSearchActivity).sendTrendingBookClickEvent(action)
                deeplinkAction.performAction(requireContext(), action.data.deeplink, Constants.PAGE_SEARCH_SRP)
            }

            is TrendingCourseClicked -> {
                (activity as InAppSearchActivity).sendTrendingCourseClickEvent(action)
                deeplinkAction.performAction(requireContext(), action.data.deeplinkUrl, Constants.PAGE_SEARCH_SRP)
                action.data.type
            }

        }
    }

    private fun setUpSearchFeed() {

        searchFeedCardsList.clearOnScrollListeners()

        val linearLayoutManager = LinearLayoutManager(activity)
        searchFeedCardsList.layoutManager = linearLayoutManager

        searchFeedCardsList.adapter = TrendingSearchFeedsAdapter(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }
}
