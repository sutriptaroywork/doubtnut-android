package com.doubtnutapp.domain.store.entities

data class StoreEntity(
    val coins: Int,
    val freexp: Int,
    val tabs: List<String>,
    val storeItems: HashMap<String, List<StoreResultEntity>>
)
