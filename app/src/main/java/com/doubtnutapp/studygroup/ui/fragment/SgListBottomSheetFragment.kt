package com.doubtnutapp.studygroup.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider

import com.doubtnutapp.base.StudyGroupClick
import com.doubtnutapp.databinding.FragmentSgListBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.loadImage
import com.doubtnutapp.show
import com.doubtnutapp.studygroup.model.SgChatRequestDialogConfig
import com.doubtnutapp.studygroup.model.StudyGroupList
import com.doubtnutapp.studygroup.ui.adapter.StudyGroupListAdapter
import com.doubtnutapp.studygroup.viewmodel.StudyGroupListViewModel
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.utils.Utils
import javax.inject.Inject


class SgListBottomSheetFragment :
    BaseBindingBottomSheetDialogFragment<StudyGroupListViewModel, FragmentSgListBinding>(),
    ActionPerformer {

    companion object {

        const val TAG = "SgListBottomSheetFragment"

        private const val ARG_INVITEE = "invitee"

        fun newInstance(invitee: String) = SgListBottomSheetFragment().apply {
            val bundle = Bundle()
            bundle.putString(ARG_INVITEE, invitee)
            arguments = bundle
        }
    }

    private val invitee: String? by lazy { arguments?.getString(ARG_INVITEE) }
    private var clickGroupPosition: Int? = null

    private val udid by lazy { Utils.getUDID() }
    private var uniqueLogTag = ""

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private val studyGroupListAdapter: StudyGroupListAdapter by lazy {
        StudyGroupListAdapter(
            this,
            toInvite = true
        )
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSgListBinding =
        FragmentSgListBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): StudyGroupListViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpListeners()
        viewModel.getInvitationStatus(invitee!!)
    }

    override fun onStart() {
        super.onStart()
        val timeStamp = System.currentTimeMillis()
        val params = HashMap<String, Any>()
        uniqueLogTag = "id_" + udid + "_" + System.currentTimeMillis().toString()
        params[EventConstants.STATE] = EventConstants.SG_SCREEN_ENTERED_FOREGROUND
        params[EventConstants.SCREEN_NAME] = "sg_invite"
        params[EventConstants.TIME_STAMP] = timeStamp
        params[EventConstants.EVENT_NAME_ID] = uniqueLogTag
        params[EventConstants.EVENT_UDID] = udid
        viewModel.publishTimeSpentEvent(
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
        params[EventConstants.SCREEN_NAME] = "sg_invite"
        params[EventConstants.TIME_STAMP] = timeStamp
        params[EventConstants.EVENT_NAME_ID] = uniqueLogTag
        params[EventConstants.EVENT_UDID] = udid
        viewModel.publishTimeSpentEvent(
            category = EventConstants.SG_SCREEN_STATE,
            action = EventConstants.SG_SCREEN_ENTERED_BACKGROUND_ACTION,
            params = params
        )
    }

    private fun setUpListeners() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }
    }

    override fun setupObservers() {
        viewModel.groupListLiveData.observe(viewLifecycleOwner, {
            setUpGroupList(it)
        })

        viewModel.invitedLiveData.observe(viewLifecycleOwner, {
            if (it.isInvited) {
                val groupList = viewModel.groupListLiveData.value?.groups ?: return@observe
                if (clickGroupPosition != null &&
                    clickGroupPosition != RecyclerView.NO_POSITION &&
                    clickGroupPosition!! < groupList.size
                ) {
                    val clickedGroup = groupList[clickGroupPosition!!]
                    clickedGroup.invitationStatus = 1
                    studyGroupListAdapter.updateGroupAtPosition(clickedGroup, clickGroupPosition!!)
                }
                viewModel.sendEvent(EventConstants.SG_INVITE_SENT, hashMapOf<String, Any>().apply {
                    put(EventConstants.SOURCE, "profile")
                })
            } else {
                toast(it.message.orEmpty())
            }
        })
    }

    private fun setUpGroupList(studyGroupList: StudyGroupList) {
        if (studyGroupList.groups.isEmpty()) {
            studyGroupList.noGroupContainer?.let {
                showNoGroupContainer(it)
            }
        } else {
            mBinding?.apply {
                val linearLayoutManager = LinearLayoutManager(requireContext())
                val dividerItemDecoration =
                    DividerItemDecoration(requireContext(), linearLayoutManager.orientation)
                rvGroupList.addItemDecoration(dividerItemDecoration)
                rvGroupList.layoutManager = linearLayoutManager
                rvGroupList.adapter = studyGroupListAdapter
                studyGroupListAdapter.updateGroupList(studyGroupList.groups)
            }
        }
    }

    private fun showNoGroupContainer(data: SgChatRequestDialogConfig) {
        mBinding?.apply {
            noListContainer.show()
            ivChatImage.loadImage(data.image)
            title.text = data.heading
            subtitle.text = data.description
            btAcceptRequest.apply {
                isVisible = data.primaryCta != null
                text = data.primaryCta
                setOnClickListener {
                    if (data.primaryCtaDeeplink != null) {
                        deeplinkAction.performAction(requireContext(), data.primaryCtaDeeplink)
                        dismiss()
                    }
                }
            }
        }
    }

    override fun performAction(action: Any) {
        when (action) {
            is StudyGroupClick -> {
                clickGroupPosition = action.position
                viewModel.inviteToStudyGroup(action.data.groupId, invitee!!)
            }
            else -> {
            }
        }
    }

    interface StudyGroupInvitationListener {
        fun onSuccess(ctaText: String)
    }
}