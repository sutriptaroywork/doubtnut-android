package com.doubtnutapp.liveclass.adapter

import android.view.*
import android.widget.PopupMenu
import android.widget.PopupMenu.OnMenuItemClickListener
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R
import com.doubtnutapp.base.*
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.data.remote.models.LiveClassChatData
import com.doubtnutapp.databinding.ItemChatUserJoinedBinding
import com.doubtnutapp.databinding.ItemLiveClassChatBinding
import com.doubtnutapp.databinding.ItemLiveClassChatSenderBinding
import com.doubtnutapp.loadImage
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.widgetmanager.WidgetFactory
import java.text.SimpleDateFormat
import java.util.*

class LiveClassChatAdapter(private val actionPerformer: ActionPerformer) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val SENDER = 1
        const val RECEIVER = 0
        const val JOINED = 2
        const val WIDGET = 3
    }

    private val widgetMap = hashMapOf<Int, String>()

    private val messageList = mutableListOf<LiveClassChatData>()
    private var isAdminLoggedIn = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (widgetMap.containsKey(viewType)) {
            return LiveClassChatWidgetViewHolder(
                WidgetFactory.createViewHolder(parent.context, parent, widgetMap[viewType]!!)!!
            ) as BaseViewHolder<RecyclerViewItem>
        }
        return when (viewType) {
            SENDER -> {
                SenderViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_live_class_chat_sender, parent, false
                    )
                )
            }
            JOINED -> {
                ChatJoinedViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_chat_user_joined, parent, false
                    )
                )
            }
            else -> {
                ReceiverViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_live_class_chat, parent, false
                    )
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        if (holder is LiveClassChatWidgetViewHolder) {
            holder.bind(LiveClassWidgetViewItem(message.widgetData!!.widget))
        } else if (message.type != JOINED) {
            if (holder is SenderViewHolder){
                holder.binding.userImage.setOnClickListener {
                    if (message.type != SENDER) {
                        FragmentWrapperActivity.userProfile(
                            holder.itemView.context,
                            message.studentId.orEmpty(), "chat"
                        )
                    }
                }
                holder.binding.userName.setOnClickListener {
                    if (message.type != SENDER) {
                        FragmentWrapperActivity.userProfile(
                            holder.itemView.context,
                            message.studentId.orEmpty(), "chat"
                        )
                    }
                }
                if (message.isAuthor == true || message.type == SENDER) {
                    holder.binding.overflow.visibility = View.INVISIBLE
                } else {
                    holder.binding.overflow.visibility = View.VISIBLE
                }
                if (message.isAdmin == true && message.userTag?.isNotEmpty() == true) {
                    holder.binding.userTag.visibility = View.VISIBLE
                    holder.binding.userTag.text = message.userTag
                } else {
                    holder.binding.userTag.visibility = View.GONE
                }
                if (message.isAdmin == true) {
                    holder.binding.userName.setTextColor(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.color_eb532c
                        )
                    )
                } else {
                    holder.binding.userName.setTextColor(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.color_2f80ed
                        )
                    )
                }
                holder.binding.overflow.setOnClickListener { view ->
                    val popup = PopupMenu(holder.itemView.context, view)
                    val inflater: MenuInflater = popup.menuInflater
                    inflater.inflate(R.menu.menu_chat, popup.menu)
                    popup.setOnMenuItemClickListener(
                        MyMenuItemClickListener(
                            actionPerformer, position,
                            message.studentId.orEmpty(),
                            message.postId.orEmpty(),
                            message.roomId.orEmpty()
                        )
                    )
                    if (!isAdminLoggedIn) {
                        popup.menu.removeItem(R.id.report)
                    }
                    popup.show()
                }
                if (message.message.isNullOrEmpty()) {
                    holder.binding.msgTv.visibility = View.GONE
                } else {
                    holder.binding.msgTv.visibility = View.VISIBLE
                    holder.binding.msgTv.text = message.message.orEmpty()
                }
                holder.binding.userName.text = message.name.orEmpty()
                holder.binding.userImage.loadImage(
                    message.imageUrl.orEmpty(),
                    R.drawable.ic_person_grey
                )
                if (message.timestamp.isNullOrEmpty()) {
                    holder.binding.tvTimestamp.visibility = View.INVISIBLE
                } else {
                    holder.binding.tvTimestamp.visibility = View.VISIBLE
                    holder.binding.tvTimestamp.text = getFormattedTime(message.timestamp.orEmpty())
                }
                if (message.attachment.isNullOrEmpty()) {
                    holder.binding.ivAttachment.visibility = View.GONE
                } else {
                    holder.binding.ivAttachment.visibility = View.VISIBLE
                    if (message.cdnUrl.isNullOrEmpty()) {
                        holder.binding.ivAttachment.loadImageEtx(message.attachment.orEmpty())
                    } else {
                        holder.binding.ivAttachment.loadImageEtx(
                            message.cdnUrl.orEmpty()
                                    + message.attachment
                        )
                    }
                }
                holder.binding.ivAttachment.setOnClickListener {
                    if (message.cdnUrl.isNullOrEmpty()) {
                        actionPerformer.performAction(OnChatImageClicked(message.attachment.orEmpty()))
                    } else {
                        actionPerformer.performAction(
                            OnChatImageClicked(
                                message.cdnUrl +
                                        message.attachment.orEmpty()
                            )
                        )
                    }
                }
            } else if (holder is ReceiverViewHolder) {
                holder.binding.userImage.setOnClickListener {
                    if (message.type != SENDER) {
                        FragmentWrapperActivity.userProfile(
                            holder.itemView.context,
                            message.studentId.orEmpty(), "chat"
                        )
                    }
                }
                holder.binding.userName.setOnClickListener {
                    if (message.type != SENDER) {
                        FragmentWrapperActivity.userProfile(
                            holder.itemView.context,
                            message.studentId.orEmpty(), "chat"
                        )
                    }
                }
                if (message.isAuthor == true || message.type == SENDER) {
                    holder.binding.overflow.visibility = View.INVISIBLE
                } else {
                    holder.binding.overflow.visibility = View.VISIBLE
                }
                if (message.isAdmin == true && message.userTag?.isNotEmpty() == true) {
                    holder.binding.userTag.visibility = View.VISIBLE
                    holder.binding.userTag.text = message.userTag
                } else {
                    holder.binding.userTag.visibility = View.GONE
                }
                if (message.isAdmin == true) {
                    holder.binding.userName.setTextColor(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.color_eb532c
                        )
                    )
                } else {
                    holder.binding.userName.setTextColor(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.color_2f80ed
                        )
                    )
                }
                holder.binding.overflow.setOnClickListener { view ->
                    val popup = PopupMenu(holder.itemView.context, view)
                    val inflater: MenuInflater = popup.menuInflater
                    inflater.inflate(R.menu.menu_chat, popup.menu)
                    popup.setOnMenuItemClickListener(
                        MyMenuItemClickListener(
                            actionPerformer, position,
                            message.studentId.orEmpty(),
                            message.postId.orEmpty(),
                            message.roomId.orEmpty()
                        )
                    )
                    if (!isAdminLoggedIn) {
                        popup.menu.removeItem(R.id.report)
                    }
                    popup.show()
                }
                if (message.message.isNullOrEmpty()) {
                    holder.binding.msgTv.visibility = View.GONE
                } else {
                    holder.binding.msgTv.visibility = View.VISIBLE
                    holder.binding.msgTv.text = message.message.orEmpty()
                }
                holder.binding.userName.text = message.name.orEmpty()
                holder.binding.userImage.loadImage(
                    message.imageUrl.orEmpty(),
                    R.drawable.ic_person_grey
                )
                if (message.timestamp.isNullOrEmpty()) {
                    holder.binding.tvTimestamp.visibility = View.INVISIBLE
                } else {
                    holder.binding.tvTimestamp.visibility = View.VISIBLE
                    holder.binding.tvTimestamp.text = getFormattedTime(message.timestamp.orEmpty())
                }
                if (message.attachment.isNullOrEmpty()) {
                    holder.binding.ivAttachment.visibility = View.GONE
                } else {
                    holder.binding.ivAttachment.visibility = View.VISIBLE
                    if (message.cdnUrl.isNullOrEmpty()) {
                        holder.binding.ivAttachment.loadImageEtx(message.attachment.orEmpty())
                    } else {
                        holder.binding.ivAttachment.loadImageEtx(
                            message.cdnUrl.orEmpty()
                                    + message.attachment
                        )
                    }
                }
                holder.binding.ivAttachment.setOnClickListener {
                    if (message.cdnUrl.isNullOrEmpty()) {
                        actionPerformer.performAction(OnChatImageClicked(message.attachment.orEmpty()))
                    } else {
                        actionPerformer.performAction(
                            OnChatImageClicked(
                                message.cdnUrl +
                                        message.attachment.orEmpty()
                            )
                        )
                    }
                }
            }

        } else if (holder is ChatJoinedViewHolder) {
            holder.binding.msgTv.text = message.message.orEmpty()
        }
    }

    class MyMenuItemClickListener(
        val actionPerformer: ActionPerformer,
        val position: Int, val studentId: String,
        val postId: String, val roomId: String
    ) : OnMenuItemClickListener {
        override fun onMenuItemClick(menuItem: MenuItem): Boolean {
            when (menuItem.itemId) {
                R.id.delete -> {
                    actionPerformer.performAction(
                        OnDeleteMessageClicked(
                            position,
                            studentId,
                            postId
                        )
                    )
                    return true
                }
                R.id.report -> {
                    actionPerformer.performAction(
                        OnReportMessageClicked(
                            position,
                            studentId,
                            postId,
                            roomId
                        )
                    )
                    return true
                }
            }
            return false
        }
    }

    private fun getFormattedTime(timeStamp: String): String {
        if (timeStamp.isEmpty())
            return ""
        try {
            val f = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            val d: Date = f.parse(timeStamp)
            val f2 = SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.ENGLISH)
            return f2.format(d)
        } catch (e: Exception) {
        }
        return ""
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        if (message.type == WIDGET) {
            return message.widgetData!!.widget.type.hashCode()
        }
        return message.type
    }

    fun addMessage(message: LiveClassChatData) {
        if (message.type == WIDGET) {
            widgetMap[message.widgetData!!.widget.type.hashCode()] = message.widgetData.widget.type
        }
        messageList.add(message)
        notifyDataSetChanged()
    }

    fun deleteMessage(postId: String) {
        val message = messageList.filter { it.postId == postId }
        if (message.isNotEmpty()) {
            messageList.remove(message[0])
            notifyDataSetChanged()
        }
    }

    fun addMessagetoBottom(message: LiveClassChatData) {
        if (message.type == WIDGET) {
            widgetMap[message.widgetData!!.widget.type.hashCode()] = message.widgetData.widget.type
        }
        messageList.add(0, message)
        notifyItemInserted(0)
    }

    fun addMessages(messages: List<LiveClassChatData>, isAdminLoggedIn: Boolean?) {
        for (item in messages) {
            if (item.type == WIDGET) {
                widgetMap[item.widgetData!!.widget.type.hashCode()] = item.widgetData.widget.type
            }
        }
        this.isAdminLoggedIn = isAdminLoggedIn ?: false
        messageList.addAll(messages)
        notifyDataSetChanged()
    }
}

class SenderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val binding = ItemLiveClassChatSenderBinding.bind(view)
}

class ReceiverViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val binding = ItemLiveClassChatBinding.bind(view)
}

class ChatJoinedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val binding = ItemChatUserJoinedBinding.bind(view)
}