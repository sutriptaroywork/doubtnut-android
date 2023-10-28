package com.doubtnutapp.store.dto

import androidx.annotation.Keep

@Keep
data class ConvertCoinsResultDTO(
        val isConverted: Boolean,
        val message: String,
        val convertedXp: Int
)