package com.doubtnutapp

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * Created by Anand Gaurav on 02/04/20.
 */
class ProgressRequestBody(
    private val file: File,
    private val content_type: String,
    private val listener: UploadProgressListener,
) : RequestBody() {
    private val path: String? = null

    private val progressPublishSubject: Subject<Int>
    private val progressSubscription: Disposable

    interface UploadProgressListener {
        fun onProgressUpdate(percentage: Int)
    }

    override fun contentType(): MediaType? {
        return content_type.toMediaTypeOrNull()
    }

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return file.length()
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val fileLength = file.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val `in` = FileInputStream(file)
        var uploaded: Long = 0
        try {
            var read: Int
            while (`in`.read(buffer).also { read = it } != -1) { // update progress on UI thread
                progressPublishSubject.onNext(((uploaded / fileLength.toFloat()) * 100).toInt())
                uploaded += read.toLong()
                sink.write(buffer, 0, read)
            }
        } finally {
            `in`.close()
            progressSubscription.dispose()
        }
    }



    init {
        progressPublishSubject = PublishSubject.create()
        progressSubscription = progressPublishSubject
                .observeOn(AndroidSchedulers.mainThread())
                .distinctUntilChanged()
                .subscribe({ progress: Int ->
                    listener.onProgressUpdate(progress)
                }, {
                    it.printStackTrace()
                })
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }

}