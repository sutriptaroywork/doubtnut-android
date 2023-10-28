package com.doubtnutapp.base.extension

import com.doubtnut.core.utils.ThreadUtils
import com.doubtnut.core.utils.applyIoToMainSchedulerOnSingle2
import com.doubtnut.core.utils.subscribeToSingle2
import com.doubtnutapp.Constants
import com.doubtnutapp.utils.RetryManager
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Publisher
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

fun <T> Single<T>.applyIoToMainSchedulerOnSingle(): Single<T> =
    this.applyIoToMainSchedulerOnSingle2()

fun <T> Single<T>.applyAnalyticsToMainSchedulerOnSingle(): Single<T> = this.compose {
    it.observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.from(ThreadUtils.logEventThread))
}

inline fun <T> Single<T>.subscribeToSingle(
    crossinline success: (T) -> Unit,
    crossinline error: (Throwable) -> Unit = {}
): Disposable = this.subscribeToSingle2(success, error)

fun Completable.applyIoToMainSchedulerOnCompletable(): Completable = this.compose {
    it.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
}

inline fun Completable.subscribeToCompletable(
    crossinline success: () -> Unit,
    crossinline error: (Throwable) -> Unit = {}
): Disposable {
    return this.subscribe({
        success()
    }, {
        error(it)
    })
}

/**
 * === RetryHandler - Decides whether to retry api after exception occurs  ===
 * For TimeoutException exception - it retries until max retry limit reached
 * For UnknownHostException - Used to show Network Error Dialog
 * For HTTP_NOT_FOUND, HTTP_INTERNAL_ERROR, HTTP_UNAVAILABLE - No need to retry
 * Otherwise - Propagate Exception to calling ViewModel
 */
private val retryHandler: Function<Flowable<Throwable>, Publisher<Boolean>>
    get() {
        val retryManager = RetryManager()
        return Function { throwableFlowable: Flowable<Throwable> ->
            throwableFlowable.flatMapSingle { throwable: Throwable ->
                when {
                    retryManager.shouldRetry() -> {
                        when {
                            throwable is TimeoutException -> {
                                Single.just(true)
                                    .delay(Constants.RETRY_DELAY_TIME, TimeUnit.MILLISECONDS)
                            }

                            throwable is UnknownHostException -> Single.error<Boolean>(throwable)

                            (throwable as? HttpException)?.code() == HttpURLConnection.HTTP_NOT_FOUND ||
                                    (throwable as? HttpException)?.code() == HttpURLConnection.HTTP_INTERNAL_ERROR ||
                                    (throwable as? HttpException)?.code() == HttpURLConnection.HTTP_BAD_REQUEST ||
                                    (throwable as? HttpException)?.code() == HttpURLConnection.HTTP_UNAVAILABLE -> {
                                Single.just(false)
                                    .delay(Constants.RETRY_DELAY_TIME, TimeUnit.MILLISECONDS)
                            }

                            else -> Single.error<Boolean>(throwable)
                        }
                    }
                    else -> Single.error<Boolean>(throwable)
                }
            }
        }
    }

/**
 * == Retry Api with Timeout ==
 * It attaches retry handler and timeout to any Api
 * After TimeoutException it will retry until max retry count limit exceeded
 * @param single - Single Observable
 * @param timeout - timeout after which api will retry with same observable
 * @param unit - timeout in seconds
 * @return - Single Observable with retry handler and timeout
 */
fun <T> Single<T>.attachRetryHandler(
    timeout: Long = 20,
    unit: TimeUnit = TimeUnit.SECONDS
): Single<T> =
    timeout(timeout, unit).retryWhen(retryHandler)
