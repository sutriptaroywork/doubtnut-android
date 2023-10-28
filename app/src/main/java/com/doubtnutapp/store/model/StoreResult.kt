package com.doubtnutapp.store.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class StoreResult(
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
        val availableDnCash: Int,
        override val viewType: Int
) : BaseStoreViewItem, Parcelable