package com.doubtnut.core.utils

import android.annotation.SuppressLint
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

object ImageUtils

/**
 * This is similar to loadImage. To prevent imports related changes
 * create a new one.
 */
@SuppressLint("CheckResult")
fun ImageView.loadImage2(
    url: String?,
    @DrawableRes placeholder: Int? = null,
    @DrawableRes errorDrawable: Int? = null,
    diskCacheStrategy: DiskCacheStrategy = CoreRemoteConfigUtils.getDiskCacheStrategy(),
    format: DecodeFormat = CoreRemoteConfigUtils.getDecodeFormat(),
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
        .into(this)
}

fun ImageView.load2(
    @DrawableRes resId: Int,
    diskCacheStrategy: DiskCacheStrategy = CoreRemoteConfigUtils.getDiskCacheStrategy(),
    format: DecodeFormat = CoreRemoteConfigUtils.getDecodeFormat(),
) {
    if (Utils.isValidContextForGlide(context).not()) return
    Glide.with(this).load(resId)
        .diskCacheStrategy(diskCacheStrategy)
        .format(format)
        .into(this)
}
