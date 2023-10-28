package com.doubtnutapp.ui.common.address

import android.animation.Animator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.*
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.FragmentSubmitAddressDialogBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.utils.KeyboardUtils
import javax.inject.Inject

class SubmitAddressDialogFragment :
    BaseBindingDialogFragment<SubmitAddressDialogFragmentVM, FragmentSubmitAddressDialogBinding>() {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private val type: String by lazy { requireArguments().getString(KEY_TYPE).orEmpty() }
    private val id: String by lazy { requireArguments().getString(KEY_ID).orEmpty() }

    private var selectedStatePosition = 0
    private var selectedSizePosition = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSubmitAddressDialogBinding {
        return FragmentSubmitAddressDialogBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): SubmitAddressDialogFragmentVM {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        init()
        initListeners()
        viewModel.addressFormData(
            type = type,
            id = id
        )
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.widgetsLiveData.observeNonNull(this) { outcome ->
            when (outcome) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = outcome.loading
                }
                is Outcome.Success -> {
                    val loadUi = {
                        binding.progressBar.isVisible = false
                        binding.lottieAnimationView.hide()

                        binding.tvTitle.isVisible = outcome.data.title.isNullOrEmpty().not()
                        binding.tvTitle.text = outcome.data.title
                        binding.tvTitle.applyTextSize(outcome.data.titleTextSize)
                        binding.tvTitle.applyTextColor(outcome.data.titleTextColor)

                        binding.clLink.isVisible =
                            outcome.data.hintLink.isNullOrEmpty().not()
                        binding.etLink.hint = outcome.data.hintLink
                        binding.tvLink.isVisible = outcome.data.hintLink2.isNotNullAndNotEmpty()
                        binding.tvLink.text = outcome.data.hintLink2

                        binding.clFullName.isVisible =
                            outcome.data.hintFullName.isNullOrEmpty().not()
                        binding.etFullName.hint = outcome.data.hintFullName

                        binding.clMobile.isVisible = outcome.data.countryCode.isNullOrEmpty()
                            .not() || outcome.data.countryCode.isNullOrEmpty().not()
                        binding.tvCountryCode.text = outcome.data.countryCode
                        binding.etMobile.hint = outcome.data.hintMobileNumber

                        binding.clPin.isVisible = outcome.data.hintPinCode.isNullOrEmpty().not()
                        binding.etPin.hint = outcome.data.hintPinCode

                        binding.clAddressOne.isVisible =
                            outcome.data.hintAddressOne.isNullOrEmpty().not()
                        binding.etAddressOne.hint = outcome.data.hintAddressOne

                        binding.clAddressTwo.isVisible =
                            outcome.data.hintAddressTwo.isNullOrEmpty().not()
                        binding.etAddressTwo.hint = outcome.data.hintAddressTwo

                        binding.clLandmark.isVisible =
                            outcome.data.hintLandmark.isNullOrEmpty().not()
                        binding.etLandmark.hint = outcome.data.hintLandmark

                        binding.clFullAddress.isVisible =
                            outcome.data.hintFullAddress.isNullOrEmpty().not()
                        binding.etFullAddress.hint = outcome.data.hintFullAddress

                        binding.clCity.isVisible = outcome.data.hintCity.isNullOrEmpty().not()
                        binding.etCity.hint = outcome.data.hintCity

                        binding.clState.isVisible = outcome.data.states.isNullOrEmpty().not()
                        outcome.data.states?.map { it.title }?.let {
                            binding.spinnerState.onItemSelectedListener = stateItemSelectedListener
                            binding.spinnerState.adapter = ArrayAdapter<String>(
                                requireContext(),
                                R.layout.support_simple_spinner_dropdown_item,
                                it
                            )
                        }

                        binding.clSize.isVisible = outcome.data.sizes.isNullOrEmpty().not()
                        outcome.data.sizes?.map { it.title }?.let {
                            binding.spinnerSize.onItemSelectedListener = sizeItemSelectedListener
                            binding.spinnerSize.adapter = ArrayAdapter<String>(
                                requireContext(),
                                R.layout.support_simple_spinner_dropdown_item,
                                it
                            )
                        }

                        binding.btnSubmit.isVisible = outcome.data.submitText.isNullOrEmpty().not()
                        binding.btnSubmit.text = outcome.data.submitText
                    }

                    if (outcome.data.lottieUrl.isNotNullAndNotEmpty()) {
                        binding.lottieAnimationView.show()
                        binding.lottieAnimationView.setAnimationFromUrl(outcome.data.lottieUrl)
                        binding.lottieAnimationView.addAnimatorListener(object :
                            Animator.AnimatorListener {

                            override fun onAnimationStart(animation: Animator?) {
                                binding.progressBar.isVisible = false
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                mBinding ?: return
                                loadUi()
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                            }

                            override fun onAnimationRepeat(animation: Animator?) {
                            }
                        })
                        binding.lottieAnimationView.playAnimation()
                    } else {
                        loadUi()
                    }

                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            "${EVENT_TAG}_${EventConstants.VIEWED}",
                            hashMapOf<String, Any>(
                                EventConstants.WIDGET to type,
                                EventConstants.ID to id
                            ).apply {
                                putAll(outcome.data.extraParams.orEmpty())
                            }
                        )
                    )
                }
                else -> {
                    dismiss()
                }
            }
        }

        viewModel.submitAddress.observeNonNull(this) { outcome ->
            when (outcome) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = outcome.loading
                }
                is Outcome.Success -> {
                    binding.progressBar.isVisible = false

                    KeyboardUtils.hideKeyboard(binding.root)

                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                            hashMapOf<String, Any>(
                                EventConstants.WIDGET to type,
                                EventConstants.ID to id,
                                EventConstants.CTA_TEXT to binding.btnSubmit.text.toString(),
                            ).apply {
                                putAll(outcome.data.extraParams.orEmpty())
                            }
                        )
                    )

                    outcome.data.errorMessage
                        ?.takeIf { it.isEmpty().not() }
                        ?.let {
                            toast(it)
                            return@observeNonNull
                        }

                    outcome.data.toastMessage
                        ?.takeIf { it.isEmpty().not() }
                        ?.let {
                            toast(it)
                        }

                    outcome.data.deeplink
                        ?.takeIf { it.isEmpty().not() }
                        ?.let {
                            deeplinkAction.performAction(requireContext(), it)
                            dismiss()
                            return@observeNonNull
                        }

                    binding.tvMessage.show()
                    binding.tvMessage.text = outcome.data.message
                    binding.tvTitle.hide()
                    binding.clLink.hide()
                    binding.clFullName.hide()
                    binding.clMobile.hide()
                    binding.clPin.hide()
                    binding.clAddressOne.hide()
                    binding.clAddressTwo.hide()
                    binding.clLandmark.hide()
                    binding.clFullAddress.hide()
                    binding.clCity.hide()
                    binding.clState.hide()
                    binding.clSize.hide()
                    binding.btnSubmit.hide()
                }
                else -> {
                    dismiss()
                }
            }
        }
    }

    private fun init() {
    }

    private fun initListeners() {
        binding.viewIvCloseClickHandler.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CROSS_CLICKED}",
                    hashMapOf(
                        EventConstants.WIDGET to type,
                        EventConstants.ID to id,
                    )
                )
            )
            dismiss()
        }
        binding.tvLink.setOnClickListener {
            requireContext()
                .getTextFromClipboard()
                .takeIf { it.isNotNullAndNotEmpty() }
                ?.let {
                    binding.etLink.setText(it)
                }
        }
        binding.btnSubmit.setOnClickListener {
            var selectedStateId = ""
            var selectedSizeId = ""
            val outcome = viewModel.widgetsLiveData.value
            if (outcome is Outcome.Success) {
                selectedStateId = outcome.data.states?.getOrNull(selectedStatePosition)?.id ?: ""
                selectedSizeId = outcome.data.sizes?.getOrNull(selectedSizePosition)?.id ?: ""
            }

            viewModel.submitAddress(
                type = type,
                id = id,
                link = binding.etLink.text.toString(),
                fullName = binding.etFullName.text.toString(),
                countryCode = binding.tvCountryCode.text.toString(),
                mobileNumber = binding.etMobile.text.toString(),
                pinCode = binding.etPin.text.toString(),
                addressOne = binding.etAddressOne.text.toString(),
                addressTwo = binding.etAddressTwo.text.toString(),
                landmark = binding.etLandmark.text.toString(),
                fullAddress = binding.etFullAddress.text.toString(),
                city = binding.etCity.text.toString(),
                stateId = selectedStateId,
                sizeId = selectedSizeId
            )
        }
    }

    override fun onStart() {
        super.onStart()
        activity ?: return

        dialog?.window?.setLayout(
            requireActivity().getScreenWidth() - 32.dpToPx(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private val stateItemSelectedListener: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (pos == 0) {
                    (parent.getChildAt(0) as? TextView)?.applyTextColor("#808080")
                } else {
                    (parent.getChildAt(0) as? TextView)?.applyTextColor("#000000")
                }
                selectedStatePosition = pos
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    private val sizeItemSelectedListener: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (pos == 0) {
                    (parent.getChildAt(0) as? TextView)?.applyTextColor("#808080")
                } else {
                    (parent.getChildAt(0) as? TextView)?.applyTextColor("#000000")
                }
                selectedSizePosition = pos
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    companion object {
        const val TAG = "SubmitAddressDialogFragment"
        const val EVENT_TAG = "submit_address_dialog_fragment"

        private const val KEY_TYPE = "type"
        private const val KEY_ID = "id"

        fun newInstance(
            type: String,
            id: String?
        ) =
            SubmitAddressDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_TYPE, type)
                    putString(KEY_ID, id)
                }
            }
    }

}

