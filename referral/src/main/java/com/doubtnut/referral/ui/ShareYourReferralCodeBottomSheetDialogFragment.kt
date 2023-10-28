package com.doubtnut.referral.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.doubtnut.core.actions.ItemClick
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.ui.base.CoreBindingBottomSheetDialogFragment
import com.doubtnut.core.utils.*
import com.doubtnut.referral.R
import com.doubtnut.referral.data.entity.ButtonData
import com.doubtnut.referral.databinding.DialogShareYourReferralCodeBottomSheetBinding
import com.doubtnut.referral.ui.adapter.ReferContactAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.net.URLEncoder
import javax.inject.Inject

class ShareYourReferralCodeBottomSheetDialogFragment :
    CoreBindingBottomSheetDialogFragment<ShareYourReferralCodeBottomSheetDialogFragmentVM, DialogShareYourReferralCodeBottomSheetBinding>(),
    ActionPerformer {

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    private val buttonData: ButtonData by lazy {
        requireArguments().getParcelable(KEY_BUTTON_DATA)!!
    }

    private val adapter by lazy { ReferContactAdapter(this) }
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null

    companion object {
        const val TAG = "ShareYourReferralCodeBottomSheetDialogFragment"
        const val EVENT_TAG = "share_your_referral_code_bottom_sheet_dialog_fragment"

        private const val KEY_BUTTON_DATA = "button_data"

        /**
         * Used for event tracking, to identify whether this bottom sheet called when permission was granted or permission was already available.
         */
        var isCalledAfterGrantPermission = false

        fun newInstance(buttonData: ButtonData) =
            ShareYourReferralCodeBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_BUTTON_DATA, buttonData)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.TransparentBottomSheetStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#94000000")))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCanceledOnTouchOutside(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogShareYourReferralCodeBottomSheetBinding {
        return DialogShareYourReferralCodeBottomSheetBinding
            .inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): ShareYourReferralCodeBottomSheetDialogFragmentVM {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        init()
        initListeners()
    }

    private fun init() {
        viewModel.uploadAndFetchLatestMobileNumbers(
            buttonData.shareContactBatchSize ?: Int.MAX_VALUE
        )
        bottomSheetBehavior = BottomSheetBehavior.from(binding.root.parent as View)

        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.apply {
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        binding.tvShareMessage.text = buttonData.shareMessage
        binding.rvMain.adapter = adapter

        viewModel.contacts.observe(viewLifecycleOwner) { contacts ->

            val filteredContacts = contacts.orEmpty()

            if (contacts == null) {
                binding.tvNoData.isVisible = true
            } else {
                binding.tvNoData.isVisible = filteredContacts.isEmpty()
            }
            adapter.submitList(filteredContacts)
            binding.rvMain.isVisible = filteredContacts.isNotEmpty()

            if (filteredContacts.isNotEmpty()) {
                val contactCount = filteredContacts.size
                val contactCountSegment = if (contactCount < 500) {
                    "0-500"
                } else if (contactCount < 1000) {
                    "500-1000"
                } else if (contactCount < 2500) {
                    "1000-2500"
                } else if (contactCount < 5000) {
                    "2500-5000"
                } else if (contactCount < 7500) {
                    "5000-7500"
                } else if (contactCount < 10000) {
                    "7500-10000"
                } else {
                    "10000+"
                }
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        CoreEventConstants.USER_CONTACTS_COUNT,
                        hashMapOf(
                            CoreEventConstants.COUNT to contactCountSegment,
                            CoreEventConstants.COUNT_ORIGINAL to contactCount
                        )
                    )
                )
            }
        }

        viewModel.progress.observe(viewLifecycleOwner) {
            binding.progressBar.setVisibleState2(it)
        }

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                "${EVENT_TAG}_${CoreEventConstants.VIEWED}",
                hashMapOf(
                    CoreEventConstants.IS_CALLED_AFTER_GRANT_PERMISSION to isCalledAfterGrantPermission,
                    CoreEventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                )
            )
        )
    }

    private fun initListeners() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.clCopyReferralMessage.setOnClickListener {
            requireContext().copy(
                text = buttonData.shareMessage,
                label = "dn-referral-code",
                toastMessage = getString(R.string.referral_code_copied)
            )
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${CoreEventConstants.CTA_CLICKED}",
                    hashMapOf(
                        CoreEventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                        CoreEventConstants.CTA_TEXT to binding.tvCopy.text
                    )
                )
            )
        }


        binding.etSearch.addTextChangedListener(object : TextWatcher {

            fun openBottomSheetIfCollapsed() {
                bottomSheetBehavior?.let {
                    if (it.state == BottomSheetBehavior.STATE_COLLAPSED) {
                        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                openBottomSheetIfCollapsed()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                openBottomSheetIfCollapsed()
            }

            override fun afterTextChanged(query: Editable?) {

                val contacts = viewModel.contacts.value
                    ?: return

                if (query.isNullOrEmpty()) {
                    adapter.submitList(contacts)
                    binding.rvMain.smoothScrollToPosition(0)
                    return
                }

                val filteredContacts =
                    contacts.filter {
                        it.type.equals("title").not() && it.name?.contains(
                            query.toString(),
                            true
                        ) == true
                    }

                adapter.submitList(filteredContacts)
                binding.rvMain.smoothScrollToPosition(0)

                binding.tvNoData.isVisible = filteredContacts.isEmpty()
                binding.rvMain.isVisible = filteredContacts.isNotEmpty()
            }

        })

    }

    override fun performAction(action: Any) {
        when (action) {
            is ItemClick -> {
                when (action.item) {
                    is ContactData -> {
                        (action.item as? ContactData)?.let {

                            val name = it.name ?: return
                            var phoneNumber = it.mobileNumber ?: return
                            phoneNumber = phoneNumber.replace("/(?<!^)\\+|[^\\d+]+//g", "")
                            if (!phoneNumber.startsWith("+")
                                && !phoneNumber.startsWith("+91")
                                && !phoneNumber.startsWith("91")
                            ) {
                                phoneNumber = "+91$phoneNumber"
                            }
                            val message = URLEncoder.encode(
                                buttonData.shareMessage ?: return,
                                "UTF-8"
                            )

                            deeplinkAction.performAction(
                                requireContext(),
                                "doubtnutapp://whatsapp?external_url=https://api.whatsapp.com/send?phone=$phoneNumber&text=$message"
                            )
                            viewModel.referralShareInfo(
                                refereeName = name,
                                refereePhone = phoneNumber
                            )

                            analyticsPublisher.publishEvent(
                                AnalyticsEvent(
                                    "${EVENT_TAG}_${CoreEventConstants.SHARE_CLICKED}",
                                    hashMapOf(
                                        CoreEventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                                        "contact_type" to it.type.orEmpty()
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }

}
