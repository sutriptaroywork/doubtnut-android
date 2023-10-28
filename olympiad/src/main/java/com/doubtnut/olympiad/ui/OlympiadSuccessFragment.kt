package com.doubtnut.olympiad.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.data.remote.Outcome
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.ui.base.CoreBadRequestDialog
import com.doubtnut.core.ui.base.CoreBindingFragment
import com.doubtnut.core.utils.*
import com.doubtnut.olympiad.databinding.FragmentOlympiadSuccessBinding
import com.doubtnut.olympiad.ui.viewmodel.OlympiadActivityVM
import com.doubtnut.olympiad.ui.viewmodel.OlympiadSuccessFragmentVM
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class OlympiadSuccessFragment :
    CoreBindingFragment<OlympiadSuccessFragmentVM, FragmentOlympiadSuccessBinding>() {

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
    ): FragmentOlympiadSuccessBinding {
        return FragmentOlympiadSuccessBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): OlympiadSuccessFragmentVM {
        return viewModelProvider(viewModelFactory)
    }

    override fun provideActivityViewModel() {
        activityViewModel = activityViewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        if (title.isEmpty().not()) {
            activityViewModel.updateTitle(title)
        }
        viewModel.getOlympiadDetails(
            type = type,
            id = id
        )
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.olympiadSuccessResponse.observeNonNull(viewLifecycleOwner) { outcome ->
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

                    binding.ivTop.isVisible = response.successImageUrl.isNullOrEmpty().not()
                    binding.ivTop.loadImage2(response.successImageUrl.ifEmptyThenNull2())

                    binding.ivMain.isVisible = response.imageUrl.isNullOrEmpty().not()
                    binding.ivMain.loadImage2(response.imageUrl.ifEmptyThenNull2())

                    binding.tvTitle.isVisible = response.message.isNullOrEmpty().not()
                    binding.tvTitle.text = response.message
                    binding.tvTitle.applyTextSize(response.messageTextSize)
                    binding.tvTitle.applyTextColor(response.messageTextColor)

                    var autoRedirectJob: Job? = null
                    response.autoRedirect?.let {
                        response.cta?.let { data ->
                            autoRedirectJob = lifecycleScope.launchWhenResumed {
                                delay(TimeUnit.SECONDS.toMillis(response.autoRedirect))

                                analyticsPublisher.publishEvent(
                                    AnalyticsEvent(
                                        "${EVENT_TAG}_${CoreEventConstants.AUTO_REDIRECT}",
                                        hashMapOf<String, Any>(
                                            CoreEventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                                            CoreEventConstants.TYPE to type,
                                            CoreEventConstants.ID to id,
                                        ).apply {
                                            putAll(outcome.data.extraParams.orEmpty())
                                        }
                                    )
                                )

                                if (data.deeplink.isNullOrEmpty().not()) {
                                    deeplinkAction.performAction(requireContext(), data.deeplink)
                                }
                            }
                        }
                    }

                    binding.btnCta.isVisible = response.cta != null
                    response.cta?.let { data ->
                        binding.btnCta.isVisible = data.titleOne.isNullOrEmpty().not()
                        binding.btnCta.text = data.titleOne
                        binding.btnCta.applyTextSize(data.titleOneTextSize)
                        binding.btnCta.applyTextColor(data.titleOneTextColor)
                        binding.btnCta.applyBackgroundTint(data.backgroundColor)

                        binding.btnCta.setOnClickListener {
                            autoRedirectJob?.cancel()

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
                        }
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
        private const val TAG = "OlympiadSuccessFragment"
        private const val EVENT_TAG = "olympiad_success_fragment"
    }

}