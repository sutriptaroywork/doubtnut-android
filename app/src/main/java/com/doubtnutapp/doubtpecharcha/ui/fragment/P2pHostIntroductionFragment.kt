package com.doubtnutapp.doubtpecharcha.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.SingleEvent
import com.doubtnut.core.utils.immediateParentViewModelStoreOwner
import com.doubtnutapp.*
import com.doubtnutapp.databinding.BottomsheetHostHelpIntroductionBinding
import com.doubtnutapp.doubtpecharcha.model.P2pRequestData
import com.doubtnutapp.doubtpecharcha.ui.activity.DoubtP2pActivity
import com.doubtnutapp.doubtpecharcha.ui.adapter.DoubtP2pAnimationAdapter
import com.doubtnutapp.matchquestion.viewmodel.MatchQuestionViewModel
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment

/**
 * Created by Sachin Saxena on 2020-04-10.
 */

class P2pHostIntroductionFragment :
    BaseBindingBottomSheetDialogFragment<MatchQuestionViewModel, BottomsheetHostHelpIntroductionBinding>() {

    companion object {
        const val TAG = "P2pHostIntroductionFragment"
        fun newInstance() = P2pHostIntroductionFragment()
    }

    private var p2pRequestData: P2pRequestData? = null

    private val adapter: DoubtP2pAnimationAdapter by lazy { DoubtP2pAnimationAdapter(isHost = true) }

    override fun getTheme(): Int = R.style.BottomSheetDialog

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomsheetHostHelpIntroductionBinding =
        BottomsheetHostHelpIntroductionBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): MatchQuestionViewModel {
        val vm: MatchQuestionViewModel by viewModels(
            ownerProducer = { immediateParentViewModelStoreOwner }
        ) { viewModelFactory }
        return vm
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        p2pRequestData = viewModel.getDataToRequestP2p()
        viewModel.sendEvent(
            EventConstants.P2P_ASKER_LANDING_ON_EXPLAINER_SCREEN
        )
        setUpQuestion()
        setUpHostAnimation()
        setUpClickListeners()
    }

    private fun setUpQuestion() {
        p2pRequestData?.let { requestData ->
            when {
                requestData.questionImageUrl.isNullOrEmpty().not() -> {
                    binding.ivQuestion.loadImage(requestData.questionImageUrl)
                }
                requestData.questionText.isNullOrEmpty().not() -> {
                    binding.tvOcr.text = requestData.questionText
                }
            }
        }
    }

    private fun setUpClickListeners() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.btConnect.setOnClickListener {
            viewModel.sendEvent(EventConstants.P2P_HOST_CONNECT_NOW)
            val p2pRequestData = p2pRequestData ?: return@setOnClickListener
            binding.progressBar.show()
            viewModel.connectToPeer(
                questionImage = p2pRequestData.questionImageUrl,
                questionText = p2pRequestData.questionText,
                questionId = p2pRequestData.questionId
            )
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.doubtP2PData.observe(viewLifecycleOwner) { p2PEvent ->
            p2PEvent.getContentIfNotHandled()?.let { p2pData ->
                val p2pRequestData = p2pRequestData ?: return@observe
                binding.progressBar.hide()
                viewModel.isDoubtP2PConnected = true
                DoubtP2pActivity.getStartIntent(
                    context = requireContext(),
                    questionText = p2pRequestData.questionText,
                    roomId = p2pData.roomId,
                    isHost = p2pData.isHost,
                    questionImageUrl = p2pRequestData.questionImageUrl,
                    source = Constants.PAGE_SRP
                ).apply {
                    startActivity(this)
                }
            }
            viewModel.removeFeatureWidget.postValue(SingleEvent(MatchQuestionViewModel.MatchPageFeature.P2P))
            dismiss()
        }
    }

    private fun setUpHostAnimation() {
        binding.rvHostAnimation.adapter = adapter
        adapter.updateData(p2pRequestData?.thumbnailImages.orEmpty())
    }
}
