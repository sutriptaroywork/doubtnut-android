package com.doubtnutapp.ui.forum.comments

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableField
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.DnException
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.dpToPxFloat
import com.doubtnut.core.utils.toO1
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.Comment
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.doubt.bookmark.data.entity.BookmarkResponse
import com.doubtnutapp.ui.mediahelper.MediaPlayerHelper
import com.doubtnutapp.utils.BranchIOUtils
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentClass
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.videoPage.event.VideoPageEventManager
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.json.JSONObject

class CommentItemViewModel(
    val comment: Comment,
    private val videoPageEventManager: VideoPageEventManager,
    private val decorView: View? = null,
    private val source: String? = null,
    private val analyticsPublisher: AnalyticsPublisher,
    private val showBookmarkView: (BookmarkResponse) -> Unit = {},
    private val assortmentId: String? = "",
) : MediaPlayerHelper.MediaCurrentPositionListener, MediaPlayerHelper.OnCompleteListener {

    val image = ObservableField(comment.image)

    val studentUserName = ObservableField(comment.studentUsername)

    val message = ObservableField(comment.message)

    val createdAt = ObservableField(comment.createdAt)
    val audio = ObservableField(comment.audio)

    val isLiked = ObservableField(comment.isLiked)
    val isBookmarked = ObservableField(comment.isBookmarked)

    val likeCount = ObservableField(comment.likeCount)
    val replyCount = ObservableField(comment.replyCount)

    val studentAvatar = ObservableField(comment.studentAvatar)

    val isPlaying = ObservableField(false)

    val progress = ObservableField(0)
    val max = ObservableField(0)

    @Suppress("SameParameterValue")
    fun onPlayButtonClicked(view: View) {
        isPlaying.set(true)
        comment.audio?.let {

            try {
                MediaPlayerHelper.start(it)
                MediaPlayerHelper.setOnCurrentPositionChangeListener(this)
                MediaPlayerHelper.setOnCompleteListener(this)
                max.set(MediaPlayerHelper.getMediaDuration() / 1000)
            } catch (ex: Exception) {
                ToastUtils.makeText(
                    view.context,
                    R.string.string_someErrorOccured,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun onStopButtonClicked() {
        stopMediaPlayer()
    }

    private fun stopMediaPlayer() {
        isPlaying.set(false)
        MediaPlayerHelper.stop()
    }

    override fun onComplete() {
        isPlaying.set(false)
        progress.set(max.get())

        //set to zero after delay
        Handler().postDelayed({
            progress.set(0)
        }, 200)
    }

    fun onUserChangeSeekBar(newPosition: Int) {
        MediaPlayerHelper.seekTo(newPosition)
    }

    override fun onCurrentPositionChange(currentPosition: Int) {
        progress.set(currentPosition / 1000)
    }

    fun onBookmarkClicked(view: View) {
        if (NetworkUtils.isConnected(view.context).not()) {
            ToastUtils.makeText(
                view.context,
                R.string.string_noInternetConnection,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val commentsActivity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.context as? AppCompatActivity
        } else {
            view.rootView.context as? AppCompatActivity
        } ?: return

        setBookmarkClicked()

        val eventName = if (comment.isBookmarked == true) {
            EventConstants.DOUBT_BOOKMARKED
        } else {
            EventConstants.DOUBT_UNBOOKMARKED
        }

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                hashMapOf(
                    EventConstants.SOURCE to (source ?: "CommentActivity"),
                    EventConstants.QUESTION_ID to comment.questionId.orEmpty(),
                    EventConstants.STUDENT_ID to getStudentId(),
                    EventConstants.ID to comment.id,
                    EventConstants.ENTITY_ID to comment.entityId.orEmpty(),
                    EventConstants.SELF_DOUBT to (getStudentId() == comment.studentId).toString()
                )
            )
        )

        DataHandler.INSTANCE.commentRepository.bookmarkComment(
            comment.id,
            assortmentId ?: "",
            "doubt"
        )
            .observe(commentsActivity, {
                when (it) {
                    is Outcome.Success -> {
                        if (comment.isBookmarked == true) {
                            if (defaultPrefs().getBoolean(
                                    Constants.IS_ACCESS_DOUBT_BOOKMARK_UI_SHOWN,
                                    false
                                )
                            ) {
                                showSnackbarMessage(
                                    commentsActivity,
                                    it.data.data.message
                                        ?: commentsActivity.getString(R.string.your_doubt_is_bookmarked)
                                )
                            } else {
                                showBookmarkView(it.data.data)
                            }
                        }
                    }
                    is Outcome.Failure -> {
                        onError(commentsActivity, true)
                    }

                    is Outcome.ApiError -> {
                        onError(commentsActivity, true)
                    }

                    is Outcome.BadRequest -> {
                        onError(commentsActivity, true)
                    }
                    else -> {
                    }
                }
            })
    }

    fun showSnackbarMessage(activty: AppCompatActivity, message: String) {
        Snackbar.make(
            decorView ?: activty.findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        ).apply {
            view.layoutParams = view.layoutParams.apply {
//                height = 37.dpToPx()
                if (this is ViewGroup.MarginLayoutParams) {
                    setMargins(33.dpToPx(), topMargin, 33.dpToPx(), bottomMargin)
                }
            }
            this.view.background =
                AppCompatResources.getDrawable(
                    activty,
                    R.drawable.bg_rounded_corner_white_fill_80dp
                )
            this.view.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#b9000000"))
            val textView =
                this.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            } else {
                textView.gravity = Gravity.CENTER_HORIZONTAL
            }
            textView.setTypeface(textView.typeface, Typeface.BOLD)
            textView.setTextColor(ContextCompat.getColor(activty, R.color.white))
            view.translationY = (-156).dpToPxFloat()
            show()
        }
    }

    fun onLikeClicked(view: View) {

        !view.isSelected
        if (NetworkUtils.isConnected(view.context).not()) {
            ToastUtils.makeText(
                view.context,
                R.string.string_noInternetConnection,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        setCommentClicked()

        val commentsActivity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.context as? AppCompatActivity
        } else {
            with(FirebaseCrashlytics.getInstance()) {
                log("view.context: ${view.context}")
                log("view.rootView.context: ${view.rootView.context}")
                recordException(DnException("NPE is getting activity instance"))
            }
            view.rootView.context as? AppCompatActivity
        } ?: return


        DataHandler.INSTANCE.commentRepository.likeComment(getLikeRequestBody())
            .observe(commentsActivity, {
                when (it) {
                    is Outcome.Failure -> {
                        onError(commentsActivity, false)
                    }

                    is Outcome.ApiError -> {
                        onError(commentsActivity, false)
                    }

                    is Outcome.BadRequest -> {
                        onError(commentsActivity, false)
                    }
                    else -> {
                    }
                }
            })
    }

    fun onCommentImageClicked(view: View) {
        if (comment.questionId.isNullOrBlank()) {
            val intent = Intent(view.context, CommentImageFullView::class.java)
            intent.putExtra(CommentImageFullView.INTENT_EXTRA_COMMENT_IMAGE_URL, comment.image)
            view.context.startActivity(intent)
            sendEvent(EventConstants.EVENT_NAME_COMMENT_ITEM_FULL_VIEW, view.context)
        } else {
            onCommentPlayImageClicked(view)
        }
    }

    fun onCommentPlayImageClicked(view: View) {
        videoPageEventManager.playVideoClick(
            comment.questionId.orEmpty(),
            source ?: "CommentActivity"
        )
        val intent = VideoPageActivity.startActivity(
            context = view.context,
            questionId = comment.questionId.orEmpty(),
            playlistId = "",
            mcId = "",
            page = Constants.PAGE_COMMUNITY,
            mcClass = "",
            isMicroConcept = false,
            referredStudentId = "",
            parentId = "",
            fromNotificationVideo = false
        )
        intent.putExtra(CommentImageFullView.INTENT_EXTRA_COMMENT_IMAGE_URL, comment.image)
        sendEventByQID(
            EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
            comment.questionId.orEmpty(),
            view.context
        )

        // Send this event to Branch
//        BranchIOUtils
//            .userCompletedAction(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, JSONObject().apply {
//                put(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, comment.questionId)
//            })

        sendClevertapEventByQID(
            EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
            comment.questionId.orEmpty(),
            view.context,
            Constants.PAGE_COMMUNITY
        )
        view.context.startActivity(intent)
    }

    private fun onError(context: Context, isBookmarked: Boolean) {
        ToastUtils.makeText(
            context,
            context.getString(R.string.somethingWentWrong),
            Toast.LENGTH_SHORT
        ).show()
        if (isBookmarked) {
            revertBookmark()
        } else {
            revertLike()
        }
    }

    private fun revertLike() {
        comment.isLiked = 1 - comment.isLiked
        isLiked.set(comment.isLiked)
    }

    private fun revertBookmark() {
        comment.isBookmarked = (comment.isBookmarked ?: false).not()
        isBookmarked.set(comment.isBookmarked)
    }

    private fun setCommentClicked() {
        comment.isLiked = 1 - comment.isLiked
        isLiked.set(comment.isLiked)
        if (comment.isLiked.toBoolean()) comment.likeCount = (comment.likeCount ?: 0) + 1
        else comment.likeCount = (comment.likeCount ?: 1) - 1
        likeCount.set(comment.likeCount ?: 0)
    }

    private fun setBookmarkClicked() {
        comment.isBookmarked = (comment.isBookmarked ?: false).not()
        isBookmarked.set(comment.isBookmarked)
    }

    private fun getLikeRequestBody() = hashMapOf(
        "comment_id" to comment.id,
        "is_like" to comment.isLiked
    ).toRequestBody()

    private fun getBookmarkRequestBody() = hashMapOf(
        "resource_id" to comment.id,
        "bookmark" to (comment.isBookmarked ?: false).toO1(),
        "type" to "doubt",
    ).toRequestBody()

    private fun sendEventByQID(
        @Suppress("SameParameterValue") eventName: String,
        qid: String,
        context: Context
    ) {
        (context.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(context).toString())
            .addStudentId(getStudentId())
            .addScreenName(EventConstants.PAGE_COMMENT_ACTIVITY)
            .addEventParameter(Constants.QUESTION_ID, qid)
            .track()
    }

    private fun sendClevertapEventByQID(
        @Suppress("SameParameterValue") eventName: String,
        qid: String,
        context: Context,
        @Suppress("SameParameterValue") page: String
    ) {
        (context.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(context).toString())
            .addStudentId(getStudentId())
            .addScreenName(EventConstants.PAGE_COMMENT_ACTIVITY)
            .addEventParameter(Constants.QUESTION_ID, qid)
            .addEventParameter(Constants.PAGE, page)
            .addEventParameter(Constants.STUDENT_CLASS, getStudentClass())
            .cleverTapTrack()
    }

    private fun sendEvent(@Suppress("SameParameterValue") eventName: String, context: Context) {
        (context.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(context).toString())
            .addStudentId(getStudentId())
            .addScreenName(EventConstants.PAGE_COMMENT_ACTIVITY)
            .track()

    }
}