package com.doubtnutapp.data.remote

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.content.edit
import com.doubtnut.core.utils.isNotNullAndNotEmpty2
import com.doubtnutapp.Constants
import com.doubtnutapp.Constants.RESPONSE_XAUTH_HEADER_REFRESH_TOKEN
import com.doubtnutapp.Constants.RESPONSE_XAUTH_HEADER_TOKEN
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.login.ui.activity.FailedGuestLoginActivity
import com.doubtnutapp.ui.splash.SplashActivity
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.HttpURLConnection

class RefreshTokenInterceptor(
    private val mContext: Context,
    private val userPreference: UserPreference,
    val gson: Gson
) : Interceptor {

    companion object {
        private const val TAG = "RefreshTokenInterceptor"
    }

    var movedToSplashTime: Long? = null

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = defaultPrefs(mContext)
            .getString(Constants.XAUTH_HEADER_TOKEN, "")
        val request = chain
            .request()
            .newBuilder()
            .apply {
                if (!token.isNullOrBlank()) {
                    removeHeader(Constants.XAUTH_HEADER_TOKEN)
                    addHeader(
                        Constants.XAUTH_HEADER_TOKEN,
                        token.orEmpty()
                    )
                }
                addUserJourneyCountToHeader(this)
            }
            .addHeader(Constants.VERSION_CODE, Utils.getVersionCode(mContext).toString())
            .addHeader(
                Constants.HAS_UPI,
                defaultPrefs().getBoolean(Constants.HAS_UPI, false).toString()
            )
            .build()

        var response = chain.proceed(request)
        if (!response.isSuccessful && response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            synchronized(this) {
                if (!token.isNullOrBlank()) {
                    val refreshToken =
                        defaultPrefs().getString(Constants.XAUTH_HEADER_REFRESH_TOKEN, "").orEmpty()
                    val builder = request.newBuilder()
                    builder.header(Constants.XAUTH_HEADER_TOKEN, token.orEmpty())
                    builder.header(Constants.XAUTH_HEADER_REFRESH_TOKEN, refreshToken)
                    addUserJourneyCountToHeader(builder)
                    builder.removeHeader(Constants.XAUTH_HEADER_TOKEN)
                    response = chain.proceed(builder.build())

                    if (!response.isSuccessful && response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {

                        val responseBody = response.peekBody(Long.MAX_VALUE)
                        val apiResponse = gson.fromJson<ApiResponse<Any>>(
                            responseBody.string(),
                            ApiResponse::class.java
                        )
                        if (apiResponse?.meta?.extras?.guestLoginAppUseLimitExceed == true && apiResponse.meta.extras?.popupDetails != null) {

                            val intent = FailedGuestLoginActivity.getStartIntent(
                                context = mContext,
                                popupDetails = apiResponse.meta.extras?.popupDetails,
                                source = request.url.toUrl().path
                            ).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                            mContext.startActivity(intent)
                        } else {
                            // force logout
                            userPreference.logOutUser()
                            if (movedToSplashTime == null || (
                                        movedToSplashTime
                                            ?: System.currentTimeMillis()
                                        ) + 10000 < System.currentTimeMillis()
                            ) {
                                movedToSplashTime = System.currentTimeMillis()
                                val intent = Intent(mContext, SplashActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                }
                                mContext.startActivity(intent)
                                Handler(Looper.getMainLooper()).post {
                                    showToast(mContext, "You have been logged out, Please login...")
                                }
                            }
                        }
                    }
                    updateToken(
                        response.header(RESPONSE_XAUTH_HEADER_TOKEN),
                        response.header(RESPONSE_XAUTH_HEADER_REFRESH_TOKEN)
                    )
                    return response
                }
            }
        }

        updateToken(
            response.header(RESPONSE_XAUTH_HEADER_TOKEN),
            response.header(RESPONSE_XAUTH_HEADER_REFRESH_TOKEN)
        )
        return response
    }

    private fun updateToken(token: String?, refreshToken: String?) {
        if (!token.isNullOrBlank()) {
            defaultPrefs().edit(true) {
                putString(Constants.XAUTH_HEADER_TOKEN, token)
            }
        }

        if (!refreshToken.isNullOrBlank()) {
            defaultPrefs().edit(true) {
                putString(Constants.XAUTH_HEADER_REFRESH_TOKEN, refreshToken)
            }
        }
    }

    private fun addUserJourneyCountToHeader(builder: Request.Builder) {
        val userJourneyMap = userPreference.getJourneyCountDataAsString()
        if (userJourneyMap.isNotNullAndNotEmpty2()) {
            val journeyMapType = object : TypeToken<Map<String?, Int?>?>() {}.type
            val journeyMap: Map<String, Int>? = gson.fromJson(userJourneyMap, journeyMapType)
            journeyMap?.forEach {
                val key = it.key
                val value = userPreference.getUserJourneyCountForKey(key)
                builder.addHeader(key, value.toString())
            }
        }
    }
}
