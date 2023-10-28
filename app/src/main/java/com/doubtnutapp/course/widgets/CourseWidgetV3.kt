package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.TextViewUtils
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemCourseV3Binding
import com.doubtnutapp.databinding.WidgetCourseV3Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseWidgetV3(context: Context) :
    BaseBindingWidget<CourseWidgetV3.WidgetViewHolder,
        CourseWidgetV3.CourseWidgetModelV3, WidgetCourseV3Binding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    companion object {
        const val TAG = "CourseWidgetV3"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    override fun getViewBinding(): WidgetCourseV3Binding {
        return WidgetCourseV3Binding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetViewHolder, model: CourseWidgetModelV3): WidgetViewHolder {
        super.bindWidget(holder, model)
        val data: CourseWidgetDataV3 = model.data
        val binding = holder.binding

        binding.apply {
            ivIcon.apply {
                data.topIcon?.let {
                    layoutParams.height = (data.topIconHeight ?: 24).dpToPx()
                    layoutParams.width = (data.topIconWidth ?: 24).dpToPx()
                    requestLayout()
                    show()
                    loadImage(data.topIcon)
                } ?: hide()
            }

            header.apply {
                background = Utils.getShape(
                    colorString = data.backgroundColor ?: "#FFFFFF",
                    strokeColor = data.borderColor ?: "#FFFFFF",
                    cornerRadius = data.cornerRadius ?: 0F,
                    strokeWidth = data.borderWidth ?: 0
                )
            }

            tvTitle.apply {
                isVisible = !data.title.isNullOrEmpty()
                text = data.title.orEmpty()
            }

            rvCourse.apply {
                layoutManager = LinearLayoutManager(
                    context,
                    RecyclerView.HORIZONTAL, false
                )
                adapter = CourseWidgetV3.Adapter(
                    data.items.orEmpty(),
                    actionPerformer,
                    analyticsPublisher,
                    deeplinkAction,
                    context,
                    model.extraParams
                )
            }
        }

        return holder
    }

    class Adapter(
        val items: List<CourseWidgetItemV3>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val context: Context,
        val extraParams: HashMap<String, Any>?
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(ItemCourseV3Binding.inflate(LayoutInflater.from(context), parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding as ItemCourseV3Binding
            val item = items[position]
            Utils.setWidthBasedOnPercentage(holder.itemView.context, holder.itemView, item.cardWidth ?: "1.25", R.dimen.spacing)
            with(binding) {
                tvTitle.text = item.title.orEmpty()
                tvMediumTag.background = Utils.getShape(
                    item.tagData?.getOrNull(0)?.bgColor ?: "#ffffff",
                    item.tagData?.getOrNull(0)?.bgColor.orEmpty(),
                    4f
                )
                tvMediumTag.text = item.tagData?.getOrNull(0)?.title.orEmpty()
                tvMediumTag.setOnClickListener {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.POPULAR_COURSE_TAG_CLICKED,
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
                tvSeatsTag.text = item.tagData?.getOrNull(1)?.title.orEmpty()
                tvSeatsTag.background = Utils.getShape(
                    item.tagData?.getOrNull(1)?.bgColor ?: "#ffffff",
                    item.tagData?.getOrNull(1)?.bgColor ?: "#ffffff",
                    4f
                )
                tvSeatsTag.setOnClickListener {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.POPULAR_COURSE_TAG_CLICKED,
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
                header.setBackgroundColor(Color.parseColor(item.bgColor ?: "#ffffff"))

                if (item.courseDetails?.getOrNull(0)?.imageUrl.isNullOrEmpty()) {
                    ivOne.invisible()
                } else {
                    ivOne.show()
                    ivOne.loadImageEtx(item.courseDetails?.getOrNull(0)?.imageUrl.orEmpty())
                }
                tvOne.text = item.courseDetails?.getOrNull(0)?.title.orEmpty()
                if (item.courseDetails?.getOrNull(1)?.imageUrl.isNullOrEmpty()) {
                    ivTwo.invisible()
                } else {
                    ivTwo.show()
                    ivTwo.loadImageEtx(item.courseDetails?.getOrNull(1)?.imageUrl.orEmpty())
                }
                tvTwo.text = item.courseDetails?.getOrNull(1)?.title.orEmpty()
                if (item.courseDetails?.getOrNull(2)?.imageUrl.isNullOrEmpty()) {
                    ivThree.invisible()
                } else {
                    ivThree.show()
                    ivThree.loadImageEtx(item.courseDetails?.getOrNull(2)?.imageUrl.orEmpty())
                }
                tvThree.text = item.courseDetails?.getOrNull(2)?.title.orEmpty()
                if (item.courseDetails?.getOrNull(3)?.imageUrl.isNullOrEmpty()) {
                    ivFour.invisible()
                } else {
                    ivFour.show()
                    ivFour.loadImageEtx(item.courseDetails?.getOrNull(3)?.imageUrl.orEmpty())
                }
                tvFour.text = item.courseDetails?.getOrNull(3)?.title.orEmpty()

                tvFive.text = item.courseDetails?.getOrNull(4)?.title.orEmpty()
                tvSix.text = item.courseDetails?.getOrNull(5)?.title.orEmpty()

                tvPrice.text = item.priceText.orEmpty()
                tvPrice.textSize = item.priceTextSize?.toFloat() ?: 16f

                if (!item.buttonOneText.isNullOrEmpty() && !item.buttonTwoText.isNullOrEmpty()) {
                    btnViewMore.show()
                    btnViewMore.text = item.buttonOneText.orEmpty()
                    btnViewMore.setOnClickListener {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.POPULAR_COURSE_CTA_CLICKED,
                                hashMapOf<String, Any>(
                                    EventConstants.CTA_TITLE to item.buttonOneText.orEmpty(),
                                    EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty()
                                ).apply {
                                    putAll(extraParams ?: hashMapOf())
                                }, ignoreBranch = false
                            )
                        )
                        MoEngageUtils.setUserAttribute(context, "dn_bnb_clicked",true)

                        deeplinkAction.performAction(context, item.buttonOneDeeplink.orEmpty())
                    }
                    btnViewMore.background = Utils.getShape("#ffffff", "#ea532c", 4f)
                    btnGetAdmission.show()
                    btnGetAdmission.text = item.buttonTwoText.orEmpty()
                    btnGetAdmission.setOnClickListener {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.POPULAR_COURSE_CTA_CLICKED,
                                hashMapOf<String, Any>(
                                    EventConstants.CTA_TITLE to item.buttonTwoText.orEmpty(),
                                    EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty()
                                ).apply {
                                    putAll(extraParams ?: hashMapOf())
                                }, ignoreBranch = false
                            )
                        )
                        MoEngageUtils.setUserAttribute(context, "dn_bnb_clicked",true)

                        deeplinkAction.performAction(context, item.buttonTwoDeeplink.orEmpty())
                    }
                } else if (!item.buttonOneText.isNullOrEmpty()) {
                    btnGetAdmission.hide()
                    btnViewMore.show()
                    btnViewMore.text = item.buttonOneText.orEmpty()
                    btnViewMore.setOnClickListener {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.POPULAR_COURSE_CTA_CLICKED,
                                hashMapOf<String, Any>(
                                    EventConstants.CTA_TITLE to item.buttonOneText.orEmpty(),
                                    EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty()
                                ).apply {
                                    putAll(extraParams ?: hashMapOf())
                                }, ignoreBranch = false
                            )
                        )
                        MoEngageUtils.setUserAttribute(context, "dn_bnb_clicked",true)

                        deeplinkAction.performAction(context, item.buttonOneDeeplink.orEmpty())
                    }
                } else if (!item.buttonTwoText.isNullOrEmpty()) {
                    btnViewMore.visibility = View.INVISIBLE
                    btnGetAdmission.show()
                    btnGetAdmission.text = item.buttonTwoText.orEmpty()
                    btnGetAdmission.setOnClickListener {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.POPULAR_COURSE_CTA_CLICKED,
                                hashMapOf<String, Any>(
                                    EventConstants.CTA_TITLE to item.buttonTwoText.orEmpty(),
                                    EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty()
                                ).apply {
                                    putAll(extraParams ?: hashMapOf())
                                }, ignoreBranch = false
                            )
                        )
                        deeplinkAction.performAction(context, item.buttonTwoDeeplink.orEmpty())
                    }
                }

                if (item.discountText.isNullOrEmpty()) {
                    discountLayout.hide()
                } else {
                    discountLayout.show()
                    discountText.text = item.discountText.orEmpty()
                    ivDiscount.loadImageEtx(item.discountImage.orEmpty())
                }
                parenLayout.setOnClickListener {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.POPULAR_COURSE_CLICKED,
                            hashMapOf<String, Any>(EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty()).apply {
                                putAll(extraParams ?: hashMapOf())
                            }
                        )
                    )
                    deeplinkAction.performAction(context, item.deeplink.orEmpty())
                }
                TextViewUtils.setTextFromHtml(
                    binding.strikethroughPrice,
                    item.strikeThroughText.orEmpty()
                )
            }
        }

        class ViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

        override fun getItemCount(): Int {
            return items.size
        }
    }

    class WidgetViewHolder(binding: WidgetCourseV3Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseV3Binding>(binding, widget)

    class CourseWidgetModelV3 : WidgetEntityModel<CourseWidgetDataV3, WidgetAction>()

    @Keep
    data class CourseWidgetDataV3(
        @SerializedName("title") val title: String?,
        @SerializedName("items") val items: List<CourseWidgetItemV3>?,
        @SerializedName("top_icon") val topIcon: String?,
        @SerializedName("top_icon_width") val topIconWidth: Int?,
        @SerializedName("top_icon_height") val topIconHeight: Int?,
        @SerializedName("border_color") val borderColor: String?,
        @SerializedName("border_width") val borderWidth: Int?,
        @SerializedName("corner_radius") val cornerRadius: Float?,
        @SerializedName("background_color") val backgroundColor: String?
    ) : WidgetData()

    @Keep
    data class CourseDetails(
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("title") val title: String?
    )

    @Keep
    data class CourseWidgetItemV3(
        @SerializedName("title") val title: String?,
        @SerializedName("card_width") val cardWidth: String?,
        @SerializedName("course_details") val courseDetails: List<CourseDetails>?,
        @SerializedName("tag_data") val tagData: List<TagData>?,
        @SerializedName("price") val priceText: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("bg_color") val bgColor: String?,
        @SerializedName("button_one_text") val buttonOneText: String?,
        @SerializedName("button_one_deeplink") val buttonOneDeeplink: String?,
        @SerializedName("button_two_text") val buttonTwoText: String?,
        @SerializedName("button_two_deeplink") val buttonTwoDeeplink: String?,
        @SerializedName("discount_text") val discountText: String?,
        @SerializedName("discount_image") val discountImage: String?,
        @SerializedName("assortment_id") val assortmentId: String?,
        @SerializedName("price_text_size") val priceTextSize: String?,
        @SerializedName("strike_through_text") val strikeThroughText: String?,
    )

    @Keep
    data class TagData(
        @SerializedName("title") val title: String?,
        @SerializedName("bg_color") val bgColor: String?
    )
}
