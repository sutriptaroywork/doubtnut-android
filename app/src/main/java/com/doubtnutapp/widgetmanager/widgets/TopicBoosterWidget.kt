package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.graphics.toColorInt
import androidx.core.view.*
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnTopicBoosterOptionClick
import com.doubtnutapp.base.PlayTopicBoosterSolutionVideo
import com.doubtnutapp.base.SendViewSolutionTapEvents
import com.doubtnutapp.base.UpdateTopicBoosterWidgetQuestion
import com.doubtnutapp.databinding.ItemSimilarTopicBoosterBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.home.recyclerdecorator.SeparatorDecoration
import com.doubtnutapp.similarVideo.model.SimilarTopicBoosterOptionViewItem
import com.doubtnutapp.similarVideo.ui.adapter.SimilarTopicBoosterOptionListAdapter
import com.doubtnutapp.utils.Utils
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by devansh on 7/5/21.
 */

class TopicBoosterWidget(context: Context) :
    BaseBindingWidget<TopicBoosterWidget.WidgetHolder, TopicBoosterWidget.Model, ItemSimilarTopicBoosterBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private lateinit var adapter: SimilarTopicBoosterOptionListAdapter

    override fun getViewBinding(): ItemSimilarTopicBoosterBinding {
        return ItemSimilarTopicBoosterBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val data = model.data
        val binding = holder.binding
        val optionsUiUpdateDelay = 500L
        var isNoOptionSelected = true

        holder.setIsRecyclable(false)

        binding.apply {
            updateLayoutParams {
                width = Utils.getWidthFromScrollSize(holder.itemView.context, data.cardWidth) -
                        (marginStart + marginEnd)
            }
            updateMargins(right = 10.dpToPx())

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
                                    UpdateTopicBoosterWidgetQuestion(
                                        data,
                                        holder.adapterPosition,
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
                // We want the image view to take up space, hence setting isInvisible
                topicBoosterImageView.isInvisible = true
                headingTextView.text = data.heading
                headingTextView.setTextColor(Utils.parseColor(data.headingColor))
            } else {
                headingTextView.isVisible = false
                topicBoosterImageView.isVisible = true
                topicBoosterImageView.loadImage(data.headerImage)
            }

            data.options.forEach {
                it.viewType = R.layout.item_similar_topic_booster_option
            }

            val decoration =
                SeparatorDecoration(context, context.resources.getColor(R.color.light_grey), 1.0f)
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

        return holder
    }

    class WidgetHolder(binding: ItemSimilarTopicBoosterBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<ItemSimilarTopicBoosterBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: Int,
        @SerializedName("question_id") val questionId: String,
        @SerializedName("question_title") val questionTitle: String,
        @SerializedName("is_submitted") var isSubmitted: Int,
        @SerializedName("submitted_option") var submittedOption: String?,
        @SerializedName("options") val options: List<SimilarTopicBoosterOptionViewItem>,
        @SerializedName("resource_type") val resourceType: String,
        @SerializedName("widget_type") val widgetType: String,
        @SerializedName("submit_url_endpoint") val submitUrlEndpoint: String,
        @SerializedName("header_image") val headerImage: String,
        @SerializedName("background_color") val backgroundColor: String,
        @SerializedName("solution_text_color") val solutionTextColor: String,
        @SerializedName("heading") val heading: String?,
        @SerializedName("heading_color") val headingColor: String?,
        @SerializedName("card_width") val cardWidth: String,
    ) : WidgetData()
}