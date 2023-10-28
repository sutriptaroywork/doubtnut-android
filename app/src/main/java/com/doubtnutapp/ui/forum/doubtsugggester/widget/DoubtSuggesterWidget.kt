package com.doubtnutapp.ui.forum.doubtsugggester.widget

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.authToken
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.ApiVideoObj
import com.doubtnutapp.databinding.SuggestedDoubtItemBinding
import com.doubtnutapp.databinding.WidgetDoubtSuggesterBinding
import com.doubtnutapp.model.Video
import com.doubtnutapp.utils.NetworkUtil
import com.doubtnutapp.video.VideoFragment
import com.doubtnutapp.videoPage.model.VideoResource
import com.doubtnutapp.videoPage.ui.FullScreenVideoPageActivity
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class DoubtSuggesterWidget(context: Context) : BaseBindingWidget<DoubtSuggesterWidget.WidgetHolder,
        DoubtSuggesterWidget.DoubtSuggesterWidgetModel, WidgetDoubtSuggesterBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var networkUtil: NetworkUtil

    private object DoubtSuggesterConstants {
        const val TAG = "DoubtSuggesterWidget"
        const val EVENT_TAG = "doubt_suggester_widget"
        const val TYPE_TEXT = "text"
        const val TYPE_IMAGE = "image"
        const val TYPE_VIDEO = "video"
        const val TYPE_AUDIO = "audio"
        const val BOOKMARK_YES = "bookmark"
        const val BOOKMARK_REMOVE = "bookmark_remove"
        const val YES = "yes"
        const val NO = "no"
        const val UNDO = "undo"
        const val FEEDBACK_NOT_RECORDED = "not_recorded"
        const val FEEDBACK_RECORDED = "recorded"
        const val BOOKMARK = "bookmark"
    }

    companion object {
        var entityType: String = ""
        var entityId: String = ""
        var detailId: String = ""
        var message: String = ""
        var offset: String = ""
        var batchId: String = ""
        var doubtMsg: String = ""
        var qid: String = ""
        var assortmentId: String = ""
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetDoubtSuggesterBinding {
        return WidgetDoubtSuggesterBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: DoubtSuggesterWidgetModel): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
        })
        val data: DoubtSuggesterWidgetData = model.data
        val binding = holder.binding

        binding.apply {
            tvTitle.text = data.title.orEmpty()
            tvTitle.applyTextColor(data.titleColor)
            tvTitle.applyTextSize(data.titleSize)

            rvSuggestedDoubts.layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            rvSuggestedDoubts.adapter = DoubtSuggesterAdapter(
                data,
                data.items?.filterNot {
                    it.type == DoubtSuggesterConstants.TYPE_AUDIO
                } ?: emptyList(),
                actionPerformer, analyticsPublisher, model.extraParams ?: HashMap()
            )
        }

        return holder
    }

    inner class DoubtSuggesterAdapter(
        val data: DoubtSuggesterWidgetData,
        val items: List<DoubtSuggesterWidgetItem>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val extraParams: HashMap<String, Any>
    ) : RecyclerView.Adapter<DoubtSuggesterAdapter.DoubtSuggesterViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): DoubtSuggesterViewHolder {
            return DoubtSuggesterViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.suggested_doubt_item, parent, false)
            )
        }

        override fun onBindViewHolder(holder: DoubtSuggesterViewHolder, position: Int) {
            holder.binding.apply {
                if (items.isEmpty())
                    return

                val doubt = items[position]
                doubt.feedbackState = DoubtSuggesterConstants.FEEDBACK_NOT_RECORDED

                if (doubt.feedbackRecordedMsg.isNotNullAndNotEmpty2()) {
                    doubt.feedbackState = DoubtSuggesterConstants.FEEDBACK_RECORDED
                }

                doubt.title?.let { tvTitle.setTextFromHtml(it) }
                tvTitle.applyTextColor(doubt.titleColor)
                tvTitle.applyTextSize(doubt.titleSize)

                if (doubt.isBookmarked == true)
                    ivBookmark.loadImage2(data.bookmarkedUrl)
                else
                    ivBookmark.loadImage2(data.notBookmarkedUrl)

                expandCollapseDoubt(
                    doubt.isExpanded ?: false,
                    holder.binding,
                    doubt,
                    doubt.feedbackState.toString(),
                    doubt.feedbackStateType
                )

                tvTitle.setOnClickListener {
                    doubt.isExpanded = doubt.isExpanded?.not()
                    expandCollapseDoubt(
                        doubt.isExpanded ?: false,
                        holder.binding,
                        doubt,
                        doubt.feedbackState.toString(),
                        doubt.feedbackStateType
                    )
                }

                ivArrow.setOnClickListener {
                    doubt.isExpanded = doubt.isExpanded?.not()
                    expandCollapseDoubt(
                        doubt.isExpanded ?: false,
                        holder.binding,
                        doubt,
                        doubt.feedbackState.toString(),
                        doubt.feedbackStateType
                    )
                }

                ivBookmark.setOnClickListener {
                    if (networkUtil.isConnectedWithMessage()) {
                        doubt.isBookmarked = doubt.isBookmarked?.not()
                        if (doubt.isBookmarked == true) {
                            sendFeedbackOrBookmark(
                                doubtSuggesterResponse = DoubtSuggesterConstants.BOOKMARK_YES,
                                originalCommentId = doubt.originalCommentId,
                                binding = holder.binding,
                                doubt = doubt
                            )
                            ivBookmark.loadImage2(data.bookmarkedUrl)
                            analyticsPublisher.publishEvent(
                                AnalyticsEvent(
                                    "${DoubtSuggesterConstants.EVENT_TAG}_${CoreEventConstants.SUGGESTED_DOUBT_BOOKMARK}",
                                    hashMapOf(
                                        DoubtSuggesterConstants.BOOKMARK to DoubtSuggesterConstants.BOOKMARK_YES,
                                        CoreEventConstants.QUESTION_ID to qid,
                                        CoreEventConstants.ITEM_POSITION to position.toString()
                                    ),
                                    ignoreSnowplow = true
                                )
                            )
                        } else {
                            sendFeedbackOrBookmark(
                                doubtSuggesterResponse = DoubtSuggesterConstants.BOOKMARK_REMOVE,
                                originalCommentId = doubt.originalCommentId,
                                binding = holder.binding,
                                doubt = doubt
                            )
                            ivBookmark.loadImage2(data.notBookmarkedUrl)
                            analyticsPublisher.publishEvent(
                                AnalyticsEvent(
                                    "${DoubtSuggesterConstants.EVENT_TAG}_${CoreEventConstants.SUGGESTED_DOUBT_BOOKMARK}",
                                    hashMapOf(
                                        DoubtSuggesterConstants.BOOKMARK to DoubtSuggesterConstants.BOOKMARK_REMOVE,
                                        CoreEventConstants.QUESTION_ID to qid,
                                        CoreEventConstants.ITEM_POSITION to position.toString()
                                    ),
                                    ignoreSnowplow = true
                                )
                            )
                        }
                    } else {
                        ToastUtils.makeText(
                            context,
                            R.string.string_noInternetConnection,
                            Toast.LENGTH_SHORT
                        )
                    }
                }
                ivYes.setOnClickListener {
                    if (networkUtil.isConnectedWithMessage()) {
                        doubt.feedbackState = DoubtSuggesterConstants.FEEDBACK_RECORDED
                        doubt.feedbackStateType = true
                        sendFeedbackOrBookmark(
                            doubtSuggesterResponse = DoubtSuggesterConstants.YES,
                            originalCommentId = doubt.originalCommentId,
                            binding = holder.binding,
                            doubt = doubt
                        )
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                "${DoubtSuggesterConstants.EVENT_TAG}_${CoreEventConstants.FEEDBACK_YES}",
                                hashMapOf(
                                    DoubtSuggesterConstants.FEEDBACK_RECORDED to DoubtSuggesterConstants.YES,
                                    CoreEventConstants.QUESTION_ID to qid,
                                    CoreEventConstants.ITEM_POSITION to position.toString()
                                ),
                                ignoreSnowplow = true
                            )
                        )
                    }

                }
                tvYes.setOnClickListener {
                    if (networkUtil.isConnectedWithMessage()) {
                        doubt.feedbackState = DoubtSuggesterConstants.FEEDBACK_RECORDED
                        doubt.feedbackStateType = true
                        sendFeedbackOrBookmark(
                            doubtSuggesterResponse = DoubtSuggesterConstants.YES,
                            originalCommentId = doubt.originalCommentId,
                            binding = holder.binding,
                            doubt = doubt
                        )
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                "${DoubtSuggesterConstants.EVENT_TAG}_${CoreEventConstants.FEEDBACK_YES}",
                                hashMapOf(
                                    DoubtSuggesterConstants.FEEDBACK_RECORDED to DoubtSuggesterConstants.YES,
                                    CoreEventConstants.QUESTION_ID to qid,
                                    CoreEventConstants.ITEM_POSITION to position.toString()
                                ),
                                ignoreSnowplow = true
                            )
                        )
                    }

                }
                ivNo.setOnClickListener {
                    if (networkUtil.isConnectedWithMessage()) {
                        doubt.feedbackState = DoubtSuggesterConstants.FEEDBACK_RECORDED
                        doubt.feedbackStateType = false
                        sendFeedbackOrBookmark(
                            doubtSuggesterResponse = DoubtSuggesterConstants.NO,
                            originalCommentId = doubt.originalCommentId,
                            binding = holder.binding,
                            doubt = doubt
                        )
                        AnalyticsEvent(
                            "${DoubtSuggesterConstants.EVENT_TAG}_${CoreEventConstants.FEEDBACK_NO}",
                            hashMapOf(
                                DoubtSuggesterConstants.FEEDBACK_RECORDED to DoubtSuggesterConstants.NO,
                                CoreEventConstants.QUESTION_ID to qid,
                                CoreEventConstants.ITEM_POSITION to position.toString()
                            ),
                            ignoreSnowplow = true
                        )
                    }

                }
                tvNo.setOnClickListener {
                    if (networkUtil.isConnectedWithMessage()) {
                        doubt.feedbackState = DoubtSuggesterConstants.FEEDBACK_RECORDED
                        doubt.feedbackStateType = false
                        sendFeedbackOrBookmark(
                            doubtSuggesterResponse = DoubtSuggesterConstants.NO,
                            originalCommentId = doubt.originalCommentId,
                            binding = holder.binding,
                            doubt = doubt
                        )
                        AnalyticsEvent(
                            "${DoubtSuggesterConstants.EVENT_TAG}_${CoreEventConstants.FEEDBACK_NO}",
                            hashMapOf(
                                DoubtSuggesterConstants.FEEDBACK_RECORDED to DoubtSuggesterConstants.NO,
                                CoreEventConstants.QUESTION_ID to qid,
                                CoreEventConstants.ITEM_POSITION to position.toString()
                            ),
                            ignoreSnowplow = true
                        )
                    }
                }
                tvUndo.setOnClickListener {
                    if (networkUtil.isConnectedWithMessage()) {
                        doubt.feedbackState = DoubtSuggesterConstants.FEEDBACK_NOT_RECORDED
                        doubt.feedbackStateType = null
                        sendFeedbackOrBookmark(
                            doubtSuggesterResponse = DoubtSuggesterConstants.UNDO,
                            originalCommentId = doubt.originalCommentId,
                            binding = holder.binding,
                            doubt = doubt
                        )
                        AnalyticsEvent(
                            "${DoubtSuggesterConstants.EVENT_TAG}_${CoreEventConstants.FEEDBACK_UNDO}",
                            hashMapOf(
                                CoreEventConstants.CLICKED to DoubtSuggesterConstants.UNDO,
                            ),
                            ignoreSnowplow = true
                        )
                    }
                }
                listOf(ivPlayIcon, ivThumbnail).forEach {
                    it.setOnClickListener {
                        val videoObj = doubt.answer?.videoObj ?: return@setOnClickListener
                        val video = Video(
                            videoObj.questionId,
                            true,
                            videoObj.viewId,
                            videoObj.resources?.map { videoResource ->
                                VideoResource(
                                    resource = videoResource.resource,
                                    drmScheme = videoResource.drmScheme,
                                    drmLicenseUrl = videoResource.drmLicenseUrl,
                                    mediaType = videoResource.mediaType,
                                    isPlayed = false,
                                    dropDownList = null,
                                    timeShiftResource = null,
                                    offset = videoResource.offset
                                )
                            },
                            0,
                            videoObj.page,
                            false,
                            VideoFragment.DEFAULT_ASPECT_RATIO
                        )
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                "${DoubtSuggesterConstants.EVENT_TAG}_${CoreEventConstants.SUGGESTED_DOUBT_VIEWED}",
                                hashMapOf(
                                    CoreEventConstants.TYPE to doubt.type.toString()
                                ),
                                ignoreSnowplow = true
                            )
                        )
                        FullScreenVideoPageActivity.startActivity(
                            context,
                            video,
                            true
                        )
                            .apply {
                                context.startActivity(this)
                            }
                    }
                }
            }
        }

        private fun expandCollapseDoubt(
            isExpanded: Boolean,
            binding: SuggestedDoubtItemBinding,
            doubt: DoubtSuggesterWidgetItem,
            feedbackState: String,
            feedbackStateType: Boolean?
        ) {
            binding.apply {
                if (isExpanded) {
                    ivArrow.setImageResource(R.drawable.ic_arrow_up_24px)
                    tvDoubtAnswer.text = doubt.answer?.originalMessage.orEmpty()
                    doubtContentView.visibility = VISIBLE
                    layoutFeedback.visibility = VISIBLE
                    if (feedbackState == DoubtSuggesterConstants.FEEDBACK_RECORDED) {
                        layoutFeedbackRecorded.visibility = VISIBLE
                        layoutChooseOption.visibility = GONE
                        if (feedbackStateType == true) {
                            tvCorrectOrIncorrect.text = data.correctText
                            ivCorrectOrIncorrect.loadImage2(data.correctIcon)
                            tvUndo.text = doubt.undoText.orEmpty()
                        } else {
                            tvCorrectOrIncorrect.text = data.incorrectText
                            ivCorrectOrIncorrect.loadImage2(data.incorrectIcon)
                            tvUndo.text = doubt.undoText.orEmpty()
                        }
                    } else {
                        layoutChooseOption.visibility = VISIBLE
                        layoutFeedbackRecorded.visibility = GONE
                        tvIsThisCorrect.text = data.isThisCorrectText.orEmpty()
                        ivYes.loadImage2(data.iconTick)
                        ivNo.loadImage2(data.iconCross)
                    }
                    when (doubt.type) {
                        DoubtSuggesterConstants.TYPE_TEXT -> {
                            ivThumbnail.visibility = GONE
                        }
                        DoubtSuggesterConstants.TYPE_IMAGE -> {
                            ivThumbnail.visibility = VISIBLE
                            ivThumbnail.loadImage2(doubt.answer?.imageUrl)
                        }
                        DoubtSuggesterConstants.TYPE_VIDEO -> {
                            ivThumbnail.visibility = VISIBLE
                            ivPlayIcon.visibility = VISIBLE
                            ivThumbnail.loadImage2(doubt.answer?.imageUrl)
                            ivPlayIcon.loadImage2(data.playIconUrl)
                        }
                    }
                } else {
                    ivArrow.setImageResource(R.drawable.ic_arrow_down_24px)
                    doubtContentView.visibility = GONE
                }
            }

        }

        private fun sendFeedbackOrBookmark(
            doubtSuggesterResponse: String?,
            originalCommentId: String?,
            binding: SuggestedDoubtItemBinding,
            doubt: DoubtSuggesterWidgetItem
        ) {

            DataHandler.INSTANCE.commentRepository.addComment(
                doubtSuggesterResponse = doubtSuggesterResponse,
                originalCommentId = originalCommentId,
                token = authToken(DoubtnutApp.INSTANCE),
                entityType = entityType,
                entityId = entityId,
                detailId = detailId,
                message = doubtMsg,
                imageFileMultiPartRequestBody = null,
                audioFileMultiPartRequestBody = null,
                isDoubt = "1", //value always 1 as it is a doubt
                offset = offset,
                batchId = batchId,
                assortmentId = assortmentId
            ).observe(
                context as? AppCompatActivity ?: return
            ) {
                when (it) {
                    is Outcome.Success -> {
                        binding.apply {
                            when (doubtSuggesterResponse) {
                                DoubtSuggesterConstants.YES -> {
                                    val response = it.data.data
                                    layoutChooseOption.visibility = GONE
                                    layoutFeedbackRecorded.visibility = VISIBLE
                                    tvCorrectOrIncorrect.text = response.reply.orEmpty()
                                    ivCorrectOrIncorrect.loadImage2(response.iconUrl)
                                    tvUndo.text = response.undoText.orEmpty()
                                    doubt.feedbackState = DoubtSuggesterConstants.FEEDBACK_RECORDED
                                    doubt.feedbackStateType = true
                                }
                                DoubtSuggesterConstants.NO -> {
                                    val response = it.data.data
                                    layoutChooseOption.visibility = GONE
                                    layoutFeedbackRecorded.visibility = VISIBLE
                                    tvCorrectOrIncorrect.text = response.reply.orEmpty()
                                    ivCorrectOrIncorrect.loadImage2(response.iconUrl)
                                    tvUndo.text = response.undoText.orEmpty()
                                    doubt.feedbackState = DoubtSuggesterConstants.FEEDBACK_RECORDED
                                    doubt.feedbackStateType = false
                                }

                                DoubtSuggesterConstants.BOOKMARK_YES, DoubtSuggesterConstants.BOOKMARK_REMOVE -> {
                                    // no message needs to be displayed
                                    // on frontend
                                }
                                DoubtSuggesterConstants.UNDO -> {
                                    layoutFeedbackRecorded.visibility = GONE
                                    layoutChooseOption.visibility = VISIBLE
                                    ivYes.loadImage2(data.iconTick)
                                    ivNo.loadImage2(data.iconCross)
                                    tvIsThisCorrect.text = data.isThisCorrectText.orEmpty()
                                    doubt.feedbackState =
                                        DoubtSuggesterConstants.FEEDBACK_NOT_RECORDED
                                    doubt.feedbackStateType = null
                                }
                            }
                        }

                    }
                    is Outcome.Failure -> {
                        Log.e(DoubtSuggesterConstants.TAG, it.e.toString())
                    }
                    is Outcome.ApiError -> {
                        Log.e(DoubtSuggesterConstants.TAG, it.e.toString())
                    }
                    is Outcome.BadRequest -> {
                        Log.e(DoubtSuggesterConstants.TAG, it.data)
                    }
                    is Outcome.Progress -> {
                    }
                }
            }
        }

        override fun getItemCount(): Int =
            data.items?.filterNot { it.type == DoubtSuggesterConstants.TYPE_AUDIO }?.size!!

        inner class DoubtSuggesterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val binding = SuggestedDoubtItemBinding.bind(itemView)
        }
    }

    class WidgetHolder(binding: WidgetDoubtSuggesterBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetDoubtSuggesterBinding>(binding, widget)

    class DoubtSuggesterWidgetModel : WidgetEntityModel<DoubtSuggesterWidgetData, WidgetAction>()

    @Keep
    data class DoubtSuggesterWidgetData(
        @SerializedName("title") val title: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("title_size") val titleSize: String?,
        @SerializedName("not_bookmarked_icon") val notBookmarkedUrl: String?,
        @SerializedName("bookmarked_icon") val bookmarkedUrl: String?,
        @SerializedName("is_this_correct_text") val isThisCorrectText: String?,
        @SerializedName("tick_icon_url") val iconTick: String?,
        @SerializedName("cross_icon_url") val iconCross: String?,
        @SerializedName("play_icon_url") val playIconUrl: String?,
        @SerializedName("items") val items: List<DoubtSuggesterWidgetItem>?,
        @SerializedName("correct_text") val correctText: String?,
        @SerializedName("incorrect_text") val incorrectText: String?,
        @SerializedName("correct_icon") val correctIcon: String?,
        @SerializedName("incorrect_icon") val incorrectIcon: String?
    ) : WidgetData()

    @Keep
    data class DoubtSuggesterWidgetItem(
        @SerializedName("type") val type: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("title_size") val titleSize: String?,
        @SerializedName("is_expand") var isExpanded: Boolean?,
        @SerializedName("is_bookmarked") var isBookmarked: Boolean?,
        @SerializedName("answer") val answer: SuggestedDoubtAnswer?,
        @SerializedName("original_comment_id") val originalCommentId: String?,
        @SerializedName("reply") val feedbackRecordedMsg: String?,
        @SerializedName("icon_url") val feedbackIconUrl: String?,
        @SerializedName("undo_text") val undoText: String?,
        @SerializedName("feedback_state") var feedbackState: String?,
        @SerializedName("feedback_state_type") var feedbackStateType: Boolean?,
    ) : WidgetData()

    @Keep
    data class SuggestedDoubtAnswer(
        @SerializedName("image") val imageUrl: String?,
        @SerializedName("audio") val audioUrl: String?,
        @SerializedName("original_message") val originalMessage: String?,
        @SerializedName("question_id") val qid: String?,
        @SerializedName("video_obj") val videoObj: ApiVideoObj?
    ) : WidgetData()
}





