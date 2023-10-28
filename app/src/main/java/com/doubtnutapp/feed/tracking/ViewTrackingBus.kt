package com.doubtnutapp.feed.tracking

import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.concurrent.ConcurrentHashMap

class ViewTrackingBus(
    private val onSuccess: Consumer<State>,
    onError: Consumer<Throwable>
) {

    private val publishSubject: Subject<State>
    private val publishSubjectNoTimeout: Subject<State>
    private val subscription1: Disposable
    private val subscription2: Disposable

    private val activeViews: ConcurrentHashMap<Int, State?> = ConcurrentHashMap()

    private var isPaused: Boolean = false

    fun trackViewAdded(position: Int, id: String, params: HashMap<String, Any>? = hashMapOf()) {
        if (isPaused) return
        if (position == 0) {
            publishSubjectNoTimeout.onNext(
                State(
                    position, id, VIEW_ADDED, System.currentTimeMillis(), params
                        ?: hashMapOf()
                )
            )
        } else publishSubject.onNext(
            State(
                position, id, VIEW_ADDED, System.currentTimeMillis(), params
                    ?: hashMapOf()
            )
        )
    }

    fun trackViewRemoved(position: Int, id: String, params: HashMap<String, Any>? = hashMapOf()) {
        if (isPaused) return
        publishSubject.onNext(
            State(
                position, id, VIEW_REMOVED, System.currentTimeMillis(), params
                    ?: hashMapOf()
            )
        )
    }

    fun unsubscribe() {
        subscription1.dispose()
        subscription2.dispose()
    }

    fun pause() {
        isPaused = true
    }

    fun resume() {
        isPaused = false
    }

    fun isPaused() = isPaused

    private fun onCallback(state: State) {
        if (state.state == VIEW_REMOVED) {
            val viewToBeRemoved = activeViews[state.position] ?: return
            if (state.time - viewToBeRemoved.time > 2000) {
                state.state = TRACK_VIEW_DURATION
                state.time = state.time - viewToBeRemoved.time
            }
            activeViews.remove(state.position)
        }
        if (state.state == VIEW_ADDED) {
            activeViews[state.position] = state
        }
        onSuccess.accept(state)
    }

    companion object {
        const val VIEW_ADDED = "view_added"
        const val VIEW_REMOVED = "view_removed"
        const val TRACK_VIEW_DURATION = "track_view_duration"
    }

    init {
        publishSubject = PublishSubject.create()
        publishSubjectNoTimeout = PublishSubject.create()
        subscription1 = publishSubject
            .distinctUntilChanged()
            .subscribe({ visibleState: State -> onCallback(visibleState) }, onError)
        subscription2 = publishSubjectNoTimeout
            .distinctUntilChanged()
            .subscribe({ visibleState: State -> onCallback(visibleState) }, onError)
    }

    data class State(
        var position: Int,
        var trackId: String,
        var state: String,
        var time: Long,
        var trackParams: HashMap<String, Any>
    )
}