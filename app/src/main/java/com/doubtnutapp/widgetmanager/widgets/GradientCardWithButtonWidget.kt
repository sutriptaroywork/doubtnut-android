package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants.FREE_COURSE_ACTIVATE_WIDGET_CLICKED
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemWidgetCardWithButtonBinding
import com.doubtnutapp.databinding.WidgetGradientCardWithButtonBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.loadImage
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class GradientCardWithButtonWidget(context: Context) :
    BaseBindingWidget<GradientCardWithButtonWidget.WidgetHolder,
            GradientCardWithButtonWidget.GradientCardWithButtonWidgetModel, WidgetGradientCardWithButtonBinding>(
        context
    ) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetGradientCardWithButtonBinding {
        return WidgetGradientCardWithButtonBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: GradientCardWithButtonWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        holder.binding.recyclerView.layoutManager = LinearLayoutManager(context)
        holder.binding.recyclerView.adapter = Adapter(
            model.data.items.orEmpty()
        )

        model.layoutConfig?.let {
            holder.binding.recyclerView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                marginStart = it.marginLeft.toFloat().dpToPx().toInt()
                marginEnd = it.marginRight.toFloat().dpToPx().toInt()
            }
        }

        holder.binding.root.setOnClickListener {
            if (model.data.deeplink.isNotNullAndNotEmpty()) {
                deeplinkAction.performAction(context, model.data.deeplink)
                analyticsPublisher.publishEvent(AnalyticsEvent(FREE_COURSE_ACTIVATE_WIDGET_CLICKED))
            }
        }

        return holder
    }

    inner class Adapter(val listItems: List<GradientCardWithButtonData>) :
        RecyclerView.Adapter<Adapter.ItemGradientCardWithButtonViewHolder>() {


        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ItemGradientCardWithButtonViewHolder {
            val binding = ItemWidgetCardWithButtonBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ItemGradientCardWithButtonViewHolder(binding)
        }


        override fun onBindViewHolder(holder: ItemGradientCardWithButtonViewHolder, position: Int) {

            val data = listItems[position] as GradientCardWithButtonData
            val binding = holder.itembinding
            if (data.titleOne.isNotNullAndNotEmpty()) {
                binding.textViewTitle.text = data.titleOne
            }
            if (data.titleOneTextColor.isNotNullAndNotEmpty()) {
                binding.textViewTitle.setTextColor(Color.parseColor(data.titleOneTextColor))
            }
            if (data.titleTwo.isNotNullAndNotEmpty()) {
                binding.textViewSubtitle.text = HtmlCompat.fromHtml(data.titleTwo!!,HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
            if (data.titleTwoTextColor.isNotNullAndNotEmpty()) {
                binding.textViewSubtitle.setTextColor(Color.parseColor(data.titleTwoTextColor))
            }

            if (data.leftStripColor.isNotNullAndNotEmpty()) {
                binding.viewLeftStrip.visibility = View.VISIBLE
                binding.viewLeftStrip.setBackgroundColor(Color.parseColor(data.leftStripColor))
            }

            if (data.bgColorStart.isNotNullAndNotEmpty() && data.bgColorEndColor.isNotNullAndNotEmpty()) {
                val colors = intArrayOf(
                    Color.parseColor(data.bgColorStart),
                    Color.parseColor(data.bgColorEndColor)
                )

                val gradientDrawable = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM, colors
                )

                binding.rootConstraintLayout.background = gradientDrawable
            }

            if (data.titleImage.isNotNullAndNotEmpty()) {
                binding.imageViewTitle.loadImage(data.titleImage)
            }
            if (data.cardCornerRadius.isNotNullAndNotEmpty()) {
                binding.rootCard.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    binding.rootCard.radius = data.cardCornerRadius!!.toFloat()
                }
            }

            if (data.cta != null) {
                binding.cardButtonActivate.visibility = View.VISIBLE
                binding.buttonTitleTextView.text = data.cta.text
                if (data.cta.ctaDeeplink.isNotNullAndNotEmpty()) {
                    binding.cardButtonActivate.setOnClickListener {
                        deeplinkAction.performAction(context, data.cta.ctaDeeplink)
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                FREE_COURSE_ACTIVATE_WIDGET_CLICKED
                            )
                        )
                    }
                }
            } else {
                binding.cardButtonActivate.visibility = View.GONE
            }
        }

        override fun getItemCount(): Int {
            return listItems.size
        }

        inner class ItemGradientCardWithButtonViewHolder(val itembinding: ItemWidgetCardWithButtonBinding) :
            RecyclerView.ViewHolder(itembinding.root) {

            fun bind() {

            }
        }
    }

    class WidgetHolder(binding: WidgetGradientCardWithButtonBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetGradientCardWithButtonBinding>(binding, widget)

    @Keep
    class GradientCardWithButtonWidgetModel :
        WidgetEntityModel<Data, WidgetAction>()

    interface RecyclerViewItem

    @Keep
    data class Data(
        @SerializedName(
            "items",
            alternate = ["videos"]
        ) val items: List<GradientCardWithButtonData>?,
        @SerializedName("layout_config") val layoutConfig: LayoutConfig?,
        @SerializedName("deeplink") val deeplink: String?,
    ) : WidgetData()

    @Keep
    data class GradientCardWithButtonData(
        @SerializedName("title") val titleOne: String?,
        @SerializedName("title_one_text_size") val titleOneTextSize: String?,
        @SerializedName("title_one_text_color") val titleOneTextColor: String?,
        @SerializedName("title_two") val titleTwo: String?,
        @SerializedName("title_two_text_size") val titleTwoTextSize: String?,
        @SerializedName("title_two_text_color") val titleTwoTextColor: String?,
        @SerializedName("bg_color1") val bgColorStart: String?,
        @SerializedName("bg_color2") val bgColorEndColor: String?,
        @SerializedName("left_strip_color") val leftStripColor: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("card_corner_radius") val cardCornerRadius: String?,
        @SerializedName("title_image_url") val titleImage: String?,
        @SerializedName("cta") val cta: Cta?,
    ) : WidgetData()

    @Keep
    data class LayoutConfig(
        @SerializedName("margin_top") val marginTop: String?,
        @SerializedName("margin_bottom") val marginBottom: String?,
        @SerializedName("margin_left") val marginLeft: String?,
        @SerializedName("margin_right") val marginRight: String?,
    )

    @Keep
    data class Cta(
        @SerializedName("text") val text: String?,
        @SerializedName("text_size") val textSize: String?,
        @SerializedName("text_color") val textColor: String?,
        @SerializedName("bg_color") val bgColor: String?,
        @SerializedName("cta_deeplink") val ctaDeeplink: String?,
    )


}