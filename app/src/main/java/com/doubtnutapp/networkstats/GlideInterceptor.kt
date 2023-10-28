package com.doubtnutapp.networkstats

import com.doubtnutapp.networkstats.ui.NetworkStatsActivity
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Created by Raghav Aggarwal on 28/01/22.
 */

class GlideInterceptor : Interceptor {
    companion object {
        var sum: Float = 0.0F
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()

        val response: Response = chain.proceed(request)

        val contentLength = response.body?.contentLength()
        if (contentLength != null && contentLength.toInt() != -1) {
            sum += contentLength
            NetworkStatsActivity.sessionImageData = sum
        }
        response.close()
        return chain.proceed(chain.request())
    }
}