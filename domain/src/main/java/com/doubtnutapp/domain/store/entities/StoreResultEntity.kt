package com.doubtnutapp.domain.store.entities

data class StoreResultEntity(

    val id: Int,
    val resourceType: String?,
    val resourceId: Int?,
    val title: String?,
    val description: String?,
    val imgUrl: String?,
    val isActive: Int?,
    val price: Int?,
    val createdAt: String?,
    val displayCategory: String?,
    val isLast: Int?,
    val redeemStatus: Int,
    var availableDnCash: Int
)
