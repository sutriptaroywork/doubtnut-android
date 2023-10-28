package com.doubtnut.core.entitiy

import androidx.annotation.Keep

/**
 * Created by Anand Gaurav on 2019-07-17.
 */
@Keep
open class CoreAnalyticsEvent(
    val name: String,
    val params: HashMap<String, Any> = hashMapOf(),
    var ignoreApxor: Boolean = false,
    var ignoreBranch: Boolean = true,
    var ignoreFacebook: Boolean = false,
    var ignoreSnowplow: Boolean = false,
    var ignoreMoengage: Boolean = true,
    var ignoreFirebase: Boolean = true,
    var ignoreConviva: Boolean = true // Disable conviva for now
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
        ignoreFirebase: Boolean = true
    ) :
            this(
                name,
                hashMapOf<String, Any>().apply {
                    put(paramKey, paramValue)
                },
                ignoreApxor, ignoreBranch, ignoreFacebook,
                ignoreSnowplow, ignoreMoengage, ignoreFirebase
            )
}