package com.doubtnutapp.similarVideo.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.NcertBookClick
import com.doubtnutapp.data.remote.models.LayoutPadding
import com.doubtnutapp.databinding.WidgetNcertBooksBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class NcertBookWidget(context: Context) : BaseBindingWidget<NcertBookWidget.WidgetHolder,
        NcertBookWidget.Model, WidgetNcertBooksBinding>(context) {


    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(4, 4, 2, 2)
        })
        val data = model.data
        val binding = holder.binding
        Utils.setWidthBasedOnPercentage(
            holder.itemView.context,
            holder.itemView,
            data.cardWidth,
            R.dimen.spacing
        )
        requestLayout()

        holder.itemView.apply {
            binding.parentLayout.apply {
                data.layoutPadding?.let {
                    updatePadding(
                        (it.paddingStart ?: 0).dpToPx(),
                        (it.paddingTop ?: 0).dpToPx(),
                        (it.paddingEnd ?: 0).dpToPx(),
                        (it.paddingBottom ?: 0).dpToPx()
                    )
                    if (data.backgroundColor.isValidColorCode()) {
                        setBackgroundColor(Color.parseColor(data.backgroundColor))
                    }
                }
            }

            binding.ivNcertBook.apply {
                data.cardRatio?.let {
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    (layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = data.cardRatio
                }
                data.imageCornerRadius?.let {
                    shapeAppearanceModel = shapeAppearanceModel
                        .toBuilder()
                        .setAllCornerSizes(it.dpToPx())
                        .build()
                }
                loadImage(data.bookThumbnail)
            }

            setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.NCERT_NEXT_BOOK_CLICKED,
                        hashMapOf(
                            EventConstants.CLICKED_ITEM_ID to data.playlistId,
                            EventConstants.PLAYLIST_ID to data.playlistId,
                            EventConstants.PLAYLIST_TYPE to data.type,
                            EventConstants.SOURCE to source.orEmpty(),
                            EventConstants.ITEM_POSITION to widgetViewHolder.absoluteAdapterPosition
                        ), ignoreSnowplow = true
                    )
                )

                if (data.openNewPage == true) {
                    deeplinkAction.performAction(context, data.deeplink)
                } else {
                    actionPerformer?.performAction(NcertBookClick(data.deeplink, data.openNewPage))
                }
            }
        }

        binding.tvBottomTitle.apply {
            isVisible = data.bottomTitle.isNotNullAndNotEmpty()
            text = data.bottomTitle
        }

        binding.tvBottomSubtitle.apply {
            isVisible = data.bottomSubtitle.isNotNullAndNotEmpty()
            text = data.bottomSubtitle
            if (data.bottomSubtitleColor.isNotNullAndNotEmpty()) {
                setTextColor(Color.parseColor(data.bottomSubtitleColor))
            }
            if (data.bottomSubtitleIcon.isNotNullAndNotEmpty()) {
                Glide.with(binding.root.context).load(data.bottomSubtitleIcon)
                    .apply(RequestOptions().fitCenter()).into(
                        object : CustomTarget<Drawable>(28, 28) {
                            override fun onLoadCleared(placeholder: Drawable?) {
                                setCompoundDrawablesWithIntrinsicBounds(
                                    placeholder,
                                    null,
                                    null,
                                    null
                                )
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                transition: com.bumptech.glide.request.transition.Transition<in Drawable>?,
                            ) {
                                setCompoundDrawablesWithIntrinsicBounds(
                                    resource,
                                    null,
                                    null,
                                    null
                                )
                            }
                        }
                    )
            }
        }

        trackingViewId = data.playlistId

        return holder
    }

    class WidgetHolder(
        binding: WidgetNcertBooksBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetNcertBooksBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val playlistId: String,
        @SerializedName("type") val type: String,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("open_new_page") val openNewPage: Boolean?,
        @SerializedName("book_thumbnail", alternate = ["image_url"]) val bookThumbnail: String?,
        @SerializedName("card_width") val cardWidth: String,
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("image_corner_radius") val imageCornerRadius: Float?,
        @SerializedName("bottom_title") val bottomTitle: String?,
        @SerializedName("bottom_subtitle") val bottomSubtitle: String?,
        @SerializedName("bottom_subtitle_color") val bottomSubtitleColor: String?,
        @SerializedName("bottom_subtitle_icon") val bottomSubtitleIcon: String?,
        @SerializedName("background_color") val backgroundColor: String?,
        @SerializedName("layout_padding") val layoutPadding: LayoutPadding?,
    ) : WidgetData()

    override fun getViewBinding(): WidgetNcertBooksBinding {
        return WidgetNcertBooksBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

