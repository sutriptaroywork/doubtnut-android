package com.doubtnutapp.store.dto

import com.doubtnutapp.store.model.StoreResult


data class StoreResultDTO(
        val coins: Int,
        val freeExp: Int,
        val storeTabNameList: List<String>,
        val storeResult: Map<String, List<StoreResult>>
)