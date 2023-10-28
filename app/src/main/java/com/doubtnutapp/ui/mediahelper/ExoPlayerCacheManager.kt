package com.doubtnutapp.ui.mediahelper

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.net.toUri
import com.doubtnutapp.Log
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheWriter
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File

class ExoPlayerCacheManager private constructor(private val context: Context) {

    private val cacheMap = hashMapOf<String, CacheWriter>()

    val cache: SimpleCache by lazy {
        SimpleCache(
                File(context.cacheDir, "exo_cache"),
                NoOpCacheEvictor(),
                ExoDatabaseProvider(context)
        )
    }

    /**
     * Use this method to cache video
     * @param videoUrl takes the full url of video to be cached
     * @param cacheSize size of cache in bytes which will be cached
     * @param mediaSourceType define the media type
     */

    @SuppressLint("CheckResult")
    fun cacheVideo(videoUrl: String, cacheSize: Long? = null, mediaSourceType: ExoPlayerHelper.MediaSourceType) {
        Single.fromCallable {
            val dataSource = ExoPlayerHelper.getUpStreamDataSourceFactory(mediaSourceType).createDataSource()
            val dataSpec = DataSpec.Builder()
                    .setPosition(0)
                    .setFlags(DataSpec.FLAG_DONT_CACHE_IF_LENGTH_UNKNOWN)
                    .setUri(videoUrl.toUri())
                    .setLength(cacheSize ?: DEFAULT_CACHE_SIZE)
                    .setKey(videoUrl)
                    .build()
            val cacheWriter = CacheWriter(CacheDataSource(cache, dataSource), dataSpec, null) { requestLength, bytesCached, newBytesCached ->
                val downloadPercentage: Double = (bytesCached * 100.0
                        / requestLength)
                Log.d("exoPlayer", "percentage : $downloadPercentage%")
            }
            cacheMap[videoUrl] = cacheWriter
            cacheWriter.cache()

        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    /**
     * This is use to clear a specific video cache
     * @param videoUrl pass the url of video to clear it's caches if created
     */

    fun clearCache(videoUrl: String) {
        cacheMap[videoUrl]?.cancel()
        cache.removeResource(videoUrl)
    }

    /**
     * This methods clears all of cache videos
     */

    fun clearAllCache() {
       /* Single.fromCallable {
            cache.keys.forEach {
                cache.removeResource(it)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()*/
    }

    /**
     * This method is used to stop caching current files.
     * It'll all present caching process
     */

    fun stopCaching() {
       // cacheMap.mapKeys {
       //     it.value.cancel()
       // }
    }

    companion object {

        private var instance: ExoPlayerCacheManager? = null

        const val DEFAULT_CACHE_SIZE = 700 * 1024L

        fun getInstance(context: Context) = instance ?: synchronized(ExoPlayerCacheManager::class) {
            instance ?: ExoPlayerCacheManager(context).apply {
                instance = this
            }
        }

    }

}