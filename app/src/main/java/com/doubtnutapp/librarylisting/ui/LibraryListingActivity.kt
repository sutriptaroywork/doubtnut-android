package com.doubtnutapp.librarylisting.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.databinding.ActivityLibraryListingBinding
import com.doubtnutapp.domain.common.entities.model.AnnouncementEntity
import com.doubtnutapp.domain.newlibrary.entities.LibraryHistoryEntity
import com.doubtnutapp.librarylisting.model.HeaderInfo
import com.doubtnutapp.librarylisting.ui.adapter.LibraryListingPagerAdapter
import com.doubtnutapp.librarylisting.viewmodel.LibraryListingCommonViewModel
import com.doubtnutapp.newglobalsearch.ui.InAppSearchActivity
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.utils.*
import com.google.android.material.tabs.TabLayout
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_library_listing.*
import kotlinx.android.synthetic.main.activity_library_listing.bottomNavigationView
import kotlinx.android.synthetic.main.activity_library_listing.toolbar
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-09-29.
 */
class LibraryListingActivity : BaseActivity(), HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var userPreference: UserPreference

    @Inject
    lateinit var dataStore: DefaultDataStore

    private lateinit var viewModel: LibraryListingCommonViewModel

    var currentTitle: String = ""

    companion object {
        const val TAG = "LibraryPlaylist"
        const val INTENT_EXTRA_PLAYLIST_ID = "playlist_id"
        const val INTENT_EXTRA_PACKAGE_ID = "package_details_id"
        const val INTENT_EXTRA_PLAYLIST_TITLE = "playlist_title"
        const val INTENT_EXTRA_PLAYLIST_PAGE = "page"
        fun getStartIntent(
            context: Context,
            playlistId: String?,
            playlistTitle: String?,
            packageDetailsId: String?,
            page: String?
        ) = Intent(context, LibraryListingActivity::class.java).apply {
            putExtra(INTENT_EXTRA_PLAYLIST_ID, playlistId.orDefaultValue())
            putExtra(INTENT_EXTRA_PLAYLIST_TITLE, playlistTitle.orDefaultValue())
            putExtra(INTENT_EXTRA_PLAYLIST_PAGE, page)
            putExtra(INTENT_EXTRA_PACKAGE_ID, packageDetailsId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.grey_statusbar_color)
        val binding = DataBindingUtil.setContentView<ActivityLibraryListingBinding>(
            this,
            R.layout.activity_library_listing
        )
        setupToolbar()
        viewModel = viewModelProvider(viewModelFactory)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        setUpView()
        setUpObserver()
        setClickListeners()
        setSearchView()
    }

    private fun setClickListeners() {
        askQuestionLibraryButton.setOnClickListener {
            CameraActivity.getStartIntent(this, TAG).also {
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(it)
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        buttonBack.setOnClickListener {
            onBackPressed()
        }
        toolbar.setContentInsetsAbsolute(0, 0)
        buttonSearch.setColorFilter(Color.parseColor("#373737"))
        buttonSearch.setOnClickListener {
            enableSearchView(textViewTitle.isVisible)
        }
    }

    private fun enableSearchView(enable: Boolean) {
        textViewTitle?.isVisible = !enable
        editSearchBox?.isVisible = enable
        buttonSearch?.setImageResource(
            if (enable)
                R.drawable.ic_close_white
            else
                R.drawable.ic_search_grey
        )
        editSearchBox?.text = null
        if (enable) {
            editSearchBox.requestFocus()
            KeyboardUtils.showKeyboard(editSearchBox)
        } else {
            KeyboardUtils.hideKeyboard(editSearchBox)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setSearchView() {

        editSearchBox?.setOnTouchListener { _, event ->
            val drawableLeft = 0;
            val drawableRight = 2;
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= editSearchBox.right - editSearchBox.compoundDrawables[drawableLeft].bounds.width()) {
                    if (viewModel.listingItems.isNotEmpty() &&
                        (viewModel.listingItems[0].viewType == R.layout.item_library_list_books ||
                                viewModel.listingItems[0].viewType == R.layout.item_library_chapter_flex)
                    ) {

                    } else {
                        openSearchActivity(false, "")
                    }
                    return@setOnTouchListener true
                }
            }
            false
        }
        editSearchBox.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (viewModel.listingItems.isNotEmpty() &&
                    (viewModel.listingItems[0].viewType == R.layout.item_library_list_books ||
                            viewModel.listingItems[0].viewType == R.layout.item_library_chapter_flex)
                ) {
                    viewModel.searchTextLiveData.value = Event(s.toString())
                } else {
                    openSearchActivity(false, "")
                }
            }
        })
    }

    private fun openSearchActivity(startVoiceSearch: Boolean, directSearchText: String) {
        val intent = InAppSearchActivity.getStartIntent(
            context = this,
            source = TAG,
            startVoiceSearch = startVoiceSearch,
            //searchQuery = "${intent.getStringExtra(INTENT_EXTRA_PLAYLIST_TITLE)} $directSearchText"
            searchQuery = directSearchText,
            redirectTab = viewModel.resourceType
        )
        startActivity(intent)
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_SEARCH_ICON_CLICK_IN_LIBRARY, ignoreSnowplow = true))
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.EVENT_NAME_SEARCH_ICON_CLICK,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.SOURCE, TAG)
                })
        )
    }

    private fun setUpView() {
        bottomNavigationView.menu.setGroupCheckable(0, false, true)
        if (intent.hasExtra(INTENT_EXTRA_PLAYLIST_ID)) {
            val playListID = intent.getStringExtra(INTENT_EXTRA_PLAYLIST_ID).orEmpty()
            viewModel.fetchListingData(
                playListID,
                packageDetailsId = intent.getStringExtra(INTENT_EXTRA_PACKAGE_ID).orEmpty(),
                source = intent.getStringExtra(INTENT_EXTRA_PLAYLIST_PAGE).orEmpty()
            )
            val packageDetailsId = intent.getStringExtra(INTENT_EXTRA_PACKAGE_ID)
            if (packageDetailsId.isNullOrEmpty()) {
                viewModel.putLibraryHistory(
                    LibraryHistoryEntity(
                        playListID, "0", if (intent.hasExtra(INTENT_EXTRA_PLAYLIST_TITLE)) {
                            intent.getStringExtra(INTENT_EXTRA_PLAYLIST_TITLE).orEmpty()
                        } else {
                            ""
                        }
                    )
                )
            }
        } else {
            finish()
        }
        if (intent.hasExtra(INTENT_EXTRA_PLAYLIST_TITLE)) {
            setTitle(intent.getStringExtra(INTENT_EXTRA_PLAYLIST_TITLE))
        }
        setLibraryTabText()
    }

    fun setLibraryTabText() {
        lifecycleScope.launchWhenStarted {
            val bottomNavIconsData = dataStore.bottomNavigationIconsData.firstOrNull()
            if (bottomNavIconsData.isNullOrEmpty()) {
                val bottomLibraryText = Utils.getLibraryBottomText(this@LibraryListingActivity)
                if (!bottomLibraryText.isNullOrBlank()) {
                    val menu = bottomNavigationView.menu
                    menu.findItem(R.id.libraryFragment).title = bottomLibraryText
                }
            }
        }
    }

    private fun setUpObserver() {
        viewModel.messageStringIdLiveData.observe(this, EventObserver {
            showToast(it)
        })

        viewModel.headerLiveData.observe(this, EventObserver {
            if ((viewModel.listingItems == null || viewModel.listingItems.isEmpty()) && it.second == null) {
                group.show()
            }
            setTitle(it.first)
            setUpTabsAndFragments(it.second)
        })
    }

    private fun setTitle(title: String?) {
        if (!title.isNullOrBlank()) {
            currentTitle = title.orEmpty()
            textViewTitle.text = currentTitle
        }
    }

    private fun showToast(@StringRes resId: Int) {
        showToastMessage(getString(resId))
    }

    private fun showToastMessage(message: String) {
        toast(message)
    }

    private fun setUpTabsAndFragments(headerInfoList: List<HeaderInfo>?) {
        val page = intent.getStringExtra(INTENT_EXTRA_PLAYLIST_PAGE)
        if ((headerInfoList != null) && headerInfoList.isNotEmpty()) {
            tabLayout.show()
            viewPager.adapter = LibraryListingPagerAdapter(
                supportFragmentManager,
                headerInfoList, page
            )

            for (i in 0 until tabLayout.tabCount) {
                tabLayout.getTabAt(i)?.customView = getTabView(headerInfoList.getOrNull(i))
            }

            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    tab.customView?.findViewById<TextView>(R.id.customTabText)?.setTextColor(
                        ContextCompat.getColor(
                            this@LibraryListingActivity,
                            R.color.redTomato
                        )
                    )
                    viewModel.onLibraryPlaylistTabSelected(tab.text.toString(), currentTitle)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    tab.customView?.findViewById<TextView>(R.id.customTabText)?.setTextColor(
                        ContextCompat.getColor(
                            this@LibraryListingActivity,
                            R.color.black
                        )
                    )
                }

                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

            tabLayout.setupWithViewPager(viewPager)
            if (tabLayout.tabCount <= 5) {
                viewPager.offscreenPageLimit = tabLayout.tabCount
            }
        } else {
            tabLayout.hide()
            viewPager.adapter = LibraryListingPagerAdapter(
                supportFragmentManager,
                //add dummy
                listOf(
                    HeaderInfo(
                        intent.getStringExtra(INTENT_EXTRA_PLAYLIST_ID).orEmpty(),
                        if (intent.hasExtra(INTENT_EXTRA_PLAYLIST_TITLE)) {
                            intent.getStringExtra(INTENT_EXTRA_PLAYLIST_TITLE).orEmpty()
                        } else {
                            ""
                        },
                        "0",
                        intent.getStringExtra(INTENT_EXTRA_PACKAGE_ID),
                        AnnouncementEntity()
                    )
                ),
                page
            )
        }
    }

    private fun getTabView(headerInfo: HeaderInfo?): View {
        val v = LayoutInflater.from(this).inflate(R.layout.tab_header_announcement, null)
        val tv = v.findViewById(R.id.customTabText) as TextView
        val notificationBadge = v.findViewById(R.id.notification_badge) as TextView
        tv.text = headerInfo?.title ?: ""
        if (RedDotNewVisibilityUtil.shouldShowRedDot(
                headerInfo?.announcement?.type,
                headerInfo?.announcement?.state
            )
        ) {
            notificationBadge.show()
        } else {
            notificationBadge.invisible()
        }
        return v
    }
}