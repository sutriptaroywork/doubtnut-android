package com.doubtnutapp.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.databinding.WidgetStudyGroupJoinedInfoBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class StudyGroupJoinedInfoWidget(context: Context) :
    BaseBindingWidget<StudyGroupJoinedInfoWidget.WidgetHolder,
            StudyGroupJoinedInfoWidget.Model, WidgetStudyGroupJoinedInfoBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetStudyGroupJoinedInfoBinding {
        return WidgetStudyGroupJoinedInfoBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        binding.tvJoinedInfo.text = model.data.title
        return holder
    }

    class WidgetHolder(binding: WidgetStudyGroupJoinedInfoBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetStudyGroupJoinedInfoBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String
    ) : WidgetData()
}