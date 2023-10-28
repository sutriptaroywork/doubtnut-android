package com.doubtnutapp.studygroup.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.core.utils.toast
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.base.SgBlockMember
import com.doubtnutapp.base.SgMemberLongPress
import com.doubtnutapp.base.SgReportMember
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.base.extension.setNavigationResult
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.databinding.FragmentSgInfoBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.loadImage
import com.doubtnutapp.socket.viewmodel.SocketManagerViewModel
import com.doubtnutapp.studygroup.model.*
import com.doubtnutapp.studygroup.ui.activity.StudyGroupActivity
import com.doubtnutapp.studygroup.ui.activity.UpdateSgImageActivity
import com.doubtnutapp.studygroup.ui.activity.UpdateSgNameActivity
import com.doubtnutapp.studygroup.ui.adapter.StudyGroupMemberListAdapter
import com.doubtnutapp.studygroup.viewmodel.StudyGroupViewModel
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnutapp.widgets.StudyGroupJoinedInfoWidget
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.createBalloon
import dagger.Lazy
import org.json.JSONObject
import javax.inject.Inject


class SgInfoFragment : BaseBindingFragment<StudyGroupViewModel,
        FragmentSgInfoBinding>(), ActionPerformer {

    companion object {
        private const val TAG = "SgInfoFragment"
        private const val START_PAGE = 0
    }

    @Inject
    lateinit var deeplinkAction: Lazy<DeeplinkAction>

    @Inject
    lateinit var userPreference: Lazy<UserPreference>

    private lateinit var socketManagerViewModel: SocketManagerViewModel

    private val args by navArgs<SgInfoFragmentArgs>()
    private val groupId: String by lazy { args.groupId }

    private var isLoggedInUserAdmin: Boolean = false
    private var userStatus: Int = 0
    private var isLoggedInUserSubAdmin: Boolean = false
    private var groupName: String? = null
    private var canPerformGroupAction: Boolean = false
    private var toggleMessage: ToggleMessage? = null
    private var groupImage: String? = null
    private var remainingSubAdminCount: Int = 0
    private var groupType: Int? = null

    private val memberIds = mutableListOf<String>()

    private var nextPageToLoadMembers = START_PAGE

    private val studyGroupMemberListAdapter: StudyGroupMemberListAdapter by lazy {
        StudyGroupMemberListAdapter(
            this, userStatus
        )
    }

    private val infiniteScrollListener: TagsEndlessRecyclerOnScrollListener? by lazy {
        val layoutManager = mBinding?.rvMembers?.layoutManager
        layoutManager?.let { manager ->
            object : TagsEndlessRecyclerOnScrollListener(manager) {
                override fun onLoadMore(currentPage: Int) {
                    // Load more users
                    loadGroupMembers()
                }
            }
        }
    }

    private val updateSgNameResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent = result.data ?: return@registerForActivityResult
                val groupName =
                    data.getStringExtra(UpdateSgNameActivity.PARAM_KEY_GROUP_NAME)
                this.groupName = groupName
                mBinding?.tvGroupName?.text = groupName
                setNavigationResult(groupName, "group_name")
            }
        }

    private val updateSgImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent = result.data ?: return@registerForActivityResult
                val groupImage =
                    data.getStringExtra(UpdateSgImageActivity.PARAM_KEY_GROUP_IMAGE)
                this.groupImage = groupImage
                mBinding?.ivGroupImage?.loadImage(groupImage, R.drawable.ic_default_group_chat)
                setNavigationResult(groupImage, "group_image")
            }
        }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentSgInfoBinding =
        FragmentSgInfoBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): StudyGroupViewModel {
        socketManagerViewModel = activityViewModelProvider(viewModelFactory)
        return activityViewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        socketManagerViewModel.connectSocket()
        viewModel.getGroupInfo(groupId)
        mBinding?.ivBack?.setOnClickListener {
            mayNavigate {
                popBackStack()
            }
        }
        setUpClickListeners()
    }

    private fun setUpMemberRecyclerView() {
        mBinding?.rvMembers?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = studyGroupMemberListAdapter
            infiniteScrollListener?.setStartPage(START_PAGE)
            infiniteScrollListener?.let { tagsEndlessRecyclerOnScrollListener ->
                addOnScrollListener(tagsEndlessRecyclerOnScrollListener)
            }
        }
    }

    private fun loadGroupMembers() {
        viewModel.getGroupMembers(roomId = groupId, page = nextPageToLoadMembers)
    }

    @SuppressLint("RestrictedApi")
    override fun performAction(action: Any) {
        when (action) {
            is SgBlockMember -> {
                if (canPerformGroupAction.not()) {
                    toast(R.string.sg_block_error_without_join)
                    return
                }
                viewModel.block(
                    StudyGroupBlockData(
                        roomId = groupId,
                        studentId = action.studentId!!,
                        action.name.orEmpty(),
                        confirmationPopup = action.confirmationPopup,
                        adapterPosition = action.adapterPosition,
                        members = memberIds,
                    )
                )
            }

            is SgReportMember -> {
                val reportReasons = viewModel.reasonsToReportData ?: return
                if (canPerformGroupAction.not()) {
                    toast(R.string.sg_report_error_without_join)
                    return
                }
                val studyGroupReportData = StudyGroupReportData(
                    roomId = groupId,
                    reportType = SocketManagerViewModel.SgReportType.REPORT_MEMBER,
                    reportMessage = null,
                    reportMember = ReportMember(
                        reportedStudentId = action.reportedStudentId,
                        reportedStudentName = action.reportedStudentName
                    ),
                    reportGroup = null,
                    reportReasons = reportReasons,
                    isAdmin = isLoggedInUserAdmin
                )
                socketManagerViewModel.report(studyGroupReportData)
            }

            is SgMemberLongPress -> {
                PopupMenu(
                    ContextThemeWrapper(context, R.style.PopupMenuStyle),
                    action.view,
                    Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
                ).also { popupMenu ->
                    popupMenu.menuInflater.inflate(R.menu.menu_study_group_member, popupMenu.menu)
                    popupMenu.menu.findItem(R.id.itemMakeSubAdmin).isVisible =
                        isLoggedInUserAdmin && groupType == SgType.PUBLIC.type && action.adminStatus == StudyGroupMemberType.Member.type && remainingSubAdminCount > 0
                    popupMenu.menu.findItem(R.id.itemRemoveSubAdmin).isVisible =
                        isLoggedInUserAdmin && groupType == SgType.PUBLIC.type && action.adminStatus == StudyGroupMemberType.SubAdmin.type
                    val menuHelper = context?.let {
                        MenuPopupHelper(
                            it,
                            popupMenu.menu as MenuBuilder,
                            action.view
                        )
                    }
                    menuHelper?.gravity = Gravity.TOP
                    menuHelper?.setForceShowIcon(true)

                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.itemViewProfile -> {
                                FragmentWrapperActivity.userProfile(
                                    binding.root.context,
                                    action.studentId, "study_group"
                                )
                            }
                            R.id.itemMakeSubAdmin -> {
                                studyGroupMemberListAdapter.updateItemAtPosition(
                                    action.viewPosition,
                                    2
                                )
                                viewModel.makeSubAdmin(action.studentId, groupId)
                                remainingSubAdminCount -= 1
                            }
                            R.id.itemRemoveSubAdmin -> {
                                viewModel.removeSubAdmin(action.studentId, groupId)
                                remainingSubAdminCount += 1
                            }
                        }
                        true
                    }
                    menuHelper?.show()
                }
            }

            else -> {
            }
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.groupInfoLiveData.observeEvent(viewLifecycleOwner) { studyGroupInfo ->
            onGroupInfoSuccess(studyGroupInfo)
        }

        socketManagerViewModel.connectLiveData.observeEvent(viewLifecycleOwner) {
            val map = hashMapOf<String, Any?>(
                "room_id" to groupId,
                "student_displayname" to userPreference.get().getStudentName(),
                "student_id" to userPreference.get().getUserStudentId()
            )
            socketManagerViewModel.joinSocket(JSONObject(map).toString())
        }

        socketManagerViewModel.deletedMessagePositionLiveData.observeEvent(
            viewLifecycleOwner
        ) { pair ->
            val adapterPosition = pair.first
            if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition < studyGroupMemberListAdapter.memberList.size) {
                studyGroupMemberListAdapter.removeAt(adapterPosition)
            }
        }

        viewModel.messageLiveData.observeEvent(viewLifecycleOwner) { message ->
            toast(message)
        }

        viewModel.makeSubAdminLiveData.observeEvent(viewLifecycleOwner) {
            toast(it.message.orEmpty())
            viewModel.getGroupInfo(groupId)
        }

        viewModel.removeSubAdminLiveData.observeEvent(viewLifecycleOwner) {
            toast(it.message.orEmpty())
            nextPageToLoadMembers = START_PAGE
            viewModel.getGroupMembers(groupId, nextPageToLoadMembers)
        }

        viewModel.groupMembersLiveData.observeEvent(viewLifecycleOwner) {
            infiniteScrollListener?.isLastPageReached = it.members.isEmpty()
            infiniteScrollListener?.setDataLoading(false)
            if (nextPageToLoadMembers == START_PAGE) {
                studyGroupMemberListAdapter.updateGroupMembers(it.members)
            } else {
                studyGroupMemberListAdapter.addGroupMembers(it.members)
            }
            nextPageToLoadMembers = it.page
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onGroupInfoSuccess(studyGroupInfo: StudyGroupInfo) {
        mBinding?.apply {
            remainingSubAdminCount = studyGroupInfo.remainingSubAdminCount
            toggleMessage = studyGroupInfo.onlySubAdminCanPostContainer?.ToggleMessage

            val adminId = studyGroupInfo.adminId
            isLoggedInUserAdmin = adminId == userPreference.get().getUserStudentId()
            memberIds.addAll(studyGroupInfo.memberIds ?: emptyList())
            userStatus = studyGroupInfo.memberStatus ?: 0

            val groupInfo = studyGroupInfo.groupData?.groupInfo

            groupInfo?.let { info ->

                groupName = info.groupName
                groupType = info.groupType
                groupImage = info.groupImage

                ivGroupImage.loadImage(info.groupImage)
                tvGroupName.text = info.groupName

                // set up member list recycler view as it needs user status
                setUpMemberRecyclerView()
                viewModel.getGroupMembers(roomId = groupId, page = nextPageToLoadMembers)

                when (userStatus) {
                    StudyGroupMemberType.Admin.type -> {
                        isLoggedInUserAdmin = true
                    }
                    StudyGroupMemberType.SubAdmin.type -> {
                        isLoggedInUserSubAdmin = true
                    }
                }
                groupType = info.groupType
                canPerformGroupAction = when (groupType) {
                    SgType.PUBLIC.type -> {
                        studyGroupInfo.isMember == true
                    }
                    else -> {
                        studyGroupInfo.isGroupEnabled == true && studyGroupInfo.isMember == true
                    }
                }
                ivGroupImage.loadImage(info.groupImage)
                notificationGroup.isVisible = canPerformGroupAction
                switchMessagePostAccess.setOnCheckedChangeListener(null)
                setMessagePostAccess(
                    studyGroupInfo.onlySubAdminCanPostContainer,
                    groupInfo.onlySubAdminCanPost
                )
                groupImage = info.groupImage
                tvGroupName.text = info.groupName
                groupName = info.groupName

                ivEdit.isVisible = studyGroupInfo.canEditGroupInfo
            }

            tvMembers.text = studyGroupInfo.memberTitle

            setGuidelineText(
                groupGuideline = studyGroupInfo.groupGuideline,
                knowMoreText = studyGroupInfo.knowMoreText,
                knowMoreDeeplink = studyGroupInfo.knowMoreDeeplink
            )

            switchNotification.isChecked = (studyGroupInfo.isMute == true).not()
            setNotificationText(studyGroupInfo.isMute == false)
            switchNotification.setOnCheckedChangeListener { _, isChecked ->
                viewModel.muteNotification(
                    groupId,
                    if (isChecked) 1 else 0,
                    action = StudyGroupActivity.ActionSource.GROUP_CHAT
                )
                setNotificationText(isChecked)
                viewModel.sendEvent(EventConstants.SG_NOTIFICATION, hashMapOf<String, Any>().apply {
                    put(EventConstants.IS_MUTE, isChecked.not())
                    put(EventConstants.SOURCE, TAG)
                })
            }
        }
    }

    private fun setUpClickListeners() {
        mBinding?.apply {
            ivEdit.setOnClickListener {
                openScreenToUpdateSgName()
            }
            ivGroupImage.setOnClickListener {
                openScreenToUpdateSgImage()
            }
        }
    }

    private fun getTooltip(context: Context, text: String): Balloon {
        return createBalloon(context) {
            setArrowSize(10)
            setWidth(BalloonSizeSpec.WRAP)
            setHeight(BalloonSizeSpec.WRAP)
            setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            setMarginLeft(22)
            setMarginRight(61)
            setPadding(10)
            setCornerRadius(4f)
            setText(text)
            setTextSize(12F)
            setTextColorResource(R.color.black)
            setElevation(4)
            setBackgroundColorResource(R.color.white)
            setLifecycleOwner(lifecycleOwner)
            setDismissWhenTouchOutside(true)
        }
    }

    private fun setMessagePostAccess(
        data: MessagePostAccessContainer?,
        onlySubAdminCanPost: Boolean? = false
    ) {
        mBinding?.apply {
            messagePostAccessGroup.isVisible = data != null
            tvMessagePostAccess.text = data?.title
            switchMessagePostAccess.isChecked = data?.toggle == true
            ivMessagePostAccessInfo.setOnClickListener {
                context?.let {
                    getTooltip(it, data?.tooltipMessage.orEmpty())
                }?.showAlignBottom(it, 0, -10)
            }
            switchMessagePostAccess.isChecked = onlySubAdminCanPost == true
            switchMessagePostAccess.setOnCheckedChangeListener { _, isChecked ->
                val map = hashMapOf<String, Any?>(
                    "room_id" to groupId,
                    "only_sub_admin_can_post" to isChecked
                )
                socketManagerViewModel.updateMessageRestriction(JSONObject(map).toString())

                toggleMessage?.let {
                    val messageToSend = if (isChecked) it.trueMessage else it.falseMessage
                    val infoWidget = StudyGroupJoinedInfoWidget.Model().apply {
                        _widgetType = WidgetTypes.TYPE_WIDGET_STUDY_GROUP_JOINED_INFO
                        _widgetData = StudyGroupJoinedInfoWidget.Data(messageToSend)
                    }
                    socketManagerViewModel.sendGroupMessage(
                        roomId = groupId,
                        roomType = Constants.STUDY_GROUP,
                        members = memberIds,
                        isMessage = true,
                        disconnectSocket = false,
                        infoWidget
                    )
                    viewModel.addOnlySubAdminCanPostData(infoWidget)
                }
            }
        }
    }

    private fun openScreenToUpdateSgImage() {
        val intent = UpdateSgImageActivity.getStartIntent(
            context = requireContext(),
            groupId = groupId,
            groupImage = groupImage,
            isAdmin = isLoggedInUserAdmin || isLoggedInUserSubAdmin
        )
        updateSgImageResultLauncher.launch(intent)
        viewModel.sendEvent(EventConstants.SG_INFO_GROUP_IMAGE_CLICK)
    }

    private fun openScreenToUpdateSgName() {
        val intent = UpdateSgNameActivity.getStartIntent(
            context = requireContext(),
            groupId = groupId,
            groupName = groupName
        )
        updateSgNameResultLauncher.launch(intent)
        viewModel.sendEvent(EventConstants.SG_INFO_EDIT_CLICK)
    }

    private fun setGuidelineText(
        groupGuideline: String?,
        knowMoreText: String?,
        knowMoreDeeplink: String?,
    ) {

        if (groupGuideline.isNullOrEmpty() || knowMoreText.isNullOrEmpty()) return

        val finalText = groupGuideline.orEmpty() + knowMoreText.orEmpty()
        val spannable = SpannableStringBuilder(finalText)

        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.parseColor("#e95630")
                ds.isUnderlineText = false
            }

            override fun onClick(widget: View) {
                viewModel.sendEvent(EventConstants.SG_KNOW_MORE_CLICKED)
                deeplinkAction.get().performAction(requireContext(), knowMoreDeeplink)
            }
        }

        spannable.setSpan(
            clickableSpan,
            groupGuideline.length,
            finalText.length,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )

        mBinding?.tvGroupGuideline?.movementMethod = LinkMovementMethod.getInstance()
        mBinding?.tvGroupGuideline?.text = spannable
    }

    private fun setNotificationText(status: Boolean) {
        val text =
            if (status)
                resources.getString(R.string.sg_notification_on)
            else
                resources.getString(R.string.sg_notification_off)
        mBinding?.tvNotificationText?.text =
            String.format(resources.getString(R.string.sg_notification_with_bracket_text), text)
    }
}