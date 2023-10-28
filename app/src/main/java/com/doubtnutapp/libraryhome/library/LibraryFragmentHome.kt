package com.doubtnutapp.libraryhome.library

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.noticeboard.NoticeBoardConstants
import com.doubtnut.noticeboard.data.entity.UnreadNoticeCountUpdate
import com.doubtnut.noticeboard.data.remote.NoticeBoardRepository
import com.doubtnut.noticeboard.ui.NoticeBoardDetailActivity
import com.doubtnutapp.*
import com.doubtnutapp.Constants.IS_COURSE_SELECTION_SHOWN
import com.doubtnutapp.Constants.MY_COURSE
import com.doubtnutapp.Constants.SHOULD_SHOW_MY_COURSE
import com.doubtnutapp.Constants.SHOW_TIMETABLE
import com.doubtnutapp.Constants.STUDENT_CLASS
import com.doubtnutapp.EventBus.ScrollToTopEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.RefreshUI
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.FragmentLibraryHomeV2Binding
import com.doubtnutapp.domain.library.entities.ClassListViewItem
import com.doubtnutapp.home.event.HomeEventManager
import com.doubtnutapp.libraryhome.course.ui.ExploreFragment
import com.doubtnutapp.libraryhome.course.ui.ScheduleFragment
import com.doubtnutapp.libraryhome.coursev3.ui.CourseFragment
import com.doubtnutapp.libraryhome.dailyquiz.ui.DailyQuizFragment
import com.doubtnutapp.libraryhome.library.ui.adapter.LibraryTabAdapter
import com.doubtnutapp.libraryhome.library.viewmodel.LibraryHomeFragmentViewModel
import com.doubtnutapp.libraryhome.mocktest.ui.MockTestFragment
import com.doubtnutapp.newglobalsearch.ui.InAppSearchActivity
import com.doubtnutapp.newlibrary.ui.LibraryFragment
import com.doubtnutapp.notification.NotificationCenterActivity
import com.doubtnutapp.notification.NotificationCenterActivity.Companion.REQUEST_CODE_NOTIFICATION
import com.doubtnutapp.screennavigator.ClassSelectionScreen
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.teacher.TeacherFragment
import com.doubtnutapp.ui.onboarding.SelectClassFragment
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class LibraryFragmentHome : DaggerFragment() {

    companion object {
        private const val REQUEST_CODE_SELECT_CLASS = 106
        const val TAG = "LibraryFragmentHome"
    }

    private var librarySearchVersion: Int = 1
    private var isNotificationCenterEnabled: Boolean = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: LibraryHomeFragmentViewModel
    lateinit var widgetPlanButtonVM: WidgetPlanButtonVM

    private var selectedClass: ClassListViewItem? = null
    private lateinit var spinnerAdapter: ArrayAdapter<ClassListViewItem>

    private var classesList: ArrayList<ClassListViewItem> = ArrayList()

    @Inject
    lateinit var screenNavigator: Navigator

    lateinit var adapter: LibraryTabAdapter

    @Keep
    enum class TAB_TITLE(val variant: Int) {
        FREE_BOOKS(1),
        NCERT(2)
    }

    private val hideTeachersTab by lazy {
        FeaturesManager.isFeatureEnabled(
            requireContext(),
            Features.HIDE_TEACHERS_TAB
        )
    }

    @Inject
    lateinit var homeEventManager: HomeEventManager

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private lateinit var eventTracker: Tracker

    private var selectedViewPagerTab = 0
    private var source: String? = ""
    private var sourceForSearchPage: String = TAG

    private var selectedTab: TabLayout.Tab? = null

    private var titleList = mutableListOf<String>()
    private var fragmentTags = mutableListOf<String>()
    private var fragmentList = mutableListOf<Fragment>()
    private var searchHintMap = mapOf<String, List<String>?>()
    private lateinit var mListener: SharedPreferences.OnSharedPreferenceChangeListener

    var showNoticeBoard: Boolean = false
    private var appStateObserver: Disposable? = null

    private lateinit var binding: FragmentLibraryHomeV2Binding

    private val argTag: String? by lazy {
        arguments?.getString(Constants.TAG)
            ?.takeIf { it.isNotNullAndNotEmpty() }
            ?: defaultPrefs().getString(Constants.DEFAULT_ONLINE_CLASS_TAB_TAG, null)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLibraryHomeV2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (context != null && activity != null) {
            FirebaseAnalytics.getInstance(requireContext())
                .setCurrentScreen(requireActivity(), TAG, TAG)
        }
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(LibraryHomeFragmentViewModel::class.java)
        widgetPlanButtonVM = activityViewModelProvider(viewModelFactory)

        showNoticeBoard =
            FeaturesManager.isFeatureEnabled(requireContext(), Features.NOTICE_BOARD)
                    || defaultPrefs(requireContext()).getBoolean(
                NoticeBoardConstants.NB_LIVE_CLASS_ENABLED,
                false
            )


        appStateObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe {
            if (it is UnreadNoticeCountUpdate) {
                updateUnreadNoticeCount()
            } else if (it is RefreshUI) {
                viewModel.getSearchHintData()
            }
        }

        if (showNoticeBoard) {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    name = EventConstants.DN_NB_ICON_VISIBLE,
                    params = hashMapOf(
                        EventConstants.SOURCE to TAG,
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.STUDENT_CLASS to UserUtil.getStudentClass(),
                        EventConstants.BOARD to UserUtil.getUserBoard(),
                    ),
                    ignoreFacebook = true
                )
            )
        }

        isNotificationCenterEnabled = !showNoticeBoard

        binding.ivNoticeBoard.isVisible = showNoticeBoard
        binding.tvNoticeBoard.isVisible = showNoticeBoard

        arguments?.let {
            if (argTag.isNullOrEmpty()) {
                selectedViewPagerTab =
                    it.getString(Constants.LIBRARY_SCREEN_SELECTED_TAB, "0").toIntOrNull()
                        ?: 0
            }
            source = it.getString(Constants.SOURCE, "")
        }
        setupTabs()

        setData()
        setClickListener()
        eventTracker = getTracker()
        setListener()
        viewModel.getSearchHintData()
        setupNotificationCenter()

        adapter.fragmentTags.indexOf(argTag)
            .takeIf { it != -1 }
            ?.let {
                selectedViewPagerTab = it
                binding.libraryViewPager.currentItem = selectedViewPagerTab
            }

        widgetPlanButtonVM.updateWidgetViewPlanButtonVisibility(
            isWidgetViewPlanButtonVisible = adapter.fragmentTags.getOrNull(
                selectedViewPagerTab
            ) == LibraryTabAdapter.TAG_MY_COURSES
        )

    }

    private fun setPreferenceChangeListener() {
        if (!isNotificationCenterEnabled) {
            return
        }
        mListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == Constants.UNREAD_NOTIFICATION_COUNT) {
                setNotificationCount()
            }
        }
        defaultPrefs().registerOnSharedPreferenceChangeListener(mListener)
    }

    private fun setupNotificationCenter() {
        binding.tvNotificationCount.isVisible = isNotificationCenterEnabled
        binding.ivNotificationBell.isVisible = isNotificationCenterEnabled
    }

    private fun setNotificationCount() {
        if (!isNotificationCenterEnabled) {
            return
        }
        val notificationCount = defaultPrefs().getString(Constants.UNREAD_NOTIFICATION_COUNT, "0")
            .orDefaultValue()
        if (notificationCount != "0") {
            binding.tvNotificationCount.show()
            binding.tvNotificationCount.text = notificationCount
        } else {
            binding.tvNotificationCount.hide()
        }
    }

    override fun onResume() {
        super.onResume()
        val imageUrl = defaultPrefs(requireContext()).getString("image_url", "") ?: ""
        if (imageUrl.isNotBlank()) {
            binding.imageViewUser.loadImage(imageUrl, R.drawable.ic_person_grey)
        }
        selectedClass = null
        viewModel.getClassesList()
        playSearchAnimation()
        updateUnreadNoticeCount()
    }

    override fun onStart() {
        super.onStart()
        setNotificationCount()
        setPreferenceChangeListener()
    }

    override fun onStop() {
        super.onStop()
        if (this::mListener.isInitialized) {
            defaultPrefs().unregisterOnSharedPreferenceChangeListener(mListener)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.globalSearch.stopAnim()
    }

    private fun playSearchAnimation() {
        val studentClass = defaultPrefs().getString(Constants.STUDENT_CLASS, "")
        val list = searchHintMap[studentClass ?: ""]
        if (!list.isNullOrEmpty()) {
            binding.globalSearch.setTexts(list)
            binding.globalSearch.playAnim()
        }
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = activity?.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

    private fun getTabTitleList() {
        titleList = mutableListOf(
            resources.getString(R.string.free_ncert),
            resources.getString(R.string.daily_quiz),
            resources.getString(R.string.mock_test)
        )
        fragmentTags = mutableListOf(
            LibraryTabAdapter.TAG_LIBRARY,
            LibraryTabAdapter.TAG_DAILY_QUIZ,
            LibraryTabAdapter.TAG_MOCK_TEST,
        )

        if (!hideTeachersTab) {
            titleList.add(resources.getString(R.string.library_tab_teacher))
            fragmentTags.add(LibraryTabAdapter.TAG_TEACHERS)
        }

        titleList.add(0, resources.getString(R.string.free_classes))
        fragmentTags.add(0, LibraryTabAdapter.TAG_FREE_CLASSES)

        if (defaultPrefs().getBoolean(SHOW_TIMETABLE, false)) {
            titleList.add(0, resources.getString(R.string.timetable))
            fragmentTags.add(0, LibraryTabAdapter.TAG_TIMETABLE)
        }

        titleList.add(0, resources.getString(R.string.check_all_courses))
        fragmentTags.add(0, LibraryTabAdapter.TAG_CHECK_ALL_COURSES)
        sourceForSearchPage = LibraryTabAdapter.TAG_CHECK_ALL_COURSES

        if (defaultPrefs().getBoolean(SHOULD_SHOW_MY_COURSE, false)) {
            titleList.add(0, resources.getString(R.string.my_course))
            fragmentTags.add(0, LibraryTabAdapter.TAG_MY_COURSES)
            sourceForSearchPage = LibraryTabAdapter.TAG_MY_COURSES
        }
    }

    private fun setClickListener() {
        binding.ivNoticeBoard.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    name = EventConstants.DN_NB_ICON_CLICKED,
                    params = hashMapOf(
                        EventConstants.SOURCE to TAG,
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.STUDENT_CLASS to UserUtil.getStudentClass(),
                        EventConstants.BOARD to UserUtil.getUserBoard(),
                    ),
                    ignoreFacebook = true
                )
            )
            startActivity(NoticeBoardDetailActivity.getStartIntent(requireContext()))
        }

        binding.ivNotificationBell.setOnClickListener {
            activity?.startActivityForResult(
                NotificationCenterActivity.getStartIntent(
                    requireContext(),
                    NotificationCenterActivity.LIBRARY
                ), REQUEST_CODE_NOTIFICATION
            )
        }

        binding.userSelectedClass.setOnClickListener {
            sendEvent(EventConstants.EVENT_NAME_OPEN_CLASS_PAGE_LIBRARY)
        }

        binding.imageViewUser.setOnClickListener {
            if (activity != null && activity is MainActivity) {
                (activity as MainActivity).openDrawer()
            }
        }

        binding.libraryTabLayout.addOnTabSelectedListener(object :
            TabLayout.BaseOnTabSelectedListener<TabLayout.Tab> {
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
                val view = p0?.customView
                view?.findViewById<TextView>(R.id.customTabText)
                    ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                selectedTab = p0
                val view = p0?.customView
                view?.findViewById<TextView>(R.id.customTabText)
                    ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.redTomato))
                val eventName = EventConstants.SELECT_LIBRARY_TOP_TAB + "_" + p0?.text.toString()
                sourceForSearchPage = fragmentTags.get(titleList.indexOf(p0?.text.toString()))
                sendEvent(eventName)
                analyticsPublisher.publishEvent(AnalyticsEvent(eventName, ignoreSnowplow = true))
            }

        })
    }

    private fun setupTabs() {
        adapter = LibraryTabAdapter(childFragmentManager)
        binding.libraryViewPager.addOnPageChangeListener(object :ViewPager.OnPageChangeListener{
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                DoubtnutApp.INSTANCE.bus()?.send(ScrollToTopEvent())
                widgetPlanButtonVM.updateWidgetViewPlanButtonVisibility(
                    isWidgetViewPlanButtonVisible = adapter.fragmentTags.getOrNull(
                        position
                    ) == LibraryTabAdapter.TAG_MY_COURSES
                )
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })

        binding.libraryViewPager.adapter = adapter
        binding.libraryTabLayout.setupWithViewPager(binding.libraryViewPager)
        updateAdapter()
        binding.libraryViewPager.currentItem = selectedViewPagerTab
    }

    private fun updateUnreadNoticeCount() {
        if (!showNoticeBoard){
            binding.tvNoticeBoard.hide()
            return
        }
        if (NoticeBoardRepository.unreadNoticeIds.isEmpty()){
            binding.tvNoticeBoard.text = ""
            binding.tvNoticeBoard.hide()
        } else {
            binding.tvNoticeBoard.text = NoticeBoardRepository.unreadNoticeIds.size.toString()
            binding.tvNoticeBoard.show()
        }
    }

    private fun setData() {
        binding.userSelectedClass.text =
            defaultPrefs(requireActivity()).getString(Constants.STUDENT_CLASS_DISPLAY, "")
    }

    private fun getStudentClass(): String =
        defaultPrefs(requireActivity()).getString(Constants.STUDENT_CLASS, "") ?: ""

    private fun sendEvent(eventName: String) {
        activity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addStudentId(getStudentId())
                .addEventParameter(EventConstants.PARAM_CLASS, getStudentClass())
                .addScreenName(EventConstants.EVENT_NAME_LIBRARY_HOME_PAGE)
                .track()
        }
    }

    private fun setListener() {
        binding.globalSearch.setOnClickListener {
        }
        binding.globalSearch.apply {
            isRepeat = true
            setCharacterDelay(100)
            setChangeStringDelay(1000)
            setRepeatAnimDelay(5)
        }

        binding.globalSearch.setOnTouchListener { v, event ->
            val DRAWABLE_RIGHT = 2
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = binding.globalSearch.compoundDrawables[DRAWABLE_RIGHT]
                if (event.rawX >= (binding.globalSearch.right - (drawableRight?.bounds?.width()
                        ?: 0))
                ) {
                    openSearchActivity(true)
                } else if (event.rawX < (binding.globalSearch.right - (drawableRight?.bounds?.width()
                        ?: 0))
                ) {
                    openSearchActivity(false)
                }
            }
            return@setOnTouchListener false
        }

        binding.userSelectedClass.setOnClickListener {
            requestClassSelection()
        }
        viewModel.classesListLiveData.observe(viewLifecycleOwner) { outcome ->
            if (outcome is Outcome.Success) {
                updateClassList(outcome.data.classList as ArrayList<ClassListViewItem>)
            }
        }
        viewModel.searchHintMapLiveData.observe(viewLifecycleOwner) {
            searchHintMap = it
            playSearchAnimation()
        }
    }

    fun requestClassSelection() {
        val args = Bundle()
        args.putBoolean(SelectClassFragment.INTENT_SOURCE_LIBRARY, true)
        screenNavigator.startActivityForResultFromFragment(
            this,
            ClassSelectionScreen,
            args,
            REQUEST_CODE_SELECT_CLASS
        )
    }

    private fun openSearchActivity(startVoiceSearch: Boolean) {
        val userClass = defaultPrefs().getString(STUDENT_CLASS, "")?.toIntOrNull()
        selectedClass = if (userClass == null) null else classesList.find { userClass == it.classNo }
        Utils.executeIfContextNotNull(context) { context: Context ->
            InAppSearchActivity.startActivity(
                context,
                sourceForSearchPage,
                startVoiceSearch,
                selectedClass,
                classesList
            )
        }

        sendEvent(EventConstants.EVENT_NAME_SEARCH_ICON_CLICK_LIBRARY)
        homeEventManager.onSearchIconClick(TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_SELECT_CLASS) {
            updateClassText()
            binding.libraryViewPager.currentItem = 0
            sendEvent(EventConstants.EVENT_NAME_CLASS_CHANGE_FROM_LIBRARY)
        }
    }

    private fun updateClassText() {
        val selectedClass =
            defaultPrefs(requireContext()).getString(Constants.STUDENT_CLASS_DISPLAY, "")
        binding.userSelectedClass.text = selectedClass
        updateAdapter()
    }

    private fun updateAdapter() {
        fragmentList = mutableListOf(
            LibraryFragment.newInstance(),
            DailyQuizFragment.newInstance(),
            MockTestFragment.newInstance()
        )
        if (!hideTeachersTab) {
            fragmentList.add(TeacherFragment.newInstance())
        }

        fragmentList.add(0, ExploreFragment.newInstance("free_classes",
            source = source))

        if (defaultPrefs().getBoolean(SHOW_TIMETABLE, false)) {
            fragmentList.add(0, ScheduleFragment.newInstance())
        }

        fragmentList.add(0, ExploreFragment.newInstance(
            source = source
        ))
        if (defaultPrefs().getBoolean(SHOULD_SHOW_MY_COURSE, false)) {
            var categoryId: String
            var assortmentId: String
            if (defaultPrefs().getBoolean(IS_COURSE_SELECTION_SHOWN, false)) {
                categoryId = defaultPrefs().getString(Constants.SELECTED_CATEGORY_ID, "").orEmpty()
                assortmentId =
                    defaultPrefs().getString(Constants.SELECTED_ASSORTMENT_ID, "").orEmpty()
                if (categoryId.isEmpty() && assortmentId.isEmpty()) {
                    categoryId = defaultPrefs().getString(Constants.CATEGORY_ID, "").orEmpty()
                    assortmentId =
                        defaultPrefs().getString(Constants.PURCHASED_ASSORTMENT_ID, "").orEmpty()
                    defaultPrefs().edit().putBoolean(IS_COURSE_SELECTION_SHOWN, false).apply()
                }
            } else {
                categoryId = defaultPrefs().getString(Constants.CATEGORY_ID, "").orEmpty()
                assortmentId =
                    defaultPrefs().getString(Constants.PURCHASED_ASSORTMENT_ID, "").orEmpty()
            }
            when {
                categoryId.isNotEmpty() -> {
                    fragmentList.add(0, ExploreFragment.newInstance(categoryId = categoryId, source = MY_COURSE))
                }
                assortmentId.isNotEmpty() -> {
                    fragmentList.add(0, CourseFragment.newInstance(
                        assortmentId = assortmentId,
                        source = MY_COURSE
                    ))
                }
                else -> {
                    fragmentList.add(0, CourseFragment.newInstance(source = MY_COURSE))
                }
            }
        }
        getTabTitleList()
        adapter.updateTabs(titleList, fragmentTags, fragmentList)
        showNewBadge()
    }

    fun updateAdapterOnNoMockTest() {
        activity?.let { fragmentActivity ->
            fragmentActivity.supportFragmentManager.executePendingTransactions()
            fragmentManager?.executePendingTransactions()
            childFragmentManager.executePendingTransactions()
            titleList.remove(resources.getString(R.string.mock_test))
            fragmentTags.remove(LibraryTabAdapter.TAG_MOCK_TEST)
            fragmentList.find { it is MockTestFragment }?.let { mockTestFragment ->
                fragmentList.remove(mockTestFragment)
            }
            adapter.updateTabs(titleList, fragmentTags, fragmentList)
            showNewBadge()
        }
    }

    private fun showNewBadge() {
        for (i in 0 until binding.libraryTabLayout.tabCount) {
            val tab = binding.libraryTabLayout.getTabAt(i)
            tab?.customView = getTabView(i)
            var view: View? = null

            if (selectedViewPagerTab != 0) {
                val cTab = binding.libraryTabLayout.getTabAt(selectedViewPagerTab)
                view = cTab?.customView
            } else if (selectedViewPagerTab == 0 && selectedTab == null && i == 0) {
                view = tab?.customView
            } else if (selectedTab != null) {
                view = selectedTab?.customView
            }
            view?.findViewById<TextView>(R.id.customTabText)
                ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.redTomato))
        }
    }

    private fun getTabView(position: Int): View {
        val v = LayoutInflater.from(context).inflate(R.layout.item_library_tab, null)
        val customTextView = v.findViewById<TextView>(R.id.customTabText)
        val notificationBadge = v.findViewById<TextView>(R.id.notification_badge)
        customTextView.text = titleList[position]

        if (shouldShowCrashCourse() &&
            position == 1
        ) {
            notificationBadge.show()
        } else {
            notificationBadge.invisible()
        }
        return v
    }

    private fun shouldShowCrashCourse(): Boolean {
        return false
    }

    private fun updateClassList(dataList: ArrayList<ClassListViewItem>) {
        classesList = dataList
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appStateObserver?.dispose()
    }

}