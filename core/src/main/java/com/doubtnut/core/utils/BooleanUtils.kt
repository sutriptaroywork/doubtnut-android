package com.doubtnut.core.utils

fun Boolean?.toO1(): Int = if (this == true) 1 else 0
fun Int?.toBool(): Boolean = this == 1
