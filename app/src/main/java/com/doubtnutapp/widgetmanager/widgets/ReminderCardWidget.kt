package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.data.remote.models.ReminderCardWidgetModel
import com.doubtnutapp.databinding.WidgetReminderCardBinding
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import javax.inject.Inject

class ReminderCardWidget(context: Context) :
    BaseBindingWidget<ReminderCardWidget.ReminderCardWidgetHolder,
            ReminderCardWidgetModel, WidgetReminderCardBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetReminderCardBinding {
        return WidgetReminderCardBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = ReminderCardWidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: ReminderCardWidgetHolder,
        model: ReminderCardWidgetModel
    ): ReminderCardWidgetHolder {
        val binding = holder.binding

        binding.textViewTitle.text = model.data.textOne.orEmpty()
        binding.textViewTitle2.text = model.data.textTwo.orEmpty()
        binding.button.text = model.data.buttonData?.text.orEmpty()
        holder.itemView.setOnClickListener {
            val url = model.data.buttonData?.action?.actionData?.externalUrl
            if (url.isNullOrBlank()) return@setOnClickListener
            val uri: Uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            holder.itemView.context.startActivity(intent)

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.REMINDER_CARD_CLICK,
                    hashMapOf(
                        EventConstants.WIDGET to "ReminderCardWidget"
                    ), ignoreSnowplow = true
                )
            )
        }
        return holder
    }

    class ReminderCardWidgetHolder(binding: WidgetReminderCardBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetReminderCardBinding>(binding, widget)

}