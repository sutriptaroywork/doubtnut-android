package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.DoubtnutApp

import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnFreeCourseWidgetClicked
import com.doubtnutapp.databinding.ItemFreeCourseCardBinding
import com.doubtnutapp.databinding.WidgetFreeTrialCourseBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.loadImage
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class FreeTrialCourseWidget @JvmOverloads constructor(
    context: Context,
) :
    BaseBindingWidget<FreeTrialCourseWidget.FreeTrialCourseWidgetHolder, FreeTrialCourseWidget.FreeTrialCourseItemModel,
            WidgetFreeTrialCourseBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    companion object {
        const val EVENT_TAG_FREE_TRIAL_COURSE = "free_trial_course"
        const val EVENT_TAG_DISCOUNT_COURSE = "discount_course"
    }

    override fun getViewBinding(): WidgetFreeTrialCourseBinding {
        return WidgetFreeTrialCourseBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = FreeTrialCourseWidgetHolder(getViewBinding(), this)
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    override fun bindWidget(
        holder: FreeTrialCourseWidgetHolder,
        model: FreeTrialCourseItemModel
    ): FreeTrialCourseWidgetHolder {
        super.bindWidget(holder, model)

        val binding = holder.binding
        binding.rootLayout.setOnClickListener {
            deeplinkAction.performAction(context, "confirmation_dialog")
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = Adapter(
            context,
            model.data.items.orEmpty(), actionPerformer, deeplinkAction, analyticsPublisher
        )

        return holder

    }

    class Adapter(
        private var context: Context,
        private val items: List<FreeTrialCourseData>,
        private var actionPerformer: ActionPerformer?,
        private var deeplinkAction: DeeplinkAction,
        private var analyticsPublisher: AnalyticsPublisher
    ) :
        RecyclerView.Adapter<Adapter.ItemFreeCourseWidget>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemFreeCourseWidget {
            val view = ItemFreeCourseCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ItemFreeCourseWidget(view)
        }

        override fun onBindViewHolder(holder: ItemFreeCourseWidget, position: Int) {
            val data = items[position]
            val binding = holder.binding
            binding.apply {
                textviewTitle.text = data.title
                textViewPrice.text = data.price

                data.cta?.let { cta ->
                    cardSubscribe.setCardBackgroundColor(Color.parseColor(cta.backgroundColor))
                    textViewSubscribe.text = cta.title
                } ?: run {
                    cardSubscribe.visibility = View.GONE
                }

                data.heading1?.let { heading ->
                    textViewTagOne.text = heading.title
                    cardTagOne.setCardBackgroundColor(Color.parseColor(heading.backgroundColor))
                    if (heading.titleColor.isNotNullAndNotEmpty()) {
                        binding.textViewTagOne.setTextColor(Color.parseColor(heading.titleColor))
                    }
                } ?: run {
                    cardTagOne.visibility = View.GONE
                }

                data.heading2?.let { heading ->
                    textViewTagTwo.text = heading.title
                    cardTagTwo.setCardBackgroundColor(Color.parseColor(heading.backgroundColor))
                    if (heading.titleColor.isNotNullAndNotEmpty()) {
                        binding.textViewTagTwo.setTextColor(Color.parseColor(heading.titleColor))
                    }
                } ?: run {
                    cardTagTwo.visibility = View.GONE
                }

                data.rightHalf?.let { rightHalf ->
                    textViewLanguage.text = rightHalf.subtitle
                    textViewCourse.text = rightHalf.bottomText
                    imageViewGradient.loadImage(rightHalf.background)
                } ?: run {
                    layoutRightHalf.visibility = View.GONE
                }

                cardFreeCourse.setOnClickListener {
                    if (data.isTrialCard) {
                        data.id?.let { id ->
                            data.deepLink?.let { deeplink ->
                                actionPerformer?.performAction(
                                    OnFreeCourseWidgetClicked(
                                        id.toInt(),
                                        deeplink
                                    )
                                )
                            }
                        }
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent("${EVENT_TAG_FREE_TRIAL_COURSE}_${EventConstants.CARD_CLICKED}")
                        )
                    } else {
                        data.deepLink?.let { deepLink ->
                            deeplinkAction.performAction(context, deepLink)
                        }
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent("${EVENT_TAG_DISCOUNT_COURSE}_${EventConstants.CARD_CLICKED}")
                        )
                    }
                }

                if (data.bottomImage.isNotNullAndNotEmpty()) {
                    userImage.loadImage(data.bottomImage)
                }
                data.bottomText?.let {
                    binding.textViewBottomText.text = data.bottomText
                }
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }

        class ItemFreeCourseWidget(val binding: ItemFreeCourseCardBinding) :
            RecyclerView.ViewHolder(
                binding.root
            ) {
        }
    }

    class FreeTrialCourseWidgetHolder(
        binding: WidgetFreeTrialCourseBinding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<WidgetFreeTrialCourseBinding>(binding, widget) {

    }

    @Keep
    class FreeTrialCourseItemModel : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(val items: ArrayList<FreeTrialCourseData>?) : WidgetData()

    @Keep
    data class FreeTrialCourseData(
        @SerializedName("description") val title: String?,
        @SerializedName("id") val id: String?,
        @SerializedName("price") val price: String?,
        @SerializedName("bottom_image") val bottomImage: String?,
        @SerializedName("bottom_text") val bottomText: String?,
        @SerializedName("deeplink") val deepLink: String?,
        @SerializedName("heading1") val heading1: Heading?,
        @SerializedName("heading2") val heading2: Heading?,
        @SerializedName("right_half") val rightHalf: RightHalf?,
        @SerializedName("is_trial_card") val isTrialCard: Boolean = false,
        @SerializedName("cta") val cta: Cta?
    ) : WidgetData()

    @Keep
    data class Heading(
        @SerializedName("title") val title: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("background_color") val backgroundColor: String?,
        @SerializedName("corner_radius") val cornerRadius: Float?
    )

    @Keep
    data class Cta(
        @SerializedName("title") val title: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("background_color") val backgroundColor: String?,
        @SerializedName("corner_radius") val cornerRadius: Float?,
    )

    @Keep
    data class RightHalf(
        @SerializedName("title") val title: String?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("bottom_text") val bottomText: String?,
        @SerializedName("background") val background: String?,
    )


}