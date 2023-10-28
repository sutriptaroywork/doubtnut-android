package com.doubtnutapp.domain.store.entities

data class ConvertCoinsEntity(
    val isConverted: Boolean,
    val message: String,
    val convertedXp: Int
)
