package com.doubtnutapp.utils

/**
 * This class is used by
 * ApiUtils class to check whether
 * api should retry or it has reached to max limit of retries
 * @see com.doubtnutapp.base.extension.retryHandler
 */
class RetryManager {

    private val maxNumberOfRetries = 1

    private var currentRetry = 0

    fun shouldRetry(): Boolean = currentRetry++ < maxNumberOfRetries

}