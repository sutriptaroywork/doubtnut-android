package com.doubtnut.olympiad.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.data.remote.Outcome
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.ui.base.CoreBadRequestDialog
import com.doubtnut.core.ui.base.CoreBindingFragment
import com.doubtnut.core.utils.*
import com.doubtnut.core.view.GridSpaceItemDecorator
import com.doubtnut.olympiad.data.entity.DetailsContainerItem
import com.doubtnut.olympiad.data.entity.OlympiadDetailResponse
import com.doubtnut.olympiad.databinding.FragmentOlympiadRegisterBinding
import com.doubtnut.olympiad.databinding.ItemOlympiadRegisterDetailsBinding
import com.doubtnut.olympiad.ui.viewmodel.OlympiadActivityVM
import com.doubtnut.olympiad.ui.viewmodel.OlympiadRegisterFragmentVM
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import javax.inject.Inject

class OlympiadRegisterFragment :
    CoreBindingFragment<OlympiadRegisterFragmentVM, FragmentOlympiadRegisterBinding>() {

    private lateinit var activityViewModel: OlympiadActivityVM

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    private val args by navArgs<OlympiadRegisterFragmentArgs>()
    private val type by lazy { args.type.orEmpty() }
    private val id by lazy { args.id.orEmpty() }
    private val title by lazy { args.title.orEmpty() }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOlympiadRegisterBinding {
        return FragmentOlympiadRegisterBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): OlympiadRegisterFragmentVM {
        return viewModelProvider(viewModelFactory)
    }

    override fun provideActivityViewModel() {
        activityViewModel = activityViewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        if (title.isEmpty().not()) {
            activityViewModel.updateTitle(title)
        }

        KeyboardVisibilityEvent.setEventListener(
            requireActivity(),
            viewLifecycleOwner
        ) { isVisible ->
            binding.clFooter.isVisible = !isVisible
        }

        viewModel.getOlympiadDetails(
            type = type,
            id = id
        )
    }

    private var reloadDataApiOnStart = false

    override fun onStart() {
        super.onStart()
        if (reloadDataApiOnStart) {
            reloadDataApiOnStart = false
            binding.cbTermAndCondition.isChecked = false
            viewModel.getOlympiadDetails(
                type = type,
                id = id
            )
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.olympiadDetailResponse.observeNonNull(viewLifecycleOwner) { outcome ->
            mBinding ?: return@observeNonNull
            when (outcome) {
                is Outcome.Progress -> {
                    binding.progressBar.setVisibleState2(outcome.loading)
                }
                is Outcome.Success -> {
                    val response = outcome.data

                    if (response.headerTitle.isNullOrEmpty().not()) {
                        activityViewModel.updateTitle(response.headerTitle.orEmpty())
                    }

                    if (response.deeplink.isNullOrEmpty().not()) {
                        deeplinkAction.performAction(requireContext(), response.deeplink)
                        if (response.finishActivity == true) {
                            requireActivity().finish()
                        }
                        return@observeNonNull
                    }

                    binding.clTop.isVisible = response.topContainer != null
                    response.topContainer?.let {
                        binding.clTop.applyBackgroundTint(it.backgroundColor)

                        binding.tvTopTitleOne.isVisible = it.titleOne.isNullOrEmpty().not()
                        binding.tvTopTitleOne.text = it.titleOne
                        binding.tvTopTitleOne.applyTextColor(it.titleOneTextColor)
                        binding.tvTopTitleOne.applyTextSize(it.titleOneTextSize)

                        binding.ivTop.loadImage2(it.imageUrl)
                    }

                    binding.tvDetailsTitle.isVisible = response.detailsContainer != null
                            && response.detailsContainer.titleOne.isNullOrEmpty().not()
                    binding.tvDetailsSubtitle.isVisible = response.detailsContainer != null
                            && response.detailsContainer.titleTwo.isNullOrEmpty().not()
                    binding.rvDetails.isVisible = response.detailsContainer != null
                            && response.detailsContainer.items.isNullOrEmpty().not()

                    response.detailsContainer?.let { data ->
                        binding.tvDetailsTitle.text = data.titleOne
                        binding.tvDetailsTitle.applyTextColor(data.titleOneTextColor)
                        binding.tvDetailsTitle.applyTextSize(data.titleOneTextSize)

                        binding.tvDetailsSubtitle.text = data.titleTwo
                        binding.tvDetailsSubtitle.applyTextColor(data.titleTwoTextColor)
                        binding.tvDetailsSubtitle.applyTextSize(data.titleTwoTextSize)

                        binding.rvDetails.removeItemDecorations2()
                        binding.rvDetails.addItemDecoration(
                            GridSpaceItemDecorator(
                                2,
                                30.dpToPx(),
                                false
                            )
                        )
//                        response.detailsContainer.items?.size
//                            ?.takeIf { it >= 1 }
//                            ?.let {
//                                binding.rvDetails.setItemViewCacheSize(it)
//                            }
                        binding.rvDetails.adapter = Adapter(response)
                    }

                    binding.tvKnowMoreTitle.isVisible = response.knowMore != null
                    response.knowMore?.let { data ->
                        binding.tvKnowMoreTitle.isVisible = data.titleOne.isNullOrEmpty().not()
                        binding.tvKnowMoreTitle.text = data.titleOne
                        binding.tvKnowMoreTitle.applyTextColor(data.titleOneTextColor)
                        binding.tvKnowMoreTitle.applyTextSize(data.titleOneTextSize)

                        binding.viewKnowMoreTitle.setOnClickListener {
                            deeplinkAction.performAction(requireContext(), data.deeplink)

                            analyticsPublisher.publishEvent(
                                AnalyticsEvent(
                                    "${EVENT_TAG}_${CoreEventConstants.KNOW_MORE_CLICKED}",
                                    hashMapOf<String, Any>(
                                        CoreEventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                                        CoreEventConstants.TYPE to type,
                                        CoreEventConstants.ID to id,
                                        CoreEventConstants.CTA_TEXT to data.titleOne.isNullOrEmpty(),
                                    ).apply {
                                        putAll(outcome.data.extraParams.orEmpty())
                                    }
                                )
                            )
                        }
                    }

                    binding.clCtaInfo.isVisible = response.ctaInfo != null
                    response.ctaInfo?.let {
                        binding.clCtaInfo.applyBackgroundTint(it.backgroundColor)

                        binding.tvCtaInfoTitle.isVisible = it.titleOne.isNullOrEmpty().not()
                        binding.tvCtaInfoTitle.text = it.titleOne
                        binding.tvCtaInfoTitle.applyTextColor(it.titleOneTextColor)
                        binding.tvCtaInfoTitle.applyTextSize(it.titleOneTextSize)
                    }


                    binding.cbTermAndCondition.isVisible = response.termAndCondition != null
                    binding.tvTermAndCondition.isVisible = response.termAndCondition != null
                    response.termAndCondition?.let { data ->
                        binding.cbTermAndCondition.applyButtonTint(response.highlightColor)

                        binding.tvTermAndCondition.isVisible = data.titleOne.isNullOrEmpty().not()
                        TextViewUtils.setTextFromHtml(
                            binding.tvTermAndCondition,
                            data.titleOne.orEmpty()
                        )
                        binding.tvTermAndCondition.applyTextColor(data.titleOneTextColor)
                        binding.tvTermAndCondition.applyTextSize(data.titleOneTextSize)

                        binding.cbTermAndCondition.setOnCheckedChangeListener { _, isChecked ->
                            binding.btnCta.isEnabled = isChecked
                            if (isChecked) {
                                response.cta?.let { data ->
                                    binding.btnCta.applyTextColor(data.titleOneTextColor)
                                    binding.btnCta.applyBackgroundTint(data.backgroundColor)
                                }
                            } else {
                                binding.btnCta.applyTextColor("#2f2f2f")
                                binding.btnCta.applyBackgroundTint("#e2e2e2")
                            }
                        }

                        binding.tvTermAndCondition.setOnClickListener {
                            if (data.deeplink.isNullOrEmpty().not()) {
                                deeplinkAction.performAction(requireContext(), data.deeplink)
                                return@setOnClickListener
                            }

                            binding.cbTermAndCondition.toggle()
                        }
                    }

                    binding.btnCta.isVisible = response.cta != null
                    binding.btnCta.isEnabled = response.termAndCondition == null
                    response.cta?.let { data ->
                        binding.btnCta.isVisible = data.titleOne.isNullOrEmpty().not()
                        binding.btnCta.text = data.titleOne
                        binding.btnCta.applyTextSize(data.titleOneTextSize)

                        if (response.termAndCondition == null) {
                            binding.btnCta.applyTextColor(data.titleOneTextColor)
                            binding.btnCta.applyBackgroundTint(data.backgroundColor)
                        } else {
                            binding.btnCta.applyTextColor("#2f2f2f")
                            binding.btnCta.applyBackgroundTint("#e2e2e2")
                        }

                        binding.btnCta.setOnClickListener {
                            analyticsPublisher.publishEvent(
                                AnalyticsEvent(
                                    "${EVENT_TAG}_${CoreEventConstants.CTA_CLICKED}",
                                    hashMapOf<String, Any>(
                                        CoreEventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                                        CoreEventConstants.TYPE to type,
                                        CoreEventConstants.ID to id,
                                        CoreEventConstants.CTA_TEXT to data.titleOne.isNullOrEmpty(),
                                    ).apply {
                                        putAll(outcome.data.extraParams.orEmpty())
                                    }
                                )
                            )

                            if (data.deeplink.isNullOrEmpty().not()) {
                                deeplinkAction.performAction(requireContext(), data.deeplink)
                                return@setOnClickListener
                            }
                            val payload = hashMapOf<String, String>()
                            response.detailsContainer?.items?.filter { it.isEditable == true }
                                ?.forEach {
                                    payload[it.key.orEmpty()] = it.value.orEmpty()
                                }
                            viewModel.postOlympiadRegister(
                                type = type,
                                id = id,
                                payload = payload
                            )
                        }

                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                "${EVENT_TAG}_${CoreEventConstants.VIEWED}",
                                hashMapOf<String, Any>(
                                    CoreEventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                                    CoreEventConstants.TYPE to type,
                                    CoreEventConstants.ID to id,
                                ).apply {
                                    putAll(outcome.data.extraParams.orEmpty())
                                }
                            )
                        )
                    }
                }
                is Outcome.ApiError -> {
                    binding.progressBar.gone()
                    apiErrorToast2(outcome.e)
                }
                is Outcome.BadRequest -> {
                    binding.progressBar.gone()
                    val dialog = CoreBadRequestDialog.newInstance("unauthorized")
                    dialog.show(childFragmentManager, "BadRequestDialog")
                }
                is Outcome.Failure -> {
                    binding.progressBar.gone()
                    apiErrorToast2(outcome.e)
                }
            }
        }

        viewModel.postOlympiadRegisterResponse.observeNonNull(viewLifecycleOwner) { outcome ->
            mBinding ?: return@observeNonNull

            when (outcome) {
                is Outcome.Progress -> {
                    binding.progressBar.setVisibleState2(outcome.loading)
                }
                is Outcome.Success -> {
                    val response = outcome.data
                    if (response.errorMessage.isNullOrEmpty().not()) {
                        toast(response.errorMessage.orEmpty())
                        return@observeNonNull
                    }
                    deeplinkAction.performAction(requireContext(), response.deeplink)
                    reloadDataApiOnStart = true
                }
                is Outcome.ApiError -> {
                    binding.progressBar.gone()
                    apiErrorToast2(outcome.e)
                }
                is Outcome.BadRequest -> {
                    binding.progressBar.gone()
                    val dialog = CoreBadRequestDialog.newInstance("unauthorized")
                    dialog.show(childFragmentManager, "BadRequestDialog")
                }
                is Outcome.Failure -> {
                    binding.progressBar.gone()
                    apiErrorToast2(outcome.e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "OlympiadRegisterFragment"
        private const val EVENT_TAG = "olympiad_register_fragment"
    }
}

class Adapter(
    val response: OlympiadDetailResponse
) :
    ListAdapter<DetailsContainerItem, Adapter.ViewHolder>(
        DIFF_UTILS
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemOlympiadRegisterDetailsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            MyCustomEditTextListener()
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return response.detailsContainer?.items?.size ?: 0
    }

    inner class ViewHolder(
        val binding: ItemOlympiadRegisterDetailsBinding,
        private val editTextListener: MyCustomEditTextListener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.etValue.addTextChangedListener(editTextListener)
        }

        @SuppressLint("SetTextI18n")
        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            val data = response.detailsContainer ?: return
            val item = response.detailsContainer.items?.getOrNull(bindingAdapterPosition) ?: return

            editTextListener.updatePosition(bindingAdapterPosition)

            binding.tvLabel.text = item.label
            if (item.isEditable == true) {
                binding.tvLabel.applyTextSize(data.labelEditTextSize)
                binding.tvLabel.applyTextColor(data.labelEditTextColor)
            } else {
                binding.tvLabel.applyTextSize(data.labelTextSize)
                binding.tvLabel.applyTextColor(data.labelTextColor)
            }

            binding.tvValue.isVisible = item.isEditable != true
            binding.tvValue.text = item.value
            binding.tvValue.applyTextSize(data.valueTextSize)
            binding.tvValue.applyTextColor(data.valueTextColor)

            binding.etValue.isVisible = item.isEditable == true
            binding.etValue.setText(item.value.orEmpty())
            binding.etValue.hint = item.hint.orEmpty()

            binding.etValue.inputType = item.inputType ?: InputType.TYPE_CLASS_TEXT
//            if (!item.digits.isNullOrEmpty()) {
//                binding.etValue.keyListener = DigitsKeyListener.getInstance(item.digits)
//            }

            binding.etValue.filters = arrayOf(InputFilter.LengthFilter(item.maxLength ?: 30))

            binding.etValue.applyTextSize(data.valueEditTextSize)
            binding.etValue.applyTextColor(data.valueEditTextColor)
            binding.etValue.applyHintColor(data.hintTextColor)
            binding.etValue.applyBackgroundTint(response.highlightColor)
        }
    }

    inner class MyCustomEditTextListener : TextWatcher {
        private var position = 0

        fun updatePosition(position: Int) {
            this.position = position
        }

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
            response.detailsContainer?.items?.getOrNull(position)?.value = charSequence.toString()
        }

        override fun afterTextChanged(editable: Editable) {
        }
    }

    companion object {

        val DIFF_UTILS = object :
            DiffUtil.ItemCallback<DetailsContainerItem>() {
            override fun areContentsTheSame(
                oldItem: DetailsContainerItem,
                newItem: DetailsContainerItem
            ) =
                oldItem == newItem

            override fun areItemsTheSame(
                oldItem: DetailsContainerItem,
                newItem: DetailsContainerItem
            ) =
                false
        }
    }
}