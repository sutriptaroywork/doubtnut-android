package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.doubtnut.core.utils.applyTextSize
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ImageTextWidget2Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.setOnDebouncedClickListener
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ImageTextWidget(context: Context) : BaseBindingWidget<ImageTextWidget.WidgetHolder,
        ImageTextWidgetModel, ImageTextWidget2Binding>(context) {

    companion object {
        const val TAG = "ImageTextWidget"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: ImageTextWidgetModel): WidgetHolder {
        super.bindWidget(holder, model.apply {

            model.layoutConfig = WidgetLayoutConfig(
                data.marginTop?.toIntOrNull() ?: 24,
                0, 0, 0
            )
        })
        val data: ImageTextWidgetData = model.data
        val binding = holder.binding
        binding.root.setBackgroundColor(Utils.parseColor(data.bgColor))
        binding.tvTitle.text = data.title.orEmpty()
        if (data.title.orEmpty().isEmpty()) {
            binding.tvTitle.hide()
        } else {
            binding.tvTitle.show()
        }
        binding.tvTitle.applyTextSize(data.titleTextSize)
        binding.tvSubTitle.applyTextSize(data.subtitleTextSize)
        if (data.isTitleCenter == true) {
            val constraintLayout: ConstraintLayout = findViewById(R.id.mainLayout)
            val constraintSet = ConstraintSet()
            constraintSet.clone(constraintLayout)
            constraintSet.connect(
                binding.tvTitle.id,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM,
                0
            );
            constraintSet.applyTo(binding.mainLayout)
        }
        if (data.titleBold == true) {
            binding.tvTitle.setTypeface(
                ResourcesCompat.getFont(context, R.font.lato_bold),
                Typeface.BOLD
            )
        }
        binding.tvSubTitle.text = data.subtitle.orEmpty()
        binding.ivImage.isVisible = data.imageUrl.isNullOrBlank().not()
        binding.ivImage2.isVisible = data.imageUrl2.isNullOrBlank().not()
        binding.ivImage.loadImageEtx(data.imageUrl.orEmpty())
        binding.ivImage2.loadImageEtx(data.imageUrl2.orEmpty())
        binding.tvTitle.setTextColor(Utils.parseColor(data.textColor, Color.BLACK))
        binding.root.setOnDebouncedClickListener(2000) {
            deeplinkAction.performAction(context, data.deeplink)
        }
        if (data.subtitle.isNullOrEmpty()) {
            binding.tvSubTitle.hide()
        }

        if (data.imageUrl3.isNotNullAndNotEmpty()) {
            binding.ivImage3.show()
            binding.ivImage3.loadImage(data.imageUrl3)
            binding.tvEndTitle.show()
        } else {
            binding.ivImage3.hide()
            binding.tvEndTitle.hide()
        }


        if (data.cta != null) {
            binding.buttonCta.visibility = View.VISIBLE
            binding.buttonCta.text = data.cta.title
        } else {

            binding.buttonCta.visibility = View.GONE
        }

        if (data.marginTop.isNullOrEmpty()) {
            val dp6 = 6.dpToPx()
            binding.mainLayout.setPadding(0, dp6, 0, dp6)
        } else {
            binding.mainLayout.setPadding(0, 0, 0, 0)
        }

        return holder
    }

    class WidgetHolder(
        binding: ImageTextWidget2Binding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<ImageTextWidget2Binding>(binding, widget)

    override fun getViewBinding(): ImageTextWidget2Binding {
        return ImageTextWidget2Binding.inflate(LayoutInflater.from(context), this, true)
    }
}

class ImageTextWidgetModel : WidgetEntityModel<ImageTextWidgetData, WidgetAction>()

@Keep
data class ImageTextWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("title_bold") val titleBold: Boolean?,
    @SerializedName("is_title_center") val isTitleCenter: Boolean?,
    @SerializedName("title_text_size") val titleTextSize: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("subtitle_text_size") val subtitleTextSize: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("image_url2") val imageUrl2: String?,
    @SerializedName("image_url3") val imageUrl3: String?,
    @SerializedName("bg_color") val bgColor: String?,
    @SerializedName("text_color") val textColor: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("cta") val cta: Cta?,
    @SerializedName("margin_top") val marginTop: String?
) : WidgetData() {

    @Keep
    data class Cta(
        @SerializedName("title") val title: String?,
        @SerializedName("deeplink") val deeplink: String?
    )
}
