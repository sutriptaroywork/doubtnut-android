package com.doubtnut.core.view

import androidx.annotation.RestrictTo

/**
Created by Sachin Saxena on 12/07/22.
 */
interface ViewActionHandler {
    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    fun onCreate() {
    }

    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    fun onStart() {
    }

    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    fun onResume() {
    }

    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    fun onPause() {
    }

    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    fun onStop() {
    }

    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    fun onDestroy() {
    }
}