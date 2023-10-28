package com.doubtnutapp.widgetmanager.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View.OnTouchListener
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.SgChildWidgetLongClick
import com.doubtnutapp.databinding.WidgetSgVideoCardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.studygroup.ui.fragment.SgChatFragment
import com.doubtnutapp.studygroup.ui.fragment.SgPersonalChatFragment
import com.doubtnutapp.updateMargins
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by devansh on 9/1/21.
 */

class StudyGroupVideoCardWidget(context: Context) :
    BaseBindingWidget<StudyGroupVideoCardWidget.WidgetHolder, StudyGroupVideoCardWidget.Model, WidgetSgVideoCardBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    override fun getViewBinding(): WidgetSgVideoCardBinding {
        return WidgetSgVideoCardBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        val data = model.data
        val binding = holder.binding
        binding.apply {

            imageView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                dimensionRatio = null
                this.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
                height = ConstraintLayout.LayoutParams.WRAP_CONTENT

                data.maxThumbnailHeight?.let {
                    matchConstraintMaxHeight = it
                }
            }

            cardView.apply {

                cardView.radius = 12f.dpToPx()
                cardView.cardElevation = 0f

                updateLayoutParams<MarginLayoutParams> {
                    marginStart = 4.dpToPx()
                    marginEnd = 4.dpToPx()
                }

                updateMargins(top = 8.dpToPx(), bottom = 0.dpToPx())

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
                    DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams?.apply {
                        put(Constants.ID, data.id)
                    }))
                    deeplinkAction.performAction(context, data.deeplink)
                }
            }
            requestLayout()

            if (data.thumbnailUrl != null) {
                imageView.loadImage(data.thumbnailUrl)
            } else {
                val options = RequestOptions().frame(0)
                Glide.with(context).load(data.videoUrl).apply(options).into(imageView)
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetSgVideoCardBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetSgVideoCardBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String? = null,
        @SerializedName("video_url") val videoUrl: String,
        @SerializedName("thumbnail_url") val thumbnailUrl: String?,
        @SerializedName("card_width") val cardWidth: String?,
        @SerializedName("max_thumbnail_height") val maxThumbnailHeight: Int?,
        @SerializedName("deeplink") val deeplink: String,
        @SerializedName("id") val id: String,
    ) : WidgetData()
}