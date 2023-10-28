package com.doubtnutapp.widgetmanager.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View.OnTouchListener
import androidx.annotation.Keep
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher

import com.doubtnutapp.base.OpenAudioPlayerDialog
import com.doubtnutapp.base.SgChildWidgetLongClick
import com.doubtnutapp.databinding.WidgetAudioPlayerBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.getHumanTimeText
import com.google.gson.annotations.SerializedName
import javax.inject.Inject


class AudioPlayerWidget(context: Context) : BaseBindingWidget<AudioPlayerWidget.WidgetHolder,
        AudioPlayerWidget.Model, WidgetAudioPlayerBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetAudioPlayerBinding {
        return WidgetAudioPlayerBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        val binding = holder.binding
        model.layoutConfig = WidgetLayoutConfig(
            marginTop = 0,
            marginBottom = 0,
        )
        super.bindWidget(holder, model)
        val data = model.data
        binding.apply {
            tvDuration.text = data.audioDuration?.getHumanTimeText()
            seekBar.setOnTouchListener { _, _ -> true }
            seekBar.setOnDragListener { v, event ->
                actionPerformer?.performAction(
                    OpenAudioPlayerDialog(
                        data.audioDuration,
                        data.attachmentUrl
                    )
                )
                return@setOnDragListener true
            }
            val lastTouchDownXY = FloatArray(2)
            setOnTouchListener(OnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    lastTouchDownXY[0] = event.x
                    lastTouchDownXY[1] = event.y - height
                }
                return@OnTouchListener false
            })

            setOnLongClickListener {
                actionPerformer?.performAction(SgChildWidgetLongClick(model.type, lastTouchDownXY))
                true
            }
            setOnClickListener {
                actionPerformer?.performAction(
                    OpenAudioPlayerDialog(
                        data.audioDuration,
                        data.attachmentUrl
                    )
                )
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetAudioPlayerBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetAudioPlayerBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("audio_duration") val audioDuration: Long?,
        @SerializedName("attachment") val attachmentUrl: String
    ) : WidgetData()

}

