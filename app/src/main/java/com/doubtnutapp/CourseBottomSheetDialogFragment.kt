package com.doubtnutapp

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.adapter.ViewPagerAdapter
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.events.Dismiss
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.EventBus.PauseVideoPlayer
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.ActivateVipTrial
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.bottomsheetholder.BottomSheetHolderActivity
import com.doubtnutapp.callingnotice.CallingNoticeWidgetModel
import com.doubtnutapp.course.event.TrialActivated
import com.doubtnutapp.course.widgets.*
import com.doubtnutapp.data.remote.models.ActivateTrialData
import com.doubtnutapp.data.remote.models.ApiCourseDataV3
import com.doubtnutapp.databinding.FragmentCourseBottomSheetBinding
import com.doubtnutapp.databinding.FragmentCourseBottomSheetDialogBinding
import com.doubtnutapp.libraryhome.course.ui.ExploreFragment
import com.doubtnutapp.libraryhome.coursev3.ui.CourseActivityV3
import com.doubtnutapp.libraryhome.coursev3.ui.CourseFragment
import com.doubtnutapp.libraryhome.coursev3.viewmodel.CourseViewModelV3
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.showApiErrorToast
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class CourseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    val ids: ArrayList<String> by lazy { requireArguments().getStringArrayList(PARAMS_IDS)!! }
    val position: Int by lazy { requireArguments().getInt(PARAMS_POSITION, 0) }
    val flagrId: String by lazy { requireArguments().getString(KEY_FLAGR_ID).orEmpty() }
    val variantId: String by lazy { requireArguments().getString(KEY_VARIANT_ID).orEmpty() }
    val source: String by lazy { requireArguments().getString(KEY_SOURCE).orEmpty() }
    val deeplinkSource: String by lazy { requireArguments().getString(KEY_DEEPLINK_SOURCE).orEmpty() }

    private lateinit var binding: FragmentCourseBottomSheetDialogBinding

    private var appStateObserver: Disposable? = null

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCourseBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnDismissListener {
            if (activity is BottomSheetHolderActivity) {
                activity?.finish()
            }
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity ?: return

        CourseBottomSheetFragment.isLoadedViaExploreFragment = source == ExploreFragment.TAG

        DoubtnutApp.INSTANCE.bus()?.send(PauseVideoPlayer)

        val adapter = ViewPagerAdapter(childFragmentManager)
        ids.forEach {
            adapter.addFragment(
                CourseBottomSheetFragment.newInstace(
                    id = it,
                    flagrId = flagrId,
                    variantId = variantId,
                    source = source,
                    deeplinkSource = deeplinkSource
                )
            )
        }
        var selectedPagePositon = position

        binding.viewPager.offscreenPageLimit = ids.size

        if (ids.size > 1) {
            binding.viewPager.pageMargin = 4.dpToPx()
        } else {
            binding.viewPager.setPadding(
                0,
                binding.viewPager.paddingTop,
                0,
                binding.viewPager.paddingBottom,
            )
        }

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                selectedPagePositon = position
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })

        binding.viewPager.adapter = adapter
        binding.viewPager.currentItem = position

        val behaviour = BottomSheetBehavior.from(binding.root.parent as View)
        behaviour.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    isExpanded = true
                    behaviour.isDraggable = false
                    behaviour.removeBottomSheetCallback(this)

                    val id = ids.getOrNull(selectedPagePositon) ?: return

                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.MPVP_COURSE_BOTTOMSHEET_EXPANDED,
                            hashMapOf(
                                EventConstants.ASSORTMENT_ID to id,
                                EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            )
                        )
                    )

                    CourseActivityV3.startActivity(
                        context = requireContext(),
                        start = true,
                        assortmentId = id,
                        source = "NA",
                        studentClass = ""
                    )
                    dismiss()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })

        initObservers()
    }

    fun initObservers() {
        appStateObserver = DoubtnutApp.INSTANCE
            .bus()?.toObservable()?.subscribe {
                when (it) {
                    is TrialActivated -> {
                        dismiss()
                    }
                    is Dismiss -> {
                        if (it.tag == TAG) {
                            dismiss()
                        }
                    }
                }
            }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (activity is BottomSheetHolderActivity) {
            activity?.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appStateObserver?.dispose()
    }

    companion object {
        const val TAG = "CourseBottomSheetDialogFragment"

        private const val PARAMS_IDS = "ids"
        private const val PARAMS_POSITION = "position"
        private const val KEY_FLAGR_ID = "flagr_id"
        private const val KEY_VARIANT_ID = "variant_id"
        private const val KEY_SOURCE = "source"
        private const val KEY_DEEPLINK_SOURCE = "deeplink_source"

        fun newInstance(
            ids: ArrayList<String>,
            position: Int,
            flagrId: String?,
            variantId: String?,
            source: String?,
            deeplinkSource: String?,
        ) =
            CourseBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(PARAMS_IDS, ids)
                    putInt(PARAMS_POSITION, position)
                    putString(KEY_FLAGR_ID, flagrId)
                    putString(KEY_VARIANT_ID, variantId)
                    putString(KEY_SOURCE, source)
                    putString(KEY_DEEPLINK_SOURCE, deeplinkSource)
                }
            }
    }
}

class CourseBottomSheetFragment :
    Fragment(R.layout.fragment_course_bottom_sheet),
    ActionPerformer {

    private val id: String by lazy { requireArguments().getString(KEY_ID).orEmpty() }
    private val flagrId: String by lazy { requireArguments().getString(KEY_FLAGR_ID).orEmpty() }
    private val variantId: String by lazy { requireArguments().getString(KEY_VARIANT_ID).orEmpty() }
    private val source: String by lazy { requireArguments().getString(KEY_SOURCE).orEmpty() }
    private val deeplinkSource: String by lazy { requireArguments().getString(KEY_DEEPLINK_SOURCE).orEmpty() }

    private lateinit var viewModel: CourseViewModelV3

    private val binding: FragmentCourseBottomSheetBinding
        by viewBinding(
            FragmentCourseBottomSheetBinding::bind
        )

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        viewModel = viewModelProvider(viewModelFactory)
        super.onAttach(context)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        view ?: return
        val adapter = (binding.rvMain.adapter as? WidgetLayoutAdapter) ?: return

        if (isVisibleToUser && adapter.itemCount == 0) {
            val eventName = if (source == ExploreFragment.TAG) {
                EventConstants.EXPLORE_PAGE_STRIP_PREVIEW_SHOWN
            } else {
                EventConstants.MPVP_COURSE_BOTTOMSHEET_SHOWN
            }
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    eventName,
                    hashMapOf(
                        EventConstants.ASSORTMENT_ID to id,
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                    )
                )
            )

            viewModel.getAllCoursesData(
                assortmentId = id,
                subject = CourseFragment.DEFAULT_SUBJECT,
                studentClass = null,
                page = 1,
                source = deeplinkSource,
                bottomSheet = true
            )
        }

        val index =
            adapter.widgets
                .indexOfFirst { it is ParentAutoplayWidget.ParentAutoplayWidgetModel }
                .takeIf { it != -1 }
                ?: return
        val widget = binding.rvMain.layoutManager
            ?.findViewByPosition(index)
            ?.takeIf { it.isOnScreen } as? ParentAutoplayWidget
            ?: return
        if (isVisibleToUser) {
            widget.startVideo()
        } else {
            widget.stopVideo()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initObservers()

        if (userVisibleHint) {
            val eventName = if (source == ExploreFragment.TAG) {
                EventConstants.EXPLORE_PAGE_STRIP_PREVIEW_SHOWN
            } else {
                EventConstants.MPVP_COURSE_BOTTOMSHEET_SHOWN
            }
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    eventName,
                    hashMapOf(
                        EventConstants.ASSORTMENT_ID to id,
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                    ),
                    ignoreSnowplow = eventName == EventConstants.EXPLORE_PAGE_STRIP_PREVIEW_SHOWN
                )
            )

            viewModel.getAllCoursesData(
                assortmentId = id,
                subject = CourseFragment.DEFAULT_SUBJECT,
                studentClass = null,
                page = 1,
                source = deeplinkSource,
                bottomSheet = true
            )
        }
    }

    fun init() {
        val adapter = (binding.rvMain.adapter as? WidgetLayoutAdapter) ?: return
        adapter.source = TAG
        adapter.actionPerformer = this
    }

    fun initObservers() {
        viewModel.courseLiveData.observeK(
            viewLifecycleOwner,
            this::onWidgetListFetched,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )

        viewModel.activateVipLiveData.observeK(
            viewLifecycleOwner,
            this::onActivateTrialSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )
    }

    private fun onWidgetListFetched(courseData: ApiCourseDataV3) {
        courseData.widgets?.forEach { model ->
            when (model) {
                is ParentAutoplayWidget.ParentAutoplayWidgetModel -> {
                    model.data.clipToPadding = true
                    model.data.flagrId = flagrId
                    model.data.variantId = variantId
                    model.data.items?.forEach { item ->
                        if (item is AutoPlayChildWidget.AutoplayChildWidgetModel) {
                            item.data.clipToPadding = true
                            item.data.flagrId = flagrId
                            item.data.variantId = variantId
                        } else if (item is CourseAutoPlayChildWidget.CourseAutoPlayChildWidgetModel) {
                            item.data.clipToPadding = true
                            item.data.flagrId = flagrId
                            item.data.variantId = variantId
                        }
                    }
                }
                is CourseInfoWidgetV2Model -> {
                    model.data.flagrId = flagrId
                    model.data.variantId = variantId
                }
                is ButtonBorderWidgetModel -> {
                    model.data.flagrId = flagrId
                    model.data.variantId = variantId
                }
                is CallingNoticeWidgetModel -> {
                    model.data.flagrId = flagrId
                    model.data.variantId = variantId
                }
            }
        }
        binding.rvMain.setWidgets(courseData.widgets)
    }

    private fun onActivateTrialSuccess(data: ActivateTrialData) {
        DoubtnutApp.INSTANCE.bus()?.send(TrialActivated())
        showToast(requireContext(), data.message.orEmpty())
        CourseActivityV3.startActivity(
            context = requireContext(),
            start = true,
            assortmentId = id,
            source = "NA",
            studentClass = ""
        )
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        showApiErrorToast(requireContext())
    }

    private fun unAuthorizeUserError() {
        showApiErrorToast(requireContext())
    }

    private fun updateProgress(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }

    companion object {
        const val TAG = "CourseBottomSheetFragment"

        var isLoadedViaExploreFragment = false

        private const val KEY_ID = "id"
        private const val KEY_FLAGR_ID = "flagr_id"
        private const val KEY_VARIANT_ID = "variant_id"
        private const val KEY_SOURCE = "source"
        private const val KEY_DEEPLINK_SOURCE = "deeplink_source"

        fun newInstace(
            id: String,
            flagrId: String?,
            variantId: String?,
            source: String?,
            deeplinkSource: String?,
        ) = CourseBottomSheetFragment().apply {
            arguments = Bundle().apply {
                putString(KEY_ID, id)
                putString(KEY_FLAGR_ID, flagrId)
                putString(KEY_VARIANT_ID, variantId)
                putString(KEY_SOURCE, source)
                putString(KEY_DEEPLINK_SOURCE, deeplinkSource)
            }
        }
    }

    override fun performAction(action: Any) {
        if (action is ActivateVipTrial) {
            viewModel.activateTrial(action.assortmentId)
        }
    }
}
