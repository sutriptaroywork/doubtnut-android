package com.doubtnutapp.doubtpecharcha.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.databinding.ActivityHelperEntryBinding
import com.doubtnutapp.doubtpecharcha.ui.adapter.DoubtP2pAnimationAdapter
import com.doubtnutapp.doubtpecharcha.viewmodel.DoubtP2pHelperEntryViewModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.ui.base.BaseBindingActivity

/**
 * Created by Sachin Saxena on 2020-04-10.
 */

class DoubtP2pHelperEntryActivity :
    BaseBindingActivity<DoubtP2pHelperEntryViewModel, ActivityHelperEntryBinding>() {

    companion object {
        const val TAG = "DoubtP2pHelperEntryActivity"
        private const val ROOM_ID = "room_id"
        fun getStartIntent(
            context: Context,
            roomId: String?,
            isMessage: Boolean?,
            source: String? = null
        ) =
            Intent(context, DoubtP2pHelperEntryActivity::class.java).apply {
                putExtra(ROOM_ID, roomId)
                putExtra(DoubtP2pActivity.PARAM_KEY_IS_MESSAGE, isMessage)
                putExtra(DoubtP2pActivity.PARAM_KEY_SOURCE, source)
            }
    }

    private val roomId: String by lazy {
        intent.getStringExtra(ROOM_ID) ?: ""
    }
    private val source: String? by lazy {
        intent.getStringExtra(DoubtP2pActivity.PARAM_KEY_SOURCE)
    }
    private val isMessage: Boolean? by lazy {
        intent.getBooleanExtra(DoubtP2pActivity.PARAM_KEY_IS_MESSAGE, true)
    }

    private val adapter: DoubtP2pAnimationAdapter by lazy { DoubtP2pAnimationAdapter(isHost = false) }

    override fun provideViewBinding(): ActivityHelperEntryBinding =
        ActivityHelperEntryBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DoubtP2pHelperEntryViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        setUpAnimation()
        viewModel.getHelperData(roomId)
        viewModel.sendEvent(EventConstants.P2P_HELPER_SCREEN_VISIBLE, ignoreSnowplow = true)
        setUpClickListeners()
    }

    private fun setUpAnimation() {
        binding.rvHelperAnimation.adapter = adapter
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.p2pRoomId.observe(this) { p2pRoomData ->
            val questionData = p2pRoomData.quesData ?: return@observe
            binding.questionImageTitle.text = questionData.title1
            binding.tvSuggestion.text = questionData.title2
            if (questionData.questionImage.isNullOrEmpty().not()) {
                binding.ivQuestion.loadImage(questionData.questionImage)
            } else if (questionData.questionText.isNullOrEmpty().not()) {
                binding.tvOcr.text = questionData.questionText
            }
            binding.btSolveNow.text = questionData.buttonText
            adapter.updateData(p2pRoomData.quesData.thumbnailImages.orEmpty())
        }
    }

    private fun setUpClickListeners() {
        binding.ivClose.setOnClickListener {
            finish()
        }

        binding.btSolveNow.setOnClickListener {
            viewModel.sendEvent(EventConstants.P2P_HELPER_CLICKED_SOLVE_NOW, ignoreSnowplow = true)
            DoubtP2pActivity.getStartIntent(
                context = this,
                roomId = roomId,
                isHost = false,
                isMessage = isMessage,
                source = source
            ).apply {
                startActivity(this)
            }
            finish()
        }
    }
}
