package com.doubtnutapp.course.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.applyBackgroundColor
import com.doubtnut.core.utils.applyRippleColor
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.ItemYourWereWatchingV2Binding
import com.doubtnutapp.databinding.WidgetYouWereWatchingV2Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.material.color.MaterialColors
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.item_your_were_watching_v2.view.*
import javax.inject.Inject

class YouWereWatchingV2Widget(
    context: Context
) : BaseBindingWidget<YouWereWatchingV2Widget.WidgetHolder, YouWereWatchingV2Widget.Model, WidgetYouWereWatchingV2Binding>(
    context
) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    override fun getViewBinding(): WidgetYouWereWatchingV2Binding {
        return WidgetYouWereWatchingV2Binding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: Model
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        if(model.data.backgroundColor.isNotNullAndNotEmpty()) {
            binding.root.applyBackgroundColor(model.data.backgroundColor)
        }

        binding.tvTitle.isVisible = model.data.title.isNullOrEmpty().not()
        binding.tvTitle.text = model.data.title
        binding.tvTitle.applyTextSize(model.data.titleTextSize)
        binding.tvTitle.applyTextColor(model.data.titleTextColor)

        binding.rvMain.adapter = Adapter(
            model.data.items.orEmpty()
        )



        if(model.data.cta.isNotNullAndNotEmpty()){
            binding.buttonWatchNow.visibility = View.VISIBLE
            binding.buttonWatchNow.text = model.data.cta
            binding.buttonWatchNow.setOnClickListener {
                deeplinkAction.performAction(context,model.data.ctaDeeplink)
                analyticsPublisher.publishEvent(AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.WATCH_NOW_BUTTON_CLICKED}",
                ))
            }
            binding.lottieAnimationStart.resumeAnimation()
            binding.lottieAnimationStart.rotation= 180F
            binding.lottieAnimationEnd.resumeAnimation()
            binding.lottieAnimationStart.visibility = View.VISIBLE
            binding.lottieAnimationEnd.visibility = View.VISIBLE
        }
        else{
            binding.buttonWatchNow.visibility = View.GONE
            binding.lottieAnimationStart.visibility = View.GONE
            binding.lottieAnimationEnd.visibility = View.GONE
        }

        if(model.data.icon.isNotNullAndNotEmpty()){
            binding.imageTopIcon.visibility = View.VISIBLE
            binding.imageTopIcon.loadImage(model.data.icon)
        }
        else{
            binding.imageTopIcon.visibility = View.GONE
        }


        return holder
    }

    class WidgetHolder(binding: WidgetYouWereWatchingV2Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetYouWereWatchingV2Binding>(binding, widget)

    inner class Adapter(
        val items: List<Item>,
    ) :
        RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemYourWereWatchingV2Binding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind()
        }

        override fun getItemCount(): Int {
            return items.size
        }

        inner class ViewHolder(val binding: ItemYourWereWatchingV2Binding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind() {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
                if (itemCount > 1) {
                    Utils.setWidthBasedOnPercentage(context, binding.root, "1.25", R.dimen.spacing)
                }

                items[bindingAdapterPosition].let { item ->

                    binding.imageViewBackground.loadImage(item.imageBgCard.ifEmptyThenNull())

                    binding.ivImage.isVisible = item.imageUrl.isNullOrEmpty().not()
                    binding.ivImage.loadImage(item.imageUrl)
                    binding.ivIcon.isVisible = item.icon.isNullOrEmpty().not()
                    binding.ivIcon.loadImage(item.icon)

                    binding.tvTitleOne.isVisible = item.titleOne.isNullOrEmpty().not()
                    binding.tvTitleOne.text = item.titleOne
                    binding.tvTitleOne.applyTextSize(item.titleOneTextSize)
                    binding.tvTitleOne.applyTextColor(item.titleOneTextColor)

                    binding.tvTitleTwo.isVisible = item.titleTwo.isNullOrEmpty().not()
                    binding.tvTitleTwo.text = item.titleTwo
                    binding.tvTitleTwo.applyTextSize(item.titleTwoTextSize)
                    binding.tvTitleTwo.applyTextColor(item.titleTwoTextColor)

                    binding.tvTitleThree.isVisible = item.titleThree.isNullOrEmpty().not()
                    binding.tvTitleThree.text = item.titleThree
                    binding.tvTitleThree.applyTextSize(item.titleThreeTextSize)
                    binding.tvTitleThree.applyTextColor(item.titleThreeTextColor)

                    binding.viewProgress.isVisible =
                        item.watchedTime != null && item.totalTime != null
                    binding.viewProgressBackground.isVisible =
                        item.watchedTime != null && item.totalTime != null

                    if (item.watchedTime != null && item.totalTime != null) {
                        binding.guidelineProgress.setGuidelinePercent(item.watchedTime / item.totalTime)
                    }

                    if (item.deeplink.isNullOrEmpty()) {
                        binding.cardItemWidget.applyRippleColor("#00000000")
                    } else {
                        binding.root.cardItemWidget.rippleColor = ColorStateList.valueOf(
                            MaterialColors.getColor(binding.root, R.attr.colorControlHighlight)
                        )
                    }

                    // showing duration start and duration end in Minutes:Seconds format
                    // and showing progress according to watched time
                    binding.apply {
                        if(item.showDurationText) {
                            if (item.watchedTime != null && item.totalTime != null) {

                                layoutTextDuration.visibility = View.VISIBLE

                                val watchedTimeSecond =( item.watchedTime/60)
                                val totalTimeSecond = (item.totalTime/60)
                                val progress = (watchedTimeSecond/ totalTimeSecond)*100

                                progressDuration.max = 100

                                progressDuration.post {
                                    progressDuration.progress = progress.toInt()
                                }

                                val watchedTimeMinute =  (item.watchedTime/60).toInt()
                                val watchedTimeSecondRem = item.watchedTime.toInt() - watchedTimeMinute*60.toInt()
                                val watchedTimeString = String.format("%02d:%02d", watchedTimeMinute, watchedTimeSecondRem);

                                val totalTimeMinute =  (item.totalTime/60).toInt()
                                val totalTimeSecondRem = item.totalTime.toInt() - totalTimeMinute*60
                                val totalTimeString = String.format("%02d:%02d", totalTimeMinute,totalTimeSecondRem);

                                textViewStartTime.text = watchedTimeString
                                textViewEndTime.text = totalTimeString

                                // hide existing progress view for this case
                                viewProgress.visibility = View.GONE
                                viewProgressBackground.visibility = View.GONE

                            }
                        }
                        else{
                            layoutTextDuration.visibility = View.GONE
                        }
                    }

                    binding.apply {
                        if(item.viewCount.isNotNullAndNotEmpty()){
                            layoutUsersWatching.visibility = View.VISIBLE
                            textViewUsersWatched.text = item.viewCount.toString()
                        }
                        else{
                            layoutUsersWatching.visibility = View.GONE
                        }
                    }


                    binding.root.setOnClickListener {
                        if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                        deeplinkAction.performAction(context, item.deeplink)
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                "${EVENT_TAG}_${EventConstants.CARD_CLICKED}",
                                hashMapOf<String, Any>(
                                    EventConstants.WIDGET to TAG,
                                    EventConstants.STUDENT_ID to UserUtil.getStudentId()
                                ).apply {
                                    putAll(item.extraParams.orEmpty())
                                }
                            )
                        )
                    }
                }
            }
        }
    }

    @Keep
    class Model :
        WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title", alternate = ["text_one"])
        var title: String?,
        @SerializedName("title_text_color", alternate = ["text_one_color"])
        var titleTextColor: String?,
        @SerializedName("title_text_size", alternate = ["text_one_size"])
        var titleTextSize: String?,
        @SerializedName("background_color", alternate = ["bg_color"])
        var backgroundColor: String?,
        @SerializedName("items", alternate = ["videos"])
        val items: List<Item>?,
        @SerializedName("text_icon")
        val icon:String,
        @SerializedName("cta")
        val cta: String?,
        @SerializedName("cta_deeplink")
        val ctaDeeplink:String?


    ) : WidgetData()

    @Keep
    data class Item(
        @SerializedName("question_id")
        val questionId: String?,
        @SerializedName("watched_time")
        val watchedTime: Float?,
        @SerializedName("total_time")
        val totalTime: Float?,

        @SerializedName("title_one", alternate = ["title1", "text_one"])
        val titleOne: String?,
        @SerializedName("title_one_text_size", alternate = ["title1_text_size", "text_one_size"])
        val titleOneTextSize: String?,
        @SerializedName("title_one_text_color", alternate = ["title1_text_color", "text_one_color"])
        val titleOneTextColor: String?,

        @SerializedName("title_two", alternate = ["title2", "text_two"])
        val titleTwo: String?,
        @SerializedName("title_two_text_size", alternate = ["title2_text_size", "text_two_size"])
        val titleTwoTextSize: String?,
        @SerializedName("title_two_text_color", alternate = ["title2_text_color", "text_two_color"])
        val titleTwoTextColor: String?,

        @SerializedName("title_three", alternate = ["title3", "text_three"])
        val titleThree: String?,
        @SerializedName(
            "title_three_text_size",
            alternate = ["title3_text_size", "text_three_size"]
        )
        val titleThreeTextSize: String?,
        @SerializedName(
            "title_three_text_color",
            alternate = ["title3_text_color", "text_three_color"]
        )
        val titleThreeTextColor: String?,

        @SerializedName("background_color", alternate = ["bg_color"])
        val backgroundColor: String?,
        @SerializedName("deeplink")
        val deeplink: String?,
        @SerializedName("image_url", alternate = ["thumbnail_image"])
        val imageUrl: String?,
        @SerializedName("icon", alternate = ["icon_url"])
        val icon: String?,
        @SerializedName("views")
        val viewCount:String?,
        @SerializedName("show_duration_text")
        val showDurationText:Boolean,

        @SerializedName("image_bg_card")
        val imageBgCard: String?,

        @SerializedName("extra_params")
        var extraParams: HashMap<String, Any>? = null,
    ) : WidgetData()

    companion object {
        const val TAG = "YouWereWatchingV2Widget"
        const val EVENT_TAG = "you_were_watching_v_2_widget"
    }
}
