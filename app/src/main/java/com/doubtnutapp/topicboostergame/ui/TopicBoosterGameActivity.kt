package com.doubtnutapp.topicboostergame.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.findNavController
import com.doubtnut.core.utils.setStatusBarColor
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.topicboostergame.AdditionalDetails
import com.doubtnutapp.databinding.ActivityTopicBoosterGameBinding
import com.doubtnutapp.topicboostergame.viewmodel.TopicBoosterGameViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.Utils

class TopicBoosterGameActivity : BaseBindingActivity<TopicBoosterGameViewModel, ActivityTopicBoosterGameBinding>() {

    companion object {

        const val TAG = "TopicBoosterGameActivity"
        const val INTENT_EXTRA_TEST_UUID = "test_uuid"
        const val INTENT_EXTRA_ONLINE_PLAYERS_COUNT_K = "online_players_count_k"

        private const val INTENT_EXTRA_PARENT_QUESTION_ID = "parent_question_id"
        private const val INTENT_EXTRA_CHAPTER_ALIAS = "chapter_alias"
        private const val INTENT_EXTRA_OPPONENT_IMAGE = "opponent_image"
        private const val INTENT_EXTRA_OPPONENT_NAME = "opponent_name"
        private const val INTENT_EXTRA_OPPONENT_IMAGE_BACKGROUND_COLOR = "opponent_image_background_color"
        private const val INTENT_EXTRA_ADDITIONAL_DETAILS = "additional_details"

        fun getStartIntent(
                context: Context,
                parentQuestionId: String,
                testUUId: String? = null,
                onlinePlayersCountK: Float? = null,
                opponentImage: String? = null,
                opponentName: String? = null,
                opponentImageBackgroundColor: String? = null,
                chapterAlias: String? = null,
                additionalDetails: AdditionalDetails? = null,
        ): Intent = Intent(context, TopicBoosterGameActivity::class.java).apply {
            putExtra(INTENT_EXTRA_PARENT_QUESTION_ID, parentQuestionId)
            putExtra(INTENT_EXTRA_TEST_UUID, testUUId)
            putExtra(INTENT_EXTRA_ONLINE_PLAYERS_COUNT_K, onlinePlayersCountK)
            putExtra(INTENT_EXTRA_OPPONENT_IMAGE, opponentImage)
            putExtra(INTENT_EXTRA_OPPONENT_NAME, opponentName)
            putExtra(INTENT_EXTRA_CHAPTER_ALIAS, chapterAlias)
            putExtra(INTENT_EXTRA_OPPONENT_IMAGE_BACKGROUND_COLOR, opponentImageBackgroundColor)
            putExtra(INTENT_EXTRA_ADDITIONAL_DETAILS, additionalDetails)
        }
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(): ActivityTopicBoosterGameBinding =
        ActivityTopicBoosterGameBinding.inflate(layoutInflater)

    override fun provideViewModel(): TopicBoosterGameViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {

        setStatusBarColor(R.color.colorPrimary)
        getDataIntent()

        findNavController(R.id.nav_host_fragment_topic_booster_game)
            .setGraph(R.navigation.nav_graph_topic_booster_game, intent.extras)

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getDataIntent() {
        viewModel.parentQuestionId = intent?.getStringExtra(INTENT_EXTRA_PARENT_QUESTION_ID).orEmpty()
        viewModel.chapterAlias = intent?.getStringExtra(INTENT_EXTRA_CHAPTER_ALIAS)
        viewModel.opponentImageUrl = intent?.getStringExtra(INTENT_EXTRA_OPPONENT_IMAGE).orEmpty()
        viewModel.opponentName = intent?.getStringExtra(INTENT_EXTRA_OPPONENT_NAME).orEmpty()

        val opponentImageBackgroundColorString = intent?.getStringExtra(INTENT_EXTRA_OPPONENT_IMAGE_BACKGROUND_COLOR).orEmpty()
        viewModel.opponentImageBackgroundColor = Utils.parseColor(opponentImageBackgroundColorString)

        viewModel.additionalDetails = intent?.getParcelableExtra(INTENT_EXTRA_ADDITIONAL_DETAILS)
    }
}