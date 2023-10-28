package com.doubtnutapp.feed.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.R
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.data.remote.models.Comment
import com.doubtnutapp.hide
import com.doubtnutapp.loadImage
import com.doubtnutapp.show
import com.doubtnutapp.ui.forum.comments.CommentsActivity
import com.doubtnutapp.utils.Utils
import kotlinx.android.synthetic.main.view_feed_comment.view.*

class FeedCommentView(ctx: Context, attrs: AttributeSet) : LinearLayout(ctx, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_feed_comment, this, true)
    }

    fun bindData(comment: Comment, postId: String) {
        tvUsername.text = comment.studentUsername
        tvCommentMessage.text = comment.message
        tvCreatedAt.text = Utils.formatTime(context, comment.createdAt)
        ivProfileImage.loadImage(
            comment.studentAvatar,
            R.drawable.ic_default_one_to_one_chat,
            R.drawable.ic_default_one_to_one_chat
        )
        ivProfileImage.setOnClickListener {
            FragmentWrapperActivity.userProfile(
                it.context,
                comment.studentId,
                "post_featured_comment"
            )
        }

        btnReply.show()
        if (comment.replyCount != null && comment.replyCount!! > 0) {
            btnReply.text = "${comment.replyCount} Reply"
        } else {
            btnReply.text = "Reply"
        }
        btnReply.setOnClickListener {
            openCommentReply(it.context, postId, comment)
        }

        btnLikes.hide()
        btnLikes.text = "${comment.likeCount ?: 0} Likes"
        btnLikes.setOnClickListener {
            // likeComment(it.context, postId, comment)
        }
    }

    //entity_id is the parent id (post id)
    private fun openCommentReply(context: Context, entityId: String, comment: Comment) {
        CommentsActivity.startActivityForReplies(
            context as AppCompatActivity,
            entityId,
            "new_feed_type",
            0,
            comment,
            EventConstants.CATEGORY_FEED
        )
    }
}