package com.doubtnutapp.widgetmanager.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View.OnTouchListener
import androidx.annotation.Keep
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.SgChildWidgetLongClick
import com.doubtnutapp.databinding.WidgetStudyGroupLiveClassBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.studygroup.ui.fragment.SgChatFragment
import com.doubtnutapp.studygroup.ui.fragment.SgPersonalChatFragment
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class StudyGroupLiveClassWidget(context: Context) :
    BaseBindingWidget<StudyGroupLiveClassWidget.WidgetHolder,
            StudyGroupLiveClassWidget.Model, WidgetStudyGroupLiveClassBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    override fun getViewBinding(): WidgetStudyGroupLiveClassBinding {
        return WidgetStudyGroupLiveClassBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = WidgetLayoutConfig(8, 0, 4, 4)
        })

        val data = model.data
        val binding = holder.binding
        binding.mathView.text = data.ocrText

        Glide.with(context)
            .load(data.imageUrl)
            .addListener(object : RequestListener<Drawable> {

                override fun onLoadFailed(
                    e: GlideException?, model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean,
                ): Boolean {
                    binding.mathView.show()
                    binding.videoThumbnail.hide()
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?, model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?, isFirstResource: Boolean,
                ): Boolean {
                    binding.mathView.hide()
                    return false
                }
            })
            .into(binding.videoThumbnail)

        binding.apply {
            if (source == SgChatFragment.STUDY_GROUP || source == SgPersonalChatFragment.SOURCE_PERSONAL_CHAT) {
                val lastTouchDownXY = FloatArray(2)
                setOnTouchListener(OnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        lastTouchDownXY[0] = event.x
                        lastTouchDownXY[1] = event.y - height
                    }
                    return@OnTouchListener false
                })

                setOnLongClickListener {
                    actionPerformer?.performAction(
                        SgChildWidgetLongClick(
                            model.type,
                            lastTouchDownXY
                        )
                    )
                    true
                }
            }

            setOnClickListener {
                deeplinkAction.performAction(context, data.deeplink)
            }

        }

        return holder
    }

    class WidgetHolder(binding: WidgetStudyGroupLiveClassBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetStudyGroupLiveClassBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("ocr_text") val ocrText: String?,
        @SerializedName("question_id") val questionId: String?,
        @SerializedName("deeplink") val deeplink: String?,
    ) : WidgetData()

}