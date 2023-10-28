package com.doubtnutapp.studygroup.ui.fragment

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.R
import com.doubtnutapp.base.extension.setNavigationResult
import com.doubtnutapp.databinding.FragmentSgChatRequestBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.loadImage
import com.doubtnutapp.studygroup.model.SgChatRequestDialogConfig
import com.doubtnutapp.studygroup.viewmodel.SgChatRequestViewModel
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnut.core.utils.viewModelProvider
import javax.inject.Inject

class SgChatRequestBottomSheetFragment :
    BaseBindingBottomSheetDialogFragment<SgChatRequestViewModel, FragmentSgChatRequestBinding>() {

    companion object {
        const val TAG = "SgChatRequestBottomSheetFragment"
        const val CAN_ACCESS_CHAT = "can_access_chat"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private val args by navArgs<SgChatRequestBottomSheetFragmentArgs>()
    private var sgChatRequestDialogUiConfig: SgChatRequestDialogConfig? = null
    private val chatId by lazy { args.chatId }
    private var canAccessChat = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
        sgChatRequestDialogUiConfig = arguments?.getParcelable<SgChatRequestDialogConfig>("sgChatRequestConfig")
    }

    override fun onDismiss(dialog: DialogInterface) {
        setNavigationResult(canAccessChat, CAN_ACCESS_CHAT)
        super.onDismiss(dialog)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSgChatRequestBinding =
        FragmentSgChatRequestBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): SgChatRequestViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        canAccessChat = sgChatRequestDialogUiConfig?.canAccessChat == true
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog?.setCanceledOnTouchOutside(canAccessChat)
        sgChatRequestDialogUiConfig?.let {
            setupUi(it)
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.acceptRequestLiveData.observe(viewLifecycleOwner, {
            canAccessChat = true
            dismiss()
        })

        viewModel.rejectRequestLiveData.observe(viewLifecycleOwner, {
            canAccessChat = false
            dismiss()
        })
    }

    private fun setupUi(data: SgChatRequestDialogConfig) {

        mBinding ?: return

        with(binding) {
            ivChatImage.loadImage(data.image)
            title.text = data.heading
            subtitle.text = data.description
            btRejectRequest.apply {
                isVisible = data.secondaryCta != null
                text = data.secondaryCta
                setOnClickListener {
                    if (sgChatRequestDialogUiConfig?.secondaryCtaDeeplink != null) {
                        deeplinkAction.performAction(requireContext(), sgChatRequestDialogUiConfig?.secondaryCtaDeeplink)
                        dismiss()
                    } else {
                        viewModel.rejectMessageRequest(chatId)
                        viewModel.sendEvent(EventConstants.SG_REJECT_BLOCK_CHAT_REQUEST)
                    }
                }
            }

            btAcceptRequest.apply {
                isVisible = data.primaryCta != null
                text = data.primaryCta
                setOnClickListener {
                    if (sgChatRequestDialogUiConfig?.primaryCtaDeeplink != null) {
                        deeplinkAction.performAction(requireContext(), sgChatRequestDialogUiConfig?.primaryCtaDeeplink)
                        dismiss()
                    } else {
                        viewModel.acceptMessageRequest(chatId)
                        viewModel.sendEvent(EventConstants.SG_ACCEPT_CHAT_REQUEST, ignoreSnowplow = true)
                    }
                }
            }
        }
    }

}