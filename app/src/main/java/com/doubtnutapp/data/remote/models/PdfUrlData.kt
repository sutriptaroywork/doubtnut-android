package com.doubtnutapp.data.remote.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 1/1/21.
 */

@Keep
class PdfUrlData(@SerializedName("url") val url: String?)
