package com.doubtnutapp.topicboostergame2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.topicboostergame2.ui.viewholder.DialogEmojiViewHolder

class DialogEmojiAdapter(private val actionPerformer: ActionPerformer2) : RecyclerView.Adapter<DialogEmojiViewHolder>() {

    private val emojis = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogEmojiViewHolder =
        DialogEmojiViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_topic_booster_game_emoji, parent, false)).apply {
                    actionPerformer = this@DialogEmojiAdapter.actionPerformer
        }

    override fun onBindViewHolder(holder: DialogEmojiViewHolder, position: Int) {
        holder.bind(emojis[position])
    }

    override fun getItemCount(): Int = emojis.size

    fun updateList(emojis: List<String>) {
        this.emojis.clear()
        this.emojis.addAll(emojis)
        notifyDataSetChanged()
    }

}