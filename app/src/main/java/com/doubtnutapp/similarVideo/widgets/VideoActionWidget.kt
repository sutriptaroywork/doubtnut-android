package com.doubtnutapp.similarVideo.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.databinding.WidgetVideoActionBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName

class VideoActionWidget(context: Context) : BaseBindingWidget<VideoActionWidget.WidgetHolder,
        VideoActionWidget.Model, WidgetVideoActionBinding>(context) {

    override fun getViewBinding(): WidgetVideoActionBinding {
        return WidgetVideoActionBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model)
        val data = model.data
        val binding = holder.binding
        binding.apply {
            tvLike.text = data.likeCount
            tvDislike.text = data.dislikeCount
            tvComment.text = data.commentCount
            tvShare.text = data.shareCount
        }

        return holder
    }

    class WidgetHolder(binding: WidgetVideoActionBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetVideoActionBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String,
        @SerializedName("like_count") val likeCount: String,
        @SerializedName("dislike_count") val dislikeCount: String,
        @SerializedName("comment_count") val commentCount: String,
        @SerializedName("share_count") val shareCount: String
    ) : WidgetData()
}