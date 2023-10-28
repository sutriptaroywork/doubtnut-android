package com.doubtnutapp.domain.profile

import com.doubtnutapp.domain.base.CompletableUseCase
import io.reactivex.Completable
import javax.inject.Inject

class GetClass @Inject constructor(private val userProfileRepository: UserProfileRepository) : CompletableUseCase<GetClass.None> {

    override fun execute(param: None): Completable = userProfileRepository.getClass()

    class None
}
