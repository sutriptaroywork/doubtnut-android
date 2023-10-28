package com.doubtnutapp.utils

import android.content.Context
import androidx.annotation.RawRes
import com.doubtnutapp.R
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.BufferedReader
import java.io.IOException

/**
 * Fake interceptor for debugging.
 */
class FakeInterceptor(private val mContext: Context) : Interceptor {

    private var mContentType = "application/json"

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        @Suppress("UNUSED_VARIABLE")
        val uri = chain.request().url.toUrl()
        val type = chain.request().url.queryParameter("method")
        @Suppress("ControlFlowWithEmptyBody")
        if
                (uri.toString().contains("v1/clp/get-filter-datas")) {
            return getResponse(chain, R.raw.response)
        }
//            }
//            uri.toString().contains("v2/checkout/placeholder_submit") &&
//                    type == "wallet" -> {
//                return getResponse(chain, R.raw.paymentsubmitwallet)
//            }
//            uri.toString().contains("v2/checkout/placeholder_submit") &&
//                    type == "card" -> {
//                return getResponse(chain, R.raw.paymentsubmitcard)
//            }
//            uri.toString().contains("v2/checkout/placeholder_submit") &&
//                    type == "upi_collect" -> {
//                return getResponse(chain, R.raw.paymentsubmitupi)
//            }
//            uri.toString().contains("v2/checkout/placeholder") -> {
//                return getResponse(chain, R.raw.paymentnewtwo)
//            }
//        }
        else return chain.proceed(chain.request())
    }

    fun getResponse(chain: Interceptor.Chain, @RawRes resId: Int): Response {
        val jsonString = this.mContext.resources
            .openRawResource(resId)
            .bufferedReader()
            .use(BufferedReader::readText)

        val builder = Response.Builder()
        builder.request(chain.request())
        builder.protocol(Protocol.HTTP_1_0)
        builder.addHeader("content-type", mContentType)
        builder.body(
            jsonString.toByteArray().toResponseBody(mContentType.toMediaTypeOrNull())
        )
        builder.code(200)
        builder.message(jsonString)
        return builder.build()
    }
}