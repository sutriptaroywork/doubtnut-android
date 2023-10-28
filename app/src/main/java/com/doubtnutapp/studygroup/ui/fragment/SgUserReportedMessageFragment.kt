package com.doubtnutapp.studygroup.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.SgBlockMember
import com.doubtnutapp.base.SgDeleteMessage
import com.doubtnutapp.base.SgRemoveReportedContainer
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.base.extension.setNavigationResult
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.databinding.FragmentSgUsersReportedMessagesBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.socket.viewmodel.SocketManagerViewModel
import com.doubtnutapp.studygroup.model.*
import com.doubtnutapp.studygroup.viewmodel.StudyGroupDashboardViewModel
import com.doubtnutapp.studygroup.viewmodel.StudyGroupViewModel
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import org.json.JSONObject
import javax.inject.Inject


class SgUserReportedMessageFragment :
    BaseBindingFragment<StudyGroupDashboardViewModel, FragmentSgUsersReportedMessagesBinding>(),
    ActionPerformer {

    companion object {
        private const val TAG = "SgUserReportedMessageFragment"
        const val STUDY_GROUP_USER_REPORTED_MESSAGE = "STUDY_GROUP_USER_REPORTED_MESSAGE"
        const val REFRESH_DASHBOARD = "refresh_dashboard"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var userPreference: UserPreference

    private lateinit var socketManagerViewModel: SocketManagerViewModel
    private val studyGroupViewModel: StudyGroupViewModel by activityViewModels()
    private val args by navArgs<SgUserReportedMessageFragmentArgs>()

    private val reportAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(
            context = requireContext(),
            actionPerformer = this,
            source = STUDY_GROUP_USER_REPORTED_MESSAGE
        )
    }

    private val infiniteScrollListener: TagsEndlessRecyclerOnScrollListener? by lazy {
        mBinding?.rvReport?.let {
            object : TagsEndlessRecyclerOnScrollListener(it.layoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    loadNextReportMessages(currentPage)
                }
            }.apply {
                setStartPage(0)
            }
        }
    }

    private var page = 0
    private val groupId: String? by lazy { args.groupId }
    private val reportedStudentId: String? by lazy { args.reportedStudentId }
    private val reportedStudentName: String? by lazy { args.reportedStudentName }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSgUsersReportedMessagesBinding =
        FragmentSgUsersReportedMessagesBinding.inflate(layoutInflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): StudyGroupDashboardViewModel =
        viewModelProvider(viewModelFactory)

    override fun provideActivityViewModel() {
        socketManagerViewModel = activityViewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        socketManagerViewModel.connectSocket()
        setUpUi()
        viewModel.getStudentReportedMessages(groupId!!, reportedStudentId!!, page)
        studyGroupViewModel.sendEvent(EventConstants.SG_USER_REPORTED_DASHBOARD_OPEN)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.studentReportedMessageLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Failure -> {
                    apiErrorToast(it.e)
                }
                is Outcome.Progress -> {
                    mBinding?.progressBar?.isVisible = it.loading
                }
                is Outcome.Success -> {
                    updateUi(it.data)
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

        socketManagerViewModel.deletedMessagePositionLiveData.observeEvent(viewLifecycleOwner) { pair ->
            val position = pair.first
            setNavigationResult(true, REFRESH_DASHBOARD)
            when (position) {
                RecyclerView.NO_POSITION -> {
                    mayNavigate {
                        popBackStack()
                    }
                }
                else -> {
                    if (position < reportAdapter.widgets.size) {
                        reportAdapter.removeWidgetAt(position)
                        mBinding?.noMessageLayout?.isVisible = reportAdapter.widgets.isEmpty()
                    }
                }
            }
        }
    }

    private fun updateUi(data: StudyGroupDashboardMessage) {
        val messages = data.message.orEmpty()
        if (messages.isEmpty()) {
            infiniteScrollListener?.isLastPageReached = true
            mBinding?.noMessageLayout?.isVisible = page == 0
        }
        page = data.page
        reportAdapter.addWidgets(messages)
        data.primaryCta?.let { primaryCta ->
            mBinding?.tvBlockUser?.apply {
                text = primaryCta.title
                setOnClickListener {
                    handleCtaClick(primaryCta)
                }
            }
        }
        data.secondaryCta?.let { secondaryCta ->
            mBinding?.tvDeleteMessages?.apply {
                text = secondaryCta.title
                setOnClickListener {
                    handleCtaClick(secondaryCta)
                }
            }
        }
    }

    private fun handleCtaClick(cta: Cta) {
        if (cta.deeplink != null) {
            deeplinkAction.performAction(requireContext(), cta.deeplink)
        } else {
            when (cta.action) {
                StudyGroupActions.BLOCK -> {
                    blockMember(
                        studentId = reportedStudentId!!,
                        name = reportedStudentName.orEmpty(),
                        confirmationPopup = cta.confirmationPopup,
                        adapterPosition = -1
                    )
                }
                StudyGroupActions.DELETE -> {
                    deleteMessage(
                        widgetType = TAG,
                        deleteType = cta.action,
                        messageId = null,
                        confirmationPopup = cta.confirmationPopup,
                        adapterPosition = -1
                    )
                }
                StudyGroupActions.DELETE_ALL -> {
                    deleteMessage(
                        widgetType = TAG,
                        deleteType = cta.action,
                        messageId = null,
                        confirmationPopup = cta.confirmationPopup,
                        adapterPosition = -1
                    )
                }
            }
        }
    }

    override fun performAction(action: Any) {
        when (action) {
            is SgBlockMember -> {
                blockMember(
                    studentId = action.studentId!!,
                    name = action.name.orEmpty(),
                    confirmationPopup = action.confirmationPopup,
                    adapterPosition = action.adapterPosition
                )
            }
            is SgDeleteMessage -> {
                deleteMessage(
                    widgetType = action.widgetType,
                    deleteType = action.deleteType,
                    messageId = action.messageId,
                    confirmationPopup = action.confirmationPopup,
                    adapterPosition = action.adapterPosition
                )
            }
            is SgRemoveReportedContainer -> {
                studyGroupViewModel.remove(
                    StudyGroupRemoveContainerData(
                        roomId = groupId!!,
                        containerId = action.containerId,
                        containerType = action.containerType,
                        adapterPosition = action.adapterPosition
                    )
                )
            }
            else -> {
            }
        }
    }

    private fun blockMember(
        studentId: String,
        name: String,
        confirmationPopup: ConfirmationPopup?,
        adapterPosition: Int,
    ) {
        studyGroupViewModel.block(
            StudyGroupBlockData(
                roomId = args.groupId,
                studentId = studentId,
                studentName = name,
                confirmationPopup = confirmationPopup,
                adapterPosition = adapterPosition,
                members = emptyList(),
            )
        )
    }

    private fun deleteMessage(
        widgetType: String?,
        deleteType: String,
        messageId: String?,
        confirmationPopup: ConfirmationPopup?,
        adapterPosition: Int,
    ) {
        socketManagerViewModel.delete(
            StudyGroupDeleteData(
                widgetType = widgetType,
                isAdmin = true,
                deleteType = deleteType,
                roomId = groupId!!,
                deleteMessageData = DeleteMessageData(
                    messageId = messageId,
                    millis = null,
                    senderId = null
                ),
                deleteReportedMessages = DeleteReportedMessages(
                    roomId = groupId!!,
                    reportedStudentId = reportedStudentId!!
                ),
                confirmationPopup = confirmationPopup,
                adapterPosition = adapterPosition
            )
        )
    }

    private fun setUpUi() {
        mBinding?.apply {
            tvGTitle.text =
                String.format(
                    resources.getString(R.string.sg_user_reported_screen_title),
                    args.reportedStudentName
                )

            rvReport.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            rvReport.adapter = reportAdapter
            rvReport.addOnScrollListener(infiniteScrollListener!!)

            ivBack.setOnClickListener {
                mayNavigate {
                    popBackStack()
                }
            }

            tvDeleteMessages.setOnClickListener {
                socketManagerViewModel.delete(
                    StudyGroupDeleteData(
                        widgetType = TAG,
                        isAdmin = true,
                        deleteType = SocketManagerViewModel.SgDeleteType.DELETE,
                        roomId = groupId!!,
                        deleteMessageData = null,
                        deleteReportedMessages = DeleteReportedMessages(
                            roomId = groupId!!,
                            reportedStudentId = reportedStudentId!!
                        ),
                        confirmationPopup = null,
                        adapterPosition = -1
                    )
                )
            }

            tvBlockUser.setOnClickListener {
                studyGroupViewModel.block(
                    StudyGroupBlockData(
                        roomId = groupId!!,
                        studentId = reportedStudentId!!,
                        studentName = "",
                        confirmationPopup = null,
                        adapterPosition = -1,
                        members = emptyList(),
                    )
                )
            }
        }
    }

    private fun loadNextReportMessages(currentPage: Int) {
        viewModel.getStudentReportedMessages(groupId!!, reportedStudentId!!, currentPage)
    }
}