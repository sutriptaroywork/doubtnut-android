package com.doubtnutapp.ui.groupChat

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.lifecycle.Observer
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.addEventNames
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.Comment
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.ui.forum.comments.CommentImageFullView
import com.doubtnutapp.ui.mediahelper.MediaPlayerHelper
import com.doubtnutapp.utils.BranchIOUtils
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentClass
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import org.json.JSONObject

class LiveChatItemViewModel(val comment: Comment) : MediaPlayerHelper.MediaCurrentPositionListener, MediaPlayerHelper.OnCompleteListener {


    val image = ObservableField(comment.image)

    val studentUserName = ObservableField(comment.studentUsername)

    val message = ObservableField(comment.message)

    val createdAt = ObservableField(comment.createdAt)
    val audio = ObservableField(comment.audio)

    val isLiked = ObservableField(comment.isLiked)

    val studentAvatar = ObservableField(comment.studentAvatar)

    val isPlaying = ObservableField(false)

    val progress = ObservableField(0)
    val max = ObservableField(0)

   

    fun onPlayButtonClicked(view: View) {
        isPlaying.set(true)
        comment.audio?.let {

            try {
                MediaPlayerHelper.start(it)
                MediaPlayerHelper.setOnCurrentPositionChangeListener(this)
                MediaPlayerHelper.setOnCompleteListener(this)
                max.set(MediaPlayerHelper.getMediaDuration() / 1000)
            } catch (ex: Exception) {
                ToastUtils.makeText(view.context, R.string.string_someErrorOccured, Toast.LENGTH_SHORT).show()
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

    fun onLikeClicked(view: View) {

        if (NetworkUtils.isConnected(view.context).not()) {
            ToastUtils.makeText(view.context, R.string.string_noInternetConnection, Toast.LENGTH_SHORT).show()
            return
        }

        setCommentClicked()

       val commentsActivity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
           view.context as? AppCompatActivity
        }else{
           view.rootView.context as? AppCompatActivity
       }
        DataHandler.INSTANCE.commentRepository.likeComment(getLikeRequestBody()).observe(commentsActivity!!, Observer {
            when (it) {
                is Outcome.Failure -> {
                    onError(commentsActivity)
                }

                is Outcome.ApiError -> {
                    onError(commentsActivity)
                }

                is Outcome.BadRequest -> {
                    onError(commentsActivity)
                }
            }
        })
    }

    fun onCommentImageClicked(view: View) {
        if(comment.questionId.isNullOrBlank()) {
            val intent=Intent(view.context, CommentImageFullView::class.java)
            intent.putExtra(CommentImageFullView.INTENT_EXTRA_COMMENT_IMAGE_URL, comment.image)
            view.context.startActivity(intent)
            sendEvent(EventConstants.EVENT_NAME_COMMENT_ITEM_FULL_VIEW, view.context)

        }
        else{
            onCommentPlayImageClicked(view)
        }
    }

    fun onCommentPlayImageClicked(view: View) {

        val intent = VideoPageActivity.startActivity(
            view?.context,
            comment.questionId!!,
            "",
            "",
            Constants.PAGE_COMMUNITY,
            "",
            false,
            "",
            "",
            false
        )
        intent.putExtra(CommentImageFullView.INTENT_EXTRA_COMMENT_IMAGE_URL, comment.image)
        sendEventByQID(
            EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
            comment.questionId,
            view?.context
        )

        // Send this event to Branch
//        BranchIOUtils.userCompletedAction(
//            EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
//            JSONObject().apply {
//                put(EventConstants.QUESTION_ID, comment.questionId)
//            })

        sendCleverTapEventByQID(
            EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
            comment.questionId,
            view?.context,
            Constants.PAGE_COMMUNITY
        )
        view?.context.startActivity(intent)

    }

    private fun onError(context: Context) {
        ToastUtils.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
        revertLike()
    }

    private fun revertLike() {
        comment.isLiked = 1 - comment.isLiked
        isLiked.set(comment.isLiked)
    }

    private fun setCommentClicked() {
        comment.isLiked = 1 - comment.isLiked
        isLiked.set(comment.isLiked)
    }

    private fun getLikeRequestBody() = hashMapOf(
            "comment_id" to comment.id,
            "is_like" to comment.isLiked
    ).toRequestBody()


    private fun sendEventByQID(eventName : String, qid: String, context: Context){
        context?.apply {
            (context.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(context!!).toString())
                    .addStudentId(getStudentId())
                    .addScreenName(EventConstants.PAGE_COMMENT_ACTIVITY)
                    .addEventParameter(Constants.QUESTION_ID, qid)
                    .track()
        }
    }

    private fun sendCleverTapEventByQID(eventName : String, qid: String, context: Context, page : String){
        context?.apply {
            (context.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(context!!).toString())
                    .addStudentId(getStudentId())
                    .addScreenName(EventConstants.PAGE_COMMENT_ACTIVITY)
                    .addEventParameter(Constants.QUESTION_ID, qid)
                    .addEventParameter(Constants.PAGE, page)
                    .addEventParameter(Constants.STUDENT_CLASS,
                            getStudentClass())
                    .cleverTapTrack()
        }
    }



    private fun sendEvent(eventName: String, context: Context) {
        (context.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(context!!).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_COMMENT_ACTIVITY)
                .track()

    }

}