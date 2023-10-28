package com.doubtnut.core.utils

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.URLSpan
import android.util.TypedValue
import android.view.Gravity
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.doubtnut.core.helpers.GlideImageGetter

object TextViewUtils {

    @JvmStatic
    fun setTextFromHtml(
        textView: TextView,
        message: String,
        imageGetter: Html.ImageGetter? = null,
        tagHandler: Html.TagHandler? = null
    ) {
        textView.setTextFromHtml(
            message = message,
            imageGetter = imageGetter ?: GlideImageGetter(textView),
            tagHandler = tagHandler
        )
    }
}

fun TextView.setTextFromHtml(
    message: String,
    imageGetter: Html.ImageGetter = GlideImageGetter(this),
    tagHandler: Html.TagHandler? = null
) {
    this.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY, imageGetter, tagHandler) as Spannable
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(message, imageGetter, tagHandler) as Spannable
    }
}

fun TextView.applyTextSize(textSize: String?) {
    if (!textSize.isNullOrEmpty()) {
        this.textSize = textSize.toFloat()
    }
}

fun TextView.applyAutoSizeTextTypeUniformWithConfiguration(
    textSize: String?,
    minTextSize: Int = 10
) {
    if (!textSize.isNullOrEmpty()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.setAutoSizeTextTypeUniformWithConfiguration(
                minTextSize,
                textSize.toInt(),
                1,
                TypedValue.COMPLEX_UNIT_DIP
            )
        } else {
            this.applyTextSize(textSize)
        }
    }
}

fun TextView.applyTextColor(textColor: String?) {
    if (!textColor.isNullOrEmpty()) {
        this.setTextColor(parseColor(textColor, Color.BLACK))
    }
}

fun EditText.applyHintColor(textColor: String?) {
    if (!textColor.isNullOrEmpty()) {
        this.setHintTextColor(parseColor(textColor, Color.BLACK))
    }
}

fun TextView.applyTextGravity(horizontalBias: Float?) {
    gravity = when (horizontalBias) {
        0.5f -> {
            Gravity.CENTER
        }
        1f -> {
            Gravity.END
        }
        else -> {
            Gravity.START
        }
    }
}

fun TextView.applyTypeface(isBold: Boolean?) {
    setTypeface(typeface, if (isBold == true) Typeface.BOLD else Typeface.NORMAL)
}

fun TextView.underline() {
    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

fun TextView.applyStrike(strike: Boolean?) {
    paintFlags = if (strike == true) {
        paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    } else {
        paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }
}

fun TextView.removeLinksUnderline(textColor: String?) {
    val spannable = SpannableString(text)
    for (u in spannable.getSpans(0, spannable.length, URLSpan::class.java)) {
        spannable.setSpan(
            object : URLSpan(u.url) {
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                    if (textColor.isNullOrEmpty().not()) {
                        ds.color = parseColor(textColor, Color.BLACK)
                    }
                }
            },
            spannable.getSpanStart(u), spannable.getSpanEnd(u), 0
        )
    }
    text = spannable
}

@SuppressLint("CheckResult")
fun TextView.loadIntoCustomViewTarget(
    url: String?,
    @DrawableRes placeholder: Int? = null,
    @DrawableRes errorDrawable: Int? = null,
    diskCacheStrategy: DiskCacheStrategy = CoreRemoteConfigUtils.getDiskCacheStrategy(),
    format: DecodeFormat = CoreRemoteConfigUtils.getDecodeFormat(),
    _onResourceReady: (Drawable) -> Unit,
    _onLoadFailed: (Drawable?) -> Unit,
    _onResourceCleared: (Drawable?) -> Unit
) {
    if (!Utils.isValidContextForGlide(this.context)) return
    Glide.with(this)
        .load(url)
        .also { glide ->
            val requestOptions = RequestOptions()
            placeholder?.also { drawable ->
                requestOptions.placeholder(drawable)
            }
            errorDrawable?.let { errorDrawable ->
                requestOptions.error(errorDrawable)
            }
            glide.apply(requestOptions)
        }
        .diskCacheStrategy(diskCacheStrategy)
        .format(format)
        .into(
            object : CustomViewTarget<TextView, Drawable>(this) {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    _onResourceReady(resource)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    _onLoadFailed(errorDrawable)
                }

                override fun onResourceCleared(placeholder: Drawable?) {
                    _onResourceCleared(placeholder)
                }
            }
        )
}

