package com.doubtnut.analytics

/**
 * Class is used to handles the event logging
 * */
interface TrackerClient {

    // this will uniquely identify the TrackerClient
    val id: String

    /**
     * Provide implementation for send logic for Different Analytics SDK by converting
     * passed [Map] as argument into desired format required by different Analytics SDK
     */
    fun track(appEvent: AppEvent, appEventParams: Map<String, *>)

    /**
     * Implement this class and create [Map] with default (key, value) pairs required by the
     * Analytics sdk for each events.
     */
    fun defaultParams(): MutableMap<String, Any> = mutableMapOf()
}
