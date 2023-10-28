package com.doubtnutapp.data.remote.models

import androidx.annotation.Keep
import com.doubtnut.core.data.remote.CoreResponse
import com.doubtnut.core.data.remote.ResponseMeta

@Keep
class ApiResponse<T>(
    meta: ResponseMeta,
    data: T,
    error: T? = null
) :
    CoreResponse<T>(
        meta = meta,
        data = data,
        error = error
    )


