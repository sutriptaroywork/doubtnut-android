package com.doubtnutapp.similarVideo.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.NcertVideoClick
import com.doubtnutapp.databinding.WidgetNcertSimilarBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.videoPage.entities.ApiVideoResource
import com.doubtnutapp.domain.videoPage.entities.ImaAdTagResource
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class NcertSimilarWidget(context: Context) : BaseBindingWidget<NcertSimilarWidget.WidgetHolder,
        NcertSimilarWidget.Model, WidgetNcertSimilarBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    override fun getViewBinding(): WidgetNcertSimilarBinding {
        return WidgetNcertSimilarBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(4, 4, 16, 16)
        })
        val data = model.data

        holder.binding.apply {
            holder.itemView.setWidthFromScrollSize(data.cardWidth)

            contentLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = if (data.cardRatio != null) 0 else height
                dimensionRatio = data.cardRatio
            }

            tvTitle.isVisible = data.title.isNotNullAndNotEmpty()
            tvTitle.text = data.title
            tvQuestionId.text = data.questionId
            data.questionLanguage?.let {
                tvLanguage.show()
                tvLanguage.text = it
            } ?: tvLanguage.hide()
            data.askedCount?.let {
                tvPeopleAsked.show()
                tvPeopleAsked.text = it.toString()
            }
            if (data.ocrText.isNotEmpty()) {
                dmathView.text = data.ocrText
            }
            data.questionThumbnail?.let {
                setImageUrl(this, it)
            }

            setOnClickListener {
                // TODO: 13/08/21 Avoid sending this event
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.NCERT_SIMILAR_VIDEO_CLICKED,
                        hashMapOf(
                            EventConstants.PLAYLIST_ID to data.playlistId,
                            EventConstants.QUESTION_ID to data.questionId,
                            EventConstants.SOURCE to source.orEmpty(),
                            EventConstants.ANSWER_ID to (data.answerId ?: -1L)
                        ), ignoreSnowplow = true
                    )
                )

                //Can avoid this event using this action
                actionPerformer?.performAction(NcertVideoClick(data.playlistId, data.activeChapterId))

                val extraParams = if (model.extraParams == null) hashMapOf() else model.extraParams
                extraParams?.put(Constants.WIDGET_CLICK_SOURCE, Constants.WIDGET_TOP_100_QUESTIONS)
                DoubtnutApp.INSTANCE.bus()?.send(
                    WidgetClickedEvent(
                        extraParams = extraParams,
                        widgetType = WidgetTypes.TYPE_WIDGET_NCERT_SIMILAR
                    )
                )

                if (data.deeplink.isNotNullAndNotEmpty()) {
                    deeplinkAction.performAction(context, data.deeplink)
                }
            }
        }

        return holder
    }

    private fun setImageUrl(binding: WidgetNcertSimilarBinding, thumbnailUrl: String) {
        Glide.with(context)
            .load(thumbnailUrl)
            .addListener(object : RequestListener<Drawable> {

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.ivMatch.hide()
                    binding.dmathView.show()
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?, model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?, isFirstResource: Boolean
                ): Boolean {
                    binding.dmathView.hide()
                    binding.ivMatch.show()
                    return false
                }
            })
            .into(binding.ivMatch)
    }

    class WidgetHolder(binding: WidgetNcertSimilarBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetNcertSimilarBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val playlistId: String,
        @SerializedName("active_chapter_id") val activeChapterId: String?,
        @SerializedName("type") val type: String,
        @SerializedName("title") val title: String?,
        @SerializedName("video_title") val videoTitle: String?,
        @SerializedName("is_playing") var isPlaying: Boolean?,
        @SerializedName("question_language") val questionLanguage: String?,
        @SerializedName("ocr_text") val ocrText: String,
        @SerializedName("question_thumbnail") val questionThumbnail: String?,
        @SerializedName("question_id") val questionId: String,
        @SerializedName("question") val question: String,
        @SerializedName("asked_count") val askedCount: Long?,
        @SerializedName("answer_id") val answerId: Long?,
        @SerializedName("video_resources") val resources: List<ApiVideoResource>,
        @SerializedName("card_width") val cardWidth: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("adTagResource") val imaAdTagResource: List<ImaAdTagResource>?
    ) : WidgetData()
}

