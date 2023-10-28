package com.doubtnut.analytics

/**
 * AppEvent class is used to hold the event name and tracker client id.
 *
 * @param eventName name of the event need by FirebaseAnalytics or any other client
 * @param trackerId id that used to uniquely identify the [TrackerClient]
 * */
class AppEvent(val eventName: String, val trackerId: String)
