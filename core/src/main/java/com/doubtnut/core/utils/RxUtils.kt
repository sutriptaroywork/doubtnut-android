package com.doubtnut.core.utils

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

fun <T> Single<T>.applyIoToMainSchedulerOnSingle2(): Single<T> = this.compose {
    it.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
}

inline fun <T> Single<T>.subscribeToSingle2(
    crossinline success: (T) -> Unit,
    crossinline error: (Throwable) -> Unit = {}
): Disposable {
    return this.subscribe({
        success(it)
    }, {
        error(it)
    })
}