package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.LiveClassInfoItem
import com.doubtnutapp.data.remote.models.LiveClassInfoWidgetData
import com.doubtnutapp.data.remote.models.LiveClassInfoWidgetModel
import com.doubtnutapp.databinding.ItemLiveClassInfoBinding
import com.doubtnutapp.databinding.WidgetLiveClassInfoBinding
import com.doubtnutapp.hide
import com.doubtnutapp.loadImage
import com.doubtnutapp.show
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget

class LiveClassInfoWidget(context: Context) :
    BaseBindingWidget<LiveClassInfoWidgetHolder, LiveClassInfoWidgetModel, WidgetLiveClassInfoBinding>(
        context
    ) {

    override fun getView(): View {
        return View.inflate(context, R.layout.widget_live_class_info, this)
    }

    override fun bindWidget(holder: LiveClassInfoWidgetHolder, model: LiveClassInfoWidgetModel): LiveClassInfoWidgetHolder {
        super.bindWidget(holder, model)
        val data: LiveClassInfoWidgetData = model.data
        val binding = holder.binding
        binding.tvTitle.text = data.title
        binding.rvLiveClassInfo.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL, false
        )
        binding.rvLiveClassInfo.adapter = LiveClassInfoListAdapter(
            context,
            data.items.orEmpty().toMutableList()
        )
        return holder
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = LiveClassInfoWidgetHolder(getViewBinding(), this)
    }

    override fun getViewBinding(): WidgetLiveClassInfoBinding {
        return WidgetLiveClassInfoBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

class LiveClassInfoListAdapter(val context: Context, val items: List<LiveClassInfoItem>) :
    RecyclerView.Adapter<LiveClassInfoListAdapter.LiveClassInfoListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveClassInfoListViewHolder {
        return LiveClassInfoListViewHolder(
            ItemLiveClassInfoBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: LiveClassInfoListViewHolder, position: Int) {
        val binding = holder.binding
        binding.parentLayout.background =
            Utils.getShape("#152838", "#152838")
        binding.tvHeading.text = items[position].title
        if (items[position].imageUrl.isNullOrBlank()) {
            binding.ivLogo.hide()
        } else {
            binding.ivLogo.show()
            binding.ivLogo.loadImage(
                items[position].imageUrl.orEmpty(),
                R.drawable.circle_transparent, R.drawable.circle_transparent
            )
        }
    }

    override fun getItemCount(): Int = items.size

    class LiveClassInfoListViewHolder(val binding: ItemLiveClassInfoBinding) : RecyclerView.ViewHolder(binding.root)
}

class LiveClassInfoWidgetHolder(
    binding: WidgetLiveClassInfoBinding,
    widget: BaseWidget<*, *>
) :
    WidgetBindingVH<WidgetLiveClassInfoBinding>(binding, widget)
