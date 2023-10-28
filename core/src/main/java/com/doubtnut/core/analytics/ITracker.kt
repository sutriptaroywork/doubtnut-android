package com.doubtnut.core.analytics

interface ITracker {
    fun addEventName(eventName: String, trackerId: String): ITracker

    fun addNetworkState(value: Any): ITracker

    fun addStudentId(value: Any): ITracker

    fun addScreenState(key: String, value: Any): ITracker

    fun addScreenName(value: Any): ITracker

    fun track()
}

fun ITracker.addEventNames2(eventNameFb: String, eventNameBr: String = eventNameFb): ITracker {
    this.addEventName(eventNameFb, TrackerConstants.TRACKER_CLIENT_FIREBASE)
    this.addEventName(eventNameBr, TrackerConstants.TRACKER_CLIENT_BRANCH)
    this.addEventName(eventNameBr, TrackerConstants.TRACKER_CLIENT_CLEVER_TAP)
    return this
}