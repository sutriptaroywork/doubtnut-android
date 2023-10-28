package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Parcelable
import android.view.LayoutInflater
import android.widget.TextView
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.CoreBindingWidget
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.base.OnDoubtPeCharchaRewardCtaClicked
import com.doubtnutapp.base.OnFilterSelected
import com.doubtnutapp.databinding.WidgetGradientBannerWithActionButtonBinding
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgets.DoubtNutAppGlideModule
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.ArrayList
import javax.inject.Inject

class GradientBannerWithActionButtonWidget(context: Context) :
    BaseBindingWidget<GradientBannerWithActionButtonWidget.GradientBannerWidgetViewHolder,
            GradientBannerWithActionButtonWidget.WidgetModel, WidgetGradientBannerWithActionButtonBinding>(
        context
    ) {

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    override fun getViewBinding(): WidgetGradientBannerWithActionButtonBinding {
        return WidgetGradientBannerWithActionButtonBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = GradientBannerWidgetViewHolder(getViewBinding(), this)
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    override fun bindWidget(
        holder: GradientBannerWidgetViewHolder,
        model: WidgetModel
    ): GradientBannerWidgetViewHolder {

        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
        })

        val binding = holder.binding
        val data = model.data
        if (data.imageUrl.isNotNullAndNotEmpty()) {
            binding.imageViewLeft.loadImage(data.imageUrl)
            binding.imageViewLeft.show()
        } else {
            binding.imageViewLeft.hide()
        }
        if (data.rightImageUrl.isNotNullAndNotEmpty()) {
            binding.imageRight.loadImage(data.rightImageUrl)
            binding.imageRight.show()
        } else {
            binding.imageRight.hide()
        }
        binding.tvTitle.text = data.title.orEmpty()
        binding.tvSubtitle.text = data.subtitle.orEmpty()

        binding.tvTitle.applyTextSize(data.titleTextSize)
        binding.tvSubtitle.applyTextSize(data.subtitleTextSize)

        data.subtitleTopMargin?.let {
            val margin10dp = 10.dpToPx();
            val marginTop = it.dpToPx();
            binding.tvSubtitle.setMargins(margin10dp, marginTop, margin10dp, 0)
        }

        data.cta?.let {
            binding.buttonAction.show()
            binding.buttonAction.text = it.title.orEmpty()
            binding.buttonAction.applyBackgroundColor(it.bgColor)
            binding.buttonAction.applyTextColor(it.textColor)
            binding.buttonAction.setOnClickListener {
                actionPerformer?.performAction(OnDoubtPeCharchaRewardCtaClicked(data.rewardsFaqData))
            }
            if (it.showArrowImage == true) {
                binding.ivArrowRight.show()
                binding.ivArrowRight.setColorFilter(ContextCompat.getColor(context,R.color.colorPrimary));
            } else {
                binding.ivArrowRight.hide()
            }
        } ?: run {
            binding.buttonAction.hide()
            binding.ivArrowRight.hide()
        }

        data.imageHeight?.let {
            binding.imageViewLeft.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = it.toInt().dpToPx()
            }

        }

        if (data.alignImageTop != null && data.alignImageTop) {
            binding.tvTitle.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToTop = binding.imageViewLeft.id
            }
            val marginStart = 10.dpToPx();
            binding.tvTitle.setMargins(marginStart, 0, 0, 0)
        } else {
            binding.tvTitle.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToTop = binding.rootContainer.top
            }
        }

        data.imageWidth?.let {
            binding.imageViewLeft.updateLayoutParams<ConstraintLayout.LayoutParams> {
                width = it.toInt().dpToPx()
            }

        }

        if (data.bgStartColor.isNotNullAndNotEmpty() && data.bgEndColor.isNotNullAndNotEmpty()) {
            binding.rootContainer.background = Utils.getGradientView(
                data.bgStartColor!!,
                data.bgStartColor, data.bgEndColor!!,
                GradientDrawable.Orientation.LEFT_RIGHT

            )
        }

        if (data.showProgressView != null && data.showProgressView) {
            binding.viewProgress.show()
        } else {
            binding.viewProgress.hide()
        }


        return holder
    }

    private fun setSolidBackground(context: Context, view: TextView) {
        val radius = resources.getDimension(R.dimen.dimen_6dp)
        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, radius)
            .build()
        val materialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        materialShapeDrawable.fillColor =
            ContextCompat.getColorStateList(context, R.color.colorPrimary)
        view.background = materialShapeDrawable
    }

    class GradientBannerWidgetViewHolder(
        binding: WidgetGradientBannerWithActionButtonBinding, widget: BaseWidget<*, *>
    ) : WidgetBindingVH<WidgetGradientBannerWithActionButtonBinding>(binding, widget) {
    }

    @Keep
    class WidgetModel : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    class Data(
        @SerializedName("title") val title: String?,
        @SerializedName("title_text_size") val titleTextSize: String?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("subtitle_text_size") val subtitleTextSize: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("image_height") val imageHeight: String?,
        @SerializedName("image_width") val imageWidth: String?,
        @SerializedName("right_image_url") val rightImageUrl: String?,
        @SerializedName("bg_start_color") val bgStartColor: String?,
        @SerializedName("bg_end_color") val bgEndColor: String?,
        @SerializedName("show_progress_view") val showProgressView: Boolean?,
        @SerializedName("rewards_faq_data") val rewardsFaqData: RewardsData?,
        @SerializedName("subtitle_top_margin") val subtitleTopMargin: Int?,
        @SerializedName("align_image_top") val alignImageTop: Boolean?,
        @SerializedName("cta") val cta: Cta?,
    ) : WidgetData()

    @Keep
    @Parcelize
    data class RewardsData(
        @SerializedName("title") val title: String?,
        @SerializedName("img_url") val imageUrl: String?,
        @SerializedName("bullet_img_url") val bulletImgUrl: String?,
        @SerializedName("data") val listData: ArrayList<String>?,
    ) : Parcelable

    @Keep
    class Cta(
        @SerializedName("title") val title: String?,
        @SerializedName("bg_color") val bgColor: String?,
        @SerializedName("title_color") val textColor: String?,
        @SerializedName("show_arrow_image") val showArrowImage: Boolean?,
    )
}