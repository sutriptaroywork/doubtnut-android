package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.MicroService
import com.doubtnutapp.data.remote.api.services.PdfService
import com.doubtnutapp.data.remote.models.PdfUrlData
import com.doubtnutapp.data.toRequestBody
import io.reactivex.Single
import okhttp3.ResponseBody
import java.io.BufferedOutputStream
import java.io.FileOutputStream

class PdfRepository(private val pdfService: PdfService, private val microService: MicroService) {

    fun downloadPdf(pdfUrl: String, storagePath: String): Single<String> {
        return pdfService.downloadPdf(pdfUrl)
            .flatMap {
                savePdfToDisk(it, storagePath)
            }
    }

    private fun savePdfToDisk(pdfResponseBody: ResponseBody, storagePath: String): Single<String> {
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

    fun getPdfUrl(entityId: String, limit: Int, title: String, fileName: String, persist: Boolean): Single<PdfUrlData> {

        val params = mapOf(
            "filter" to mapOf("entityId" to entityId),
            "limit" to limit,
            "title" to title,
            "fileName" to fileName,
            "persist" to persist
        )

        return microService.getPdfUrl(params.toRequestBody())
    }
}
