package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.databinding.WidgetSgHomeBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.loadImage
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SgHomeWidget(context: Context) : BaseBindingWidget<
        SgHomeWidget.WidgetHolder,
        SgHomeWidget.Model,
        WidgetSgHomeBinding>(context) {

    var source: String? = null

    init {
        DoubtnutApp.INSTANCE
            .daggerAppComponent
            .forceUnWrap()
            .inject(this)
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetSgHomeBinding =
        WidgetSgHomeBinding.inflate(LayoutInflater.from(context), this, true)

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        model.layoutConfig = WidgetLayoutConfig(
            marginTop = 0,
            marginBottom = 0,
        )
        super.bindWidget(holder, model)

        val data = model.data
        val binding = holder.binding

        with(binding) {
            tvTitle.apply {
                isVisible = data.title.isEmpty().not()
                text = data.title
            }
            ivImage.apply {
                isVisible = data.image.isNullOrEmpty().not()
                loadImage(data.image)
            }
            btCheck.apply {
                isVisible = data.cta.isNullOrEmpty().not()
                text = data.cta
                if (data.ctaColor.isNullOrEmpty().not()) {
                    setTextColor(Color.parseColor(data.ctaColor))
                }
                if (data.ctaBackgroundColor.isNullOrEmpty().not()) {
                    setBackgroundColor(Color.parseColor(data.ctaBackgroundColor))
                }
                setOnClickListener {
                    deeplinkAction.performAction(binding.root.context, data.deeplink)
                }
            }
            tvSubtitle.apply {
                val subtitle = data.subtitle ?: return@apply
                val charToReplace = data.subtitleCharToReplace
                isVisible = subtitle.isEmpty().not()
                if (charToReplace.isNullOrEmpty().not() &&
                    subtitle.isEmpty().not() &&
                    subtitle.contains(charToReplace!!)
                ) {
                    val newSubtitle =
                        subtitle.replace(charToReplace, data.stringToReplaceWith.orEmpty())
                    text = setSpan(newSubtitle, data.stringToReplaceWith.orEmpty(), data.stringToReplaceWithColor)
                } else {
                    text = data.subtitle
                }
            }

            setOnClickListener {
                deeplinkAction.performAction(binding.root.context, data.deeplink)
            }
        }

        return holder
    }

    private fun setSpan(
        subtitle: String,
        stringToReplaceWith: String,
        stringToReplaceWithColor: String?
    ): SpannableStringBuilder {
        val builder = SpannableStringBuilder(subtitle)
        val startIndex = subtitle.indexOf(stringToReplaceWith)
        val lastIndex = startIndex + stringToReplaceWith.length

        val span = object : ClickableSpan() {
            override fun onClick(widget: View) {

            }
            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
                if (stringToReplaceWithColor.isNullOrEmpty().not()) {
                    ds.color = Color.parseColor(stringToReplaceWithColor)
                }
            }
        }
        builder.setSpan(span, startIndex, lastIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return builder
    }

    class WidgetHolder(binding: WidgetSgHomeBinding, baseWidget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetSgHomeBinding>(binding, baseWidget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val groupId: String,
        @SerializedName("title") val title: String,
        @SerializedName("image") val image: String?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("string_to_replace_with") val stringToReplaceWith: String?,
        @SerializedName("subtitle_char_to_replace") val subtitleCharToReplace: String?,
        @SerializedName("string_to_replace_with_color") val stringToReplaceWithColor: String?,
        @SerializedName("cta") val cta: String?,
        @SerializedName("cta_color") val ctaColor: String?,
        @SerializedName("cta_background_color") val ctaBackgroundColor: String?,
        @SerializedName("cta_deeplink") val deeplink: String?,
    ) : WidgetData()

}