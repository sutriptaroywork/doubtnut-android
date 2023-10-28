package com.doubtnutapp.doubtpecharcha.ui.viewholder

import android.graphics.Color
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.UnderlineSpan
import android.view.View
import androidx.core.view.isVisible
import com.doubtnutapp.*
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OnChatImageClicked
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.data.remote.models.LiveClassChatData
import com.doubtnutapp.databinding.ItemDoubtPeCharchaSenderBinding
import com.doubtnutapp.videoPage.ui.VideoPageActivity

class DoubtPeCharchaSenderViewHolder(itemView: View) : BaseViewHolder<LiveClassChatData>(itemView) {

    val binding = ItemDoubtPeCharchaSenderBinding.bind(itemView)

    override fun bind(data: LiveClassChatData) {

        binding.apply {
            tvMessage.isVisible = data.message.isNullOrEmpty().not()
            tvMessage.text = data.message

            data.questionId?.let {
                ivPlay.show()
                appendQuestionIdAsLink(it)
            } ?: ivPlay.hide()

            userImage.loadImage(data.imageUrl, R.drawable.ic_person_grey)
            userImage.setOnClickListener {
                FragmentWrapperActivity.userProfile(
                    itemView.context,
                    data.studentId.orEmpty(), "doubt_p2p"
                )
            }

            ivAttachment.apply {
                isVisible = data.attachment.isNullOrEmpty().not()
                when {
                    data.questionId.isNullOrEmpty().not() && data.thumbnailImage.isNullOrEmpty()
                        .not() -> {
                        loadImage(data.thumbnailImage)
                    }
                    else -> loadImage(getAttachmentUrl(data.cdnUrl, data.attachment))
                }

                setOnClickListener {
                    when {
                        data.questionId.isNullOrEmpty().not() -> {
                            openVideoPage(questionId = data.questionId!!)
                        }
                        else -> {
                            actionPerformer?.performAction(
                                OnChatImageClicked(getAttachmentUrl(data.cdnUrl, data.attachment))
                            )
                        }
                    }
                }
            }
        }
    }

    fun appendQuestionIdAsLink(questionId: String) {
        val spannableQuestionId = SpannableString(questionId)
        spannableQuestionId.setSpan(UnderlineSpan(), 0, spannableQuestionId.length, 0)
        binding.tvMessage.apply {
            append("\n")
            append(spannableQuestionId)
            movementMethod = LinkMovementMethod.getInstance()
            highlightColor = Color.TRANSPARENT

            setOnClickListener {
                openVideoPage(questionId)
            }
        }
    }

    private fun openVideoPage(questionId: String) {
        VideoPageActivity.startActivity(
            context = itemView.context,
            questionId = questionId,
            page = Constants.PAGE_P2P
        ).apply {
            itemView.context.startActivity(this)
        }
    }

    private fun getAttachmentUrl(cdnUrl: String?, attachment: String?): String {
        return (cdnUrl.orEmpty() + attachment.orEmpty()).trim()
    }
}
