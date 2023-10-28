package com.doubtnutapp.liveclass.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.SubmitLiveClassPoll
import com.doubtnutapp.databinding.ItemLiveClassPollsOptionsBinding
import com.doubtnutapp.utils.Utils

class LiveClassPollsOptionsAdapter(
    val items: List<LiveClassPollOptions>,
    val actionPerformer: ActionPerformer?
) : RecyclerView.Adapter<LiveClassPollsOptionsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_live_class_polls_options,
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.mathViewQuizOptions.text = item.value
        if (item.isSelected == true && !item.isResultShown) {
            holder.binding.layoutOption.background = Utils.getShape(
                "#ffffff",
                "#223d4d",
                16f,
                4
            )
        } else {
            holder.binding.layoutOption.background = Utils.getShape(
                "#ffffff",
                items[position].progressColour ?: "#cbcbcb",
                16f,
                4
            )
        }
        holder.binding.pollProgress.progress = item.progress?.toInt() ?: 0
        if (item.progress != null) {
            holder.binding.resultPercentageTv.visibility = LinearLayout.VISIBLE
            holder.binding.resultPercentageTv.text = item.progressDisplay
        } else {
            holder.binding.resultPercentageTv.visibility = LinearLayout.GONE
        }

        holder.binding.layoutOption.setOnClickListener {
            actionPerformer?.performAction(SubmitLiveClassPoll(item.key))
        }
    }

//    private fun setProgressColor(progressBar: ProgressBar, color: String) {
//        val progressDrawable: Drawable = progressBar.progressDrawable.mutate()
//        progressDrawable.setColorFilter(Utils.parseColor(color), PorterDuff.Mode.SRC_IN)
//        progressBar.progressDrawable = progressDrawable
//    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemLiveClassPollsOptionsBinding.bind(itemView)
    }
}