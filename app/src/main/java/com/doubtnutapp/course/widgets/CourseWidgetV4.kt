package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemCourseV4Binding
import com.doubtnutapp.databinding.WidgetCourseV4Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.hide
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.doubtnutapp.widgets.TimerWidget
import com.google.android.material.shape.CornerFamily
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CourseWidgetV4(context: Context) :
    BaseBindingWidget<CourseWidgetV4.WidgetViewHolder,
            CourseWidgetV4.CourseWidgetModelV4, WidgetCourseV4Binding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null
    private var autoScrollJob: Job? = null
    var isAutoScrollWidgetInteractionDone = false
    private lateinit var data: CourseWidgetDataV4

    companion object {
        const val TAG = "CourseWidgetV4"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    override fun getViewBinding(): WidgetCourseV4Binding {
        return WidgetCourseV4Binding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetViewHolder,
        model: CourseWidgetModelV4
    ): WidgetViewHolder {
        super.bindWidget(holder, model)
        data = model.data
        val binding = holder.binding
        val snapHelper = PagerSnapHelper()

        binding.tvTitle.isVisible = data.title.isNullOrEmpty().not()
        binding.tvTitle.text = data.title.orEmpty()
        binding.rvCourse.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL, false
        )
        binding.rvCourse.adapter = Adapter(
            data.items.orEmpty(),
            actionPerformer,
            analyticsPublisher,
            deeplinkAction,
            context, model.extraParams
        )

        binding.circleIndicator.isVisible = data.items?.size ?: 0 > 1
        (context as? AppCompatActivity)?.let { appCompatActivity ->

            binding.rvCourse.addOnItemTouchListener(object :
                RecyclerView.SimpleOnItemTouchListener() {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    if (rv.scrollState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        isAutoScrollWidgetInteractionDone = true
                    }
                    return super.onInterceptTouchEvent(rv, e)
                }
            })

            binding.circleIndicator.attachToRecyclerView(binding.rvCourse, snapHelper)
            binding.rvCourse.smoothScrollToPosition(model.data.selectedPagePosition)

            autoScrollJob?.cancel()
            model.data.autoScrollTimeInSec?.let { autoScrollTimeInSec ->
                if (autoScrollTimeInSec > 0L) {
                    autoScrollJob = appCompatActivity.lifecycleScope.launchWhenResumed {
                        autoRotate(
                            binding.rvCourse,
                            autoScrollTimeInSec,
                            model.data.items?.lastIndex ?: 0
                        )
                    }
                }
            }
        }

        return holder
    }

    private suspend fun autoRotate(
        recyclerView: RecyclerView,
        delayInSec: Long,
        lastIndex: Int
    ) {
        delay(TimeUnit.SECONDS.toMillis(delayInSec))
        if (isAutoScrollWidgetInteractionDone) {
            isAutoScrollWidgetInteractionDone = false
        } else {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val visibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
            val scrollToPosition =
                if (visibleItemPosition == lastIndex) 0 else visibleItemPosition + 1
            recyclerView.smoothScrollToPosition(scrollToPosition)
            data.selectedPagePosition = scrollToPosition
        }
        autoRotate(recyclerView, delayInSec, lastIndex)
    }

    class Adapter(
        val items: List<CourseWidgetItemsV4>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val context: Context,
        val extraParams: HashMap<String, Any>?
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        class ViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemCourseV4Binding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val binding = holder.binding as ItemCourseV4Binding
            val item = items[position]
            with(binding) {
                if (items.any { it.widgetTimer != null }) {
                    widgetTimerView.isInvisible = item.widgetTimer == null
                } else {
                    widgetTimerView.isVisible = item.widgetTimer != null
                }

                item.widgetTimer?.let { widgetTimerData ->
                    widgetTimerView.bindWidget(
                        widgetTimerView.widgetViewHolder,
                        TimerWidget.Model().apply {
                            _data = widgetTimerData
                            layoutConfig = WidgetLayoutConfig(
                                marginTop = 16,
                                marginBottom = 0,
                                marginLeft = 6,
                                marginRight = 6
                            )
                        }
                    )
                }

                val topRadius = if (item.widgetTimer == null) {
                    4.dpToPxFloat()
                } else {
                    0.dpToPxFloat()
                }

                parentLayout.shapeAppearanceModel = parentLayout.shapeAppearanceModel.toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, topRadius)
                    .setTopRightCorner(CornerFamily.ROUNDED, topRadius)
                    .setBottomLeftCorner(CornerFamily.ROUNDED, 4.dpToPxFloat())
                    .setBottomRightCorner(CornerFamily.ROUNDED, 4.dpToPxFloat())
                    .build()

                cardView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    dimensionRatio = item.cardRatio ?: "16:9"
                }

                rvPlayer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    dimensionRatio = item.cardRatio ?: "16:9"
                }

                binding.tvHeader.applyBackgroundColor(item.headerBackgroundColor)
                binding.tvHeader.text = item.headerTitle
                binding.tvHeader.applyTextSize(item.headerTitleTextSize)
                binding.tvHeader.applyTextColor(item.headerTitleTextColor)
                binding.tvHeader.isVisible = item.headerTitle.isNullOrEmpty().not()

                tvMedium.text = item.mediumText.orEmpty()
                tvMedium.setBackgroundColor(Color.parseColor(item.mediumBgColor))

                textViewTitleInfo.text = item.title.orEmpty()
                tvTitle.text = item.courseTitle.orEmpty()
                tvPrice.text = item.price.orEmpty()
                if (item.strikethroughText.isNullOrEmpty()) {
                    tvSlashPrice.visibility = GONE
                } else {
                    tvSlashPrice.visibility = VISIBLE
                    TextViewUtils.setTextFromHtml(tvSlashPrice, item.strikethroughText.orEmpty())
                }
                imageViewFaculty.loadImageEtx(item.facultyImageUrl.orEmpty())
                if (item.bgColor.isNullOrEmpty()) {
                    imageViewBackground.loadImageEtx(item.imageUrl.orEmpty())
                } else {
                    imageViewBackground.setBackgroundColor(Color.parseColor(item.bgColor.orEmpty()))
                }
                if (item.tagData?.getOrNull(0)?.title.isNullOrEmpty()) {
                    tvTagOne.hide()
                } else {
                    tvTagOne.text = item.tagData?.getOrNull(0)?.title
                    tvTagOne.background = com.doubtnutapp.utils.Utils.getShape(
                        item.tagData?.getOrNull(0)?.bgColor.orEmpty(),
                        item.tagData?.getOrNull(0)?.bgColor.orEmpty(),
                        4f
                    )
                    tvTagOne.setOnClickListener {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.COURSE_V4_TAG_CLICKED,
                                hashMapOf<String, Any>(
                                    EventConstants.CTA_TITLE to item.tagData?.getOrNull(0)?.title.orEmpty(),
                                    EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty()
                                ).apply {
                                    putAll(extraParams ?: hashMapOf())
                                },
                                ignoreSnowplow = true
                            )
                        )
                    }
                }

                if (item.tagData?.getOrNull(1)?.title.isNullOrEmpty()) {
                    tvTagTwo.hide()
                } else {
                    tvTagTwo.text = item.tagData?.getOrNull(1)?.title
                    tvTagTwo.background = com.doubtnutapp.utils.Utils.getShape(
                        item.tagData?.getOrNull(1)?.bgColor.orEmpty(),
                        item.tagData?.getOrNull(1)?.bgColor.orEmpty(),
                        4f
                    )
                    tvTagTwo.setOnClickListener {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.COURSE_V4_TAG_CLICKED,
                                hashMapOf<String, Any>(
                                    EventConstants.CTA_TITLE to item.tagData?.getOrNull(1)?.title.orEmpty(),
                                    EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty()
                                ).apply {
                                    putAll(extraParams ?: hashMapOf())
                                },
                                ignoreSnowplow = true
                            )
                        )
                    }
                }

                if (item.tagData?.getOrNull(2)?.title.isNullOrEmpty()) {
                    tvTagThree.hide()
                } else {
                    tvTagThree.text = item.tagData?.getOrNull(2)?.title
                    tvTagThree.background = com.doubtnutapp.utils.Utils.getShape(
                        item.tagData?.getOrNull(2)?.bgColor.orEmpty(),
                        item.tagData?.getOrNull(2)?.bgColor.orEmpty(),
                        4f
                    )
                    tvTagThree.setOnClickListener {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.COURSE_V4_TAG_CLICKED,
                                hashMapOf<String, Any>(
                                    EventConstants.CTA_TITLE to item.tagData?.getOrNull(2)?.title.orEmpty(),
                                    EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty()
                                ).apply {
                                    putAll(extraParams ?: hashMapOf())
                                },
                                ignoreSnowplow = true
                            )
                        )
                    }
                }

                tvStudentCount.text = item.studentCountText.orEmpty()
                tvStartDate.text = item.startingDateText.orEmpty()

                admissionBtn.text = item.buttonText.orEmpty()
                admissionBtn.setOnClickListener {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.COURSE_V4_CTA_CLICKED,
                            hashMapOf<String, Any>(
                                EventConstants.CTA_TITLE to item.buttonText.orEmpty(),
                                EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty()
                            ).apply {
                                putAll(extraParams ?: hashMapOf())
                            }, ignoreBranch = false
                        )
                    )
                    MoEngageUtils.setUserAttribute(context, "dn_bnb_clicked",true)

                    deeplinkAction.performAction(context, item.buttonDeeplink.orEmpty())
                }

                parentLayout.setOnClickListener {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.COURSE_V4_CLICKED,
                            hashMapOf<String, Any>(EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty())
                                .apply {
                                    putAll(extraParams ?: hashMapOf())
                                }
                        )
                    )
                    deeplinkAction.performAction(context, item.deeplink.orEmpty())
                }
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }
    }

    class WidgetViewHolder(binding: WidgetCourseV4Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseV4Binding>(binding, widget)

    class CourseWidgetModelV4 : WidgetEntityModel<CourseWidgetDataV4, WidgetAction>()

    @Keep
    data class CourseWidgetDataV4(
        @SerializedName("title") val title: String?,
        @SerializedName("auto_scroll_time_in_sec") val autoScrollTimeInSec: Long?,
        @SerializedName("items") val items: List<CourseWidgetItemsV4>?,
        var selectedPagePosition: Int = 0
    ) : WidgetData()

    @Keep
    data class CourseWidgetItemsV4(
        @SerializedName("header_title")
        val headerTitle: String?,
        @SerializedName("header_title_text_size")
        val headerTitleTextSize: String?,
        @SerializedName("header_title_text_color")
        val headerTitleTextColor: String?,
        @SerializedName("header_background_color")
        val headerBackgroundColor: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("button_deeplink") val buttonDeeplink: String?,
        @SerializedName("button_text") val buttonText: String?,
        @SerializedName("student_count_image_url") val studentCountImageUrl: String?,
        @SerializedName("student_count_text") val studentCountText: String?,
        @SerializedName("starting_date_image_url") val startingDateImageUrl: String?,
        @SerializedName("starting_date_text") val startingDateText: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("medium_text") val mediumText: String?,
        @SerializedName("medium_bg_color") val mediumBgColor: String?,
        @SerializedName("faculty_image") val facultyImageUrl: String?,
        @SerializedName("bg_color") val bgColor: String?,
        @SerializedName("tag_data") val tagData: List<TagData>?,
        @SerializedName("course_title") val courseTitle: String?,
        @SerializedName("price") val price: String?,
        @SerializedName("video_resource") val videoResource: ApiVideoResource?,
        @SerializedName("default_mute") var defaultMute: Boolean?,
        @SerializedName("id") val id: String,
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("assortment_id") val assortmentId: String?,
        @SerializedName("strike_through_text") val strikethroughText: String?,
        @SerializedName("widget_timer") val widgetTimer: TimerWidget.Data?,

        )

    @Keep
    data class ApiVideoResource(
        @SerializedName("resource") val resource: String?,
        @SerializedName("drm_scheme") val drmScheme: String?,
        @SerializedName("drm_license_url") val drmLicenseUrl: String?,
        @SerializedName("auto_play_duration") val autoPlayDuration: Long?,
        @SerializedName("media_type") val mediaType: String?
    )

    @Keep
    data class TagData(
        @SerializedName("title") val title: String?,
        @SerializedName("bg_color") val bgColor: String?
    )
}
