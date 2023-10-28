package com.doubtnutapp.data.remote

import android.content.Context
import android.content.Intent
import com.doubtnut.analytics.constant.EventPlatforms
import com.doubtnut.core.data.remote.ResponseMeta
import com.doubtnut.core.entitiy.CoreAnalyticsEvent
import com.doubtnut.core.utils.isRunning
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.Features
import com.doubtnutapp.FeaturesManager
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.login.ui.activity.FailedGuestLoginActivity
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.utils.ApxorUtils
import com.google.gson.Gson
import dagger.Lazy
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class ApiMetaInterceptor(
    private val mContext: Context,
    val gson: Gson
) : Interceptor {

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    @Inject
    lateinit var analyticsPublisher: Lazy<AnalyticsPublisher>

    @Inject
    lateinit var deeplinkAction: Lazy<DeeplinkAction>

    @Inject
    lateinit var userPreference: Lazy<UserPreference>

    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()
        request = addVariationIdsHeader(request)

        val response = chain.proceed(request)
        interceptMetaResponse(response)

        return response
    }

    /**
     * adds all the evaluated variation ids from flagr in a header
     */
    private fun addVariationIdsHeader(originalRequest: Request): Request {
        if (Features.capabilities.keys.isEmpty()) return originalRequest

        val variationIds = arrayListOf<String>()
        Features.capabilities.keys.forEach {
            val featureVariant = FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, it)
            if (featureVariant != -1) {
                variationIds.add(featureVariant.toString())
            }
        }

        if (variationIds.isEmpty()) return originalRequest

        return originalRequest.newBuilder().addHeader(
            "flagr_variation_ids",
            variationIds.joinToString(",")
        ).build()
    }

    private fun interceptMetaResponse(response: Response) {
        try {
            val responseBody = response.peekBody(Long.MAX_VALUE)
            val apiMeta = gson.fromJson<ApiResponse<Any>>(
                responseBody.string(),
                ApiResponse::class.java
            )
            interceptExtrasMeta(apiMeta?.meta)

            // Launching api based popup deeplink
            apiMeta?.meta?.inAppPopUp?.deeplink
                ?.takeIf { it.isNotNullAndNotEmpty() }
                ?.let { deeplink ->
                    val activity = DoubtnutApp.INSTANCE.lifecycleHandler.activityResumed?.get()
                    if (activity != null && activity.isRunning()) {
                        deeplinkAction.get().performAction(activity, deeplink)
                    }
                }

            // peek 5kb of response and if the meta doesn't have analytics info,
            // return and don't proceed with deserialization
            interceptAnalyticsMeta(apiMeta?.meta)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * intercept extras info in the meta and use it to show popup
     *.For ex - prompt guest user for login
     */
    private fun interceptExtrasMeta(response: ResponseMeta?) {
        val guestLoginAppUseLimitExceed = response?.extras?.guestLoginAppUseLimitExceed ?: false

        // Keys to increment counts in user journey like QA count, VV count etc...
        val incrementKeys = response?.incrementKeys
        incrementUserJourneyKeys(incrementKeys)

        if (!guestLoginAppUseLimitExceed) return
        val popupDetails = response?.extras?.popupDetails ?: return
        val intent = FailedGuestLoginActivity.getStartIntent(
            context = mContext,
            popupDetails = popupDetails,
            source = null
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        mContext.startActivity(intent)
    }

    private fun incrementUserJourneyKeys(incrementKeys: Map<String, Int>?) {
        val journeyCounter = incrementKeys ?: return
        journeyCounter.forEach { entry ->
            val key = entry.key
            val interval = entry.value
            val previousValue = userPreference.get().getUserJourneyCountForKey(key)
            userPreference.get().setUserJourneyCountForKey(key, previousValue + interval)
        }
    }

    /**
     * intercept analytics info in the meta and use them accordingly.
     *
     * If some of the flags were evaluated by the backend for this api, api can return
     * the flag variant info in meta and we update the same accordingly in apxor
     */
    private fun interceptAnalyticsMeta(apiMeta: ResponseMeta?) {
        val analyticsMeta = apiMeta?.analytics ?: return
        // Updating apxor attributes from variant Info
        analyticsMeta.variantInfo
            ?.takeIf { it.isNotEmpty() }
            ?.forEach {
                ApxorUtils.userInfo.putAttribute("experiment-${it.flagName}", it.variationId)
                ApxorUtils.setUserCustomInfo()
            }

        // Updating attributes for different platforms dynamically
        analyticsMeta.attributes?.forEach { attribute ->
            attribute.platforms?.forEach { platform ->
                when (EventPlatforms.fromString(platform)) {
                    EventPlatforms.APXOR -> {
                        when (val value = attribute.value) {
                            is String -> ApxorUtils.userInfo.putAttribute(attribute.key, value)
                            is Int -> ApxorUtils.userInfo.putAttribute(attribute.key, value)
                            is Double -> ApxorUtils.userInfo.putAttribute(attribute.key, value)
                            is Long -> ApxorUtils.userInfo.putAttribute(attribute.key, value)
                            is Float -> ApxorUtils.userInfo.putAttribute(attribute.key, value)
                            is Boolean -> ApxorUtils.userInfo.putAttribute(attribute.key, value)
                        }
                        ApxorUtils.setUserCustomInfo()
                    }
                    EventPlatforms.BRANCH -> {}
                    EventPlatforms.CLEVERTAP -> {}
                    EventPlatforms.MOENGAGE -> {
                        MoEngageUtils.setUserAttribute(
                            appContext = DoubtnutApp.INSTANCE.applicationContext,
                            key = attribute.key.orEmpty(),
                            value = attribute.value,
                        )
                    }
                    EventPlatforms.FIREBASE -> {}
                    EventPlatforms.FACEBOOK -> {}
                    EventPlatforms.SNOWPLOW -> {}
                    else -> {}
                }
            }
        }

        // Sending event for different platform dynamically
        analyticsMeta.events?.forEach { event ->
            analyticsPublisher.get()?.publishEvent(
                CoreAnalyticsEvent(
                    name = event.name.orEmpty(),
                    params = hashMapOf<String, Any>().apply {
                        putAll(event.params.orEmpty())
                    },
                    ignoreApxor = !event.platforms.orEmpty()
                        .contains(EventPlatforms.APXOR.toString()),
                    ignoreBranch = !event.platforms.orEmpty()
                        .contains(EventPlatforms.BRANCH.toString()),
                    ignoreFacebook = !event.platforms.orEmpty()
                        .contains(EventPlatforms.FACEBOOK.toString()),
                    ignoreSnowplow = !event.platforms.orEmpty()
                        .contains(EventPlatforms.SNOWPLOW.toString()),
                    ignoreMoengage = !event.platforms.orEmpty()
                        .contains(EventPlatforms.MOENGAGE.toString()),
                    ignoreFirebase = !event.platforms.orEmpty()
                        .contains(EventPlatforms.FIREBASE.toString())
                )
            )
        }
    }

    companion object {
        private const val MAX_META_BYTES = 1024 * 5L
    }
}
