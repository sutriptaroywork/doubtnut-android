package com.doubtnutapp.studygroup.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.base.extension.setNavigationResult
import com.doubtnutapp.databinding.FragmentInvitationEntryDialogBinding
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.studygroup.viewmodel.StudyGroupViewModel
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import kotlinx.coroutines.delay

class SgJoinGroupDialogFragment :
    BaseBindingDialogFragment<StudyGroupViewModel, FragmentInvitationEntryDialogBinding>() {

    companion object {

        const val TAG = "SgJoinGroupDialogFragment"
        private const val ARG_INVITER = "inviter"
        private const val ARG_GROUP_ID = "group_id"
        private const val DELAY_IN_DISMISS = 3000L

        fun newInstance(inviter: String, groupId: String): SgJoinGroupDialogFragment =
            SgJoinGroupDialogFragment().apply {
                val bundle = Bundle()
                bundle.putString(ARG_INVITER, inviter)
                bundle.putString(ARG_GROUP_ID, groupId)
                arguments = bundle
            }
    }

    private val navController by findNavControllerLazy()
    private val args by navArgs<SgJoinGroupDialogFragmentArgs>()
    private val inviter: String by lazy { args.inviter.orEmpty() }
    private val groupId: String by lazy { args.groupId }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        return object : Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                navController.navigate(SgJoinGroupDialogFragmentDirections.actionOpenSgHomeFragment())
            }
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentInvitationEntryDialogBinding =
        FragmentInvitationEntryDialogBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): StudyGroupViewModel =
        activityViewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        viewModel.acceptStudyGroupInvitation(inviter, groupId)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.acceptInvitationLiveData.observe(viewLifecycleOwner) { studyGroupInvitation ->
            when (studyGroupInvitation.isAlreadyMember) {
                true -> {
                    setNavigationResult(null, "join_info")
                    dismissWithDelay()
                }

                else -> {
                    when (studyGroupInvitation.isMemberJoined) {
                        true -> {
                            viewModel.sendEvent(
                                EventConstants.SG_MEMBER_JOINED,
                                hashMapOf<String, Any>().apply {
                                    put(EventConstants.GROUP_ID, groupId)
                                }
                            )
                            setNavigationResult(studyGroupInvitation.socketMsg, "join_info")
                            dismissWithDelay()
                        }
                        else -> {
                            view.apply {
                                binding.tvAvailableMessage.text = studyGroupInvitation.description
                                binding.buttonAction.apply {
                                    if (studyGroupInvitation.ctaText.isNullOrEmpty().not()) {
                                        show()
                                        text = studyGroupInvitation.ctaText
                                        setOnClickListener {
                                            viewModel.sendEvent(
                                                EventConstants.SG_UNABLE_TO_JOIN_CLICK,
                                                hashMapOf<String, Any>().apply {
                                                    put(
                                                        EventConstants.SG_CTA_TEXT,
                                                        studyGroupInvitation.ctaText.orEmpty()
                                                    )
                                                    put(
                                                        EventConstants.SG_DESCRIPTION,
                                                        studyGroupInvitation.description.orEmpty()
                                                    )
                                                    put(EventConstants.GROUP_ID, groupId)
                                                })
                                            navController.navigate(
                                                SgJoinGroupDialogFragmentDirections.actionOpenSgHomeFragment()
                                            )
                                        }
                                    } else {
                                        hide()
                                    }
                                }
                            }

                            viewModel.sendEvent(
                                EventConstants.SG_UNABLE_TO_JOIN,
                                hashMapOf<String, Any>().apply {
                                    put(
                                        EventConstants.SG_CTA_TEXT,
                                        studyGroupInvitation.ctaText.orEmpty()
                                    )
                                    put(
                                        EventConstants.SG_DESCRIPTION,
                                        studyGroupInvitation.description.orEmpty()
                                    )
                                    put(EventConstants.GROUP_ID, groupId)
                                })
                        }
                    }
                }
            }
        }
    }

    private fun dismissWithDelay() {
        lifecycleScope.launchWhenStarted {
            delay(DELAY_IN_DISMISS)
            dismiss()
        }
    }
}