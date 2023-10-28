package com.doubtnutapp.domain.mainscrren.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.mainscrren.repository.MainScreenRepository
import io.reactivex.Single
import javax.inject.Inject

class SetBottomNavigationButtonClicked @Inject constructor(private val mainScreenRepository: MainScreenRepository) : SingleUseCase<Boolean, String> {
    override fun execute(param: String): Single<Boolean> = mainScreenRepository.isBottomNavigationButtonClicked(param)
}
