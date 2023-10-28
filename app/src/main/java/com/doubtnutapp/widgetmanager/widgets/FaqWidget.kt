package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ViewUtils
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemFaqBinding
import com.doubtnutapp.databinding.WidgetFaqBinding
import com.doubtnutapp.dialogHolder.DialogHolderActivity
import com.doubtnutapp.videoPage.model.VideoResource
import com.doubtnutapp.widgets.SpaceItemDecoration
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

class FaqWidget(context: Context) : BaseBindingWidget<FaqWidget.FaqWidgetHolder,
        FaqWidget.FaqWidgetModel, WidgetFaqBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetFaqBinding {
        return WidgetFaqBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = FaqWidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: FaqWidgetHolder, model: FaqWidgetModel): FaqWidgetHolder {
        super.bindWidget(holder, model)
        setMargins(WidgetLayoutConfig(0, 0, 0, 0))
        val data: FaqWidgetData = model.data
        val binding = holder.binding

        binding.rvFaq.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.rvFaq.adapter = FaqAdapter(
            data.items!!,
            actionPerformer, analyticsPublisher, model.extraParams ?: HashMap()
        )
        if (binding.rvFaq.itemDecorationCount == 0)
            binding.rvFaq.addItemDecoration(
                SpaceItemDecoration(
                    ViewUtils.dpToPx(
                        8f,
                        context!!
                    ).toInt()
                )
            )
        return holder
    }

    class FaqAdapter(
        val items: List<FaqItem>?,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val extraParams: HashMap<String, Any>,
        val isShowOnTBP: Boolean = false
    ) : RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

        var mLastClickTime: Long = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
            return FaqViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_faq, parent, false)
            )
        }

        override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
            holder.binding.apply {
                val faqItem = items?.get(position)
                tvQuestion.text = items?.get(position)?.question
                updateToggle(
                    holder.itemView.context,
                    ivExpand,
                    faqItem?.is_expand,
                    faqVideoContainer,
                    tvAnswer,
                    videoIcon,
                    answerThumbnail,
                    faqItem!!
                )
                viewToggleHelper.setOnClickListener {
                    faqItem.is_expand = faqItem.is_expand != true
                    updateToggle(
                        holder.itemView.context,
                        ivExpand,
                        faqItem.is_expand,
                        faqVideoContainer,
                        tvAnswer,
                        videoIcon,
                        answerThumbnail,
                        faqItem
                    )
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.FAQ_ITEM_EXPAND_COLLAPSE,
                            hashMapOf<String, Any>(
                                EventConstants.ITEM_ID to faqItem.id,
                                EventConstants.PRIORITY to faqItem.priority,
                                EventConstants.ITEM_POSITION to position
                            ).apply {
                                putAll(extraParams)
                            }, ignoreSnowplow = true
                        )
                    )
                }

                videoIcon.setOnClickListener {
                    playVideo(position, holder.itemView.context, isShowOnTBP)
                }

                answerThumbnail.setOnClickListener {
                    if (faqItem.type == "video") {
                        playVideo(position, holder.itemView.context, isShowOnTBP)
                    }
                }
            }
        }

        private fun playVideo(position: Int, context: Context, isShowOnTBP: Boolean) {
            val faqItem = items?.get(position)
            if (System.currentTimeMillis() - mLastClickTime < 1000) return
            mLastClickTime = System.currentTimeMillis()

            if (isShowOnTBP) {
                analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.TBG_FAQ_CLICKED))
            } else {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.FAQ_ITEM_VIDEO_PLAY,
                        hashMapOf<String, Any>(
                            EventConstants.ITEM_ID to faqItem?.id!!,
                            EventConstants.QUESTION_ID to faqItem.question_id!!
                        ).apply {
                            putAll(extraParams)
                        }, ignoreSnowplow = true
                    )
                )
            }
            if (faqItem?.video_resources != null) {
                val intent = DialogHolderActivity.getVideoWithUrlIntent(
                    context = context,
                    url = faqItem.video_resources[0].resource
                )
                context.startActivity(intent)
            }
        }

        private fun updateToggle(
            context: Context,
            imageView: ImageView,
            toggle: Boolean?, faqVideoContainer: View,
            answerView: TextView,
            videoIcon: ImageView, answerThumbnail: ImageView,
            faqItem: FaqItem
        ) {
            val toggleResID: Int
            if (toggle == true) {
                toggleResID = R.drawable.ic_arrow_up_24px
                if (faqItem.answer == null) {
                    answerView.hide()
                } else {
                    answerView.show()
                    answerView.text = faqItem.answer
                }

                when (faqItem.type) {
                    "text" -> {
                        faqVideoContainer.hide()
                    }
                    "image" -> {
                        faqVideoContainer.show()
                        videoIcon.hide()
                        answerThumbnail.loadImage(faqItem.thumbnail, R.color.gray_c4c4c4)
                        answerThumbnail.show()
                    }
                    "video" -> {
                        answerThumbnail.loadImage(faqItem.thumbnail, R.color.gray_c4c4c4)
                        videoIcon.loadImage(faqItem.video_icon_url, R.color.md_deep_orange_200)
                        faqVideoContainer.show()
                        videoIcon.show()
                        answerThumbnail.show()
                    }
                }
            } else {
                toggleResID = R.drawable.ic_arrow_down_24px
                faqVideoContainer.hide()
                answerView.hide()
            }

            imageView.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    toggleResID
                )
            )

        }

        override fun getItemCount(): Int = items?.size!!

        class FaqViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val binding = ItemFaqBinding.bind(itemView)
        }
    }

    class FaqWidgetHolder(binding: WidgetFaqBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetFaqBinding>(binding, widget)

    class FaqWidgetModel : WidgetEntityModel<FaqWidgetData, WidgetAction>()

    @Keep
    data class FaqWidgetData(
        @SerializedName("faq_list") val items: List<FaqItem>?,
    ) : WidgetData()

    @Keep
    @Parcelize
    data class FaqItem(
        @SerializedName("id") val id: Int,
        @SerializedName("bucket") val bucket: String,
        @SerializedName("question") val question: String,
        @SerializedName("type") val type: String,
        @SerializedName("answer") val answer: String?,
        @SerializedName("question_id") val question_id: Int?,
        @SerializedName("page") val page: String?,
        @SerializedName("thumbnail") val thumbnail: String?,
        @SerializedName("video_orientation") val video_orientation: String?,
        @SerializedName("video_icon_url") val video_icon_url: String?,
        @SerializedName("priority") val priority: Int,
        @SerializedName("is_expand") var is_expand: Boolean?,
        @SerializedName("video_resources") val video_resources: List<VideoResource>?,
    ) : Parcelable
}