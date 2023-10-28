package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.core.utils.gone
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.databinding.WidgetStudyGroupGuidlineBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class StudyGroupGuidelineWidget(context: Context) : BaseBindingWidget<
        StudyGroupGuidelineWidget.WidgetHolder,
        StudyGroupGuidelineWidget.Model, WidgetStudyGroupGuidlineBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetStudyGroupGuidlineBinding {
        return WidgetStudyGroupGuidlineBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        model.data.title?.let {
            binding.tvTitle.text = it
        } ?: binding.tvTitle.gone()
        binding.tvGuidline.text = model.data.groupGuideline
        return holder
    }

    class WidgetHolder(binding: WidgetStudyGroupGuidlineBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetStudyGroupGuidlineBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("_id") val id: String,
        @SerializedName("title") val title: String?,
        @SerializedName("group_guideline") val groupGuideline: String?
    ) : WidgetData()

}