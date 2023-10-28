package com.doubtnutapp.doubt.bookmark.widget

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.text.util.Linkify
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.annotation.Keep
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.TextViewUtils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.PlayAudioEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.GetDoubtSolutions
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.ImageViewerActivity
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.Comment
import com.doubtnutapp.databinding.ItemBookmarkSolutionBinding
import com.doubtnutapp.databinding.ItemCommentBookmarkBinding
import com.doubtnutapp.databinding.WidgetBookmarkListBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.liveclass.ui.AudioPlayerActivity
import com.doubtnutapp.model.Video
import com.doubtnutapp.ui.forum.comments.CommentItemViewModel
import com.doubtnutapp.utils.DebouncedOnClickListener
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.video.VideoFragment
import com.doubtnutapp.videoPage.event.VideoPageEventManager
import com.doubtnutapp.videoPage.model.VideoResource
import com.doubtnutapp.videoPage.ui.FullScreenVideoPageActivity
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class BookmarkListWidget
@JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<BookmarkListWidget.WidgetHolder, BookmarkListWidgetModel, WidgetBookmarkListBinding>(
    context, attrs, defStyle
) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var videoPageEventManager: VideoPageEventManager

    var adapter: WidgetLayoutAdapter? = null

    override fun getViewBinding(): WidgetBookmarkListBinding {
        return WidgetBookmarkListBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: BookmarkListWidgetModel
    ): WidgetHolder {
        model.layoutConfig = WidgetLayoutConfig(16, 0, 10, 10)
        super.bindWidget(holder, model)

        val binding = holder.binding

        model.data.color?.let { colorStr ->
            Color.parseColor(colorStr).let { colorInt ->
                binding.cvMain.strokeColor = colorInt
                binding.tvTitle.setTextColor(colorInt)
            }
        }

        binding.tvTitle.text = model.data.title
        binding.tvSubtitle.text = model.data.subtitle
        binding.tvLabel.text = model.data.labelOne

        if (model.data.isExpanded == true) {
            binding.ivExpandCollapse.rotation = 180f
            binding.rvBookmark.show()
        } else {
            binding.ivExpandCollapse.rotation = 0f
            binding.rvBookmark.hide()
        }

        binding.ivExpandCollapse.setOnClickListener {
            val eventName = if (model.data.isExpanded == true) {
                EventConstants.BOOKMARK_CHAPTER_COLLAPSE
            } else {
                EventConstants.BOOKMARK_CHAPTER_EXPAND
            }

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    eventName,
                    hashMapOf(
                        EventConstants.SOURCE to TAG,
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.ASSORTMENT_ID to model.data.assortmentId.orEmpty(),
                        EventConstants.ITEM_POSITION to (model.data.itemPosition ?: -1).toString()
                    )
                )
            )

            if (binding.rvBookmark.isVisible) {
                binding.ivExpandCollapse.animate().rotation(0f)
                binding.rvBookmark.hide()
                model.data.isExpanded = false
            } else {
                binding.ivExpandCollapse.animate().rotation(180f)
                binding.rvBookmark.show()
                model.data.isExpanded = true
            }
        }

        binding.rvBookmark.adapter = BookmarkAdapter(
            context = context,
            data = model.data,
            items = model.data.items ?: ArrayList(),
            deeplinkAction = deeplinkAction,
            analyticsPublisher = analyticsPublisher,
            videoPageEventManager = videoPageEventManager,
            actionPerformer = actionPerformer,
            viewType = BookmarkAdapter.TYPE_DOUBTS
        ) {
            if (model.data.items.isNullOrEmpty()) {
                adapter?.removeWidget(model)
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetBookmarkListBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetBookmarkListBinding>(binding, widget)

    companion object {
        const val TAG = "BookmarkListWidget"
    }
}

@Keep
class BookmarkListWidgetModel :
    WidgetEntityModel<BookmarkListWidgetData, WidgetAction>()

@Keep
data class BookmarkListWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("label_one") val labelOne: String?,
    @SerializedName("items") val items: ArrayList<Comment>?,
    @SerializedName("color") val color: String?,
    @SerializedName("is_expanded") var isExpanded: Boolean?,
    @SerializedName("assortment_id") var assortmentId: String?,
    @SerializedName("item_position") var itemPosition: Int?,
) : WidgetData()

class BookmarkAdapter(
    private val context: Context,
    private val data: BookmarkListWidgetData,
    private val items: ArrayList<Comment>,
    private val deeplinkAction: DeeplinkAction,
    private val analyticsPublisher: AnalyticsPublisher,
    private val videoPageEventManager: VideoPageEventManager,
    private val actionPerformer: ActionPerformer?,
    private val viewType: Int,
    private val onBookmarkRemoved: () -> Unit = {}
) :
    ListAdapter<Comment, RecyclerView.ViewHolder>(
        DIFF_UTILS
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_SOLUTIONS) {
            return SolutionViewHolder(
                ItemBookmarkSolutionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
        return ViewHolder(
            ItemCommentBookmarkBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemViewType(position: Int): Int {
        return viewType
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> holder.bind()
            is SolutionViewHolder -> holder.bind()
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: ItemCommentBookmarkBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.ivProfileImage.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                val comment = items[bindingAdapterPosition]
                FragmentWrapperActivity.userProfile(it.context, comment.studentId, "comment")
            }
            binding.ivBookMark.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                val comment = items[bindingAdapterPosition]
                if (comment.isBookmarked == true) {
                    AlertDialog.Builder(context).apply {
                        setMessage(context.getString(R.string.question_remove_bookmark))
                        setPositiveButton(
                            context.getString(R.string.string_yes)
                        ) { dialog, _ ->
                            dialog.dismiss()
                            toggleBookmark(comment)
                        }
                        setNegativeButton(
                            context.getString(R.string.string_no)
                        ) { dialog, _ ->
                            dialog.dismiss()
                        }
                        show()
                    }
                } else {
                    toggleBookmark(comment)
                }
            }

            binding.tvCommentMessage.setOnLongClickListener {
                showPopUpMenu(it as TextView)
                true
            }

            binding.tvActionOne.setOnClickListener(
                object : DebouncedOnClickListener(minimumInterval = 1000) {
                    override fun onDebouncedClick(v: View?) {
                        if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
                        val comment = items[bindingAdapterPosition]
                        when {
                            comment.isExpanded == true -> {
                                comment.isExpanded = false
                                notifyItemChanged(bindingAdapterPosition)
                            }
                            comment.items == null -> {
                                comment.isExpanded = true
                                actionPerformer?.performAction(GetDoubtSolutions(comment.id))
                            }
                            else -> {
                                comment.isExpanded = true
                                notifyItemChanged(bindingAdapterPosition)
                            }
                        }
                        if (comment.isExpanded == true) {
                            analyticsPublisher.publishEvent(
                                AnalyticsEvent(
                                    EventConstants.BOOKMARK_VIEW_SOLUTION,
                                    hashMapOf(
                                        EventConstants.SOURCE to BookmarkListWidget.TAG,
                                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                                        EventConstants.QUESTION_ID to comment.questionId.orEmpty(),
                                        EventConstants.ID to comment.id,
                                        EventConstants.ENTITY_ID to comment.entityId.orEmpty(),
                                        EventConstants.ASSORTMENT_ID to data.assortmentId.orEmpty(),
                                        EventConstants.SELF_DOUBT to (UserUtil.getStudentId() == comment.studentId).toString()
                                    )
                                )
                            )
                        }
                    }
                }
            )

            listOf(binding.tvActionTwo, binding.ivActionTwo).forEach {
                it.setOnClickListener {
                    if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                    val comment = items[bindingAdapterPosition]
                    deeplinkAction.performAction(context, comment.actionTwoDl)

                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.BOOKMARK_GOTO_CLASS,
                            hashMapOf(
                                EventConstants.SOURCE to BookmarkListWidget.TAG,
                                EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                                EventConstants.QUESTION_ID to comment.questionId.orEmpty(),
                                EventConstants.ID to comment.id,
                                EventConstants.ENTITY_ID to comment.entityId.orEmpty(),
                                EventConstants.ASSORTMENT_ID to data.assortmentId.orEmpty(),
                                EventConstants.SELF_DOUBT to (UserUtil.getStudentId() == comment.studentId).toString()
                            )
                        )
                    )
                }
            }
        }

        private fun toggleBookmark(comment: Comment) {
            comment.isBookmarked = !(comment.isBookmarked ?: return)

            val eventName = if (comment.isBookmarked == true) {
                EventConstants.DOUBT_BOOKMARKED
            } else {
                EventConstants.DOUBT_UNBOOKMARKED
            }

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    eventName,
                    hashMapOf(
                        EventConstants.SOURCE to BookmarkListWidget.TAG,
                        EventConstants.QUESTION_ID to comment.questionId.orEmpty(),
                        EventConstants.ID to comment.id,
                        EventConstants.ENTITY_ID to comment.entityId.orEmpty(),
                        EventConstants.SELF_DOUBT to (UserUtil.getStudentId() == comment.studentId).toString()
                    )
                )
            )

            DataHandler.INSTANCE.commentRepository.bookmark(
                comment.id,
                comment.assortmentId ?: "", "doubt",
            ).applyIoToMainSchedulerOnCompletable()
                .subscribeToCompletable({})
            items.indexOfFirst { it.id == comment.id }
                .takeIf { it >= 0 }
                ?.let {
                    items.removeAt(it)
                    notifyItemRemoved(it)
                    onBookmarkRemoved()
                }
        }

        @SuppressLint("SetTextI18n")
        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            val comment = items[bindingAdapterPosition]
            val viewModel = CommentItemViewModel(
                comment = comment,
                videoPageEventManager = videoPageEventManager,
                source = BookmarkListWidget.TAG,
                analyticsPublisher = analyticsPublisher
            )
            binding.ivProfileImage.loadImage(
                comment.studentAvatar.ifEmptyThenNull(),
                R.drawable.ic_profile_placeholder
            )
            binding.tvUsername.text = comment.studentUsername
            setTime(binding.tvCreatedAt, comment.createdAt)
            setIsSelected(binding.ivBookMark, comment.isBookmarked ?: false)
            TextViewUtils.setTextFromHtml(
                binding.tvCommentMessage,
                comment.message.orEmpty()
            )
            Linkify.addLinks(binding.tvCommentMessage, Linkify.ALL)

            updateCommentUi(
                context = context,
                viewModel = viewModel,
                comment = comment,
                tvViewSolution = binding.tvViewSolution,
                imageViewComment = binding.imageViewComment,
                imageButtonCommentPlay = binding.imageButtonCommentPlay
            )

            if (comment.items == null) {
                binding.rvReplies.hide()
            } else {
                binding.rvReplies.show()
                binding.rvReplies.adapter = BookmarkAdapter(
                    context = context,
                    data = data,
                    items = comment.items ?: ArrayList(),
                    deeplinkAction = deeplinkAction,
                    analyticsPublisher = analyticsPublisher,
                    videoPageEventManager = videoPageEventManager,
                    actionPerformer = actionPerformer,
                    viewType = TYPE_SOLUTIONS
                )
            }

            if (comment.isExpanded == true) {
                binding.tvActionOne.text =
                    "${context.getString(R.string.top_doubt_hide_solution)}(${comment.replyCount})"
            } else {
                binding.tvActionOne.text =
                    "${context.getString(R.string.top_doubt_view_solution)}(${comment.replyCount})"
            }

            binding.tvActionTwo.text = comment.actionTwoText
            binding.tvActionTwo.isVisible = !comment.actionTwoText.isNullOrEmpty()
            binding.ivActionTwo.isVisible = !comment.actionTwoImg.isNullOrEmpty()
            binding.ivActionTwo.loadImage(comment.actionTwoImg.ifEmptyThenNull())

            if (comment.isExpanded == true) {
                binding.rvReplies.show()
            } else {
                binding.rvReplies.hide()
            }
        }
    }

    inner class SolutionViewHolder(val binding: ItemBookmarkSolutionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.ivProfileImage.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                val comment = items[bindingAdapterPosition]
                FragmentWrapperActivity.userProfile(it.context, comment.studentId, "comment")
            }

            binding.tvCommentMessage.setOnLongClickListener {
                showPopUpMenu(it as TextView)
                true
            }
        }

        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            val comment = items[bindingAdapterPosition]
            val viewModel = CommentItemViewModel(
                comment = comment,
                videoPageEventManager = videoPageEventManager,
                source = BookmarkListWidget.TAG,
                analyticsPublisher = analyticsPublisher
            )
            binding.ivProfileImage.loadImage(
                comment.studentAvatar.ifEmptyThenNull(),
                R.drawable.ic_profile_placeholder
            )
            binding.tvUsername.text = comment.studentUsername
            setTime(binding.tvCreatedAt, comment.createdAt)
            TextViewUtils.setTextFromHtml(
                binding.tvCommentMessage,
                comment.message.orEmpty()
            )
            Linkify.addLinks(binding.tvCommentMessage, Linkify.ALL)

            updateCommentUi(
                context = context,
                viewModel = viewModel,
                comment = comment,
                tvViewSolution = binding.tvViewSolution,
                imageViewComment = binding.imageViewComment,
                imageButtonCommentPlay = binding.imageButtonCommentPlay
            )

            if (comment.isAdmin == true && comment.userTag?.isNotEmpty() == true) {
                binding.userTag.visibility = View.VISIBLE
                binding.userTag.text = comment.userTag
                binding.ivBulletImage.visibility = View.VISIBLE
            } else {
                binding.userTag.text = ""
                binding.userTag.visibility = View.GONE
                binding.ivBulletImage.visibility = View.GONE
            }
        }
    }

    private fun updateCommentUi(
        context: Context,
        viewModel: CommentItemViewModel,
        comment: Comment,
        tvViewSolution: TextView,
        imageViewComment: ImageView,
        imageButtonCommentPlay: ImageButton,
    ) {

        imageViewComment.hide()
        imageButtonCommentPlay.hide()
        tvViewSolution.hide()

        when {
            comment.type == "top_doubt_answer_audio" && comment.resourceUrl.isNullOrBlank()
                .not() -> {
                tvViewSolution.show()
                tvViewSolution.setOnClickListener {
                    DoubtnutApp.INSTANCE.bus()?.send(PlayAudioEvent(true))
                    AudioPlayerActivity.getStartIntent(
                        context,
                        comment.resourceUrl.orEmpty()
                    ).apply {
                        context.startActivity(this)
                    }
                }
            }
            comment.type == "top_doubt_answer_text_image" -> {
                if (comment.resourceUrl.isNullOrBlank()) {
                    imageViewComment.hide()
                } else {
                    imageViewComment.show()
                    imageViewComment.loadImageEtx(comment.resourceUrl.orEmpty())
                }
                imageViewComment.setOnClickListener {
                    ImageViewerActivity.getStartIntent(
                        context,
                        comment.resourceUrl.orEmpty()
                    )
                        .apply {
                            context.startActivity(this)
                        }
                }
            }
            comment.type == "top_doubt_answer_video" -> {
                imageViewComment.show()
                imageViewComment.loadImageEtx(comment.resourceUrl.orEmpty())
                imageButtonCommentPlay.show()
                listOf(imageButtonCommentPlay, imageViewComment).forEach {
                    it.setOnClickListener {
                        val videoObj = comment.videoObj ?: return@setOnClickListener
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
            else -> {
                if (comment.image.isNullOrBlank()) {
                    imageViewComment.hide()
                } else {
                    imageViewComment.show()
                    imageViewComment.loadImageEtx(comment.image.orEmpty())
                }
                imageViewComment.setOnClickListener { view ->
                    viewModel.onCommentImageClicked(view)
                }
                if (!comment.questionId.isNullOrBlank()) imageButtonCommentPlay.visibility =
                    View.VISIBLE else imageButtonCommentPlay.visibility = View.GONE

                imageButtonCommentPlay.setOnClickListener {
                    viewModel.onCommentPlayImageClicked(it)
                }
            }
        }
    }

    private fun showPopUpMenu(anchor: TextView) {
        val popupMenu = PopupMenu(anchor.context, anchor)
        val menu = R.menu.menu_copytext
        popupMenu.inflate(menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            copyText(anchor.context, anchor.text.toString())
            true
        }
    }

    private fun copyText(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText("", text)
        clipboard?.setPrimaryClip(clip)
    }

    companion object {
        const val TYPE_DOUBTS = 1
        const val TYPE_SOLUTIONS = 2

        val DIFF_UTILS = object : DiffUtil.ItemCallback<Comment>() {
            override fun areContentsTheSame(
                oldItem: Comment,
                newItem: Comment
            ) =
                oldItem == newItem

            override fun areItemsTheSame(
                oldItem: Comment,
                newItem: Comment
            ) =
                oldItem.id == newItem.id
        }
    }
}
