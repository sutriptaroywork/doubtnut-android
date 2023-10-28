package com.doubtnutapp.similarVideo.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.databinding.WidgetNcertTopicBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName

class ChildGridSelectionWidget(context: Context)
    : BaseBindingWidget<ChildGridSelectionWidget.WidgetHolder,
        ChildGridSelectionWidget.Model,WidgetNcertTopicBinding>(context) {

    override fun getViewBinding(): WidgetNcertTopicBinding {
        return WidgetNcertTopicBinding.inflate(LayoutInflater.from(context), this,true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(),this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model)
        val data = model.data
        holder.binding.apply {
            tvTitle.text = data.title
            if (data.isSelected) {
                tvTitle.background = ContextCompat.getDrawable(context, R.drawable.bg_ncert_topic_selected)
                tvTitle.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                tvTitle.background = ContextCompat.getDrawable(context, R.drawable.bg_ncert_topic_unselected)
                tvTitle.setTextColor(ContextCompat.getColor(context, R.color.purple))
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetNcertTopicBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetNcertTopicBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
            @SerializedName("id") val id: String,
            @SerializedName("type") val type: String,
            @SerializedName("title") val title: String,
            @SerializedName("is_selected") val isSelected: Boolean,
            @SerializedName("items") val items: List<WidgetEntityModel<*, *>>?
    ) : WidgetData()
}