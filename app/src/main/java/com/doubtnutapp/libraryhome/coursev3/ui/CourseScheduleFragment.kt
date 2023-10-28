package com.doubtnutapp.libraryhome.coursev3.ui

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.ScheduleMonthFilterSelectAction
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.course.widgets.ScheduleMonthFilterWidgetModel
import com.doubtnutapp.course.widgets.ScheduleWidget
import com.doubtnutapp.data.remote.models.ApiCourseDetailData
import com.doubtnutapp.databinding.FragmentCourseScheduleBinding
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.libraryhome.coursev3.viewmodel.CourseScheduleViewModel
import com.doubtnutapp.liveclass.ui.CourseCategoryActivity
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class CourseScheduleFragment : BaseBindingFragment<CourseScheduleViewModel, FragmentCourseScheduleBinding>(),
    ActionPerformer {

    companion object {
        private const val TAG = "CourseScheduleFragment"
        private const val ASSORTMENT_ID = "assortment_id"
        fun newInstance(assortmentId: String, source: String) = CourseScheduleFragment()
                .apply {
                    arguments = Bundle().apply {
                        putString(ASSORTMENT_ID, assortmentId)
                        putString(Constants.SOURCE, source)
                    }
                }
    }

    private lateinit var adapter: WidgetLayoutAdapter

    private lateinit var filterAdapter: WidgetLayoutAdapter

    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener

    private lateinit var recyclerViewListing: RecyclerView

    private var assortmentId: String = ""

    private var selectedMonth: String? = null

    private var prevMonth: String? = null

    private var widgets: MutableList<WidgetEntityModel<*, *>?> = mutableListOf()
    private var filterWidgets: MutableList<WidgetEntityModel<*, *>?> = mutableListOf()

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var monthTag: String = ""

    override fun providePageName(): String = TAG

    override fun provideViewModel(): CourseScheduleViewModel =
        viewModelProvider(viewModelFactory)

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCourseScheduleBinding =
        FragmentCourseScheduleBinding.inflate(layoutInflater)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        assortmentId = arguments?.getString(ASSORTMENT_ID, "").orEmpty()
        viewModel.extraParams.apply {
            put(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.NAME, TAG)
            if (activity is MainActivity) {
                put(EventConstants.EVENT_PARENT_SCREEN_PREFIX + EventConstants.NAME, MainActivity.TAG)
            } else {
                put(EventConstants.EVENT_PARENT_SCREEN_PREFIX + EventConstants.NAME, CourseCategoryActivity.TAG)
            }
        }
        setUpRecyclerView()
    }

    private fun manageScrollUpFabAnimation(topReached: Boolean) {
        if (topReached) {
            if (scaleDownAnimator.isRunning || mBinding?.fabScrollUp?.scaleX == 0F) return
            scaleDownAnimator.start()

        } else {
            if (scaleUpAnimator.isRunning || mBinding?.fabScrollUp?.scaleX == 1F) return
            scaleUpAnimator.start()
        }
    }

    private val scaleDownAnimator by lazy {
        val objectAnimator = ValueAnimator.ofFloat(1f, 0f)
        objectAnimator.addUpdateListener {
            mBinding?.fabScrollUp?.scaleX = it.animatedValue as Float
            mBinding?.fabScrollUp?.scaleY = it.animatedValue as Float
        }
        objectAnimator
    }

    private val scaleUpAnimator by lazy {
        val objectAnimator = ValueAnimator.ofFloat(0f, 1f)
        objectAnimator.addUpdateListener {
            mBinding?.fabScrollUp?.scaleX = it.animatedValue as Float
            mBinding?.fabScrollUp?.scaleY = it.animatedValue as Float
        }
        objectAnimator
    }

    private fun setUpRecyclerView() {
        mBinding?.rvWidgets?.let { recyclerViewListing = it }
        adapter = WidgetLayoutAdapter(requireContext(), this, arguments?.getString(Constants.SOURCE))
        recyclerViewListing.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewListing.adapter = adapter

        filterAdapter = WidgetLayoutAdapter(requireContext(), this)
        mBinding?.rvFilterWidgets?.layoutManager = LinearLayoutManager(requireContext())
        mBinding?.rvFilterWidgets?.adapter = filterAdapter
        mBinding?.fabScrollUp?.setOnClickListener {
            recyclerViewListing.scrollToPosition(0)
        }

        initiateRecyclerListenerAndFetchInitialData()
    }

    private fun initiateRecyclerListenerAndFetchInitialData() {
        mBinding?.rvFilterWidgets?.hide()
        adapter.clearData()
        filterAdapter.clearData()
        val startPage = 1
        recyclerViewListing.clearOnScrollListeners()
        infiniteScrollListener = object : TagsEndlessRecyclerOnScrollListener(recyclerViewListing.layoutManager) {
            override fun onLoadMore(currentPage: Int) {
                context?.run {
                    fetchList(currentPage)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val topReached = dy == 0 || recyclerView.computeVerticalScrollOffset() == 0
                analyticsPublisher.publishEvent(
                        AnalyticsEvent(EventConstants.LC_COURSE_TIMELINE_SCROLL,
                                hashMapOf<String, Any>().apply {
                                    put(EventConstants.ASSORTMENT_ID, assortmentId)
                                })
                )
                manageScrollUpFabAnimation(topReached)
                val layoutManager = (recyclerView.layoutManager) as LinearLayoutManager
                val firstItem = layoutManager.findFirstVisibleItemPosition()
                if (firstItem < 0) {
                    return
                }
                val itemAtPosition = widgets.getOrNull(firstItem)
                if (itemAtPosition != null && itemAtPosition is ScheduleWidget.WidgetChildModel) {
                    if (monthTag != itemAtPosition.data.tag.orEmpty()) {
                        monthTag = itemAtPosition.data.tag.orEmpty()
                        filterWidgets.find { it is ScheduleMonthFilterWidgetModel }
                                .apply {
                                    if (this != null) {
                                        val model = (this as ScheduleMonthFilterWidgetModel)
                                        model.data.subTitle = monthTag
                                    }
                                }
                        filterAdapter.setWidgets(filterWidgets)
                    }
                }
            }
        }.also {
            it.setStartPage(startPage)
        }
        recyclerViewListing.addOnScrollListener(infiniteScrollListener)
        fetchList(startPage)
    }

    private fun fetchList(pageNumber: Int) {
        viewModel.getCourseScheduleDetail(pageNumber, assortmentId, selectedMonth, prevMonth)
    }

    override fun setupObservers() {
        viewModel.widgetsLiveData.observeK(
                viewLifecycleOwner,
                this::onWidgetListFetched,
                this::onApiError,
                this::unAuthorizeUserError,
                this::ioExceptionHandler,
                this::updateProgress
        )
    }


    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(childFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        context?.let { currentContext ->
            if (NetworkUtils.isConnected(currentContext)) {
                toast(getString(R.string.somethingWentWrong))
            } else {
                toast(getString(R.string.string_noInternetConnection))
            }
        }
    }

    private fun updateProgress(state: Boolean) {
        infiniteScrollListener.setDataLoading(state)
        mBinding?.progressBar?.setVisibleState(state)
    }

    private fun onWidgetListFetched(data: ApiCourseDetailData) {
        if (data.widgets.isNullOrEmpty()) {
            infiniteScrollListener.setLastPageReached(true)
        }
        prevMonth = data.prevMonth
        if (infiniteScrollListener.currentPage == 1) {
            widgets = mutableListOf()
            widgets.addAll(data.widgets.orEmpty())
            adapter.setWidgets(data.widgets.orEmpty())
            filterWidgets = data.filterWidgets.orEmpty().toMutableList()
            if (filterWidgets.isNullOrEmpty()) {
                mBinding?.rvFilterWidgets?.hide()
            } else {
                mBinding?.rvFilterWidgets?.show()
                filterAdapter.setWidgets(filterWidgets)
            }
        } else {
            widgets.addAll(data.widgets.orEmpty())
            adapter.addWidgets(data.widgets.orEmpty())
        }
    }

    override fun performAction(action: Any) {
        if (action is ScheduleMonthFilterSelectAction) {
            selectedMonth = action.data.id
            analyticsPublisher.publishEvent(
                    AnalyticsEvent(EventConstants.LC_COURSE_TIMELINE_MONTH_SELECT,
                            hashMapOf<String, Any>().apply {
                                put(EventConstants.ASSORTMENT_ID, assortmentId)
                                put(EventConstants.MONTH, selectedMonth.orEmpty())
                            })
            )
            initiateRecyclerListenerAndFetchInitialData()
        }
    }

}