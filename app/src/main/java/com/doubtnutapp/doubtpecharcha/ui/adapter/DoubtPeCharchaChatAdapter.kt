package com.doubtnutapp.doubtpecharcha.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.data.remote.models.LiveClassChatData
import com.doubtnutapp.doubtpecharcha.ui.viewholder.DoubtPeCharchaViewHolderFactory

class DoubtPeCharchaChatAdapter(private val actionPerformer: ActionPerformer) :
    RecyclerView.Adapter<BaseViewHolder<LiveClassChatData>>() {

    private val viewHolderFactory: DoubtPeCharchaViewHolderFactory = DoubtPeCharchaViewHolderFactory()

    companion object {
        const val SENDER = 1
        const val RECEIVER = 0
        const val JOINED = 2
    }

    private val messageList = mutableListOf<LiveClassChatData>()
    private var isAdminLoggedIn = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<LiveClassChatData> {
        return viewHolderFactory.getViewHolderFor(parent, viewType).apply {
            actionPerformer = this@DoubtPeCharchaChatAdapter.actionPerformer
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder<LiveClassChatData>, position: Int) {
        holder.bind(messageList[position])
    }

    override fun getItemViewType(position: Int): Int {
        return messageList[position].type
    }

    fun deleteMessage(postId: String) {
        val message = messageList.filter { it.postId == postId }
        if (message.isNotEmpty()) {
            messageList.remove(message[0])
            notifyDataSetChanged()
        }
    }

    fun addMessageToBottom(message: LiveClassChatData) {
        messageList.add(0, message)
        notifyItemInserted(0)
    }

    fun addMessages(messages: List<LiveClassChatData>, isAdminLoggedIn: Boolean?) {
        this.isAdminLoggedIn = isAdminLoggedIn ?: false
        messageList.addAll(messages)
        notifyDataSetChanged()
    }
}
