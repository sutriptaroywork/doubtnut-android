package com.doubtnut.videocompressor.utils

import com.doubtnut.videocompressor.VideoCompress
import io.reactivex.Completable

/**
 * Created by Anand Gaurav on 25/03/20.
 */
object RxVideoCompressor {

    fun videoCompressorCompletable(path: String, destPath: String): Completable {
        return Completable.create { emitter ->
            @Suppress("INACCESSIBLE_TYPE")
            VideoCompress.compressVideoMedium(
                path,
                destPath,
                object : VideoCompress.CompressListener {
                    override fun onSuccess() {
                        emitter.onComplete()
                    }

                    override fun onFail() {
                        emitter.onError(Throwable("OnFail"))
                    }

                    override fun onProgress(percent: Float) {
                    }

                    override fun onStart() {
                    }
                })
        }
    }

}