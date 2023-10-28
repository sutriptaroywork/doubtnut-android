package com.doubtnut.analytics

import com.doubtnut.analytics.debug.AnalyticsInterceptor
import com.doubtnut.core.analytics.ITracker

/**
 * Whoever want to send event through all the registered client uses this class.
 * */
class Tracker : ITracker {

    private val trackerClients: MutableMap<String, TrackerClient> = mutableMapOf()
    private val appEvents: MutableList<AppEvent> = mutableListOf()
    private val appEventParams: MutableMap<String, Any> = mutableMapOf()

    private val cleverTapTrackerClient: MutableMap<String, CleverTapTrackerClient> = mutableMapOf()

    /**
     * Application must call this method n times if he want to add n register [TrackerClient]s.
     *
     * @param trackerClient application wants to use for event tracking
     */
    @Synchronized
    fun addClient(trackerClient: TrackerClient) {
        trackerClients[trackerClient.id] = trackerClient
    }

    @Synchronized
    fun addCleverTapClient(trackerClient: CleverTapTrackerClient) {
        cleverTapTrackerClient[trackerClient.id] = trackerClient
    }

    /**
     * Call this to add n number of events for different Analytics clients
     *@param eventName required to identify the event at Analytics clients dashboard
     *@param trackerId to uniquely identify [TrackerClient].
     */
    @Synchronized
    override fun addEventName(eventName: String, trackerId: String): Tracker {
        appEvents.add(AppEvent(eventName, trackerId))
        return this
    }

    /**
     * Call this to send extra (key, value) pair to Analytics client.
     */
    @Synchronized
    fun addEventParameter(key: String, value: Any): Tracker {
        appEventParams[key] = value
        return this
    }

    /**
     * Call this to send extra when you have created (key, value) Map on your side to Analytics client.
     */
    @Synchronized
    fun addEventParameter(eventParameters: Map<String, Any>): Tracker {
        appEventParams.clear()
        appEventParams.putAll(eventParameters)
        return this
    }

    /**
     * Call this to send studentId to Analytics client.
     */
    @Synchronized
    override fun addStudentId(value: Any): Tracker {
        appEventParams[EventConstants.EVENT_PRAMA_KEY_STUDENT_ID] = value
        return this
    }

    /**
     * Call this to send ScreenState to Analytics client.
     */
    @Synchronized
    override fun addScreenState(key: String, value: Any): Tracker {
        appEventParams[key] = value
        return this
    }

    /**
     * Call this to send ScreenName to Analytics client.
     */
    @Synchronized
    fun addItemClickedValue(value: Any): Tracker {
        appEventParams[EventConstants.EVENT_PRAMA_KEY_CLICKED_ITEM_NAME] = value
        return this
    }

    /**
     * Call this to send ScreenName to Analytics client.
     */
    @Synchronized
    override fun addScreenName(value: Any): Tracker {
        appEventParams[EventConstants.EVENT_PRAMA_KEY_SCREEN_NAME] = value
        return this
    }

    /**
     * Call this to send network state  to Analytics client.
     */
    @Synchronized
    override fun addNetworkState(value: Any): Tracker {
        appEventParams[EventConstants.EVENT_PRAMA_KEY_NETWORK_CONNECTED] = value
        return this
    }

    /**
     * Method calls the specific [TrackerClient] depending upon the trackerId defined by the
     * [AppEvent.trackerId]
     */
    @Synchronized
    override fun track() {

        val eventIterator = appEvents.iterator()

        while (eventIterator?.hasNext()) {
            val appEvent = eventIterator.next()
            trackerClients[appEvent.trackerId]?.also {
                /**
                 * add default parameter if application has provided the implementation of
                 * [TrackerClient.defaultParams]
                 */
                appEventParams.putAll(it.defaultParams())
                it.track(appEvent, appEventParams)
                AnalyticsInterceptor.intercept(
                    AnalyticsInterceptor.Event(
                        arrayListOf(
                            AnalyticsInterceptor.getEventDestination(appEvent.trackerId)
                        ), appEvent.eventName, appEventParams.toString()
                    )
                )
            }
            eventIterator?.remove()
        }

        appEventParams.clear()
    }

    /**
     * Method calls the specific [TrackerClient] depending upon the trackerId defined by the
     * [AppEvent.trackerId]
     */
    @Synchronized
    fun cleverTapTrack() {

        val eventIterator = appEvents.iterator()

        while (eventIterator.hasNext()) {
            val appEvent = eventIterator.next()
            cleverTapTrackerClient[appEvent.trackerId]?.also {
                /**
                 * add default parameter if application has provided the implementation of
                 * [TrackerClient.defaultParams]
                 */
                appEventParams.putAll(it.defaultParams())
                it.cleverTapTrack(appEvent, appEventParams)
                AnalyticsInterceptor.intercept(
                    AnalyticsInterceptor.Event(
                        arrayListOf(
                            EventDestinations.CLEVERTAP
                        ), appEvent.eventName, appEventParams.toString()
                    )
                )
            }
            eventIterator.remove()
        }

        appEventParams.clear()
    }
}
