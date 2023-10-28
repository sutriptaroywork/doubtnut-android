package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.ToppersSpeakItem
import com.doubtnutapp.data.remote.models.ToppersSpeakWidgetData
import com.doubtnutapp.data.remote.models.ToppersSpeakWidgetModel
import com.doubtnutapp.databinding.ItemsToppersSpeakBinding
import com.doubtnutapp.databinding.WidgetTitleVerticalListBinding
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.orDefaultValue
import com.doubtnut.core.widgets.ui.WidgetBindingVH

class ToppersSpeakWidget(context: Context) : BaseBindingWidget<ToppersSpeakWidget.WidgetHolder,
        ToppersSpeakWidgetModel, WidgetTitleVerticalListBinding>(context) {

    override fun getViewBinding(): WidgetTitleVerticalListBinding {
        return WidgetTitleVerticalListBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: ToppersSpeakWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        val data: ToppersSpeakWidgetData = model.data
        binding.tvTitle.text = data.title
        binding.rvItems.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        val actionActivity = model.action?.actionActivity.orDefaultValue()
        binding.rvItems.adapter =
            ToppersSpeakAdapter(data.items.orEmpty(), actionActivity)
        trackingViewId = model.data.title
        return holder
    }

    class ToppersSpeakAdapter(val items: List<ToppersSpeakItem>, val actionActivity: String) :
        RecyclerView.Adapter<ToppersSpeakAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.items_toppers_speak, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding
            binding.textViewTitle.text = items[position].description
            binding.textViewTitleTwo.text = items[position].title
            binding.textViewTitleThree.text = items[position].subtitle
            binding.imageViewCircular.loadImageEtx(items[position].imageUrl.orEmpty())
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val binding = ItemsToppersSpeakBinding.bind(itemView)
        }
    }

    class WidgetHolder(binding: WidgetTitleVerticalListBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTitleVerticalListBinding>(binding, widget)
}