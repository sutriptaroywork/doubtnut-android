package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
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
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetDoubtP2pBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.hide
import com.doubtnutapp.loadImage
import com.doubtnutapp.show
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.annotations.SerializedName
import javax.inject.Inject
import kotlin.math.min

class DoubtP2PWidget(context: Context) : BaseBindingWidget<DoubtP2PWidget.WidgetHolder,
        DoubtP2PWidget.Model, WidgetDoubtP2pBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    var source: String? = null

    override fun getViewBinding(): WidgetDoubtP2pBinding {
        return WidgetDoubtP2pBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        val data = model.data
        val binding = holder.binding

        binding.apply {

            setQuestion(data.questionImage, data.questionText, binding)

            val ivList: List<ShapeableImageView> = listOf(ivStudyDost1, ivStudyDost2)
            ivList.forEach {
                it.hide()
            }
            val images: List<String>? = data.membersImage

            if (images.isNullOrEmpty().not()) {
                val size = images?.size
                for (i in 0 until min(2, size!!)) {
                    ivList[i].apply {
                        loadImage(images[i])
                        show()
                    }
                }
            }

            ivTopIcon.apply {
                data.topIcon?.let { icon ->
                    show()
                    loadImage(icon)
                    background.alpha = 123
                } ?: hide()
            }

            tvDate.apply {
                data.createdAt?.let {
                    show()
                    text = it
                } ?: hide()
            }

            tvChatCount.apply {
                data.messageCount?.let { count ->
                    isVisible = count > 0
                    text = count.toString()
                } ?: hide()
            }

            tvChatAction.apply {
                isVisible = data.ctaText != null
                text = data.ctaText
            }

            ivMessage.apply {
                data.messageIcon?.let { icon ->
                    show()
                    loadImage(icon)
                } ?: hide()
            }

            tvMemberCount.apply {
                data.peopleCount?.let { count ->
                    isVisible = count > 0
                    text = count.toString()
                } ?: hide()
            }

            setOnClickListener {
                deeplinkAction.performAction(context, data.deeplink, source.orEmpty())
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.P2P_WIDGET_CTA_CLICK,
                        hashMapOf(
                            EventConstants.SOURCE to source.orEmpty(),
                            EventConstants.CTA_TEXT to data.ctaText.orEmpty()
                        ), ignoreSnowplow = true, ignoreMoengage = false
                    )
                )
            }
        }
        return holder
    }

    private fun setQuestion(
        questionImage: String?,
        questionText: String?,
        binding: WidgetDoubtP2pBinding
    ) {
        Glide.with(binding.root.context)
            .load(questionImage)
            .addListener(object : RequestListener<Drawable> {

                override fun onLoadFailed(
                    e: GlideException?, model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.tvQuestion.text = questionText
                    binding.tvQuestion.show()
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?, model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?, isFirstResource: Boolean
                ): Boolean {
                    binding.tvQuestion.hide()
                    return false
                }
            })
            .into(binding.ivQuestion)
    }

    class WidgetHolder(binding: WidgetDoubtP2pBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetDoubtP2pBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("question_id") val questionId: String?,
        @SerializedName("members_image") val membersImage: List<String>?,
        @SerializedName("created_at") val createdAt: String?,
        @SerializedName("question_image") val questionImage: String?,
        @SerializedName("top_icon") val topIcon: String?,
        @SerializedName("room_id") val roomId: String?,
        @SerializedName("question_text") val questionText: String?,
        @SerializedName("deeplink") var deeplink: String?,
        @SerializedName("message_count") var messageCount: Int?,
        @SerializedName("people_count") var peopleCount: Int?,
        @SerializedName("cta_text") var ctaText: String?,
        @SerializedName("message_icon") var messageIcon: String?,
    ) : WidgetData()

}