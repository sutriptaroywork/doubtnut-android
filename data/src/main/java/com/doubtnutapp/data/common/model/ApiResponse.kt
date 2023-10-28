package com.doubtnutapp.data.common.model

import androidx.annotation.Keep
import com.doubtnut.core.data.remote.ResponseMeta

@Keep
data class ApiResponse<T>(val meta: ResponseMeta, val data: T, val error: T?)
