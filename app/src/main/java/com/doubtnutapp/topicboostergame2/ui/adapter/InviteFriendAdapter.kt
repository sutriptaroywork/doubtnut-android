package com.doubtnutapp.topicboostergame2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.data.remote.models.topicboostergame2.Friend
import com.doubtnutapp.topicboostergame2.ui.viewholder.InviteFriendViewHolder

/**
 * Created by devansh on 22/06/21.
 */

class InviteFriendAdapter(private val actionsPerformer: ActionPerformer2, val source: String? = null) : RecyclerView.Adapter<InviteFriendViewHolder>() {

    private var friends = mutableListOf<Friend>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteFriendViewHolder =
        InviteFriendViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_topic_booster_game_invite_friend, parent, false), source
        ).apply {
            actionPerformer = actionsPerformer
        }

    override fun onBindViewHolder(holder: InviteFriendViewHolder, position: Int) {
        holder.bind(friends[position])
    }

    override fun getItemCount(): Int = friends.size

    fun updateList(friends: List<Friend>?) {
        this.friends.clear()
        this.friends.addAll(friends.orEmpty())
        notifyDataSetChanged()
    }
}