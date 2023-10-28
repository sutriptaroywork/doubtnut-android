package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.databinding.WidgetStudyGroupBannerImageBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.BannerActionUtils
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class StudyGroupBannerImageWidget(context: Context) :
    BaseBindingWidget<StudyGroupBannerImageWidget.WidgetHolder,
            StudyGroupBannerImageWidget.Model, WidgetStudyGroupBannerImageBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetStudyGroupBannerImageBinding {
        return WidgetStudyGroupBannerImageBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: Model
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        binding.ivBanner.loadImage(model.data.imageUrl, null)
        holder.itemView.setOnClickListener {
            val actionData = model.action?.actionData
            if (actionData != null && model.action!!.actionActivity != null) {
                BannerActionUtils.performAction(
                    holder.itemView.context,
                    model.action!!.actionActivity!!,
                    actionData
                )
            } else {
                deeplinkAction.performAction(holder.itemView.context, model.data.deeplink)
            }
        }
        trackingViewId = model.data.id
        return holder
    }

    class WidgetHolder(binding: WidgetStudyGroupBannerImageBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetStudyGroupBannerImageBinding>(binding, widget)

    class Model : WidgetEntityModel<BannerImageWidgetData, WidgetAction>()

    @Keep
    data class BannerImageWidgetData(
        @SerializedName("_id") val id: String,
        @SerializedName("image_url") val imageUrl: String,
        @SerializedName("deeplink") val deeplink: String?
    ) : WidgetData()
}