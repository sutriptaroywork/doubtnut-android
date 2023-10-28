package com.doubtnutapp.widgetmanager.widgets

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.checkInternetConnection
import com.doubtnutapp.course.widgets.CommonCourseWidget
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.NotifyClassWidgetModel
import com.doubtnutapp.databinding.WidgetNotifyLiveClassBinding
import com.doubtnutapp.orDefaultValue
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class NotifyClassWidget(context: Context)
    : BaseBindingWidget<NotifyClassWidget.WidgetHolder,
        NotifyClassWidgetModel, WidgetNotifyLiveClassBinding>(context) {

    companion object {
        const val TAG = "NotifyClassWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: NotifyClassWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val data = model.data
        val binding = holder.binding

        binding.tvLiveClassText.text = data.title
        binding.tvClassTime.text = data.subtitle
        binding.btnNotify.text = data.buttonText
        binding.layoutNotifyMe.setOnClickListener {
            checkInternetConnection(holder.itemView.context) {
                (holder.itemView.context as? Activity)?.let { context ->
                    Snackbar.make(
                            context.findViewById(android.R.id.content),
                            data.reminderMessage
                                    .orDefaultValue("Your reminder has been set"),
                            Snackbar.LENGTH_LONG
                    ).apply {
                        this.view.background = context.getDrawable(R.drawable.bg_capsule_black_90)
                        setActionTextColor(ContextCompat.getColor(context, R.color.redTomato))
                        val textView = this.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                        textView.setTextColor(ContextCompat.getColor(context, R.color.white))
                        show()
                    }
                }
                markInterested(data.id.orEmpty(), true, data.assortmentId.orEmpty(), data.liveAt, 1)
            }
            analyticsPublisher.publishEvent(
                    AnalyticsEvent(TAG + EventConstants.EVENT_ITEM_CLICK,
                            hashMapOf<String, Any>(
                                    EventConstants.EVENT_NAME_ID to "notifyme_Resourcepage_header",
                                    EventConstants.WIDGET to CommonCourseWidget.TAG
                            ).apply {
                                putAll(model.extraParams ?: HashMap())
                            }, ignoreSnowplow = true
                    )
            )
        }
        return holder
    }

    private fun markInterested(id: String, isReminder: Boolean, assortmentId: String, liveAt: String?, reminderSet: Int?) {
        DataHandler.INSTANCE.courseRepository.markInterested(id, isReminder, assortmentId, liveAt, reminderSet)
                .applyIoToMainSchedulerOnCompletable()
                .subscribeToCompletable({})
    }

    class WidgetHolder(
        binding: WidgetNotifyLiveClassBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetNotifyLiveClassBinding>(binding, widget)

    override fun getViewBinding(): WidgetNotifyLiveClassBinding {
        return WidgetNotifyLiveClassBinding.inflate(LayoutInflater.from(context), this, true)
    }
}