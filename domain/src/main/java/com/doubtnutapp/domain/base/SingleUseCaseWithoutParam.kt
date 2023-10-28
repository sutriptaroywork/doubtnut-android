package com.doubtnutapp.domain.base

import io.reactivex.Single

interface SingleUseCaseWithoutParam<T> {
    fun execute(): Single<T>
}
