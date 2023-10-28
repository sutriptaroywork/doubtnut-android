package com.doubtnut.core.entitiy

import androidx.annotation.Keep

/**
 * Created by Anand Gaurav on 2019-07-17.
 */
@Keep
open class AnalyticsEvent(
    name: String,
    params: HashMap<String, Any> = hashMapOf(),
    ignoreApxor: Boolean = false,
    ignoreBranch: Boolean = true,
    ignoreFacebook: Boolean = false,
    ignoreSnowplow: Boolean = false,
    ignoreMoengage: Boolean = true,
    ignoreFirebase: Boolean = true,
    ignoreConviva: Boolean = true // Disable conviva for now
) : CoreAnalyticsEvent(
    name = name,
    params = params,
    ignoreApxor = ignoreApxor,
    ignoreBranch = ignoreBranch,
    ignoreFacebook = ignoreFacebook,
    ignoreSnowplow = ignoreSnowplow,
    ignoreMoengage = ignoreMoengage,
    ignoreFirebase = ignoreFirebase,
    ignoreConviva = ignoreConviva
) {

    // use this if one param is to be sent (eg. source: home)
    constructor(
        name: String,
        paramKey: String,
        paramValue: String,
        ignoreApxor: Boolean = false,
        ignoreBranch: Boolean = true,
        ignoreFacebook: Boolean = false,
        ignoreSnowplow: Boolean = false,
        ignoreMoengage: Boolean = true,
        ignoreFirebase: Boolean = true,
        ignoreConviva: Boolean = true // Disable conviva for now
    ) :
            this(
                name = name,
                params = hashMapOf<String, Any>().apply {
                    put(paramKey, paramValue)
                },
                ignoreApxor = ignoreApxor,
                ignoreBranch = ignoreBranch,
                ignoreFacebook = ignoreFacebook,
                ignoreSnowplow = ignoreSnowplow,
                ignoreMoengage = ignoreMoengage,
                ignoreFirebase = ignoreFirebase,
                ignoreConviva = ignoreConviva
            )

    fun copy() : AnalyticsEvent {
        return  AnalyticsEvent(
            name = name,
            params = hashMapOf<String, Any>().apply {
                this.putAll(params)
            },
            ignoreApxor = ignoreApxor,
            ignoreBranch = ignoreBranch,
            ignoreFacebook = ignoreFacebook,
            ignoreSnowplow = ignoreSnowplow,
            ignoreMoengage = ignoreMoengage,
            ignoreFirebase = ignoreFirebase,
            ignoreConviva = ignoreConviva
        )
    }
}