package com.doubtnutapp.liveclass.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.HomeWorkVideoSolution
import com.doubtnutapp.databinding.ItemHomeWorkVideoSolutionBinding
import com.doubtnutapp.deeplink.DeeplinkAction

class HomeWorkSolutionAdapter(
    val context: Context,
    private val solutionList: List<HomeWorkVideoSolution>,
    val deeplinkAction: DeeplinkAction?
) : RecyclerView.Adapter<HomeWorkSolutionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeWorkSolutionViewHolder {
        return HomeWorkSolutionViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_home_work_video_solution, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: HomeWorkSolutionViewHolder, position: Int) {
        val data = solutionList[position]
        holder.binding.tvQuestionId.text = data.questionId
        holder.binding.questionAskedCount.text = data.asked
        holder.binding.questionAskedYear.text = data.locale
        holder.binding.tvQuestionNo.text = data.title
        holder.binding.mViewQuestion.text = data.ocrText.orEmpty()
        holder.binding.clickHelperView.setOnClickListener {
            deeplinkAction?.performAction(holder.itemView.context, data.deeplink)
        }
        if (data.duration?.toInt() ?: 0 < 60) {
            holder.binding.durationTv.text = data.duration
        } else {
            holder.binding.durationTv.text = Math.ceil(
                (data.duration?.toDouble()?.div(60.toDouble())
                    ?: 0.0)
            ).toInt().toString()
        }
        if (data.type.equals("text", true)) {
            holder.binding.textSolutionImage.visibility = View.VISIBLE
        } else {
            holder.binding.textSolutionImage.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return solutionList.size
    }
}

class HomeWorkSolutionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val binding = ItemHomeWorkVideoSolutionBinding.bind(view)
}
