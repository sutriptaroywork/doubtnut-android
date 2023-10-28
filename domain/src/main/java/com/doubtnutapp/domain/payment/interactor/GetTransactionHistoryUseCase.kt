package com.doubtnutapp.domain.payment.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.payment.entities.TransactionHistoryItem
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-09-29.
 */
class GetTransactionHistoryUseCase @Inject constructor(private val paymentRepository: PaymentRepository) :
    SingleUseCase<List<TransactionHistoryItem>, TransactionHistoryParams> {

    override fun execute(param: TransactionHistoryParams): Single<List<TransactionHistoryItem>> {
        return paymentRepository.getTransactionHistoryV2(param.status, param.page)
    }
}

@Keep
class TransactionHistoryParams(
    val status: String,
    val page: Int
)
