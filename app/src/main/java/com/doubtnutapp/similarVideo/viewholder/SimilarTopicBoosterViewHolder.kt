package com.doubtnutapp.similarVideo.viewholder

import android.os.Handler
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.base.*
import com.doubtnutapp.databinding.ItemSimilarTopicBoosterBinding
import com.doubtnutapp.home.recyclerdecorator.SeparatorDecoration
import com.doubtnutapp.loadImage
import com.doubtnutapp.show
import com.doubtnutapp.similarVideo.model.SimilarTopicBoosterViewItem
import com.doubtnutapp.similarVideo.ui.adapter.SimilarTopicBoosterOptionListAdapter

class SimilarTopicBoosterViewHolder(private val containerView: View) :
    BaseViewHolder<SimilarTopicBoosterViewItem>(containerView) {

    private lateinit var adapter: SimilarTopicBoosterOptionListAdapter

    val binding = ItemSimilarTopicBoosterBinding.bind(itemView)

    override fun bind(data: SimilarTopicBoosterViewItem) {
        val optionsUiUpdateDelay = 500L
        var isNoOptionSelected = true

        binding.apply {
            adapter = SimilarTopicBoosterOptionListAdapter(object : ActionPerformer {

                override fun performAction(action: Any) {
                    when (action) {
                        is OnTopicBoosterOptionClick -> {
                            if (data.isSubmitted == 0 && isNoOptionSelected) {
                                isNoOptionSelected = false
                                val clickedOption = action.topicBoosterOptionViewItem
                                data.submittedOption = clickedOption.optionCode
                                if (clickedOption.isAnswer == 1) {
                                    data.isSubmitted = 1
                                    clickedOption.optionStatus =
                                        1 // Green Background if clicked option is correct.
                                    adapter.updateItemAtPosition(
                                        clickedOption.position,
                                        clickedOption
                                    )
                                    viewSolutionTextView.show()

                                } else {
                                    clickedOption.optionStatus =
                                        2 // Red Background for clicked option.
                                    adapter.updateItemAtPosition(
                                        clickedOption.position,
                                        clickedOption
                                    )
                                    Handler().postDelayed({
                                        data.options.find {
                                            it.isAnswer == 1
                                        }?.let {
                                            it.optionStatus =
                                                1// Green background for correct answer
                                            data.isSubmitted = 1
                                            adapter.updateItemAtPosition(it.position, it)
                                            viewSolutionTextView.show()
                                        }
                                    }, optionsUiUpdateDelay)
                                }

                                actionPerformer?.performAction(
                                    UpdateSimilarTopicBoosterQuestion(
                                        data,
                                        adapterPosition,
                                        data.options.indexOfFirst {
                                            it.isAnswer == 1
                                        },
                                        if (data.options.indexOfFirst {
                                                it.isAnswer == 1
                                            } == clickedOption.position) {
                                            null
                                        } else {
                                            clickedOption.position
                                        })
                                )
                            }
                        }
                    }
                }
            })

            viewSolutionTextView.isVisible = data.submittedOption != null
            viewSolutionTextView.setTextColor(data.solutionTextColor.toColorInt())

            questionImage.loadImage(data.questionTitle)
            rootConstraintLayout.setBackgroundColor(data.backgroundColor.toColorInt())

            if (data.heading.isNullOrBlank().not()) {
                headingTextView.isVisible = true
                topicBoosterImageView.isInvisible =
                    true // We want the image view to take up space, hence setting isInvisible
                headingTextView.text = data.heading
            } else {
                headingTextView.isVisible = false
                topicBoosterImageView.isVisible = true
                topicBoosterImageView.loadImage(data.headerImage)
            }

            val decoration =
                SeparatorDecoration(
                    binding.root.context,
                    binding.root.context.resources.getColor(R.color.light_grey),
                    1.0f
                )
            optionRecycleView.addItemDecoration(decoration)

            optionRecycleView.adapter = adapter
            adapter.updateFeeds(data.options)

            viewSolutionTextView.setOnClickListener {
                actionPerformer?.performAction(SendViewSolutionTapEvents(EventConstants.TOPIC_BOOSTER_VIEW_SOLUTION_TAP))
                actionPerformer?.performAction(
                    PlayTopicBoosterSolutionVideo(
                        data.questionId,
                        Constants.PAGE_SIMILAR,
                        "",
                        "",
                        data.resourceType
                    )
                )
            }
        }
    }
}