package com.doubtnutapp.studygroup.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.OpenSgUserReportMessageFragment
import com.doubtnutapp.base.SgBlockMember
import com.doubtnutapp.base.SgDeleteMessage
import com.doubtnutapp.base.SgRemoveReportedContainer
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.base.extension.getNavigationResult
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.databinding.FragmentSgAdminDashboardBinding
import com.doubtnutapp.socket.viewmodel.SocketManagerViewModel
import com.doubtnutapp.studygroup.model.DeleteMessageData
import com.doubtnutapp.studygroup.model.StudyGroupBlockData
import com.doubtnutapp.studygroup.model.StudyGroupDeleteData
import com.doubtnutapp.studygroup.model.StudyGroupRemoveContainerData
import com.doubtnutapp.studygroup.viewmodel.StudyGroupDashboardViewModel
import com.doubtnutapp.studygroup.viewmodel.StudyGroupViewModel
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import org.json.JSONObject
import javax.inject.Inject


class SgAdminDashboardFragment :
    BaseBindingFragment<StudyGroupDashboardViewModel, FragmentSgAdminDashboardBinding>(),
    ActionPerformer {

    companion object {
        private const val TAG = "SgAdminDashboardFragment"
        const val STUDY_GROUP_ADMIN_DASHBOARD = "STUDY_GROUP_ADMIN_DASHBOARD"
        const val START_PAGE = 0
    }

    @Inject
    lateinit var userPreference: UserPreference

    private val navController by findNavControllerLazy()
    private val args by navArgs<SgAdminDashboardFragmentArgs>()

    private lateinit var socketManagerViewModel: SocketManagerViewModel
    private lateinit var studyGroupViewModel: StudyGroupViewModel

    private val infiniteScrollListener: TagsEndlessRecyclerOnScrollListener? by lazy {
        val layoutManager = mBinding?.rvReport?.layoutManager
        layoutManager?.let {
            object : TagsEndlessRecyclerOnScrollListener(layoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    loadNextReportMessages(currentPage)
                }
            }.apply {
                setStartPage(START_PAGE)
            }
        }
    }

    private var nextPageToLoad = START_PAGE

    private val dashboardAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(
            context = requireContext(),
            actionPerformer = this,
            source = STUDY_GROUP_ADMIN_DASHBOARD
        )
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSgAdminDashboardBinding =
        FragmentSgAdminDashboardBinding.inflate(layoutInflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): StudyGroupDashboardViewModel =
        viewModelProvider(viewModelFactory)

    override fun provideActivityViewModel() {
        studyGroupViewModel = activityViewModelProvider(viewModelFactory)
        socketManagerViewModel = activityViewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpUi()
        if (nextPageToLoad == START_PAGE) {
            loadNextReportMessages(nextPageToLoad)
            studyGroupViewModel.sendEvent(
                EventConstants.SG_ADMIN_DASHBOARD_OPEN
            )
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.adminDashboardMessageLiveData.observe(
            viewLifecycleOwner
        ) {
            when (it) {
                is Outcome.Failure -> {
                    apiErrorToast(it.e)
                }
                is Outcome.Progress -> {
                    mBinding?.progressBar?.isVisible = it.loading
                }
                is Outcome.Success -> {
                    val messages = it.data.message.orEmpty()
                    if (messages.isEmpty()) {
                        infiniteScrollListener?.isLastPageReached = true
                    }
                    mBinding?.noMessageLayout?.isVisible = nextPageToLoad == 0 && messages.isEmpty()
                    nextPageToLoad = it.data.page
                    dashboardAdapter.addWidgets(messages)
                }
                is Outcome.ApiError -> {
                    mBinding?.progressBar?.isVisible = false
                    apiErrorToast(it.e)
                }
                is Outcome.BadRequest -> {}
            }
        }

        socketManagerViewModel.connectLiveData.observeEvent(viewLifecycleOwner) {
            val map = hashMapOf<String, Any?>(
                "room_id" to args.groupId,
                "student_displayname" to userPreference.getStudentName(),
                "student_id" to userPreference.getUserStudentId()
            )
            socketManagerViewModel.joinSocket(JSONObject(map).toString())
        }

        socketManagerViewModel.deletedMessagePositionLiveData.observeEvent(
            viewLifecycleOwner
        ) { pair ->
            val adapterPosition = pair.first
            if (pair.second == true) {
                refreshDashboard()
            } else if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition < dashboardAdapter.widgets.size) {
                dashboardAdapter.removeWidgetAt(adapterPosition)
                mBinding?.noMessageLayout?.isVisible = dashboardAdapter.widgets.isEmpty()
            }
        }

        getNavigationResult<Boolean>(SgUserReportedMessageFragment.REFRESH_DASHBOARD)?.observe(
            viewLifecycleOwner
        ) {
            refreshDashboard()
        }
    }

    override fun performAction(action: Any) {
        when (action) {
            is SgBlockMember -> {
                studyGroupViewModel.block(
                    StudyGroupBlockData(
                        roomId = args.groupId,
                        studentId = action.studentId!!,
                        studentName = action.name.orEmpty(),
                        confirmationPopup = action.confirmationPopup,
                        adapterPosition = action.adapterPosition,
                        members = emptyList()
                    )
                )
            }
            is SgDeleteMessage -> {
                socketManagerViewModel.delete(
                    StudyGroupDeleteData(
                        widgetType = action.widgetType,
                        isAdmin = true,
                        deleteType = action.deleteType,
                        roomId = args.groupId,
                        deleteMessageData = DeleteMessageData(
                            messageId = action.messageId,
                            millis = action.millis,
                            senderId = action.senderId
                        ),
                        deleteReportedMessages = null,
                        confirmationPopup = action.confirmationPopup,
                        adapterPosition = action.adapterPosition
                    )
                )
            }
            is SgRemoveReportedContainer -> {
                studyGroupViewModel.remove(
                    StudyGroupRemoveContainerData(
                        roomId = args.groupId,
                        containerId = action.containerId,
                        containerType = action.containerType,
                        adapterPosition = action.adapterPosition
                    )
                )
            }
            is OpenSgUserReportMessageFragment -> {
                val deeplinkUri = Uri.parse(action.deeplink)
                if (navController.graph.hasDeepLink(deeplinkUri)) {
                    mayNavigate {
                        navigate(deeplinkUri)
                    }
                }
            }
            else -> {

            }
        }
    }

    private fun setUpUi() {
        socketManagerViewModel.connectSocket()
        mBinding?.apply {
            rvReport.apply {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                adapter = dashboardAdapter
                infiniteScrollListener?.let { tagsEndlessRecyclerOnScrollListener ->
                    addOnScrollListener(tagsEndlessRecyclerOnScrollListener)
                }
            }

            ivBack.setOnClickListener {
                mayNavigate {
                    popBackStack()
                }
            }
        }
    }

    private fun loadNextReportMessages(currentPage: Int) {
        viewModel.getAdminDashboardMessages(args.groupId, currentPage)
    }

    private fun refreshDashboard() {
        nextPageToLoad = START_PAGE
        dashboardAdapter.clearData()
        loadNextReportMessages(nextPageToLoad)
    }
}