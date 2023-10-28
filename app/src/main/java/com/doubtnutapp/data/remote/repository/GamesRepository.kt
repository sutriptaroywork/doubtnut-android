package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import io.reactivex.Single
import okhttp3.ResponseBody
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import javax.inject.Inject

class GamesRepository @Inject constructor(private val networkService: NetworkService) {

    fun fetchGamesList(source: String?) = networkService.fetchGamesList(source)

    fun downloadGame(pdfUrl: String, storagePath: String): Single<String> {
        return networkService.downloadGame(pdfUrl)
            .flatMap {
                saveGameToDisk(it, storagePath)
            }
    }

    private fun saveGameToDisk(pdfResponseBody: ResponseBody, storagePath: String): Single<String> {
        return Single.fromCallable {
            val bufferedOutputStream = BufferedOutputStream(FileOutputStream(storagePath))
            val bufferSize = 1024
            bufferedOutputStream.use { op ->
                val inputStream = pdfResponseBody.byteStream()
                inputStream.use { ip ->
                    ip.copyTo(op, bufferSize)
                }
                storagePath
            }
        }
    }
}
