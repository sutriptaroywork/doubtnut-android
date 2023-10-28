package com.doubtnutapp.widgetmanager.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.*
import android.view.View.OnTouchListener
import android.widget.ImageView
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnAutoPostItemSelected
import com.doubtnutapp.base.SgChildWidgetLongClick
import com.doubtnutapp.databinding.WidgetImageCardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.doubtpecharcha.ui.activity.DoubtP2pActivity
import com.doubtnutapp.studygroup.ui.fragment.SgAdminDashboardFragment
import com.doubtnutapp.studygroup.ui.fragment.SgChatFragment
import com.doubtnutapp.studygroup.ui.fragment.SgPersonalChatFragment
import com.doubtnutapp.studygroup.ui.fragment.SgUserReportedMessageFragment
import com.doubtnutapp.utils.Utils
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RelativeCornerSize
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by devansh on 9/1/21.
 */

class ImageCardWidget(context: Context) :
    BaseBindingWidget<ImageCardWidget.WidgetHolder, ImageCardWidget.Model, WidgetImageCardBinding>(
        context
    ) {

    companion object {
        private const val TAG = "ImageCardWidget"
        private const val IMAGE_CARD_WIDGET_CLICK = "image_card_widget_click"
        private const val ALIGN_BOTTOM = "bottom"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    private var isImageDownloaded = false

    override fun getView(): View {
        return View.inflate(context, R.layout.widget_image_card, this)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        if (source != SgChatFragment.STUDY_GROUP &&
            source != DoubtP2pActivity.DOUBT_P2P &&
            source != SgAdminDashboardFragment.STUDY_GROUP_ADMIN_DASHBOARD &&
            source != SgUserReportedMessageFragment.STUDY_GROUP_USER_REPORTED_MESSAGE &&
            source != SgPersonalChatFragment.SOURCE_PERSONAL_CHAT
        ) {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val data = model.data
        val binding = holder.binding
        with(binding) {
            val width =
                Utils.getWidthFromScrollSize(holder.itemView.context, data.cardWidth ?: "1.2") -
                        (cardView.marginStart + cardView.marginEnd)
            val ratio = if (data.isCircle == true) {
                "1:1"
            } else {
                data.cardRatio ?: Utils.getRatioFromScrollSize(data.cardWidth ?: "1.2")
            }
            val radius =
                data.cornerRadius ?: (if (data.isCircle == true) width / 2f else 4f).dpToPx()

            if (data.title.isNullOrEmpty().not()) {
                tvTitle.show()
                tvTitle.text = data.title
            } else {
                tvTitle.hide()
            }

            if (data.isCircle == true) {
                cardView.background = MaterialShapeDrawable(
                    ShapeAppearanceModel()
                        .toBuilder()
                        .setAllCorners(RoundedCornerTreatment())
                        .setAllCornerSizes(RelativeCornerSize(.5f))
                        .build()
                ).apply {
                    fillColor = ColorStateList.valueOf(Color.WHITE)
                }
            } else {
                if (source == SgChatFragment.STUDY_GROUP || source == SgPersonalChatFragment.SOURCE_PERSONAL_CHAT) {
                    cardView.radius = 12f.dpToPx()
                    cardView.cardElevation = 0f
                } else {
                    cardView.radius = radius.dpToPx()
                }
            }
            if (source == SgChatFragment.STUDY_GROUP ||
                source == DoubtP2pActivity.DOUBT_P2P ||
                source == SgPersonalChatFragment.SOURCE_PERSONAL_CHAT
            ) {
                imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                imageView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    dimensionRatio = null
                    this.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
                    height = ConstraintLayout.LayoutParams.WRAP_CONTENT

                    data.maxImageHeight?.let {
                        matchConstraintMaxHeight = it
                    }
                }
                cardView.updateLayoutParams<MarginLayoutParams> {
                    marginStart = 4.dpToPx()
                    marginEnd = 4.dpToPx()
                }
                cardView.updateMargins(top = 8.dpToPx(), bottom = 0.dpToPx())
            } else {
                cardView.layoutParams.width = width
                (imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = ratio
            }
            requestLayout()

            when (source) {
                SgPersonalChatFragment.SOURCE_PERSONAL_CHAT,
                SgChatFragment.STUDY_GROUP,
                SgAdminDashboardFragment.STUDY_GROUP_ADMIN_DASHBOARD,
                SgUserReportedMessageFragment.STUDY_GROUP_USER_REPORTED_MESSAGE -> {
                    val isAutoDownloadEnabledInStudyGroup =
                        defaultPrefs().getBoolean(Constants.SG_IMAGE_AUTO_DOWNLOAD, false)
                    if (isAutoDownloadEnabledInStudyGroup || data.autoDownloadImage) {
                        downloadImage(url = data.imageUrl)
                    } else {
                        downloadImage(url = data.imageUrl, onlyFromCache = true)
                    }
                }
                DoubtP2pActivity.DOUBT_P2P -> {
                    val isAutoDownloadEnabledInP2p =
                        defaultPrefs().getBoolean(Constants.P2P_IMAGE_AUTO_DOWNLOAD, false)
                    if (isAutoDownloadEnabledInP2p || data.autoDownloadImage) {
                        downloadImage(url = data.imageUrl)
                    } else {
                        downloadImage(url = data.imageUrl, onlyFromCache = true)
                    }
                }
                else -> {
                    downloadImage(url = data.imageUrl)
                }
            }

            cardView.setOnClickListener {
                if (isImageDownloaded.not()) {
                    downloadImage(url = data.imageUrl)
                    return@setOnClickListener
                }
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        IMAGE_CARD_WIDGET_CLICK,
                        hashMapOf<String, Any>(
                            EventConstants.CLICKED_ITEM_ID to data.id.orEmpty(),
                            EventConstants.WIDGET to TAG,
                            EventConstants.ITEM_POSITION to widgetViewHolder.absoluteAdapterPosition,
                            EventConstants.SOURCE to source.orEmpty(),
                            EventConstants.WIDGET_TITLE to data.title.orEmpty()
                        ).apply {
                            putAll(model.extraParams ?: HashMap())
                        }
                    )
                )

                DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams?.apply {
                    put(Constants.ID, data.id)
                }))
                if (data.deeplink.isNotNullAndNotEmpty()) {
                    deeplinkAction.performAction(context, data.deeplink)
                } else {
                    actionPerformer?.performAction(OnAutoPostItemSelected(data.id))
                }
            }

            if (source == SgChatFragment.STUDY_GROUP || source == SgPersonalChatFragment.SOURCE_PERSONAL_CHAT) {
                val lastTouchDownXY = FloatArray(2)
                cardView.setOnTouchListener(OnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        lastTouchDownXY[0] = event.x
                        lastTouchDownXY[1] = event.y - cardView.height
                    }
                    return@OnTouchListener false
                })

                cardView.setOnLongClickListener {
                    actionPerformer?.performAction(
                        SgChildWidgetLongClick(
                            model.type,
                            lastTouchDownXY
                        )
                    )
                    true
                }
            }

            imageView.setScaleType(data.scaleType, ImageView.ScaleType.FIT_CENTER)


            if (data.titleAlignment.isNotNullAndNotEmpty() && data.titleAlignment.equals(
                    ALIGN_BOTTOM
                )
            ) {
                binding.tvTitle.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    bottomToBottom = binding.imageContainer.id
                    topToTop = ConstraintLayout.LayoutParams.UNSET
                }

                binding.imageView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    topToTop = binding.imageContainer.id
                    topToBottom = ConstraintLayout.LayoutParams.UNSET
                }

                val padding = 8.dpToPx()
                tvTitle.setPadding(padding)

                if (data.titleBackgroundColor.isNotNullAndNotEmpty()) {
                    tvTitle.applyBackgroundColor(data.titleBackgroundColor)
                } else {
                    tvTitle.applyBackgroundColor("#ffebffda")
                }
                if (data.titleTextColor.isNotNullAndNotEmpty()) {
                    tvTitle.applyTextColor(data.titleTextColor)
                } else {
                    tvTitle.applyTextColor("#110b32")
                }

                tvTitle.elevation = 10f.dpToPx()
                tvTitle.visibility = View.VISIBLE
                tvTitle.gravity = Gravity.CENTER


            } else {
                binding.tvTitle.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    topToTop = binding.imageContainer.id
                    topToBottom = ConstraintLayout.LayoutParams.UNSET
                }

                binding.imageView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    topToBottom = binding.tvTitle.id
                    bottomToBottom = binding.imageContainer.id
                }

                val padding = 12.dpToPx()
                tvTitle.setPadding(padding)
                tvTitle.applyBackgroundColor("#ffebffda")
                tvTitle.applyTextColor("#110b32")
                tvTitle.elevation = 0f
                tvTitle.visibility = View.GONE
                tvTitle.gravity = Gravity.START

            }
        }
        return holder
    }

    private fun downloadImage(url: String, onlyFromCache: Boolean = false) {
        widgetViewHolder.binding.apply {
            if (onlyFromCache) {
                imageView.loadImageFromCache(
                    url = url,
                    placeholder = R.drawable.ic_blur_background,
                    onLoadFailed = {
                        isImageDownloaded = false
                        downloadImage.visible()
                    },
                    onResourceReady = {
                        isImageDownloaded = true
                        downloadImage.gone()
                    }
                )
            } else {
                imageView.loadImageEtx(
                    url = url,
                    onLoadFailed = {
                        isImageDownloaded = false
                        downloadImage.visible()
                    },
                    onResourceReady = {
                        isImageDownloaded = true
                        downloadImage.gone()
                    }
                )
            }
        }
    }

    class WidgetHolder(binding: WidgetImageCardBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetImageCardBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String? = null,
        @SerializedName("title_text_color") val titleTextColor: String? = null,
        @SerializedName("title_bg_color") val titleBackgroundColor: String? = null,
        @SerializedName("title_alignment") val titleAlignment: String?,
        @SerializedName("image_url") val imageUrl: String,
        @SerializedName("auto_download_image") val autoDownloadImage: Boolean = false,
        @SerializedName("scale_type") val scaleType: String?,
        @SerializedName("card_width") val cardWidth: String?,
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("max_image_height") val maxImageHeight: Int?,
        @SerializedName("is_circle") val isCircle: Boolean?,
        @SerializedName("deeplink") val deeplink: String,
        @SerializedName("corner_radius") val cornerRadius: Float?,
        @SerializedName("name") val name: String = "",
        @SerializedName("id") val id: String
    ) : WidgetData()

    override fun getViewBinding(): WidgetImageCardBinding {
        return WidgetImageCardBinding.inflate(LayoutInflater.from(context), this, true)
    }
}