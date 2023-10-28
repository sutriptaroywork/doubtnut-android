package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.RankersClassesWidgetData
import com.doubtnutapp.data.remote.models.RankersClassesWidgetModel
import com.doubtnutapp.databinding.ItemRankersClassesBinding
import com.doubtnutapp.databinding.WidgetRankersClassesBinding
import com.doubtnutapp.loadBackgroundImage
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget

class RankersClassesWidget(context: Context) : BaseBindingWidget<RankersClassesWidgetViewHolder,
    RankersClassesWidgetModel, WidgetRankersClassesBinding>(context) {

    override fun getViewBinding(): WidgetRankersClassesBinding {
        return WidgetRankersClassesBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun bindWidget(
        holder: RankersClassesWidgetViewHolder,
        model: RankersClassesWidgetModel
    ): RankersClassesWidgetViewHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data: RankersClassesWidgetData = model.data
        binding.tvTitle.text = data.title
        binding.root.loadBackgroundImage(data.bgImage, R.color.grey_2a4863)
        binding.rvRankersClasses.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL, false
        )
        binding.rvRankersClasses.adapter = RankersClassesListAdapter(
            context,
            data.items.orEmpty().toMutableList()
        )
        return holder
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = RankersClassesWidgetViewHolder(getViewBinding(), this)
    }
}

class RankersClassesListAdapter(val context: Context, val items: List<String>) :
    RecyclerView.Adapter<RankersClassesListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemRankersClassesBinding
                .inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        binding.tvHeading.text = items[position]
        if (position == items.size - 1) {
            binding.ivSeparator.visibility = View.GONE
        } else {
            binding.ivSeparator.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: ItemRankersClassesBinding) :
        RecyclerView.ViewHolder(binding.root)
}

class RankersClassesWidgetViewHolder(
    binding: WidgetRankersClassesBinding,
    widget: BaseWidget<*, *>
) : WidgetBindingVH<WidgetRankersClassesBinding>(binding, widget)
