@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.doubtnutapp.gamification.otheruserprofile.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.gamification.otheruserprofile.model.OtherUserStatsDataModel
import com.doubtnutapp.gamification.otheruserprofile.ui.viewholder.OtherUserStatsViewHolder

class OtherProfileStatsAdapter : RecyclerView.Adapter<OtherUserStatsViewHolder>() {

    private var otherStatsList: List<OtherUserStatsDataModel>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OtherUserStatsViewHolder {

        return OtherUserStatsViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_other_user_activity, parent, false))
    }

    override fun onBindViewHolder(holder: OtherUserStatsViewHolder, position: Int) {
        holder.bind(otherStatsList!![position])

    }

    override fun getItemCount(): Int {
        return otherStatsList?.size ?: 0
    }

    fun updateData(otherStatsList: List<OtherUserStatsDataModel>) {
        this.otherStatsList = otherStatsList
    }

}

