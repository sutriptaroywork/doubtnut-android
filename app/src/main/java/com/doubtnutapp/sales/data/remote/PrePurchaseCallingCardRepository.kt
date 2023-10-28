package com.doubtnutapp.sales.data.remote

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.sales.data.entity.CallingCardDismissRequest
import com.doubtnutapp.sales.data.entity.RequestCallbackRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PrePurchaseCallingCardRepository @Inject constructor(
    private val networkService: NetworkService
) {

    fun dismissPrePurchaseCallingCard(
        assortmentId: String?,
        view: String?,
        source: String?
    )
            : Flow<Unit> =
        flow {
            networkService.dismissPrePurchaseCallingCard(
                CallingCardDismissRequest(
                    assortmentId = assortmentId,
                    view = view,
                    source = source
                )
            )
        }

    fun requestCallback(assortmentId: String)
            : Flow<String?> =
        flow {
            emit(
                networkService.requestCallback(RequestCallbackRequest(assortmentId)).meta.message
            )
        }

}