package com.doubtnutapp.data.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.doubtnutapp.data.Outcome

fun <A, B> zipLiveData(a: LiveData<A>, b: LiveData<B>): LiveData<Pair<A?, B?>> {
    return MediatorLiveData<Pair<A?, B?>>().apply {
        var lastA: A? = null
        var lastB: B? = null

        fun update() {
            val localLastA = lastA
            val localLastB = lastB

            if (localLastA is Outcome.Progress<*> && localLastB is Outcome.Progress<*>) {
                this.value = Pair(localLastA, localLastB)
            } else if (localLastA is Outcome.Success<*> && localLastB is Outcome.Success<*>) {
                this.value = Pair(localLastA, localLastB)
            } else if (localLastA is Outcome.ApiError<*> ||
                localLastA is Outcome.Failure<*> ||
                localLastA is Outcome.BadRequest<*> ||
                localLastB is Outcome.ApiError<*> ||
                localLastB is Outcome.Failure<*> ||
                localLastB is Outcome.BadRequest<*>
            ) {
                this.value = Pair(localLastA, localLastB)
                removeSource(a)
                removeSource(b)
            }
        }

        addSource(a) {
            lastA = it
            update()
        }
        addSource(b) {
            lastB = it
            update()
        }
    }
}
