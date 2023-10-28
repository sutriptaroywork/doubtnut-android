package com.doubtnutapp.libraryhome.course.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.ApiScheduleData
import com.doubtnutapp.data.remote.models.ScheduleTodayData
import com.doubtnutapp.databinding.FragmentScheduleBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.libraryhome.course.data.*
import com.doubtnutapp.libraryhome.course.viewmodel.ScheduleViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.vipplan.ui.MyPlanActivity
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.uxcam.UXCam
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 26/09/20.
 */
class ScheduleFragment : BaseBindingFragment<ScheduleViewModel, FragmentScheduleBinding>() {

    companion object {
        const val ID = "id"
        const val TAG = "ScheduleFragment"
        const val NEXT = "next"
        const val PREVIOUS = "previous"
        const val FIRST = "first"
        fun newInstance(): ScheduleFragment = ScheduleFragment()
    }

    private var adapter: ScheduleAdapter? = null

    private var isViewEventSent = false

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private var previous: String? = null

    private var next: String? = null

    private var scheduleTodayData: ScheduleTodayData? = null

    var infiniteScrollListener: TwoWayRecyclerOnScrollListener? = null

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentScheduleBinding {
        return FragmentScheduleBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
       return TAG
    }

    override fun provideViewModel(): ScheduleViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        viewModel.extraParams.apply {
            put(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.NAME, TAG)
        }
        adapter = ScheduleAdapter(null)
        mBinding?.recyclerView?.adapter = adapter
        mBinding?.viewGoToToday?.setOnClickListener {
            navigateToToday(true)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(EventConstants.TIME_TABLE_TODAY_CLICK,
                    hashMapOf<String, Any>().apply {
                        putAll(viewModel.extraParams)
                    }, ignoreSnowplow = true)
            )
        }

        mBinding?.textViewMyPlan?.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(EventConstants.MY_PLAN_VIEW_CLICK,
                    hashMapOf<String, Any>().apply {
                        putAll(viewModel.extraParams)
                    })
            )
            startActivity(MyPlanActivity.getStartIntent(requireContext()))
        }

        mBinding ?: return
        infiniteScrollListener =
            object : TwoWayRecyclerOnScrollListener(
                binding.recyclerView.layoutManager) {
                override fun onLoadMore() {
                    if (next != null)
                        viewModel.fetchScheduleData(NEXT, null, next)
                }

                override fun onLoadMorePrev() {
                    if (previous != null)
                        viewModel.fetchScheduleData(PREVIOUS, previous, null)
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = (recyclerView.layoutManager) as LinearLayoutManager
                    val firstItem = layoutManager.findFirstVisibleItemPosition()
                    if (firstItem < 0) {
                        return
                    }
                    val itemAtPosition = adapter?.listings?.getOrNull(firstItem)
                    if (itemAtPosition != null && itemAtPosition is Schedule) {
                        mBinding?.textViewTag?.text = itemAtPosition.tag
                    } else if (itemAtPosition != null && itemAtPosition is ScheduleHeader) {
                        mBinding?.textViewTag?.text = ""
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val firstItem = ((recyclerView.layoutManager) as LinearLayoutManager)
                        .findFirstVisibleItemPosition()
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(EventConstants.TIME_TABLE_SCROLL,
                            hashMapOf<String, Any>().apply {
                                putAll(viewModel.extraParams)
                                put(EventConstants.ITEM_POSITION, firstItem)
                            }, ignoreSnowplow = true
                        )
                    )
                }
            }

        mBinding?.recyclerView?.addOnScrollListener(infiniteScrollListener!!)
        viewModel.fetchScheduleData(FIRST, null, null)
        viewModel.responseData.observeK(
            viewLifecycleOwner,
            this::onResponse,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )
    }

    private fun onResponse(response: Pair<String, ApiScheduleData>) {
        val (requestType, data) = response
        val listData = data.widgets?.mapNotNull { model ->
            when (model) {
                is ScheduleWidgetModel -> {
                    model.data
                }
                is ScheduleHeaderWidgetModel -> {
                    model.data
                }
                is ScheduleNoDataWidgetModel -> {
                    model.data
                }
                else -> {
                    null
                }
            }
        }.orEmpty()

        when (requestType) {
            FIRST -> {
                if (data.noSchedule != null) {
                    mBinding?.layoutTop?.hide()
                    mBinding?.recyclerView?.hide()
                    mBinding?.layoutNoSchedule?.show()
                    mBinding?.imageViewNoSchedule?.loadImageEtx(data.noSchedule.imageUrl.orEmpty())
                    mBinding?.textViewNoScheduleTitle?.text = data.noSchedule.title.orEmpty()
                    mBinding?.textViewNoScheduleSubTitle?.text = data.noSchedule.subTitle.orEmpty()
                    mBinding?.buttonNoSchedule?.text = data.noSchedule.buttonText.orEmpty()
                    mBinding?. buttonNoSchedule?.setOnClickListener {
                        deeplinkAction.performAction(
                            context.forceUnWrap(),
                            data.noSchedule.deeplink
                        )
                    }
                } else {
                    mBinding?.layoutTop?.show()
                    mBinding?.recyclerView?.show()
                    mBinding?.layoutNoSchedule?.hide()
                    mBinding?.rvWidgets?.show()
                }
                val adapter = WidgetLayoutAdapter(requireActivity())
                mBinding?.rvWidgets?.adapter = adapter
                val widgetList = mutableListOf<WidgetEntityModel<*, *>>()
                data.widgets?.forEach {
                    if (it.type != "schedule_header" && it.type != "schedule_no_data"
                        && it.type != "schedule"
                    ) {
                        widgetList.add(it)
                    }
                }
                adapter.setWidgets(widgetList)
                addToBottom(listData)
                previous = data.cursor?.previous
                next = data.cursor?.next
                scheduleTodayData = data.today
                val isListNullOrEmpty = listData.isNullOrEmpty()
                if (scheduleTodayData != null && !isListNullOrEmpty) {
                    mBinding?.viewGoToToday?.show()
                }
                if (isListNullOrEmpty) {
                    infiniteScrollListener?.setLastPageReached(true)
                    infiniteScrollListener?.setPrevLastPageReached(true)
                } else {
                    if (next == null) {
                        infiniteScrollListener?.setLastPageReached(true)
                    }
                    if (previous == null) {
                        infiniteScrollListener?.setPrevLastPageReached(true)
                    }
                }
                navigateToToday(false)
            }
            PREVIOUS -> {
                addToTop(listData)
                previous = data.cursor?.previous
                if (listData.isNullOrEmpty() || previous == null) {
                    infiniteScrollListener?.setPrevLastPageReached(true)
                }
            }
            NEXT -> {
                addToBottom(listData)
                next = data.cursor?.next
                if (listData.isNullOrEmpty() || next == null) {
                    infiniteScrollListener?.setLastPageReached(true)
                }
            }
        }
    }

    private fun navigateToToday(userOpted: Boolean) {
        if (scheduleTodayData == null || adapter?.listings.isNullOrEmpty()) {
            return
        }
        val requiredIndex = adapter?.listings?.indexOfFirst {
            (it is Schedule
                    && it.day == scheduleTodayData?.day
                    && it.tag == scheduleTodayData?.tag
                    && it.date == scheduleTodayData?.date)
        }
        if (requiredIndex != null && requiredIndex != -1) {
            mBinding?.recyclerView?.scrollToPosition(requiredIndex)
        } else if (userOpted) {
            toast("No Classes Today")
        }
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireFragmentManager(), "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        val currentContext = context ?: return
        if (NetworkUtils.isConnected(currentContext)) {
            toast(getString(R.string.somethingWentWrong))
        } else {
            toast(getString(R.string.string_noInternetConnection))
        }
    }

    private fun updateProgress(state: Boolean) {
        infiniteScrollListener?.setDataLoading(state)
        mBinding?.progressBar?.setVisibleState(state)
    }

    private fun addToTop(list: List<Any>) {
        adapter?.listings?.addAll(0, list)
        adapter?.notifyItemRangeInserted(0, list.size)
        infiniteScrollListener?.setDataLoading(false)
    }

    private fun addToBottom(list: List<Any>) {
        val index = adapter?.listings?.size
        if (index != null) {
            adapter?.listings?.addAll(index, list)
            adapter?.notifyItemRangeInserted(index, list.size)
        }
        infiniteScrollListener?.setDataLoading(false)
    }

    override fun onResume() {
        super.onResume()
        UXCam.tagScreenName(TAG)
        handlePageViewEvent()
    }

    @Synchronized
    private fun handlePageViewEvent() {
        if (!isViewEventSent) {
            isViewEventSent = true
            analyticsPublisher.publishEvent(
                AnalyticsEvent(EventConstants.TIME_TABLE_PAGE_VIEW,
                    hashMapOf<String, Any>().apply {
                        putAll(viewModel.extraParams)
                    }, ignoreSnowplow = true)
            )
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        analyticsPublisher.publishEvent(
            AnalyticsEvent(EventConstants.TIME_TABLE_BACK,
                hashMapOf<String, Any>().apply {
                    putAll(viewModel.extraParams)
                }, ignoreSnowplow = true)
        )
    }


}