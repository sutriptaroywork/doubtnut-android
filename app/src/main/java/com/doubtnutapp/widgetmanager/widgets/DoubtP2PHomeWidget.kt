package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.RemoveP2PHomeWidget
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.databinding.WidgetDoubtP2pHomeBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.hide
import com.doubtnutapp.loadImage
import com.doubtnutapp.show
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnut.core.widgets.ui.WidgetBindingVH

import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.annotations.SerializedName
import javax.inject.Inject
import kotlin.math.min

class DoubtP2PHomeWidget(context: Context) : BaseBindingWidget<DoubtP2PHomeWidget.WidgetHolder,
        DoubtP2PHomeWidget.Model,WidgetDoubtP2pHomeBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var userPreference: UserPreference

    var source: String? = null

    init{
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetDoubtP2pHomeBinding {
        return WidgetDoubtP2pHomeBinding.inflate(LayoutInflater.from(context),this,true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(),this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model)
        setMargins(
            WidgetLayoutConfig(
                marginTop = 0,
                marginBottom = 0,
                marginLeft = 0,
                marginRight = 0
            )
        )
        val data = model.data

        holder.binding.apply {

            visibility = VISIBLE

            setQuestion(data.imageUrl, data.questionText)
            ivQuestion.setOnClickListener {
                deeplinkAction.performAction(context, data.deeplink, source.orEmpty())
                sendEvent()
            }
            tvQuestion.setOnClickListener {
                deeplinkAction.performAction(context, data.deeplink, source.orEmpty())
                sendEvent()
            }

            if (data.isAnyMemberJoined != true) {
                membersJoinLayout.hide()
                tvDescription.show()
                tvDescription.text = data.description
            } else {
                membersJoinLayout.show()
                tvDescription.hide()

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

                tvMessage.text = data.description

                if (data.unreadCount != null && data.unreadCount != 0) {
                    tvChatCount.show()
                    tvChatCount.text = data.unreadCount?.toString()
                } else {
                    tvChatCount.hide()
                }

                tvIgnore.text = data.secondaryCtaText
                tvJoinNow.text = data.ctaText
                tvJoinNow.setOnClickListener {
                    deeplinkAction.performAction(context, data.deeplink, source.orEmpty())
                    sendEvent()
                }

                tvIgnore.setOnClickListener {
                    userPreference.setDoubtP2PHomeWidgetVisibility(false)
                    actionPerformer?.performAction(RemoveP2PHomeWidget(WidgetTypes.TYPE_WIDGET_DOUBT_P2P_HOME))
                }
            }
        }

        return holder
    }

    private fun setQuestion(questionImage: String?, questionText: String?) {
        Glide.with(widgetViewHolder.binding.root.context)
            .load(questionImage)
            .addListener(object : RequestListener<Drawable> {

                override fun onLoadFailed(
                    e: GlideException?, model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    widgetViewHolder.binding.tvQuestion.text = questionText
                    widgetViewHolder.binding.tvQuestion.show()
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?, model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?, isFirstResource: Boolean
                ): Boolean {
                    widgetViewHolder.binding.tvQuestion.hide()
                    return false
                }
            })
            .into(widgetViewHolder.binding.ivQuestion)
    }

    private fun sendEvent(){
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                 EventConstants.EVENT_ITEM_CLICK, ignoreSnowplow = true
            )
        )
    }

    class WidgetHolder(binding: WidgetDoubtP2pHomeBinding,widget: BaseWidget<*, *>) :
            WidgetBindingVH<WidgetDoubtP2pHomeBinding>(binding,widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String?,
        @SerializedName("members_image") val membersImage: List<String>?,
        @SerializedName("text") val description: String?,
        @SerializedName("is_any_member_joined") val isAnyMemberJoined: Boolean?,
        @SerializedName("question_image") val imageUrl: String?,
        @SerializedName("question_text") val questionText: String?,
        @SerializedName("cta_text") val ctaText: String?,
        @SerializedName("top_icon") val topIcon: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("secondary_cta_text") val secondaryCtaText: String?,
        @SerializedName("card_width") val cardWidth: String?,
        @SerializedName("deeplink") var deeplink: String?,
        @SerializedName("unread_count") var unreadCount: Int?,
    ) : WidgetData()
}