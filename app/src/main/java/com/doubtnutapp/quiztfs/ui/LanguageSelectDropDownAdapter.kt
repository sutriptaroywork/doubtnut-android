package com.doubtnutapp.quiztfs.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.quiztfs.LiveQuestionsMedium

/**
 * Created by Mehul Bisht on 26-08-2021
 */
class LanguageSelectDropDownAdapter(private val list: List<LiveQuestionsMedium>) :
    RecyclerView.Adapter<LanguageSelectDropDownAdapter.LanguageSelectViewHolder>() {
    private var languageSelectedListener: LanguageSelectedListener? = null

    fun setLanguageSelectedListener(languageSelectedListener: LanguageSelectedListener) {
        this.languageSelectedListener = languageSelectedListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageSelectViewHolder {
        return LanguageSelectViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_course_filter_drop_down, parent, false)
        )
    }

    override fun onBindViewHolder(holder: LanguageSelectViewHolder, position: Int) {
        val data: LiveQuestionsMedium = list[position]
        holder.tvDisplay.text = data.title
        if (data.isSelected) {
            holder.tvDisplay.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_small_tick,
                0
            )
        } else {
            holder.tvDisplay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        holder.itemView.setOnClickListener {
            languageSelectedListener?.onLanguageSelected(position, data)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class LanguageSelectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvDisplay: AppCompatTextView = itemView.findViewById(R.id.tvDisplay)
    }

    interface LanguageSelectedListener {
        fun onLanguageSelected(position: Int, data: LiveQuestionsMedium)
    }
}