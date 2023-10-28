package com.doubtnutapp.domain.store.entities

data class MyOrderResultEntity(

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
    val isLast: Int?
)
