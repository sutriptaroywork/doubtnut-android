package com.doubtnutapp.widgets

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.doubtnut.core.utils.CoreRemoteConfigUtils
import com.doubtnutapp.StethoUtils
import com.doubtnutapp.networkstats.GlideInterceptor
import com.doubtnutapp.utils.videothumbnail.VideoThumbnailModelLoaderFactory
import okhttp3.OkHttpClient
import java.io.InputStream

@GlideModule
class DoubtNutAppGlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(String::class.java, Bitmap::class.java, VideoThumbnailModelLoaderFactory())
        val builder = OkHttpClient.Builder()
        StethoUtils.addNetworkInterceptor(builder)
        builder.addInterceptor(GlideInterceptor())
        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(builder.build())
        )
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
        if (CoreRemoteConfigUtils.enableGlideOptimisation()) {
            val diskCacheSizeBytes = 1024 * 1024 * 100 // 100 MB
            builder.setDiskCache(
                InternalCacheDiskCacheFactory(
                    context,
                    diskCacheSizeBytes.toLong()
                )
            )
        }

        if (CoreRemoteConfigUtils.enableRgb565()) {
            builder.setDefaultRequestOptions(
                RequestOptions().format(DecodeFormat.PREFER_RGB_565))
        }
    }
}