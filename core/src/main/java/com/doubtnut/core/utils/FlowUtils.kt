package com.doubtnut.core.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest

suspend inline fun <T> Flow<T>.collectSafely(crossinline action: suspend (value: T) -> Unit): Unit =
    catch {
        ToastUtils.makeTextInDev(
            message = it.message ?: "Error in collecting data in flow !"
        )
    }.collect { action(it) }

suspend inline fun <T> Flow<T>.collectLatestSafely(crossinline action: suspend (value: T) -> Unit): Unit =
    catch {
        ToastUtils.makeTextInDev(
            message = it.message ?: "Error in collecting data in flow !"
        )
    }.collectLatest { action(it) }
