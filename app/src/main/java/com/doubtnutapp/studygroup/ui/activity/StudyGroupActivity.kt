package com.doubtnutapp.studygroup.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.databinding.ActivityStudyGroupBinding
import com.doubtnutapp.socket.*
import com.doubtnutapp.socket.viewmodel.SocketManagerViewModel
import com.doubtnutapp.studygroup.model.*
import com.doubtnutapp.studygroup.ui.fragment.SgActionDialogFragment
import com.doubtnutapp.studygroup.ui.fragment.SgReportDescriptionFragment
import com.doubtnutapp.studygroup.ui.fragment.SgReportReasonsFragment
import com.doubtnutapp.studygroup.ui.fragment.SgUserBannedStatusBottomSheetFragment
import com.doubtnutapp.studygroup.viewmodel.StudyGroupViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnutapp.widgets.StudyGroupJoinedInfoWidget
import io.branch.referral.Defines
import org.json.JSONObject
import javax.inject.Inject

class StudyGroupActivity :
    BaseBindingActivity<SocketManagerViewModel, ActivityStudyGroupBinding>() {

    @Inject
    lateinit var userPreference: UserPreference

    companion object {

        private const val TAG = "StudyGroupActivity"
        private const val INTENT_EXTRA_URI = "uri"
        private const val INITIAL_MESSAGE_INFO = "initialMessageInfo"
        private const val GROUP_ID = "groupId"
        private const val HOME_SCREEN_DEEPLINK_PREFIX = "doubtnutapp://study_group/list"
        private const val DEFAULT_HOME_SCREEN_DEEPLINK =
            "doubtnutapp://study_group/list?tab_position=0"
        private const val PREVIOUS_VERSION_DEEPLINK_PREFIX =
            "doubtnutapp://study_group?is_study_group_exist="
        private const val TAB_POSITION = "tab_position"

        fun getStartIntent(
            context: Context,
            initialMessageInfo: InitialMessageData? = null,
            groupId: String? = null,
        ): Intent =
            Intent(context, StudyGroupActivity::class.java).apply {
                putExtra(INITIAL_MESSAGE_INFO, initialMessageInfo)
                putExtra(GROUP_ID, groupId)
            }

        fun getDeeplinkStartIntent(context: Context, uri: Uri): Intent =
            Intent(context, StudyGroupActivity::class.java).apply {
                putExtra(INTENT_EXTRA_URI, uri)
            }
    }

    private val udid by lazy { Utils.getUDID() }
    private var uniqueLogTag = ""
    private val navController by lazy { findNavController(R.id.nav_host_fragment_study_group) }

    private lateinit var studyGroupViewModel: StudyGroupViewModel

    override fun provideViewBinding(): ActivityStudyGroupBinding =
        ActivityStudyGroupBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): SocketManagerViewModel {
        studyGroupViewModel = viewModelProvider(viewModelFactory)
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        window.statusBarColor = ContextCompat.getColor(this, R.color.tomato)
        navController.setGraph(R.navigation.nav_graph_study_group, intent.extras)
        handleIntent(intent)

        // Check if user is banned from study group, show them non cancellable bottom sheet
        navController.addOnDestinationChangedListener { _, _, _ ->
            val userBannedStatus = studyGroupViewModel.userBannedStatusLiveData.value?.peekContent()
            if (userBannedStatus == null) {
                studyGroupViewModel.checkUserBannedStatus()
            } else if (userBannedStatus.isBanned && studyGroupViewModel.userBannedBottomSheetVisible.not()) {
                showUserBannedStatusBottomSheet(userBannedStatus.bottomSheet)
            }
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        studyGroupViewModel.userBannedStatusLiveData.observeEvent(this) {
            if (it.isBanned && it.bottomSheet != null) {
                showUserBannedStatusBottomSheet(it.bottomSheet)
            } else {
                // If user is not banned, verify user with google auth if not found.
                verifyUserWithGoogle()
            }
        }

        viewModel.socketMessage.observe(this, EventObserver {
            if (it is SocketErrorEventType) {
                onSocketError(it)
            } else {
                onSocketMessage(it)
            }
        })

        viewModel.reportLiveData.observeEvent(this) { studyGroupReportData ->
            showReportReasonDialog(studyGroupReportData)
        }

        viewModel.deleteLiveData.observeEvent(this) { studyGroupReportData ->
            showDeleteConfirmationDialog(studyGroupReportData)
        }

        studyGroupViewModel.blockLiveData.observeEvent(this) { studyGroupBlockData ->
            showBlockConfirmationPopUp(studyGroupBlockData)
        }

        studyGroupViewModel.blockMemberLiveData.observeEvent(this) {
            val socketMessage = it.first.socketMsg
            val studyGroupBlockData = it.second
            if (socketMessage.isNullOrEmpty().not()) {
                addBlockInfoWidget(studyGroupBlockData, socketMessage!!)
            }
            viewModel.deleteMessageAt(studyGroupBlockData.adapterPosition, true)
            studyGroupViewModel.sendBlockEvent(
                eventName = EventConstants.SG_MEMBER_BLOCKED,
                studyGroupBlockData = studyGroupBlockData
            )
        }

        studyGroupViewModel.removeMessageLiveContainerData.observeEvent(this) {
            removeMessage(it)
        }

        studyGroupViewModel.removeReportedContainerLiveContainerData.observeEvent(
            this
        ) { studyGroupRemoveData ->
            viewModel.deleteMessageAt(studyGroupRemoveData.adapterPosition, false)
            studyGroupViewModel.sendEvent(
                eventName = EventConstants.SG_REPORTED_CONTAINER_REMOVE,
                params = hashMapOf<String, Any>().apply {
                    put(EventConstants.SG_CONTAINER_ID, studyGroupRemoveData.containerId)
                    put(EventConstants.SG_CONTAINER_TYPE, studyGroupRemoveData.containerType)
                }
            )
        }
    }

    private fun showUserBannedStatusBottomSheet(bottomSheet: SgUserBannedBottomSheet?) {
        bottomSheet ?: return
        val userBannedStatusBottomSheet =
            SgUserBannedStatusBottomSheetFragment.newInstance(bottomSheet)
        userBannedStatusBottomSheet.setOnDismissListener(object :
            SgUserBannedStatusBottomSheetFragment.OnDismissListener {
            override fun onDismissBottomSheet() {
                studyGroupViewModel.userBannedBottomSheetVisible = false
                onBackPressed()
            }
        })
        userBannedStatusBottomSheet.show(
            supportFragmentManager,
            SgUserBannedStatusBottomSheetFragment.TAG
        )
        studyGroupViewModel.userBannedBottomSheetVisible = true
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            val uri = it.getParcelableExtra<Uri>(INTENT_EXTRA_URI)
            if (uri != null) {
                val deeplinkUri = Uri.parse(getFormattedDeeplink(uri))
                if (navController.graph.hasDeepLink(deeplinkUri)) {
                    navController.navigate(deeplinkUri)
                }
            }
        }
    }

    private fun getFormattedDeeplink(uri: Uri): String =
        when {
            uri.toString().startsWith("doubtnutapp://study_group_chat") -> {
                uri.toString()
                    .replace(
                        "doubtnutapp://study_group_chat",
                        "doubtnutapp://study_group/chat"
                    )
            }
            uri.toString().startsWith(PREVIOUS_VERSION_DEEPLINK_PREFIX) -> {
                DEFAULT_HOME_SCREEN_DEEPLINK
            }
            uri.toString().startsWith("doubtnutapp://study_group_v2") -> {
                uri.toString()
                    .replace(
                        "doubtnutapp://study_group_v2",
                        "doubtnutapp://study_group"
                    )
            }
            else -> uri.toString()
        }

    private fun handleIntent(intent: Intent) {
        val uri = intent.getParcelableExtra<Uri>(INTENT_EXTRA_URI)
        if (uri != null) {
            val deeplinkUri = Uri.parse(getFormattedDeeplink(uri))
            if (navController.graph.hasDeepLink(deeplinkUri)) {
                if (deeplinkUri.toString().startsWith(HOME_SCREEN_DEEPLINK_PREFIX)) {
                    val tabPosition = deeplinkUri.getQueryParameter(TAB_POSITION)
                    if (tabPosition != null) {
                        navController.navigate(
                            deeplinkUri,
                            navOptions {
                                popUpTo(R.id.sgHomeFragment) {
                                    inclusive = true
                                }
                            }
                        )
                    }
                } else {
                    navController.navigate(
                        deeplinkUri,
                        navOptions {
                            popUpTo(R.id.sgHomeFragment) {
                                inclusive = false
                            }
                        }
                    )
                }

            }
        }
    }

    private fun removeMessage(studyGroupRemoveContainerData: StudyGroupRemoveContainerData) {
        studyGroupViewModel.removeReportedContainer(studyGroupRemoveContainerData)
    }

    private fun onSocketMessage(event: SocketEventType) {
        when (event) {
            is OnConnect -> {
                viewModel.onConnect(event)
            }

            is OnJoin -> {
                viewModel.onJoin(event)
            }

            is OnResponseData -> {
                if (event.data is SgReport) {
                    if (event.data.studentId == userPreference.getUserStudentId()) {
                        toast(event.data.message.orEmpty())
                    }
                } else if (event.data is SgDelete) {
                    if (event.data.studentId == userPreference.getUserStudentId()) {
                        toast(event.data.message.orEmpty())
                    }
                } else {
                    viewModel.onResponse(event)
                }
            }
            else -> {
            }
        }
    }

    private fun onSocketError(errorEventType: SocketErrorEventType) {
        when (errorEventType) {
            is OnDisconnect -> {
            }
            is OnConnectError -> {
            }
            is OnConnectTimeout -> {
            }
        }
    }

    private fun showReportReasonDialog(
        studyGroupReportData: StudyGroupReportData,
    ) {
        val sgReportReasonFragment =
            SgReportReasonsFragment.newInstance(studyGroupReportData.reportReasons)
        sgReportReasonFragment.setReportReasonListener(object :
            SgReportReasonsFragment.ReportReasonListener {
            override fun onReport(selectedOption: String?) {
                report(studyGroupReportData, selectedOption.orEmpty())
                sgReportReasonFragment.dismiss()
            }

            override fun onOtherOptionSelected() {
                sgReportReasonFragment.dismiss()
                showReportDescriptionFragment(studyGroupReportData)
            }

            override fun onCancel() {
                sgReportReasonFragment.dismiss()
            }
        })
        sgReportReasonFragment.show(supportFragmentManager, SgReportReasonsFragment.TAG)
        studyGroupViewModel.sendReportEvent(
            eventName = EventConstants.SG_REPORT_POPUP_OPEN,
            studyGroupReportData = studyGroupReportData,
            reason = ""
        )
    }

    private fun showReportDescriptionFragment(
        studyGroupReportData: StudyGroupReportData,
    ) {
        val fragment =
            SgReportDescriptionFragment.newInstance(studyGroupReportData.reportReasons.otherContainer)
        fragment.setReportDescriptionListener(object :
            SgReportDescriptionFragment.ReportDescriptionListener {
            override fun onSubmit(description: String?) {
                report(studyGroupReportData, description.orEmpty())
                fragment.dismiss()
            }

            override fun onCancel() {
                fragment.dismiss()
            }
        })
        fragment.show(supportFragmentManager, SgReportDescriptionFragment.TAG)
        studyGroupViewModel.sendReportEvent(
            eventName = EventConstants.SG_REPORT_OTHER_POPUP_OPEN,
            studyGroupReportData = studyGroupReportData,
            reason = ""
        )
    }

    private fun report(studyGroupReportData: StudyGroupReportData, reason: String) {
        val map = mutableMapOf<String, Any?>()
        map["room_id"] = studyGroupReportData.roomId
        map["room_type"] = Constants.STUDY_GROUP
        map["reason"] = reason
        when (studyGroupReportData.reportType) {
            SocketManagerViewModel.SgReportType.REPORT_GROUP -> {
                studyGroupReportData.reportGroup?.let { reportGroup ->
                    map["admin_id"] = reportGroup.adminId
                }
            }
            SocketManagerViewModel.SgReportType.REPORT_MEMBER -> {
                studyGroupReportData.reportMember?.let { reportMember ->
                    map["reported_student_id"] = reportMember.reportedStudentId
                    map["reported_student_name"] = reportMember.reportedStudentName
                }
            }
            SocketManagerViewModel.SgReportType.REPORT_MESSAGE -> {
                studyGroupReportData.reportMessage?.let { reportMessage ->
                    map["message_id"] = reportMessage.messageId
                    map["sender_id"] = reportMessage.senderId
                    map["millis"] = reportMessage.millis
                }
            }
        }
        viewModel.report(
            toReport = studyGroupReportData.reportType,
            JSONObject(map).toString()
        )

        studyGroupViewModel.sendReportEvent(
            eventName = EventConstants.SG_REPORT_MESSAGE,
            studyGroupReportData = studyGroupReportData,
            reason = reason
        )
    }

    private fun showBlockConfirmationPopUp(studyGroupBlockData: StudyGroupBlockData) {
        supportFragmentManager.findFragmentByTag(SgActionDialogFragment.TAG)?.let {
            return
        }
        val confirmationPopup = studyGroupBlockData.confirmationPopup
        val sgActionDialog = SgActionDialogFragment.newInstance(
            SgActionDialogFragment.SgActionUiConfig(
                title = confirmationPopup?.title ?: String.format(
                    resources.getString(R.string.sg_block_title),
                    studyGroupBlockData.studentName
                ),
                subtitle = confirmationPopup?.subtitle
                    ?: resources.getString(R.string.sg_block_subtitle),
                buttonYesTitle = confirmationPopup?.primaryCta
                    ?: resources.getString(R.string.sg_action_block),
                buttonNoTitle = confirmationPopup?.secondaryCta
                    ?: resources.getString(R.string.sg_action_cancel)
            )
        )
        sgActionDialog.setSgActionListener(object :
            SgActionDialogFragment.SgActionListener {
            override fun onButtonYesClick() {
                sgActionDialog.dismiss()
                when (studyGroupBlockData.actionSource) {
                    ActionSource.PERSONAL_CHAT -> {
                        when (studyGroupBlockData.actionType) {
                            ActionType.BLOCK -> {
                                studyGroupViewModel.blockPersonalChatMember(
                                    studyGroupBlockData.roomId,
                                    studyGroupBlockData.studentId,
                                    studyGroupBlockData.adapterPosition
                                )
                            }
                            ActionType.UNBLOCK -> {
                                studyGroupViewModel.unblockPersonalChatMember(
                                    studyGroupBlockData.roomId,
                                    studyGroupBlockData.studentId,
                                    studyGroupBlockData.adapterPosition
                                )
                            }
                            else -> {}
                        }
                    }
                    else -> {
                        studyGroupViewModel.blockGroupMember(studyGroupBlockData)
                    }
                }
            }

            override fun onButtonNoClick() {
                sgActionDialog.dismiss()
            }
        })
        sgActionDialog.show(supportFragmentManager, SgActionDialogFragment.TAG)
        studyGroupViewModel.sendBlockEvent(
            eventName = EventConstants.SG_BLOCK_POPUP_OPEN,
            studyGroupBlockData = studyGroupBlockData
        )
    }

    private fun addBlockInfoWidget(studyGroupBlockData: StudyGroupBlockData, message: String) {
        val map = hashMapOf<String, Any?>(
            "room_id" to studyGroupBlockData.roomId,
            "student_id" to studyGroupBlockData.studentId
        )

        val infoWidget = StudyGroupJoinedInfoWidget.Model().apply {
            _widgetType = WidgetTypes.TYPE_WIDGET_STUDY_GROUP_JOINED_INFO
            _widgetData = StudyGroupJoinedInfoWidget.Data(message)
        }
        viewModel.sendGroupMessage(
            roomId = studyGroupBlockData.roomId,
            roomType = Constants.STUDY_GROUP,
            members = studyGroupBlockData.members,
            isMessage = false,
            disconnectSocket = false,
            infoWidget
        )
        studyGroupViewModel.insert(infoWidget, studyGroupBlockData.roomId)

        viewModel.blockMember(JSONObject(map).toString(), studyGroupBlockData.roomId)
    }

    private fun showDeleteConfirmationDialog(studyGroupDeleteData: StudyGroupDeleteData) {
        val confirmationPopup = studyGroupDeleteData.confirmationPopup
        val sgActionDialog = SgActionDialogFragment.newInstance(
            SgActionDialogFragment.SgActionUiConfig(
                title = confirmationPopup?.title
                    ?: resources.getString(R.string.sg_delete_popup_title),
                subtitle = confirmationPopup?.subtitle
                    ?: resources.getString(R.string.sg_delete_popup_subtitle),
                buttonYesTitle = confirmationPopup?.primaryCta
                    ?: resources.getString(R.string.string_yes),
                buttonNoTitle = confirmationPopup?.secondaryCta
                    ?: resources.getString(R.string.string_no)
            )
        )
        sgActionDialog.setSgActionListener(object :
            SgActionDialogFragment.SgActionListener {
            override fun onButtonYesClick() {
                sgActionDialog.dismiss()
                delete(studyGroupDeleteData)
            }

            override fun onButtonNoClick() {
                sgActionDialog.dismiss()
            }
        })
        sgActionDialog.show(supportFragmentManager, SgActionDialogFragment.TAG)
        studyGroupViewModel.sendDeleteMessageEvent(
            eventName = EventConstants.SG_DELETE_POPUP_OPEN,
            studyGroupDeleteData = studyGroupDeleteData
        )
    }

    private fun delete(studyGroupDeleteData: StudyGroupDeleteData) {
        val map = mutableMapOf<String, Any?>()
        map["room_id"] = studyGroupDeleteData.roomId
        when (studyGroupDeleteData.deleteType) {
            SocketManagerViewModel.SgDeleteType.DELETE -> {
                studyGroupDeleteData.deleteMessageData?.let { deleteMessageData ->
                    map["message_id"] = deleteMessageData.messageId
                    map["millis"] = deleteMessageData.millis
                    map["sender_id"] = deleteMessageData.senderId
                }
            }
            SocketManagerViewModel.SgDeleteType.DELETE_ALL -> {
                studyGroupDeleteData.deleteReportedMessages?.let { deleteReportedMessages ->
                    map["reported_student_id"] = deleteReportedMessages.reportedStudentId
                }
            }
        }
        viewModel.deleteMessage(
            studyGroupDeleteData.deleteType,
            JSONObject(map).toString()
        )

        viewModel.deleteMessageAt(studyGroupDeleteData.adapterPosition, false)
        studyGroupViewModel.sendDeleteMessageEvent(
            eventName = EventConstants.SG_DELETE_MESSAGE,
            studyGroupDeleteData = studyGroupDeleteData
        )
    }

    override fun startActivity(intent: Intent?) {
        val isBranchLink = intent?.data?.host.equals(Constants.BRANCH_HOST)
        if (isBranchLink) {
            intent?.putExtra(Defines.IntentKeys.ForceNewBranchSession.key, true)
        }
        super.startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        val timeStamp = System.currentTimeMillis()
        uniqueLogTag = "id_" + udid + "_" + System.currentTimeMillis().toString()
        val params = HashMap<String, Any>()
        params[EventConstants.STATE] = EventConstants.SG_SCREEN_ENTERED_FOREGROUND
        params[EventConstants.SCREEN_NAME] = "sg"
        params[EventConstants.TIME_STAMP] = timeStamp
        params[EventConstants.EVENT_NAME_ID] = uniqueLogTag
        params[EventConstants.EVENT_UDID] = udid
        studyGroupViewModel.publishTimeSpentEvent(
            category = EventConstants.SG_SCREEN_STATE,
            action = EventConstants.SG_SCREEN_ENTERED_FOREGROUND_ACTION,
            params = params
        )
    }

    override fun onStop() {
        super.onStop()
        val timeStamp = System.currentTimeMillis()
        val params = HashMap<String, Any>()
        params[EventConstants.STATE] = EventConstants.SG_SCREEN_ENTERED_BACKGROUND
        params[EventConstants.SCREEN_NAME] = "sg"
        params[EventConstants.TIME_STAMP] = timeStamp
        params[EventConstants.EVENT_NAME_ID] = uniqueLogTag
        params[EventConstants.EVENT_UDID] = udid
        studyGroupViewModel.publishTimeSpentEvent(
            category = EventConstants.SG_SCREEN_STATE,
            action = EventConstants.SG_SCREEN_ENTERED_BACKGROUND_ACTION,
            params = params
        )
    }

    enum class ActionType {
        BLOCK,
        UNBLOCK
    }

    enum class ActionSource {
        GROUP_CHAT,
        PERSONAL_CHAT
    }
}