package com.doubtnutapp.domain.base

import io.reactivex.Completable

interface CompletableUseCase<PARAMS> {

    fun execute(param: PARAMS): Completable
}
