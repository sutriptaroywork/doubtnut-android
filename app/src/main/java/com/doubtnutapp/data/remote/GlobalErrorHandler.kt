package com.doubtnutapp.data.remote

import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.Log
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.uxcam.UXCam
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.EOFException
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import javax.inject.Inject

class GlobalErrorHandler private constructor() : Interceptor {

    private val CHARSET_UTF8 = "UTF-8"
    private val ENDPOINT = "endpoint"
    private val METHOD = "method"
    private val HEADERS = "headers"
    private val STATUS_CODE = "status_code"
    private val REQUEST_BODY = "request_body"
    private val RESPONSE_BODY = "response_body"
    private val TIME_TAKEN = "time_taken"
    private val HEADER_PREFIX = "header_"
    private val BINARY_BODY_STRING = "Binary %d-Byte Body"
    private val MAX_BUFFER_SIZE = 64
    private val BUFFER_SEQUENCE_SIZE = 16
    private val BUFFER_INITIAL_OFFSET = 0

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val params = getParamsForLogging(request, response)
        val eventParams = params?.filterKeys {
            it == ENDPOINT || it == STATUS_CODE
        } as? Map<String, Any> ?: emptyMap()

        if (shouldLogRequest(request, response)) {
            if (params != null) {
                Log.e(Throwable("HttpException in api url " + request.url.toString() + "\n" + params.toString()), "HttpException")
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EVENT_HTTP_ERROR,
                        params.entries.associateTo(HashMap()) {
                            it.key to it.value
                        },
                        ignoreSnowplow = true, ignoreFirebase = false
                    )
                )

                // Send errors to UXCam as well
                UXCam.logEvent(EventConstants.EVENT_HTTP_ERROR, params)
            }
        }
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.EVENT_API_REQUEST,
                eventParams.entries.associateTo(HashMap()) {
                    it.key to it.value
                },
                ignoreApxor = true, ignoreFacebook = true, ignoreSnowplow = true, ignoreFirebase = false
            )
        )
        return response
    }

    private fun shouldLogRequest(request: Request?, response: Response?): Boolean {
        val httpErrorStatusCodes = listOf(400, 401, 404, 500, 501, 502, 503, 504)
        return request != null && response != null && httpErrorStatusCodes != null && httpErrorStatusCodes.contains(response.code)
    }

    private fun getParamsForLogging(request: Request, response: Response): Map<String, String>? {
        val keys = listOf(ENDPOINT, METHOD, STATUS_CODE, TIME_TAKEN)
        val params: HashMap<String, String> = HashMap()
        for (key in keys) {
            if (key == null) continue
            when (key) {
                ENDPOINT -> params[ENDPOINT] = if (request.url != null) request.url.toString() else "#NA"
                METHOD -> params[METHOD] = request.method
                HEADERS -> if (request.headers != null) {
                    for (header in request.headers.names()) params[HEADER_PREFIX + header] = request.headers[header] ?: "NA"
                }
                STATUS_CODE -> params[STATUS_CODE] = response.code.toString()
                RESPONSE_BODY -> params[RESPONSE_BODY] = getResponseBody(response) ?: ""
                REQUEST_BODY -> params[REQUEST_BODY] = getRequestBody(request) ?: ""
                TIME_TAKEN -> params[TIME_TAKEN] = (response.receivedResponseAtMillis - response.sentRequestAtMillis).toString()
            }
        }
        return params
    }

    private fun getRequestBody(request: Request): String? {
        try {
            val requestBody = request.body
            if (requestBody != null) {
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                val charset = getCharset(requestBody.contentType())
                return if (isPlaintext(buffer)) {
                    charset?.let { buffer.readString(it) }
                } else {
                    String.format(Locale.getDefault(), BINARY_BODY_STRING, requestBody.contentLength())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "#NA"
    }

    private fun getResponseBody(response: Response): String? {
        try {
            val responseBody = response.body
            if (responseBody != null) {
                val source = responseBody.source()
                source.request(Long.MAX_VALUE) // Buffer the entire body.
                val buffer = source.buffer()
                val charset = getCharset(responseBody.contentType())
                return if (isPlaintext(buffer)) {
                    charset?.let { buffer.clone().readString(it) }
                } else {
                    String.format(Locale.getDefault(), BINARY_BODY_STRING, buffer.size)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "#NA"
    }

    private fun isPlaintext(buffer: Buffer): Boolean {
        return try {
            val prefix = Buffer()
            val byteCount = if (buffer.size < MAX_BUFFER_SIZE) buffer.size else MAX_BUFFER_SIZE.toLong()
            buffer.copyTo(prefix, BUFFER_INITIAL_OFFSET.toLong(), byteCount)
            for (index in 0 until BUFFER_SEQUENCE_SIZE) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            true
        } catch (e: EOFException) {
            e.printStackTrace()
            false // Truncated UTF-8 sequence.
        }
    }

    private fun getCharset(contentType: MediaType?): Charset? {
        return if (contentType != null) {
            contentType.charset(Charset.forName(CHARSET_UTF8))
        } else {
            Charset.forName(CHARSET_UTF8)
        }
    }

    companion object {
        private var sInstance: GlobalErrorHandler? = null
        val instance: GlobalErrorHandler?
            get() {
                if (sInstance == null) {
                    synchronized(GlobalErrorHandler::class.java) {
                        if (sInstance == null) {
                            sInstance = GlobalErrorHandler()
                        }
                    }
                }
                return sInstance
            }
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }
}
