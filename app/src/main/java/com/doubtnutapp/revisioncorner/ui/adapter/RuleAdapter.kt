package com.doubtnutapp.revisioncorner.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.revisioncorner.Rule
import com.doubtnutapp.revisioncorner.ui.viewholder.RuleViewHolder


class RuleAdapter : RecyclerView.Adapter<RuleViewHolder>() {

    private var rules = mutableListOf<Rule>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RuleViewHolder =
        RuleViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_revision_corner_rules, parent, false))

    override fun onBindViewHolder(holder: RuleViewHolder, position: Int) {
        holder.bind(rules[position])
    }

    override fun getItemCount(): Int = rules.size

    fun updateList(rules: List<Rule>) {
        this.rules.clear()
        this.rules.addAll(rules)
        notifyDataSetChanged()
    }
}