package com.doubtnutapp.topicboostergame2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.topicboostergame2.ui.viewholder.DialogMessageViewHolder

class DialogMessageAdapter : RecyclerView.Adapter<DialogMessageViewHolder>() {

    private val messages = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogMessageViewHolder =
        DialogMessageViewHolder(
            LayoutInflater.from(parent.context)
            .inflate(R.layout.item_topic_booster_game_message, parent, false))

    override fun onBindViewHolder(holder: DialogMessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    fun updateList(message: List<String>) {
        this.messages.clear()
        this.messages.addAll(message)
        notifyDataSetChanged()
    }
}