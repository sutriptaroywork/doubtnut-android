package com.doubtnutapp.model

import androidx.annotation.Keep

@Keep
data class Filter(val key: String, val value: ArrayList<String>)