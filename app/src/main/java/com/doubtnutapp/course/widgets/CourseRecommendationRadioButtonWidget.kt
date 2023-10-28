package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.CourseRecommendationRadioButtonSelected
import com.doubtnutapp.databinding.WidgetCourseRecommendationRadioButtonBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.utils.NetworkUtil
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.flexbox.FlexboxLayout
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseRecommendationRadioButtonWidget(context: Context) :
    BaseBindingWidget<CourseRecommendationRadioButtonWidget.WidgetHolder,
        CourseRecommendationRadioButtonWidget.Model,
        WidgetCourseRecommendationRadioButtonBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var networkUtil: NetworkUtil

    var source: String? = null

    override fun getViewBinding(): WidgetCourseRecommendationRadioButtonBinding {
        return WidgetCourseRecommendationRadioButtonBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(4, 4, 4, 4)
            }
        )

        val data = model.data
        val binding = holder.binding

        if (data.items.any { it.isSelected == true }) {
            binding.flexBoxLayout.isVisible = false
            binding.flexBoxLayout.removeAllViews()
        } else {
            setUpFlexBox(
                data.items,
                binding.flexBoxLayout,
                binding.root.context,
                data.submitId.orEmpty()
            )
        }

        return holder
    }

    private fun setUpFlexBox(
        flexViewItemList: List<Item>,
        flexBoxLayout: FlexboxLayout,
        context: Context,
        submitId: String
    ) {
        flexBoxLayout.isVisible = true
        flexBoxLayout.removeAllViews()
        flexViewItemList.forEach { item ->
            val textViewFlex = TextView(context)
            textViewFlex.text = item.value.orEmpty()
            textViewFlex.compoundDrawablePadding = 4
            textViewFlex.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            if (item.iconUrl.isNullOrBlank()) {
                textViewFlex.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            } else {
                Glide.with(context)
                    .load(item.iconUrl)
                    .into(object : CustomTarget<Drawable?>(40, 40) {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable?>?
                        ) {
                            try {
                                textViewFlex.setCompoundDrawablesWithIntrinsicBounds(
                                    resource,
                                    null,
                                    null,
                                    null
                                )
                            } catch (e: Exception) {
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            try {
                                textViewFlex.setCompoundDrawablesWithIntrinsicBounds(
                                    null,
                                    null,
                                    null,
                                    null
                                )
                            } catch (e: Exception) {
                            }
                        }
                    })
            }
            textViewFlex.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.toFloat())
            textViewFlex.gravity = Gravity.CENTER
            textViewFlex.background =
                context.resources.getDrawable(R.drawable.bg_capsule_tomato_13dp)
            textViewFlex.setTextColor(context.resources.getColor(R.color.white))

            val layoutParam = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            textViewFlex.layoutParams = layoutParam
            val lp = textViewFlex.layoutParams as FlexboxLayout.LayoutParams
            lp.setMargins(0, 10, 10, 10)
            textViewFlex.layoutParams = lp
            textViewFlex.setPadding(20, 20, 20, 20)
            textViewFlex.setOnClickListener {
                if (!item.deeplink.isNullOrBlank()) {
                    deeplinkAction.performAction(context, item.deeplink)
                } else if (networkUtil.isConnectedWithMessage()) {
                    item.isSelected = true
                    flexBoxLayout.isVisible = false
                    actionPerformer?.performAction(
                        CourseRecommendationRadioButtonSelected(
                            submitId,
                            item.key.orEmpty(),
                            item.value.orEmpty()
                        )
                    )
                }
            }
            flexBoxLayout.addView(textViewFlex)
        }
    }

    class WidgetHolder(
        binding: WidgetCourseRecommendationRadioButtonBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetCourseRecommendationRadioButtonBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String?,
        @SerializedName("submit_id") val submitId: String?,
        @SerializedName("items") val items: List<Item>
    ) : WidgetData()

    @Keep
    data class Item(
        @SerializedName("key") val key: String?,
        @SerializedName("value") val value: String?,
        @SerializedName("icon_url") val iconUrl: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("is_selected") var isSelected: Boolean?
    )
}
