package com.doubtnutapp.ui.questionAskedHistory

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.LayoutQuestionAskedBinding
import com.doubtnutapp.hide
import com.doubtnutapp.matchquestion.ui.activity.MatchQuestionActivity
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.show
import com.doubtnutapp.textsolution.ui.TextSolutionActivity
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import javax.inject.Inject

class QuestionAskedHistoryListAdapter :
    RecyclerView.Adapter<QuestionAskedHistoryListAdapter.QuestionAskedViewHolder>() {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private val questionsAskedList =
        mutableListOf<QuestionAskedHistoryDetails.QuestionAskedDetails>()

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): QuestionAskedHistoryListAdapter.QuestionAskedViewHolder {
        val binder = LayoutQuestionAskedBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return QuestionAskedViewHolder(binder)
    }

    override fun onBindViewHolder(
        holder: QuestionAskedHistoryListAdapter.QuestionAskedViewHolder,
        position: Int
    ) {
        holder.bind(questionsAskedList[position])
    }

    override fun getItemCount(): Int = questionsAskedList.size

    inner class QuestionAskedViewHolder(
        private val binding: LayoutQuestionAskedBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: QuestionAskedHistoryDetails.QuestionAskedDetails) {
            binding.questionAsked = item
            if (item.quesImageUrl == null) {
                binding.imgQuestion.setVisibleState(false)
                binding.questionTextView.setVisibleState(true)
                binding.questionTextView.text = item.questionText
            } else {
                binding.questionTextView.setVisibleState(false)
                binding.imgQuestion.setVisibleState(true)
                setImageUrl(item.quesImageUrl)
            }

            if (item.isSolutionPresent) {
                binding.actionTxt.text = "Watch Again"
                binding.parentLayout.setOnClickListener {
                    sendEvent(
                        EventConstants.EVENT_HISTORY_WATCH_AGAIN,
                        hashMapOf(),
                        ignoreSnowplow = true
                    )
                    showAnswer(item)
                }
            } else {
                binding.playVideoImageView.visibility = View.GONE
                binding.actionTxt.text = "Search Now"
                binding.parentLayout.setOnClickListener {
                    sendEvent(
                        EventConstants.EVENT_HISTORY_SEARCH_NOW,
                        hashMapOf(),
                        ignoreSnowplow = true
                    )
                    showSearchPage(item)
                }
            }
        }

        private fun showAnswer(item: QuestionAskedHistoryDetails.QuestionAskedDetails) {
            if (item.resourceType == "video") {
                binding.root.context.startActivity(
                    VideoPageActivity.startActivity(
                        context = binding.root.context,
                        questionId = item.questionId.toString(), page = "WATCH-HISTORY"
                    )
                )
            } else {
                binding.root.context.startActivity(
                    TextSolutionActivity.startActivity(
                        binding.root.context,
                        item.questionId.toString(),
                        "",
                        "",
                        "WATCH-HISTORY",
                        "",
                        false,
                        "",
                        "",
                        false
                    )
                )
            }
        }

        private fun showSearchPage(item: QuestionAskedHistoryDetails.QuestionAskedDetails) {
            if (item.quesImageUrl == null) {
                binding.root.context.startActivity(
                    MatchQuestionActivity.getStartIntent(
                        context = binding.root.context,
                        quesImageUri = "",
                        questionText = item.questionText,
                        source = "QuestionAskedHistory"
                    )
                )
            } else {
                binding.root.context.startActivity(
                    MatchQuestionActivity.getStartIntent(
                        context = binding.root.context,
                        quesImageUri = "QuestionAskedHistory",
                        questionText = "",
                        source = "QuestionAskedHistory",
                        imageUrl = item.quesImageUrl,
                        uploadedImageQuestionId = item.questionId
                    )
                )
            }
        }

        private fun setImageUrl(thumbnailUrl: String) {
            binding.progressBar.show()
            Glide.with(binding.root.context)
                .load(thumbnailUrl)
                .centerCrop()
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressBar.hide()
                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressBar.hide()
                        return false
                    }

                })
                .into(binding.imgQuestion)
        }
    }

    fun updateList(questionsAskList: List<QuestionAskedHistoryDetails.QuestionAskedDetails>) {
        questionsAskList.filter {
            it.quesImageUrl != null || it.questionText.isNotEmpty()
        }
        val changeStartIndex = questionsAskedList.size
        questionsAskedList.addAll(questionsAskList)
        notifyItemRangeInserted(changeStartIndex, questionsAskList.size)
    }

    fun sendEvent(
        eventName: String,
        params: HashMap<String, Any>,
        ignoreSnowplow: Boolean = false
    ) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                params,
                ignoreSnowplow = ignoreSnowplow
            )
        )
    }
}