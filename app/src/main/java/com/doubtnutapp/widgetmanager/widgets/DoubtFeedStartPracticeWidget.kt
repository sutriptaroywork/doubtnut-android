package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher

import com.doubtnutapp.databinding.WidgetDoubtFeedStartPracticeBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImage
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class DoubtFeedStartPracticeWidget(context: Context) :
    BaseBindingWidget<DoubtFeedStartPracticeWidget.WidgetHolder,
            DoubtFeedStartPracticeWidget.Model, WidgetDoubtFeedStartPracticeBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetDoubtFeedStartPracticeBinding {
        return WidgetDoubtFeedStartPracticeBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        val data = model.data
        val binding = holder.binding


        binding.apply {
            val ivList: List<ShapeableImageView> = listOf(ivUser, ivUser1, ivUser2)
            tvStartPracticeTitle.text = data.title
            tvStartPracticeSubtitle.text = data.subtitle
            ivList.zip(data.userImages) { iv, image -> iv.loadImage(image) }

            setOnClickListener {
                DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams?.apply {
                    put(Constants.ID, data.id)
                }))
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetDoubtFeedStartPracticeBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetDoubtFeedStartPracticeBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String,
        @SerializedName("subtitle") val subtitle: String,
        @SerializedName("user_images") val userImages: List<String>,
        @SerializedName("id") val id: String,
    ) : WidgetData()
}