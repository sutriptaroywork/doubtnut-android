package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.widget.TextView
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.doubtnut.core.utils.applyBackgroundColor
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.base.OnDoubtPeCharchaQuestionShareButtonClicked
import com.doubtnutapp.databinding.ItemDoubtPeCharchaQuestionBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.videoPage.ui.ShareOptionsBottomSheetFragment
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class DoubtPeCharchaQuestionWidget(context: Context) :
    BaseBindingWidget<DoubtPeCharchaQuestionWidget.DoubtPeCharchaQuestionViewholder,
            DoubtPeCharchaQuestionWidget.WidgetModel, ItemDoubtPeCharchaQuestionBinding>(
        context
    ) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): ItemDoubtPeCharchaQuestionBinding {
        return ItemDoubtPeCharchaQuestionBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = DoubtPeCharchaQuestionViewholder(getViewBinding(), this)
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    override fun bindWidget(
        holder: DoubtPeCharchaQuestionViewholder,
        model: WidgetModel
    ): DoubtPeCharchaQuestionViewholder {

        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(
                    marginTop = 0,
                    marginBottom = 0,
                    marginLeft = 0,
                    marginRight = 0
                )
            }
        )

        val binding = holder.binding
        val data = model.data
        binding.tvSubject.text = data.subject.orEmpty()
        binding.tvQuestionAskedTime.text = data.time.orEmpty()
        if (data.imageUrl.isNotNullAndNotEmpty()) {
            binding.imageViewQuestion.show()
            binding.imageViewQuestion.loadImage(data.imageUrl)
            binding.textViewQuestion.hide()
        } else {
            binding.textViewQuestion.show()
            binding.textViewQuestion.text = data.questionText
            binding.imageViewQuestion.hide()
        }

        if (data.questionTextBgColor.isNotNullAndNotEmpty()) {
            binding.textViewQuestion.applyBackgroundColor(data.questionTextBgColor)
        }

        data.featuredTag?.let { featuredTag ->
            binding.tvFeaturedTag.text = featuredTag.title
            binding.tvFeaturedTag.show()
            if (featuredTag.bgColorStart.isNotNullAndNotEmpty() && featuredTag.bgColorEnd.isNotNullAndNotEmpty()) {
                binding.tvFeaturedTag.background = Utils.getGradientView(
                    featuredTag.bgColorStart!!,
                    featuredTag.bgColorStart!!,
                    featuredTag.bgColorEnd!!,
                    GradientDrawable.Orientation.TOP_BOTTOM
                )
            }
            binding.tvFeaturedTag.applyTextColor(featuredTag.titleColor)
        } ?: run {
            binding.tvFeaturedTag.hide()
        }


        binding.imageViewShare.setColorFilter(ContextCompat.getColor(context, R.color.white))
        val tags = data.tags

        if (tags != null && tags.size > 0) {
            binding.tvTag1.text = tags[0].title.orEmpty()
            if (tags[0].bgColor.isNotNullAndNotEmpty()) {
                binding.tvTag1.background = setBackgroundForBg(tags[0].bgColor)
            }
            if (tags.size > 1) {
                binding.tvTag2.text = tags[1].title.orEmpty()
                if (tags[1].bgColor.isNotNullAndNotEmpty()) {
                    binding.tvTag2.background = setBackgroundForBg(tags[1].bgColor)
                }
            }
        }

        data.cta?.let { cta ->
            binding.buttonAction.text = cta.title
            binding.buttonAction.setOnClickListener {
                deeplinkAction.performAction(context, cta.deeplink)
            }

            binding.rootContainer.setOnClickListener {
                deeplinkAction.performAction(context, cta.deeplink)
            }
            binding.buttonAction.applyTextColor(cta.textColor)
        }

        binding.rootContainer.applyBackgroundColor(data.bgColor)

        if (data.topStripBgColor.isNotNullAndNotEmpty()) {
            binding.topStrip.applyBackgroundColor(data.topStripBgColor)
        }

        setBorderBgBackground(binding.buttonAction, data.cta?.textColor)


        return holder
    }

    private fun setBorderBgBackground(view: TextView, color: String?) {
        if (color.isNullOrEmpty()) {
            return
        }
        val radius = resources.getDimension(R.dimen.dimen_4dp)
        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, radius)
            .build()
        val materialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        val states = arrayOf(
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_enabled), // disabled
            intArrayOf(-android.R.attr.state_checked), // unchecked
            intArrayOf(android.R.attr.state_pressed)  // pressed
        )
        val colors = intArrayOf(
            Color.parseColor(color),
            Color.parseColor(color),
            Color.parseColor(color),
            Color.parseColor(color)
        )
        val colorStateList = ColorStateList(states, colors)

        materialShapeDrawable.fillColor =
            ContextCompat.getColorStateList(context, R.color.white)
        materialShapeDrawable.strokeColor = colorStateList
        materialShapeDrawable.strokeWidth = 3f
        view.background = materialShapeDrawable
    }

    private fun setBackgroundForBg(bgColor: String): MaterialShapeDrawable {
        val radius = resources.getDimension(R.dimen.dimen_150dp)
        val shapeAppearanceModel = ShapeAppearanceModel
            .builder()
            .setAllCorners(CornerFamily.ROUNDED, radius)
            .build()
        val materialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)

        val states = arrayOf(
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_enabled), // disabled
            intArrayOf(-android.R.attr.state_checked), // unchecked
            intArrayOf(android.R.attr.state_pressed)  // pressed
        )
        val colors = intArrayOf(
            Color.parseColor(bgColor),
            Color.parseColor(bgColor),
            Color.parseColor(bgColor),
            Color.parseColor(bgColor)

        )
        val colorStateList = ColorStateList(states, colors)

        materialShapeDrawable.fillColor = colorStateList
        return materialShapeDrawable
    }

    class DoubtPeCharchaQuestionViewholder(
        binding: ItemDoubtPeCharchaQuestionBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<ItemDoubtPeCharchaQuestionBinding>(binding, widget)

    class WidgetModel : WidgetEntityModel<DoubtPeCharchaItemData, WidgetAction>()

    @Keep
    data class DoubtPeCharchaItemData(
        @SerializedName("id") val id: String?,
        @SerializedName("room_id") val roomId: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("ocr_text") val questionText: String?,
        @SerializedName("question_text_bg_color") val questionTextBgColor: String?,
        @SerializedName("subject") val subject: String?,
        @SerializedName("timestamp") val time: String?,
        @SerializedName("tags") val tags: ArrayList<Tag>?,
        @SerializedName("featured_tag") val featuredTag: FeaturedTag?,
        @SerializedName("outer_bg_color") val bgColor: String?,
        @SerializedName("top_strip_bg_color") val topStripBgColor: String?,
        @SerializedName("cta") val cta: Cta?,
    ) : WidgetData()

    @Keep
    data class FeaturedTag(
        @SerializedName("title") val title: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("bg_start_color") val bgColorStart: String?,
        @SerializedName("bg_end_color") val bgColorEnd: String?
    )

    @Keep
    data class Tag(
        @SerializedName("title") val title: String,
        @SerializedName("bg_color") val bgColor: String,
    )

    @Keep
    data class Cta(
        @SerializedName("title") val title: String?,
        @SerializedName("title_color") val textColor: String?,
        @SerializedName("deeplink") val deeplink: String?
    )

}