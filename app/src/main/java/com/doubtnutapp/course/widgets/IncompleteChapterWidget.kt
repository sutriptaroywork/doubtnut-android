package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.data.remote.models.feed.IncompleteChapterWidgetModel
import com.doubtnutapp.databinding.WidgetIncompleteChapterBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import javax.inject.Inject

class IncompleteChapterWidget(context: Context) :
    BaseBindingWidget<IncompleteChapterWidget.WidgetHolder,
        IncompleteChapterWidgetModel, WidgetIncompleteChapterBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: IncompleteChapterWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val data: com.doubtnutapp.data.remote.models.feed.IncompleteChapterWidgetData = model.data

        holder.binding.apply {
            tvCompleteChapter.text = data.title
            setOnClickListener {
                deeplinkAction.performAction(holder.itemView.context, data.deeplink)
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetIncompleteChapterBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetIncompleteChapterBinding>(binding, widget)

    override fun getViewBinding(): WidgetIncompleteChapterBinding {
        return WidgetIncompleteChapterBinding.inflate(LayoutInflater.from(context), this, true)
    }
}
