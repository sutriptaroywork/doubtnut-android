package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.RankersWidgetData
import com.doubtnutapp.data.remote.models.RankersWidgetItem
import com.doubtnutapp.data.remote.models.RankersWidgetModel
import com.doubtnutapp.databinding.ItemRankersBinding
import com.doubtnutapp.databinding.WidgetRankersBinding
import com.doubtnutapp.loadImage
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget

class RankersWidget(context: Context) :
    BaseBindingWidget<RankersWidgetViewHolder, RankersWidgetModel, WidgetRankersBinding>(context) {

    override fun getViewBinding(): WidgetRankersBinding {
        return WidgetRankersBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun bindWidget(holder: RankersWidgetViewHolder, model: RankersWidgetModel): RankersWidgetViewHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data: RankersWidgetData = model.data
        binding.tvTitle.text = data.title
        binding.rvRankers.layoutManager = GridLayoutManager(context, 3)
        binding.rvRankers.adapter = RankersListAdapter(
            context,
            data.items.orEmpty().toMutableList()
        )
        return holder
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = RankersWidgetViewHolder(getViewBinding(), this)
    }
}

class RankersListAdapter(val context: Context, val items: List<RankersWidgetItem>) :
    RecyclerView.Adapter<RankersListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemRankersBinding
                .inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        binding.tvRank.text = items[position].rank
        binding.tvStudentName.text = items[position].studentName
        binding.studentImage.loadImage(
            items[position].imageUrl.orEmpty(),
            R.drawable.circle_transparent,
            R.drawable.circle_transparent
        )
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: ItemRankersBinding) :
        RecyclerView.ViewHolder(binding.root)
}

class RankersWidgetViewHolder(binding: WidgetRankersBinding, widget: BaseWidget<*, *>) :
    WidgetBindingVH<WidgetRankersBinding>(binding, widget)
