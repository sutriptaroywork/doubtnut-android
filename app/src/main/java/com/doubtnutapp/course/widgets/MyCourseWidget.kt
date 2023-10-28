package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.*
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.ItemMyCourseBinding
import com.doubtnutapp.databinding.WidgetMyCoursesBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class MyCourseWidget(
    context: Context
) : BaseBindingWidget<MyCourseWidget.WidgetHolder, MyCourseWidgetModel, WidgetMyCoursesBinding>(
    context
) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetMyCoursesBinding {
        return WidgetMyCoursesBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: MyCourseWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        if (!model.data.backgroundColor.isNullOrEmpty()) {
            holder.itemView.setBackgroundColor(Color.parseColor(model.data.backgroundColor))
        }

        if (model.data.title.isNullOrEmpty()) {
            binding.tvWidgetName.hide()
        } else {
            binding.tvWidgetName.show()
        }
        binding.tvWidgetName.text = model.data.title
        binding.rvMyCourses.adapter = MyCourseAdapter(
            context,
            model.data.items.orEmpty(),
            analyticsPublisher,
            deeplinkAction
        )
        return holder
    }

    class WidgetHolder(binding: WidgetMyCoursesBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetMyCoursesBinding>(binding, widget)

    companion object {
        @Suppress("unused")
        private const val TAG = "MyCourseWidget"
    }
}

class MyCourseAdapter(
    val context: Context,
    val items: List<MyCourseWidgetItem>,
    val analyticsPublisher: AnalyticsPublisher,
    val deeplinkAction: DeeplinkAction
) :
    RecyclerView.Adapter<MyCourseAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemMyCourseBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Utils.setWidthBasedOnPercentage(context, holder.itemView, "1.15", R.dimen.spacing)
        holder.bind()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: ItemMyCourseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            items[bindingAdapterPosition].let { item ->
                binding.root.applyBackgroundColor(item.backgroundColor)

                binding.tvTitle.text = item.title
                binding.tvTitle.applyTextSize(item.titleSize)
                binding.tvTitle.applyTextColor(item.titleColor)

                binding.tvSubtitle.text = item.subTitle
                binding.tvSubtitle.applyTextColor(item.subTitleColor)
                binding.tvSubtitle.applyTextSize(item.subTitleSize)

                if (item.bulletPoint.isNullOrEmpty()) {
                    binding.ivBulletImage.hide()
                } else {
                    binding.ivBulletImage.show()
                    if (!item.bulletImageColor.isNullOrEmpty()) {
                        binding.ivBulletImage.background.setTint(Color.parseColor(item.bulletImageColor))
                    }
                }

                TextViewUtils.setTextFromHtml(
                    binding.tvBulletLabel,
                    item.bulletPoint.orEmpty()
                )

                binding.tvBulletLabel.applyTextSize(item.bulletPointSize)
                binding.tvBulletLabel.applyTextColor(item.bulletPointColor)

                if (item.bulletPointTwo.isNullOrEmpty()) {
                    binding.ivBulletImageTwo.hide()
                } else {
                    binding.ivBulletImageTwo.show()
                    if (!item.bulletImageTwoColor.isNullOrEmpty()) {
                        binding.ivBulletImageTwo.background.setTint(Color.parseColor(item.bulletImageTwoColor))
                    }
                }

                TextViewUtils.setTextFromHtml(
                    binding.tvBulletLabelTwo,
                    item.bulletPointTwo.orEmpty()
                )

                binding.tvBulletLabelTwo.applyTextSize(item.bulletPointSizeTwo)
                binding.tvBulletLabelTwo.applyTextColor(item.bulletPointColorTwo)

                binding.tvProgress.text = item.progress
                binding.tvProgress.applyTextSize(item.progressSize)
                binding.tvProgress.applyTextColor(item.progressTextColor)

                item.progress?.replace("%", "")?.toFloatOrNull()?.let { progress ->
                    binding.guidelineProgress.setGuidelinePercent(progress / 100)
                }

                if (!item.progressColor.isNullOrEmpty()) {
                    binding.viewProgress.setBackgroundColor(Color.parseColor(item.progressColor))
                }

                binding.root.setOnClickListener {
                    if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                    deeplinkAction.performAction(context, item.deeplink)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.MY_COURSE_WIDGET_CLICKED,
                            hashMapOf<String, Any>(EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty()).apply {
                                putAll(item.extraParams.orEmpty())
                            },
                            ignoreSnowplow = true
                        )
                    )
                }

                binding.containerTrialInfo.isVisible = item.trialTitle.isNotNullAndNotEmpty() ||
                    (item.time != null && item.time > 0L)

                binding.containerTrialInfo.background = GradientUtils.getGradientBackground(
                    item.bgColorOne,
                    item.bgColorTwo,
                    GradientDrawable.Orientation.LEFT_RIGHT
                )

                if (item.trialTitle.isNullOrEmpty()) {
                    binding.tvTrialInfo.visibility = GONE
                } else {
                    binding.tvTrialInfo.visibility = VISIBLE
                    binding.tvTrialInfo.text = item.trialTitle
                    binding.tvTrialInfo.applyTextSize(item.trialTitleSize)
                    binding.tvTrialInfo.applyTextColor(item.trialTitleColor)
                }

                if (item.imageUrl.isNullOrEmpty()) {
                    binding.ivGif.visibility = GONE
                } else {
                    binding.ivGif.visibility = VISIBLE
                    Glide.with(context).load(item.imageUrl).into(binding.ivGif)
                }

                if (item.time == null || item.time <= 0L) {
                    binding.tvTimer.visibility = GONE
                    binding.ivGif.visibility = GONE
                } else {
                    val actualTimeLeft =
                        ((item.time.or(0)).minus(System.currentTimeMillis()))

                    if (actualTimeLeft > 0) {
                        binding.tvTimer.visibility = VISIBLE
                        binding.tvTimer.applyTextColor(item.timeTextColor)
                        binding.tvTimer.applyTextSize(item.timeTextSize)

                        val timer = object : CountDownTimer(
                            actualTimeLeft, 1000
                        ) {
                            override fun onTick(millisUntilFinished: Long) {
                                binding.tvTimer.text =
                                    DateTimeUtils.formatMilliSecondsToTime(millisUntilFinished)
                            }

                            override fun onFinish() {
                                binding.ivGif.visibility = GONE
                                binding.tvTimer.visibility = GONE

                                binding.containerTrialInfo.background = GradientUtils.getGradientBackground(
                                    item.bgColorOneExpired,
                                    item.bgColorTwoExpired,
                                    GradientDrawable.Orientation.LEFT_RIGHT
                                )
                                binding.tvTrialInfo.text = item.trialTitleExpired

                                item.trialTitle = item.trialTitleExpired
                                item.bgColorOne = item.bgColorOneExpired
                                item.bgColorTwo = item.bgColorTwoExpired
                            }
                        }
                        timer.start()
                    } else {
                        binding.tvTimer.visibility = GONE
                        binding.ivGif.visibility = GONE

                        binding.containerTrialInfo.background = GradientUtils.getGradientBackground(
                            item.bgColorOneExpired,
                            item.bgColorTwoExpired,
                            GradientDrawable.Orientation.LEFT_RIGHT
                        )
                        binding.tvTrialInfo.text = item.trialTitleExpired

                        item.trialTitle = item.trialTitleExpired
                        item.bgColorOne = item.bgColorOneExpired
                        item.bgColorTwo = item.bgColorTwoExpired
                    }
                }
            }
        }
    }
}

@Keep
class MyCourseWidgetModel :
    WidgetEntityModel<MyCourseWidgetData, WidgetAction>()

@Keep
data class MyCourseWidgetData(
    @SerializedName("title")
    var title: String?,
    @SerializedName("background_color")
    var backgroundColor: String?,
    @SerializedName("items")
    val items: List<MyCourseWidgetItem>?
) : WidgetData()

@Keep
data class MyCourseWidgetItem(
    @SerializedName("background_color")
    var backgroundColor: String?,

    @SerializedName("assortment_id")
    val assortmentId: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("deeplink")
    val deeplink: String?,
    @SerializedName("title_color")
    val titleColor: String?,
    @SerializedName("title_size")
    val titleSize: String?,
    @SerializedName("sub_title")
    val subTitle: String?,
    @SerializedName("sub_title_color")
    val subTitleColor: String?,
    @SerializedName("sub_title_size")
    val subTitleSize: String?,

    @SerializedName("bullet_point")
    val bulletPoint: String?,
    @SerializedName("bullet_point_color")
    val bulletPointColor: String?,
    @SerializedName("bullet_point_size")
    val bulletPointSize: String?,
    @SerializedName("bullet_image_color")
    val bulletImageColor: String?,

    @SerializedName("bullet_point_two")
    val bulletPointTwo: String?,
    @SerializedName("bullet_point_two_color")
    val bulletPointColorTwo: String?,
    @SerializedName("bullet_point_two_size")
    val bulletPointSizeTwo: String?,
    @SerializedName("bullet_image_two_color")
    val bulletImageTwoColor: String?,

    @SerializedName("progress")
    val progress: String?,
    @SerializedName("progress_text_color")
    val progressTextColor: String?,
    @SerializedName("progress_color")
    val progressColor: String?,
    @SerializedName("progress_size")
    val progressSize: String?,

    @SerializedName("extra_params")
    var extraParams: HashMap<String, Any>? = null,

    @SerializedName("trial_title")
    var trialTitle: String?,
    @SerializedName("trial_title2")
    var trialTitle2: String?,
    @SerializedName("trial_title_expired")
    val trialTitleExpired: String?,
    @SerializedName("trial_title_expired2")
    val trialTitleExpired2: String?,
    @SerializedName("trial_title_size")
    val trialTitleSize: String?,
    @SerializedName("trial_title_color")
    val trialTitleColor: String?,
    @SerializedName("time")
    val time: Long?,
    @SerializedName("time_text_color")
    val timeTextColor: String?,
    @SerializedName("time_text_size")
    val timeTextSize: String?,
    @SerializedName("bg_color_one")
    var bgColorOne: String?,
    @SerializedName("bg_color_two")
    var bgColorTwo: String?,
    @SerializedName("bg_color_one_expired")
    val bgColorOneExpired: String?,
    @SerializedName("bg_color_two_expired")
    val bgColorTwoExpired: String?,

    @SerializedName("image_url")
    val imageUrl: String?,
) : WidgetData()
