package com.doubtnutapp.domain.base

import io.reactivex.Single

interface SingleUseCase<T, PARAMS> {
    fun execute(param: PARAMS): Single<T>
}
