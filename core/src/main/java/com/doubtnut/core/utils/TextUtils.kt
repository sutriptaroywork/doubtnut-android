package com.doubtnut.core.utils

object TextUtils {
}

fun CharSequence?.isNotNullAndNotEmpty2(): Boolean = isNullOrEmpty().not()

fun String?.ifEmptyThenNull2(): String? {
    return if (this.isNullOrBlank())
        null
    else
        this
}