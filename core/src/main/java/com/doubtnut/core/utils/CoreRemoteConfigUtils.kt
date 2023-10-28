package com.doubtnut.core.utils

import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.JsonDeserializer
import org.json.JSONObject


object CoreRemoteConfigUtils {

    private const val ENABLE_GLIDE_OPTIMISATION = "enable_glide_optimisation"
    private const val ENABLE_RGB_565 = "enable_rgb_565"
    private const val ASK_FLOW_TIME_OUT = "ASK_FLOW_TIME_OUT"
    private const val RETRY_ASK_FLOW_COUNT = "RETRY_ASK_FLOW_COUNT"

    private const val APXOR_BLACKLIST = "APXOR_BLACKLIST"
    private const val BRANCH_BLACKLIST = "BRANCH_BLACKLIST"
    private const val FB_BLACKLIST = "FB_BLACKLIST"
    private const val SNOWPLOW_BLACKLIST = "SNOWPLOW_BLACKLIST"
    private const val MOENGAGE_BLACKLIST = "MOENGAGE_BLACKLIST"
    private const val FIREBASE_BLACKLIST = "FIREBASE_BLACKLIST"
    private const val GLOBAL_BLACKLIST = "GLOBAL_BLACKLIST"

    fun getApxorBlacklist() = getHashMapFromRemoteConfig(APXOR_BLACKLIST)

    fun getBranchBlacklist() = getHashMapFromRemoteConfig(BRANCH_BLACKLIST)

    fun getFacebookBlacklist() = getHashMapFromRemoteConfig(FB_BLACKLIST)

    fun getSnowPlowBlacklist() = getHashMapFromRemoteConfig(SNOWPLOW_BLACKLIST)

    fun getMoengageBlacklist() = getHashMapFromRemoteConfig(MOENGAGE_BLACKLIST)

    fun getFirebaseBlacklist() = getHashMapFromRemoteConfig(FIREBASE_BLACKLIST)

    fun getGlobalBlacklist() = getHashMapFromRemoteConfig(GLOBAL_BLACKLIST)

    private fun getHashMapFromRemoteConfig(value: String) =
        Gson().fromJson(
            FirebaseRemoteConfig.getInstance().getString(value),
            HashMap::class.java
        ) ?: hashMapOf<String, String>()

    fun enableGlideOptimisation(): Boolean =
        FirebaseRemoteConfig.getInstance().getBoolean(ENABLE_GLIDE_OPTIMISATION)

    fun getDiskCacheStrategy(): DiskCacheStrategy = if (enableGlideOptimisation()) {
        DiskCacheStrategy.RESOURCE
    } else {
        DiskCacheStrategy.AUTOMATIC
    }

    fun enableRgb565(): Boolean = FirebaseRemoteConfig.getInstance().getBoolean(ENABLE_RGB_565)

    fun getDecodeFormat(): DecodeFormat = if (enableRgb565()) {
        DecodeFormat.PREFER_RGB_565
    } else {
        DecodeFormat.PREFER_ARGB_8888
    }

    fun getAskFlowTimeOut(): Long {
        return FirebaseRemoteConfig.getInstance().getLong(ASK_FLOW_TIME_OUT)
    }

    fun getAskFlowRetryCount(): Long {
        return FirebaseRemoteConfig.getInstance().getLong(RETRY_ASK_FLOW_COUNT)
    }
}